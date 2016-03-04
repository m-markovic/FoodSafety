package uk.ac.abdn.foodsafety.provenance;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Observable;
import java.util.Optional;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra;

import uk.ac.abdn.foodsafety.common.Logging;
import eu.larkc.csparql.common.RDFTable;
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
    private EnumMap<Stage, List<String>> sparqlUpdateQueries =
            new EnumMap<Stage, List<String>>(Stage.class);

    /** Model accumulating the received triples along with the OWL ontology */
    private Optional<OntModel> m = Optional.empty();

    /** Model containing the latest provenance found */
    private Optional<Model> oldProv = Optional.empty();

    /**
     * Registers the query name and prepares for configuration by
     * addSparql() and setOwl()
     * @param queryName Name picked up from directory - used for logging
     */
    FoodSafetyFormatter(final String queryName) {
        this.queryName = queryName;
        //Initialize SPARQL update query collections
        for (Stage s : Stage.values()) {
            this.sparqlUpdateQueries.put(s, new ArrayList<String>());
        }
    }
    
    /**
     * Called when C-Sparql emits a window.
     * Adds all triples to the internal model, then runs infer().
     */
    @Override
    public void update(final Observable ignored, final Object rdfTableUntyped) {
        final RDFTable rdfTable = (RDFTable) rdfTableUntyped;
        //TODO: Do more than logging
        Logging.info(String.format("Query %s emitted %d triples", this.queryName, rdfTable.size()));
        rdfTable.stream()
            .map(t -> this.m.get().createStatement(
                    this.m.get().createResource(t.get(0)), 
                    this.m.get().createProperty(t.get(1)),
                    this.m.get().asRDFNode(NodeFactoryExtra.parseNode(t.get(2)))
                ))
            .forEach(s -> this.m.get().add(s));
        this.infer();
    }

    private void infer() {
        // TODO Auto-generated method stub
    }

    /**
     * Adds a SPARQL update query to the given stage.
     * @param stage Must be "coldstart" or "warm" to decide in which case the query will be executed.
     * @param content The text of the query.
     */
    public void addSparql(final String stage, final String content) {
        this.sparqlUpdateQueries.get(Stage.valueOf(stage.toUpperCase())).add(content);
    }

    /**
     * Loads the given ontology into the internal Jena model.
     * @param ontology Ontology, in OWL
     */
    public void setOwl(final String ontology) {
        final OntModel plan = ModelFactory.createOntologyModel();
        plan.read(new ByteArrayInputStream(ontology.getBytes()), null);
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