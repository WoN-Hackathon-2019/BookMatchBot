package won.bot.skeleton.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.bot.context.BotContext;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.extensions.matcher.MatcherExtensionAtomCreatedEvent;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.bot.skeleton.utils.BookAtomModelWrapper;
import won.protocol.message.WonMessage;
import won.protocol.message.builder.WonMessageBuilder;
import won.protocol.message.processor.impl.WonMessageSignerVerifier;
import won.protocol.model.AtomState;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MatcherExtensionAtomCreatedAction extends BaseEventBotAction {

    private static final String CONNECTED_ATOMS = "connected-atoms";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public MatcherExtensionAtomCreatedAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    @Override
    protected void doRun(Event event, EventListener executingListener) throws Exception {
        EventListenerContext ctx = getEventListenerContext();
        if (!(event instanceof MatcherExtensionAtomCreatedEvent) || !(getEventListenerContext().getBotContextWrapper() instanceof SkeletonBotContextWrapper)) {
            logger.error("MatcherExtensionAtomCreatedAction can only handle MatcherExtensionAtomCreatedEvent and only works with SkeletonBotContextWrapper");
            return;
        }
        BotContext botContext = ctx.getBotContext();
        MatcherExtensionAtomCreatedEvent atomCreatedEvent = (MatcherExtensionAtomCreatedEvent) event;
        BookAtomModelWrapper wrapper = new BookAtomModelWrapper(atomCreatedEvent.getAtomData());

        if (!wrapper.isBook()) {
            return;
        }

        tryMatchWithAny(wrapper, botContext, ctx);

        logger.info("Found atom {}, type: {}", wrapper.getSomeTitleFromIsOrAll(), wrapper.isBookSearch() ? "BookSearch" : "BookOffer");
        botContext.appendToNamedAtomUriList(atomCreatedEvent.getAtomURI(), CONNECTED_ATOMS);
    }


    private boolean tryMatchWithAny(BookAtomModelWrapper myBook, BotContext botContext, EventListenerContext ctx) {
        List<BookAtomModelWrapper> wrappers = botContext.getNamedAtomUriList(CONNECTED_ATOMS)
                .parallelStream()
                .map(uri -> ctx.getLinkedDataSource().getDataForResource(uri))
                .map(BookAtomModelWrapper::new)
                .collect(Collectors.toList());

        wrappers.stream()
                .filter(w -> w.getAtomState() != AtomState.ACTIVE)
                .map(BookAtomModelWrapper::getAtomUri)
                .forEach(uri -> botContext.removeFromListMap(uri, CONNECTED_ATOMS));

        return wrappers.stream()
                .filter(w -> w.getAtomState() == AtomState.ACTIVE)
                .map(otherBook -> {
                    if (myBook.matchesWith(otherBook) || otherBook.matchesWith(myBook)) {
                        logger.info("Match {} with {}",
                                myBook.getSomeTitleFromIsOrAll(),
                                otherBook.getSomeTitleFromIsOrAll());
                        hint(myBook, otherBook, ctx);
                        hint(otherBook, myBook, ctx);
                        return true;
                    }
                    return false;
                }).anyMatch(x -> x);
    }

    private void hint(BookAtomModelWrapper myBook, BookAtomModelWrapper otherBook, EventListenerContext ctx) {
        try {
            WonMessage wonMessage = WonMessageBuilder
                    .socketHint()
                    .hintTargetSocket(getSocketUri(myBook))
                    .recipientSocket(getSocketUri(otherBook))
                    .hintScore(1.0)
                    .build();
            wonMessage = WonMessageSignerVerifier.seal(wonMessage);
            ctx.getMatcherProtocolAtomServiceClient()
                    .hint(
                            myBook.getUri(),
                            otherBook.getUri(),
                            1.0,
                            new URI("http://localhost:8080/matcher"),
                            null,
                            wonMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private URI getSocketUri(BookAtomModelWrapper wrapper) {
        try {
            Collection<String> sockets = wrapper.getSocketUris();
            if (sockets.isEmpty()) {
                throw new Exception("no socket uri found");
            }
            return new URI(sockets.iterator().next());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
