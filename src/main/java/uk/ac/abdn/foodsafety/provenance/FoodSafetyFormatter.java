package uk.ac.abdn.foodsafety.provenance;

import java.util.Observable;

import uk.ac.abdn.foodsafety.common.Logging;
import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.core.ResultFormatter;

class FoodSafetyFormatter extends ResultFormatter {
    private final String queryName;

    FoodSafetyFormatter(final String queryName) {
        this.queryName = queryName;
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
        // TODO Auto-generated method stub
        
    }

    public void setOwl(final String content) {
        // TODO Auto-generated method stub
        
    }   
}