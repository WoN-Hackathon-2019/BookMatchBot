package won.bot.skeleton.action;

import org.apache.jena.rdf.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.framework.extensions.matcher.MatcherExtensionAtomCreatedEvent;
import won.bot.skeleton.context.SkeletonBotContextWrapper;
import won.bot.skeleton.vocabulary._SCHEMA;
import won.protocol.util.AtomModelWrapper;
import won.protocol.vocabulary.SCHEMA;

import java.lang.invoke.MethodHandles;

import static won.bot.skeleton.utils.ExtractProperty.Identity;
import static won.bot.skeleton.utils.ExtractProperty.getProperty;

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
        AtomModelWrapper wrapper = new AtomModelWrapper(atomCreatedEvent.getAtomData());

        String title = getProperty(wrapper, SCHEMA.TITLE, Statement::getString);
        String name = getProperty(wrapper, SCHEMA.NAME, Statement::getString);
        Float price = getProperty(wrapper, _SCHEMA.COMPOUNDPRICESPECIFICATION, x -> getProperty(x, SCHEMA.PRICE, Statement::getFloat));
        Statement location = getProperty(wrapper, SCHEMA.LOCATION, Identity);
        //TODO: looking for
        //TODO: author
        //TODO: website/url?
        //TODO: ISBN
        //TODO: Description
        System.out.println(title);
        System.out.println(name);
        System.out.println(price);
        if (title != null && name != null) {
            System.out.println(location);
        }
    }
}
