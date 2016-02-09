package uk.ac.abdn.foodsafety.csparql;

import java.text.ParseException;
import java.util.Scanner;

import uk.ac.abdn.foodsafety.FoodSafetyException;
import eu.larkc.csparql.cep.api.RdfStream;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;
import eu.larkc.csparql.core.engine.CsparqlQueryResultProxy;

/**
 * 
 * @author nhc
 *
 * A FoodSafetyEngine is a specific CsparqlEngine with methods
 * and queries for this project.
 */
public final class FoodSafetyEngine extends CsparqlEngineImpl {
    private final RdfStream wirelessStream = new RdfStream("http://foodsafety/wirelessTag");
    private final CsparqlQueryResultProxy wirelessQueryProxy;
    
    /**
     * Initializes this engine.
     */
    public FoodSafetyEngine() {
        this.initialize();
        this.registerStream(wirelessStream);
        String text = new Scanner(FoodSafetyEngine.class.getResourceAsStream("/wireless.sparql.txt"), "UTF-8").useDelimiter("\\A").next();
        try {
            wirelessQueryProxy = this.registerQuery(text, false);
        } catch (final ParseException e) {
            throw FoodSafetyException.internalError(e);
        }
    }
}
