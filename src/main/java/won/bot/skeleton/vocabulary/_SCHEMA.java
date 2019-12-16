package won.bot.skeleton.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;

public class _SCHEMA {
    private static Model m = ModelFactory.createDefaultModel();

    public _SCHEMA() {
    }

    public static final Property AUTHOR;
    public static final Property ISBN;

    static {
        AUTHOR = m.createProperty("http://schema.org/author");
        ISBN = m.createProperty("http://schema.org/isbn");
    }
}
