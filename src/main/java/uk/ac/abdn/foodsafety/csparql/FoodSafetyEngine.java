package uk.ac.abdn.foodsafety.csparql;

import java.io.InputStream;
import java.text.ParseException;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Consumer;

import uk.ac.abdn.foodsafety.common.FoodSafetyException;
import uk.ac.abdn.foodsafety.sensordata.MeatProbeReading;
import uk.ac.abdn.foodsafety.sensordata.TemperatureHumidityReading;
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
public final class FoodSafetyEngine
    extends CsparqlEngineImpl {
    private final RdfStream wirelessStream = new RdfStream("http://foodsafety/wirelessTag");
    
    /**
     * Initializes this engine.
     */
    public FoodSafetyEngine() {
        this.initialize();
        this.registerStream(wirelessStream);
        this.registerQueryFromResources("/wireless.sparql.txt");
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
            this.registerQuery(text, false).addObserver(new ConsoleFormatter());
        } catch (final ParseException e) {
            throw FoodSafetyException.internalError(e);
        } finally { //Close Scanner
            if (scanner != null) {
                scanner.close();
            }
        }
        
    }
    
    /**
     * Puts seven RdfQuadruples to this engine, based on one temperature/humidity reading from a wireless tag.
     */
    public void accept(final TemperatureHumidityReading reading) {
        final String time = reading.time.format(DateTimeFormatter.ISO_LOCAL_TIME);
        final long timestamp = reading.time.toInstant().toEpochMilli();
        final String baseUri = "http://foodsafety-onto#";
        UUID random = UUID.randomUUID();
        wirelessStream.put(new RdfQuadruple(baseUri +  "wirelesstag", baseUri + "observes", baseUri + "temperatureObservation/" + random.toString(), timestamp));
        wirelessStream.put(new RdfQuadruple(baseUri +  "temperatureObservation/" + random.toString(), baseUri +  "type", baseUri + "temperatureObservation", timestamp));
        wirelessStream.put(new RdfQuadruple(baseUri + "temperatureObservation/" + random.toString(), baseUri + "value", Double.toString(reading.temperature), timestamp));
        wirelessStream.put(new RdfQuadruple(baseUri + "temperatureObservation/" + random.toString(), baseUri + "time", time, timestamp));
        UUID random2 = UUID.randomUUID();
        wirelessStream.put(new RdfQuadruple(baseUri +  "wirelesstag", baseUri + "observes", baseUri + "humidityObservation/" + random2.toString(), timestamp));
        wirelessStream.put(new RdfQuadruple(baseUri + "humidityObservation/" + random2.toString(), baseUri + "value", Double.toString(reading.humidity), timestamp));
        wirelessStream.put(new RdfQuadruple(baseUri +  "humidityObservation/" + random2.toString(), baseUri +  "type", baseUri + "humidityObservation", timestamp));
        wirelessStream.put(new RdfQuadruple(baseUri + "humidityObservation/" + random2.toString(), baseUri + "time", time, timestamp));
    }

    public Consumer<TemperatureHumidityReading> temperatureHumidityConsumer() {
        return reading -> this.accept(reading);
    }

    public Consumer<MeatProbeReading> meatProbeConsumer() {
        return reading -> this.accept(reading);
    }

    public void accept(final MeatProbeReading reading) {
        //TODO
    }
}
