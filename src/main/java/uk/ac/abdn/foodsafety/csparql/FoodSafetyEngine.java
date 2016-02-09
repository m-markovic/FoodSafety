package uk.ac.abdn.foodsafety.csparql;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.UUID;

import uk.ac.abdn.foodsafety.FoodSafetyException;
import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;
import eu.larkc.csparql.core.engine.ConsoleFormatter;
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;

/**
 * 
 * @author nhc
 *
 * A FoodSafetyEngine is a specific CsparqlEngine with methods
 * and queries for this project.
 */
public final class FoodSafetyEngine extends CsparqlEngineImpl {
    private final RdfStream wirelessStream = new RdfStream("http://foodsafety/wirelessTag");
    
    /**
     * Initializes this engine.
     */
    public FoodSafetyEngine() {
        this.initialize();
        this.registerStream(wirelessStream);
        String text = new Scanner(FoodSafetyEngine.class.getResourceAsStream("/wireless.sparql.txt"), "UTF-8").useDelimiter("\\A").next();
        try { //Register query and add a System.out observer
            this.registerQuery(text, false).addObserver(new ConsoleFormatter());
        } catch (final ParseException e) {
            throw FoodSafetyException.internalError(e);
        }
    }
    
    public void addReading(final long timestamp, final String temperature, final String humidity) {
        final Date d = new Date (timestamp);      
        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        final String time = sdf.format(d);
        final String baseUri = "http://foodsafety-onto#";
        UUID random = UUID.randomUUID();
        wirelessStream.put(new RdfQuadruple(baseUri +  "wirelesstag", baseUri + "observes", baseUri + "temperatureObservation/" + random.toString(), timestamp));
        wirelessStream.put(new RdfQuadruple(baseUri +  "temperatureObservation/" + random.toString(), baseUri +  "type", baseUri + "temperatureObservation", timestamp));
        wirelessStream.put(new RdfQuadruple(baseUri + "temperatureObservation/" + random.toString(), baseUri + "value", temperature, timestamp));
        wirelessStream.put(new RdfQuadruple(baseUri + "temperatureObservation/" + random.toString(), baseUri + "time", time, timestamp));
        UUID random2 = UUID.randomUUID();
        wirelessStream.put(new RdfQuadruple(baseUri +  "wirelesstag", baseUri + "observes", baseUri + "humidityObservation/" + random2.toString(), timestamp));
        wirelessStream.put(new RdfQuadruple(baseUri + "humidityObservation/" + random2.toString(), baseUri + "value", temperature, System.currentTimeMillis()));
        wirelessStream.put(new RdfQuadruple(baseUri +  "humidityObservation/" + random2.toString(), baseUri +  "type", baseUri + "humidityObservation", timestamp));
        wirelessStream.put(new RdfQuadruple(baseUri + "humidityObservation/" + random2.toString(), baseUri + "time", time, timestamp));
    }
}
