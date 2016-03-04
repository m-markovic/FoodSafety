package uk.ac.abdn.foodsafety.provenance;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Observable;

import uk.ac.abdn.foodsafety.common.Logging;
import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.core.ResultFormatter;

class FoodSafetyFormatter extends ResultFormatter {
    /** Name picked up from directory - used for logging */
    private final String queryName;
    
    private EnumMap<Stage, List<String>> sparqlUpdateQueries =
            new EnumMap<Stage, List<String>>(Stage.class);

    FoodSafetyFormatter(final String queryName) {
        this.queryName = queryName;
        //Initialize SPARQL update query collections
        for (Stage s : Stage.values()) {
            this.sparqlUpdateQueries.put(s, new ArrayList<String>());
        }
    }
    
    /**
     * Called when C-Sparql emits a window.
     */
    @Override
    public void update(final Observable ignored, final Object rdfTableUntyped) {
        final RDFTable rdfTable = (RDFTable) rdfTableUntyped;
        Logging.info(String.format("Query %s emitted %d triples", this.queryName, rdfTable.size()));
    }

    public void addSparql(final String name, final String content) {
        this.sparqlUpdateQueries.get(Stage.valueOf(name.toUpperCase())).add(content);
    }

    public void setOwl(final String content) {
        // TODO Auto-generated method stub
        
    }
    
    private enum Stage {
      COLDSTART,
      WARM;
    };
}