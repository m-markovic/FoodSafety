package uk.ac.abdn.foodsafety.provenance;

import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.ac.abdn.foodsafety.common.FoodSafetyException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

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
    
    /** This engine's sole stream */
    private final RdfStream rdfStream = new RdfStream("http://foodsafety/ssn");
    
    /** Object to which all inferences will be passed */
    private final Consumer<Model> persistentModel;
    
    /**
     * Initializes this engine.
     * @param persistentModel Object to which all inferences will be passed
     */
    public FoodSafetyEngine(final Consumer<Model> persistentModel) {
        this.persistentModel = persistentModel;
        this.initialize();
        this.registerStream(this.rdfStream);
        new Configurator(this, this.persistentModel);
    }
    
    /**
     * To a Jena Model for a given timestamp, use engine.apply(t).accept(model)
     * This is equivalent to engine.add(t, m)
     */
    @Override
    public Consumer<Model> apply(final ZonedDateTime t) {
        return m -> this.add(t, m);
    }

    /**
     * Adds all triples in the given model to C-SPARQL
     * @param t Every triple in m will be passed with this timestamp
     * @param m A Model containing the triples to add
     */
    private void add(final ZonedDateTime t, final Model m) {
        final long timestamp = t.toInstant().toEpochMilli();
        final StmtIterator it = m.listStatements();
        while (it.hasNext()) {
            final Statement triple = it.nextStatement();
            final RDFNode o = triple.getObject();
            if (o.isAnon()) {
                FoodSafetyException.internalError(String.format("Blank node in %s", m.toString()));
            }
            this.rdfStream.put(new RdfQuadruple(
                    triple.getSubject().getURI(),
                    triple.getPredicate().getURI(),
                    o.isResource() ? 
                            o.asResource().getURI() : 
                                String.format(
                                        "\"%s\"^^%s", 
                                        o.asLiteral().getLexicalForm(),
                                        o.asLiteral().getDatatypeURI()),
                    timestamp));
        }
    }
}
