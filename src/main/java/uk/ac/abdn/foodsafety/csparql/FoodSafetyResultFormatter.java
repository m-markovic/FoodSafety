package uk.ac.abdn.foodsafety.csparql;

import java.util.Observable;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.RDFTuple;
import eu.larkc.csparql.core.ResultFormatter;

final class FoodSafetyResultFormatter extends ResultFormatter {
    private OntModel model = ModelFactory.createOntologyModel();
    
    @Override
    public void update(final Observable ignored, final Object rdfTableUntyped) {
        final RDFTable rdfTable = (RDFTable) rdfTableUntyped;
        for (RDFTuple tuple : rdfTable) {
            System.out.println(String.format(
                    "<%s><%s><%s>",
                    tuple.get(0),
                    tuple.get(1),
                    tuple.get(2)));
        }
    }

}
