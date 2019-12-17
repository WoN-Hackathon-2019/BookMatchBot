package won.bot.skeleton.utils;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import won.bot.skeleton.vocabulary._SCHEMA;
import won.protocol.model.AtomGraphType;
import won.protocol.model.Coordinate;
import won.protocol.util.AtomModelWrapper;
import won.protocol.util.RdfUtils;
import won.protocol.vocabulary.SCHEMA;
import won.protocol.vocabulary.WON;
import won.protocol.vocabulary.WONCON;
import won.protocol.vocabulary.WONMATCH;

import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class BookAtomModelWrapper extends AtomModelWrapper {

    public BookAtomModelWrapper(URI atomUri) {
        this(atomUri.toString());
    }

    public BookAtomModelWrapper(String atomUri) {
        super(atomUri);
    }

    public BookAtomModelWrapper(Dataset atomDataset) {
        super(atomDataset);
    }

    public BookAtomModelWrapper(Model atomModel, Model sysInfoModel) {
        super(atomModel, sysInfoModel);
    }

    private void createSeeksNodeIfNonExist() {
        if (!this.isSeek()) {
            this.createSeeksNode(null);

        }
    }

    public void setTitle(String title) {
        Resource atomNode = this.getAtomNode(AtomGraphType.ATOM);
        atomNode.removeAll(DC.title);
        atomNode.addLiteral(DC.title, title);
    }

    public void setName(String name) {
        Resource atomNode = this.getAtomNode(AtomGraphType.ATOM);
        atomNode.removeAll(SCHEMA.NAME);
        atomNode.addLiteral(SCHEMA.NAME, name);
    }

    public void setSeeksTitle(String title) {
        this.createSeeksNodeIfNonExist();
        this.setSeeksPropertyStringValue(DC.title, title);
    }

    public void setShapesGraphReference(URI shapesGraphReference) {
        if (this.getGoalNodes().size() == 0) {
            this.createGoalNode((String) null);
        }

        Collection nodes = this.getGoalNodes();
        Iterator var3 = nodes.iterator();

        while (var3.hasNext()) {
            Resource node = (Resource) var3.next();
            node.removeAll(WON.shapesGraph);
            node.addProperty(WON.shapesGraph, this.getAtomModel().getResource(shapesGraphReference.toString()));
        }

    }

    public String getSomeTitleFromIsOrAll(String... preferredLanguages) {
        String title = this.getAtomContentPropertyStringValue(DC.title, preferredLanguages);
        if (title != null) {
            return title;
        } else {
            title = this.getSomeContentPropertyStringValue(DC.title, preferredLanguages);
            return title;
        }
    }

    public boolean isSeek() {
        this.getSeeksSeeksNodes();
        return this.getSeeksNodes().size() != 0;
    }

    public Collection<String> getTitles(Resource contentNode) {
        return this.getTitles(contentNode, null);
    }

    Collection<String> getTitles(Resource contentNode, String language) {
        return this.getContentPropertyStringValues(contentNode, DC.title, language);
    }

    public void setSeeksDescription(String description) {
        this.createSeeksNodeIfNonExist();
        this.setSeeksPropertyStringValue(DC.description, description);
    }

    public void setDescription(String description) {
        Resource atomNode = this.getAtomNode(AtomGraphType.ATOM);
        atomNode.removeAll(DC.description);
        atomNode.addLiteral(DC.description, description);
    }

    public String getSomeDescription(String... preferredLanguages) {
        return this.getSomeContentPropertyStringValue(DC.description, preferredLanguages);
    }

    public String getSomeName(String... preferredLanguages) {
        return this.getSomeContentPropertyStringValue(SCHEMA.NAME, preferredLanguages);
    }

    public Collection<String> getDescriptions(Resource contentNode) {
        return this.getDescriptions(contentNode, (String) null);
    }

    Collection<String> getDescriptions(Resource contentNode, String language) {
        return this.getContentPropertyStringValues(contentNode, DC.description, language);
    }

    public void addTag(String tag) {
        Resource atomNode = this.getAtomNode(AtomGraphType.ATOM);
        atomNode.addLiteral(WONCON.tag, tag);
    }

    public void addSeeksTag(String tag) {
        this.createSeeksNodeIfNonExist();
        this.addSeeksPropertyStringValue(WONCON.tag, tag);
    }

    public Collection<String> getTags(Resource contentNode) {
        return this.getContentPropertyStringValues(contentNode, WONCON.tag, (String) null);
    }

    public Collection<String> getAllTags() {
        return this.getAllContentPropertyStringValues(WONCON.tag, (String) null);
    }

    public Collection<URI> getAllFlags() {
        Collection rdfFlags = this.getContentPropertyObjects(WONMATCH.flag);
        LinkedList uriFlags = new LinkedList();
        Iterator var3 = rdfFlags.iterator();

        while (var3.hasNext()) {
            RDFNode rdfFlag = (RDFNode) var3.next();
            if (rdfFlag.isURIResource()) {
                uriFlags.add(URI.create(rdfFlag.asResource().getURI()));
            }
        }

        return uriFlags;
    }

    public Collection<URI> getContentTypes() {
        Collection rdfTypes = this.getContentPropertyObjects(RDF.type);
        LinkedList uriTypes = new LinkedList();
        Iterator var3 = rdfTypes.iterator();

        while (var3.hasNext()) {
            RDFNode rdfType = (RDFNode) var3.next();
            if (rdfType.isURIResource()) {
                uriTypes.add(URI.create(rdfType.asResource().getURI()));
            }
        }

        return uriTypes;
    }

    public Collection<URI> getSeeksTypes() {
        Collection rdfTypes = this.getSeeksPropertyObjects(RDF.type);
        LinkedList uriTypes = new LinkedList();
        Iterator var3 = rdfTypes.iterator();

        while (var3.hasNext()) {
            RDFNode rdfType = (RDFNode) var3.next();
            if (rdfType.isURIResource()) {
                uriTypes.add(URI.create(rdfType.asResource().getURI()));
            }
        }

        return uriTypes;
    }

    public Collection<URI> getSeeksEventObjectAboutUris() {
        return this.getEventObjectAboutUris(this.getSeeksPropertyObjects(SCHEMA.OBJECT));
    }

    public Collection<URI> getContentEventObjectAboutUris() {
        return this.getEventObjectAboutUris(this.getContentPropertyObjects(SCHEMA.OBJECT));
    }

    private Collection<URI> getEventObjectAboutUris(Collection<RDFNode> rdfEventObjects) {
        LinkedList eventObjectAboutUris = new LinkedList();
        Iterator var3 = rdfEventObjects.iterator();

        while (true) {
            RDFNode rdfEventObject;
            boolean isEventType;
            do {
                if (!var3.hasNext()) {
                    return eventObjectAboutUris;
                }

                rdfEventObject = (RDFNode) var3.next();
                Collection rdfEventObjectTypes = this.getContentPropertyObjects(rdfEventObject.asResource(), RDF.type);
                isEventType = false;
                Iterator rdfEventObjectAbouts = rdfEventObjectTypes.iterator();

                while (rdfEventObjectAbouts.hasNext()) {
                    RDFNode rdfEventObjectType = (RDFNode) rdfEventObjectAbouts.next();
                    if (SCHEMA.EVENT.toString().equals(rdfEventObjectType.asResource().getURI())) {
                        isEventType = true;
                        break;
                    }
                }
            } while (!isEventType);

            Collection rdfEventObjectAbouts1 = this.getContentPropertyObjects(rdfEventObject.asResource(), SCHEMA.ABOUT);
            Iterator rdfEventObjectType1 = rdfEventObjectAbouts1.iterator();

            while (rdfEventObjectType1.hasNext()) {
                RDFNode rdfEventObjectAbout = (RDFNode) rdfEventObjectType1.next();
                eventObjectAboutUris.add(URI.create(rdfEventObjectAbout.asResource().getURI()));
            }
        }
    }

    public URI getHeldBy() {
        return null;
    }

    public Collection<URI> getHolds() {
        LinkedList holds = new LinkedList();
        return holds;
    }

    public Collection<String> getAllTitles() {
        return this.getAllContentPropertyStringValues(DC.title, null);
    }

    public Coordinate getAnyLocationCoordinate() {
        if (this.isSeek()) {
            return this.getSeekLocationCoordinate();
        } else {
            return getLocationCoordinate();
        }
    }

    public Coordinate getSeekLocationCoordinate() {
        for (Resource r : this.getSeeksNodes()) {
            Coordinate c = this.getLocationCoordinate(r);
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    public Coordinate getLocationCoordinate() {
        return this.getLocationCoordinate(this.getAtomContentNode());
    }

    public Coordinate getLocationCoordinate(Resource contentNode) {
        return this.getLocationCoordinate(contentNode, SCHEMA.LOCATION);
    }

    public Coordinate getJobLocationCoordinate() {
        return this.getJobLocationCoordinate(this.getAtomContentNode());
    }

    public Coordinate getJobLocationCoordinate(Resource contentNode) {
        return this.getLocationCoordinate(contentNode, SCHEMA.JOBLOCATION);
    }

    private Coordinate getLocationCoordinate(Resource contentNode, Property locationProperty) {
        Model atomModel = this.getAtomModel();
        Property geoProperty = atomModel.createProperty("http://schema.org/", "geo");
        Property longitudeProperty = atomModel.createProperty("http://schema.org/", "longitude");
        Property latitudeProperty = atomModel.createProperty("http://schema.org/", "latitude");
        RDFNode locationNode = RdfUtils.findOnePropertyFromResource(atomModel, contentNode, locationProperty);
        RDFNode geoNode = locationNode != null && locationNode.isResource() ? RdfUtils.findOnePropertyFromResource(atomModel, locationNode.asResource(), geoProperty) : null;
        RDFNode lat = geoNode != null && geoNode.isResource() ? RdfUtils.findOnePropertyFromResource(atomModel, geoNode.asResource(), latitudeProperty) : null;
        RDFNode lon = geoNode != null && geoNode.isResource() ? RdfUtils.findOnePropertyFromResource(atomModel, geoNode.asResource(), longitudeProperty) : null;
        if (lat != null && lon != null) {
            Float latitude = Float.valueOf(lat.asLiteral().getString());
            Float longitude = Float.valueOf(lon.asLiteral().getString());
            return new Coordinate(latitude, longitude);
        } else {
            return null;
        }
    }

    public String getAnyAuthorName() {
        return (this.isSeek()) ? this.getSeeksAuthorName() : this.getAuthorName();
    }

    public String getSeeksAuthorName() {
        for (Resource r : this.getSeeksNodes()) {
            String c = this.getAuthorName(r);
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    public String getAuthorName() {
        return getAuthorName(this.getAtomContentNode());
    }

    private String getAuthorName(Resource contentNode) {
        return getPersonName(contentNode, _SCHEMA.AUTHOR);
    }

    private String getPersonName(Resource contentNode, Property personProperty) {
        Model atomModel = this.getAtomModel();
        RDFNode personNode = RdfUtils.findOnePropertyFromResource(atomModel, contentNode, personProperty);
        RDFNode nameNode = personNode != null && personNode.isResource() ? RdfUtils.findOnePropertyFromResource(atomModel, personNode.asResource(), SCHEMA.NAME) : null;
        return (nameNode != null) ? nameNode.asLiteral().getString() : null;
    }

    public String getSomeIsbn(String... preferredLanguages) {
        return this.getSomeContentPropertyStringValue(_SCHEMA.ISBN, preferredLanguages);
    }

    public Float getAnyPrice() {
        if (this.isSeek()) {
            return this.getSeeksPrice();
        } else {
            return this.getPrice();
        }
    }

    public Float getSeeksPrice() {
        for (Resource r : this.getSeeksNodes()) {
            Float c = this.getPrice(r, SCHEMA.PRICESPECIFICATION);
            if (c != null) {
                return c;
            }
        }
        return null;
    }

    public Float getPrice() {
        return this.getPrice(this.getAtomContentNode(), SCHEMA.PRICESPECIFICATION);
    }

    private Float getPrice(Resource contentNode, Property priceProperty) {
        Model atomModel = this.getAtomModel();
        RDFNode priceCurrencyNode = RdfUtils.findOnePropertyFromResource(atomModel, contentNode, priceProperty);
        RDFNode priceNode = priceCurrencyNode != null && priceCurrencyNode.isResource() ? RdfUtils.findOnePropertyFromResource(atomModel, priceCurrencyNode.asResource(), SCHEMA.PRICE) : null;

        if (priceNode != null) {
            return priceNode.asLiteral().getFloat();
        } else {
            return null;
        }
    }

    public String getAnyLocalName() {
        return (this.isSeek() ? this.getSeekLocalName() : this.getLocalName());
    }

    public String getSeekLocalName() {

        for (Resource r : this.getSeeksNodes()) {
            String localName = this.getLocalName(r);
            if (localName != null) {
                return localName;
            }
        }
        return null;
    }

    public String getLocalName() {
        return this.getLocalName(this.getAtomContentNode());
    }

    public String getLocalName(Resource contentNode) {
        Model atomModel = this.getAtomModel();
        RDFNode objectNode = RdfUtils.findOnePropertyFromResource(atomModel, contentNode, SCHEMA.OBJECT);
        RDFNode aboutNode = objectNode != null && objectNode.isResource() ? RdfUtils.findOnePropertyFromResource(atomModel, objectNode.asResource(), SCHEMA.ABOUT) : null;

        if (aboutNode != null) {
            return aboutNode.asResource().getLocalName();
        } else {
            return null;
        }
    }

    public boolean isBook() {
        return this.getSomeTitleFromIsOrAll() != null
                && (this.getAllTags().contains("book") || this.getSomeIsbn() != null);
    }

    public boolean matchesWith(BookAtomModelWrapper otherBook) {
        if (this.isSeek()) {
            return false;
        }

        if (!otherBook.isSeek()) {
            return false;
        }

        String myIsbn = this.getSomeIsbn();
        String otherIsbn = otherBook.getSomeIsbn();

        if (myIsbn != null && otherIsbn != null) {
            return myIsbn.equalsIgnoreCase(otherIsbn);
        }

        String myAuthor = this.getAnyAuthorName();
        String otherAuthor = otherBook.getAuthorName();

        if (myAuthor != null && otherAuthor != null && !myAuthor.equalsIgnoreCase(otherAuthor)) {
            return false;
        }

        String myTitle = this.getSomeTitleFromIsOrAll();
        String otherTitle = otherBook.getSomeTitleFromIsOrAll();

        return myTitle.contains(otherTitle) || otherTitle.contains(myTitle);
    }
}
