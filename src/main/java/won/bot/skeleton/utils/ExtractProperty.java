package won.bot.skeleton.utils;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import won.protocol.util.AtomModelWrapper;

import java.util.function.Function;

public class ExtractProperty {

    public static final Function<Statement, Statement> Identity = x -> x;

    public static <T> T getProperty(AtomModelWrapper wrapper, Property property, Function<Statement, T> transformer) {
        Statement propertyValue = wrapper.getAtomContentNode().getProperty(property);

        if (propertyValue == null) {
            return null;
        }
        return transformer.apply(propertyValue);
    }

    public static <T> T getProperty(Statement statement, Property property, Function<Statement, T> transformer) {
        if (statement == null) {
            return null;
        }
        Statement propertyValue = statement.getProperty(property);

        if (propertyValue == null) {
            return null;
        }
        return transformer.apply(propertyValue);
    }
}
