package won.bot.skeleton.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;

public class _SCHEMA {
    private static Model m = ModelFactory.createDefaultModel();

    public _SCHEMA() {
    }

    public static final Property COMPOUNDPRICESPECIFICATION;

    static {
        COMPOUNDPRICESPECIFICATION = m.createProperty("http://schema.org/CompoundPriceSpecification");
        //TERMS_OF_SERVICE = m.createProperty("http://schema.org/termsOfService");
        //TEXT = new BaseDatatype("http://schema.org/Text");
    }
}
