package uk.ac.abdn.foodsafety.provenance;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Observable;
import java.util.Optional;

import uk.ac.abdn.foodsafety.common.FoodSafetyException;
import uk.ac.abdn.foodsafety.common.Logging;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra;
import com.hp.hpl.jena.update.UpdateAction;

import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.RDFTuple;
import eu.larkc.csparql.core.ResultFormatter;

/**
 * 
 * @author nhc
 *
 * A FoodSafetyFormatter listens to one C-SPARQL query.
 * When the query emits a result (i.e. triples for one window),
 * the FoodSafetyFormatter executes the registered SPARQL updates
 * and handles provenance results.
 */
class FoodSafetyFormatter extends ResultFormatter {
    /** Name picked up from directory - used for logging */
    private final String queryName;
    
    /** The SPARQL update queries to execute */
    private EnumMap<Stage, HashMap<String, String>> sparqlUpdateQueries =
            new EnumMap<Stage, HashMap<String, String>>(Stage.class);

    /** Model accumulating the received triples along with the OWL ontology */
    private Optional<OntModel> m = Optional.empty();

    /** Model containing the latest provenance found */
    private Optional<Model> oldProv = Optional.empty();

    /** Final resting place for inferred provenance */
    private final Model persistentModel;

    /**
     * Registers the query name and prepares for configuration by
     * addSparql() and setOwl()
     * @param queryName Name picked up from directory - used for logging
     * @param persistentModel All inferred provenance will be added to this model
     */
    FoodSafetyFormatter(final String queryName, final Model persistentModel) {
        this.queryName = queryName;
        this.persistentModel = persistentModel;
        //Initialize SPARQL update query collections
        for (Stage s : Stage.values()) {
            this.sparqlUpdateQueries.put(s, new HashMap<String, String>());
        }
    }
    
    /**
     * Called when C-Sparql emits a window.
     * Adds all triples to the internal model, then runs infer().
     */
    @Override
    public synchronized void update(final Observable ignored, final Object rdfTableUntyped) {
        final RDFTable rdfTable = (RDFTable) rdfTableUntyped;
        rdfTable.stream()
            .map(this::convert)
            .forEach(s -> this.m.get().add(s));
        this.infer();
    }
    
    /**
     * Converts a in an RdfTuple to a Jena Statement.
     * @param t must have URIs as element 0 and 1, and a URI or a Literal as element 2
     * @return The Statement representing elements 0,1,2 of t
     */
    private Statement convert(final RDFTuple t) {
        RDFNode o;
        try { //Try to parse t.get(2) as a Literal
            o = this.m.get().asRDFNode(NodeFactoryExtra.parseNode(t.get(2)));
        } catch (final Exception e) { //Failed to parse, it must be a URI
            o = this.m.get().createResource(t.get(2));
        }
        try { //Compose and return the Statement
            return this.m.get().createStatement(
                    this.m.get().createResource(t.get(0)), 
                    this.m.get().createProperty(t.get(1)),
                    o);
        } catch (final Exception e) {
            throw FoodSafetyException.internalError(String.format("Problem converting %s", t.get(2)));
        }
    }

    /**
     * Runs coldstart or warm SPARQL queries, updating Jena models as appropriate.
     */
    private void infer() {
        final Model provmod = ModelFactory.createDefaultModel();
        final long s = provmod.size();
        provmod.add(this.m.get());
        if (this.oldProv.isPresent()) {
            //Note: this modification needs to happen before updating oldProv and persistentModel
            provmod.add(this.oldProv.get());
            this.sparqlUpdateQueries.get(Stage.WARM)
                .forEach((name, query) -> update(name, query, provmod));
            provmod.remove(this.m.get());
            provmod.remove(this.oldProv.get());
            if (provmod.size() > s) { //we inferred something
                this.oldProv = Optional.of(provmod);
                this.persistentModel.add(provmod);
            }
        } else { //First run
            this.sparqlUpdateQueries.get(Stage.COLDSTART)
                .forEach((name, query) -> update(name, query, provmod));
            provmod.remove(this.m.get());
            if (provmod.size() > s) { //we inferred something
                this.oldProv = Optional.of(provmod);
                this.persistentModel.add(provmod);
            } else { //No inference - error
                throw FoodSafetyException.configurationError(String.format("The coldstart SPARQL for %s did not infer anything", this.queryName));
            }
        }
    }
    
    /**
     * Parse&execute a SPARQL update query, logging stats about the execution.
     * @param name Name of the query, for logging
     * @param query The SPARQL text
     * @param provmod The model to update
     */
    private static void update(final String name, final String query, final Model provmod) {
        final long beforeSize = provmod.size();
        final Instant before = Instant.now();
        UpdateAction.parseExecute(query, provmod);
        final Instant after = Instant.now();
        final long afterSize = provmod.size();
        Logging.info(String.format(
                "Query %s: %d ms ; %d triples generated", 
                name, 
                Duration.between(before, after).toMillis(),
                afterSize - beforeSize));
    }

    /**
     * Adds a SPARQL update query to the given stage.
     * @param stage Must be "coldstart" or "warm" to decide in which case the query will be executed.
     * @param name name of this update
     * @param content The text of the query.
     */
    public void addSparql(final String stage, final String name, final String content) {
        this.sparqlUpdateQueries.get(Stage.valueOf(stage.toUpperCase())).put(
                String.format("%s/%s", stage, name), 
                content);
    }

    /**
     * Loads the given ontology into the internal Jena model.
     * @param ontology Ontology, in TTL
     */
    public void setOntology(final String ontology) {
        final OntModel plan = ModelFactory.createOntologyModel();
        plan.read(new ByteArrayInputStream(ontology.getBytes(StandardCharsets.ISO_8859_1)), null, "TTL");
        this.m = Optional.of(ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RDFS_INF, plan));
    }
    
    /**
     * Stages in which SPARQL update queries are executed
     */
    private enum Stage {
      /** Coldstart: No provenance has been inferred yet */
      COLDSTART,
      /** Warm: Provenance has already been inferred */
      WARM;
    };
}