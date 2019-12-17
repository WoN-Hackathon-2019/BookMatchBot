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
import won.bot.skeleton.utils.StorableBook;
import won.protocol.message.WonMessage;
import won.protocol.message.builder.WonMessageBuilder;
import won.protocol.message.processor.impl.WonMessageSignerVerifier;
import won.protocol.util.linkeddata.LinkedDataSource;
import won.protocol.util.linkeddata.WonLinkedDataUtils;
import won.protocol.vocabulary.WXCHAT;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;

public class MatcherExtensionAtomCreatedAction extends BaseEventBotAction {

    private static final String CONNECTED_ATOMS = "connected-atoms";
    private static final String STORED_BOOK_OFFERS = "stored-book-offers";
    private static final String STORED_BOOK_SEARCHES = "stored-book-searches";
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

        botContext.addToListMap(wrapper.isBookSearch() ? STORED_BOOK_SEARCHES : STORED_BOOK_OFFERS, wrapper.getAtomUri(), wrapper.toStorableBook());

        botContext.appendToNamedAtomUriList(atomCreatedEvent.getAtomURI(), CONNECTED_ATOMS);
    }


    private URI toURI(String string) {
        try {
            return new URI(string);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean tryMatchWithAny(BookAtomModelWrapper myBook, BotContext botContext, EventListenerContext ctx) {

        String storageKey = myBook.isBookSearch() ? STORED_BOOK_OFFERS : STORED_BOOK_SEARCHES;
        return botContext.loadObjectMap(storageKey)
                .entrySet()
                .stream()
                .map(e -> new HashMap.SimpleEntry<>(e.getKey(), (StorableBook) e.getValue()))
                .filter(e -> myBook.matchesWith(e.getValue()))
                .peek(e -> {
                    hint(getSocketUri(myBook), getSocketUri(toURI(e.getKey())), ctx);
                    hint(getSocketUri(toURI(e.getKey())), getSocketUri(myBook), ctx);
                })
                .anyMatch(x -> true);
    }

    private void hint(URI mySocketURI, URI otherSocketURI, EventListenerContext ctx) {
        try {
            WonMessage wonMessage = WonMessageBuilder
                    .socketHint()
                    .hintTargetSocket(mySocketURI)
                    .recipientSocket(otherSocketURI)
                    .hintScore(1.0)
                    .build();
            wonMessage = WonMessageSignerVerifier.seal(wonMessage);
            ctx.getMatcherProtocolAtomServiceClient()
                    .hint(
                            mySocketURI,
                            otherSocketURI,
                            1.0,
                            new URI("http://localhost:8080/matcher"),
                            null,
                            wonMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private URI getSocketUri(URI uri) {
        try {
            URI socketURI = URI.create(WXCHAT.ChatSocket.getURI()); //or any other socket type you want
            LinkedDataSource linkedDataSource = getEventListenerContext().getLinkedDataSource();
            Collection<URI> sockets = WonLinkedDataUtils.getSocketsOfType(uri, socketURI, linkedDataSource);
            if (sockets.isEmpty()) {
                throw new Exception("no socket uri found");
            }
            return sockets.iterator().next();
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
