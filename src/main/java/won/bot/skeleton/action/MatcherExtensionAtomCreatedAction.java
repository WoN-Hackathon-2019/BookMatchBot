package won.bot.skeleton.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.extensions.matcher.MatcherExtensionAtomCreatedEvent;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.bot.skeleton.utils.BookAtomModelWrapper;
import won.protocol.model.Coordinate;

import java.lang.invoke.MethodHandles;

public class MatcherExtensionAtomCreatedAction extends BaseEventBotAction {
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
        SkeletonBotContextWrapper botContextWrapper = (SkeletonBotContextWrapper) ctx.getBotContextWrapper();
        MatcherExtensionAtomCreatedEvent atomCreatedEvent = (MatcherExtensionAtomCreatedEvent) event;
        BookAtomModelWrapper wrapper = new BookAtomModelWrapper(atomCreatedEvent.getAtomData());

        String title = wrapper.getSomeTitleFromIsOrAll();
        String name = wrapper.getSomeName();
        Coordinate location = wrapper.getAnyLocationCoordinate();
        String description = wrapper.getSomeDescription();
        String isbn = wrapper.getSomeIsbn();
        String author =  wrapper.getAnyAuthorName();
        Float price = wrapper.getAnyPrice();

        //TODO: website/url?
        System.out.println(title);
        System.out.println(name);
        System.out.println(price);
        if (name != null && name.toLowerCase().contains("book")) {
            System.out.println(location);
        }
    }
}
