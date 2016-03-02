package uk.ac.abdn.foodsafety.provenance;

import java.io.InputStream;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import uk.ac.abdn.foodsafety.common.FoodSafetyException;
import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;

/**
 * 
 * @author nhc
 *
 * A FoodSafetyEngine is a specific CsparqlEngine with methods
 * and queries for this project.
 */
public final class FoodSafetyEngine
    extends CsparqlEngineImpl 
    implements Function<ZonedDateTime, Consumer<Model>> {
    private final RdfStream rdfStream = new RdfStream("http://foodsafety/ssn");
    
    /**
     * Initializes this engine.
     */
    public FoodSafetyEngine() {
        this.initialize();
        this.registerStream(this.rdfStream);
        this.registerQueryFromResources("/window.sparql.txt");
    }
        
    /**
     * Registers a SPARQL query read from /src/main/resources
     * The query must be encoded in UTF-8.
     * A ConsoleFormatter will be added as observer to the query.
     * @param path The path to the query, relative to FoodSafety/src/main/resources,
     * example: "/myquery.sparql.txt"
     */
    private void registerQueryFromResources(final String path) {
        //Get InputStream for the file
        final InputStream resourceAsStream = FoodSafetyEngine.class.getResourceAsStream(path);
        Scanner scanner = null;
        try {
            //Read entire file as UTF-8 into String
            scanner = new Scanner(resourceAsStream, "UTF-8");
            final String text = scanner.useDelimiter("\\A").next();
            //Register query and add observer
            this.registerQuery(text, false);//.addObserver(formatter);
        } catch (final ParseException e) {
            throw FoodSafetyException.internalError(e);
        } finally { //Close Scanner
            if (scanner != null) {
                scanner.close();
            }
        }
        
    }
    
    @Override
    public Consumer<Model> apply(final ZonedDateTime t) {
        return (m -> this.add(t, m));
    }

    private void add(final ZonedDateTime t, final Model m) {
        final long timestamp = t.toInstant().toEpochMilli();
        final StmtIterator it = m.listStatements();
        while (it.hasNext()) {
            final Statement triple = it.nextStatement();
            final RDFNode o = triple.getObject();
            if (o.isAnon()) {
                FoodSafetyException.internalError(String.format("Blank node in %s", m.toString()));
            };
            this.rdfStream.put(new RdfQuadruple(
                    triple.getSubject().getURI(),
                    triple.getPredicate().getURI(),
                    (o.isResource()) ? o.asResource().getURI() : o.asLiteral().getLexicalForm(),
                    timestamp));
        }
    }
}
