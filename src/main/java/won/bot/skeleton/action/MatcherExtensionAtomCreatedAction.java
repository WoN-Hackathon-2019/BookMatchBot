package won.bot.skeleton.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.bot.context.BotContext;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.wonmessage.HintFromMatcherEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.extensions.matcher.MatcherExtensionAtomCreatedEvent;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.bot.skeleton.utils.BookAtomModelWrapper;
import won.protocol.message.WonMessage;
import won.protocol.message.builder.WonMessageBuilder;
import won.protocol.model.AtomState;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
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

        botContext.appendToNamedAtomUriList(atomCreatedEvent.getAtomURI(), CONNECTED_ATOMS);
    }


    private boolean tryMatchWithAny(BookAtomModelWrapper myBook, BotContext botContext, EventListenerContext ctx) {
        boolean hint = false;


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
                    if (myBook.matchesWith(otherBook)) {
                        hint(otherBook, myBook, ctx);
                        return true;
                    }

                    if (otherBook.matchesWith(myBook)) {
                        hint(myBook, otherBook, ctx);
                        return true;
                    }
                    return false;
                }).anyMatch(x -> x);
    }

    private void hint(BookAtomModelWrapper myBook, BookAtomModelWrapper otherBook, EventListenerContext ctx) {
        try {
            WonMessage wonMessage = WonMessageBuilder
                    .atomHint()
                    .hintTargetAtom(new URI(myBook.getAtomUri()))
                    .hintScore(1.0)
                    .content().dataset(otherBook.copyDataset())
                    .build();
            ctx.getWonMessageSender().prepareAndSendMessage(wonMessage);
        } catch (URISyntaxException e) {
            HintFromMatcherEvent
            throw new RuntimeException(e);
        }
    }
}
