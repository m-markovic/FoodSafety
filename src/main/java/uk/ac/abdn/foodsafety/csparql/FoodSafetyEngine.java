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
import eu.larkc.csparql.core.engine.CsparqlEngineImpl;

//import eu.larkc.csparql.common.*;
/**
 * 
 * @author nhc
 *
 * A FoodSafetyEngine is a specific CsparqlEngine with methods
 * and queries for this project.
 */
public final class FoodSafetyEngine
    extends CsparqlEngineImpl {
    private final RdfStream rdfStream = new RdfStream("http://foodsafety/parsed");
    private final FoodSafetyResultFormatter formatter = new FoodSafetyResultFormatter();
    private int quadruples = 0;
    private int readings = 0;
    
    /**
     * Initializes this engine.
     */
    public FoodSafetyEngine() {
        this.initialize();
        this.registerStream(this.rdfStream);
        this.registerQueryFromResources("/meatprobe.sparql.txt");
    }
    
    /**
     * Call this once all streams have been processed.
     * Currently dumps the internal Jena model.
     */
    public void done() {
        this.formatter.dump();
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
            this.registerQuery(text, false).addObserver(formatter);
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
        this.put(new RdfQuadruple(baseUri +  "wirelesstag", baseUri + "observes", baseUri + "temperatureObservation/" + random.toString(), timestamp));
        this.put(new RdfQuadruple(baseUri +  "temperatureObservation/" + random.toString(), baseUri +  "type", baseUri + "temperatureObservation", timestamp));
        this.put(new RdfQuadruple(baseUri + "temperatureObservation/" + random.toString(), baseUri + "value", Double.toString(reading.temperature), timestamp));
        this.put(new RdfQuadruple(baseUri + "temperatureObservation/" + random.toString(), baseUri + "time", time, timestamp));
        UUID random2 = UUID.randomUUID();
        this.put(new RdfQuadruple(baseUri +  "wirelesstag", baseUri + "observes", baseUri + "humidityObservation/" + random2.toString(), timestamp));
        this.put(new RdfQuadruple(baseUri + "humidityObservation/" + random2.toString(), baseUri + "value", Double.toString(reading.humidity), timestamp));
        this.put(new RdfQuadruple(baseUri +  "humidityObservation/" + random2.toString(), baseUri +  "type", baseUri + "humidityObservation", timestamp));
        this.put(new RdfQuadruple(baseUri + "humidityObservation/" + random2.toString(), baseUri + "time", time, timestamp));
    }

    /**
     * Puts three RdfQuadruples to this engine, based on one meat probe reading.
     */
    public void accept(final MeatProbeReading reading) {
        this.readings  += 1;
        final String baseUri = "http://foodsafety-onto#";
        final long timestamp = reading.time.toInstant().toEpochMilli();
        this.put(new RdfQuadruple(
                baseUri +  "meatProbe", 
                baseUri + "observes", 
                baseUri + "observation" + reading.id, 
                timestamp));
        this.put(new RdfQuadruple(
                baseUri + "observation" + reading.id, 
                baseUri + "value", 
                String.format("\"%f\"^^http://www.w3.org/2001/XMLSchema#decimal", reading.temperature), 
                timestamp));
        this.put(new RdfQuadruple(
                baseUri + "observation" + reading.id, 
                baseUri + "time", 
                String.format("\"%s\"^^http://www.w3.org/2001/XMLSchema#dateTime", reading.time.format(DateTimeFormatter.ISO_INSTANT)), 
                timestamp));
    }

    private void put(final RdfQuadruple q) {
        this.quadruples += 1;
        this.rdfStream.put(q);
    }

    public Consumer<TemperatureHumidityReading> temperatureHumidityConsumer() {
        return reading -> this.accept(reading);
    }

    public Consumer<MeatProbeReading> meatProbeConsumer() {
        return reading -> this.accept(reading);
    }

}
