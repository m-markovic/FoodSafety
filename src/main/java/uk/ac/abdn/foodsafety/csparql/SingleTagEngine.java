package uk.ac.abdn.foodsafety.csparql;

import java.io.InputStream;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Stream;

import uk.ac.abdn.foodsafety.common.FoodSafetyException;
import uk.ac.abdn.foodsafety.common.Logging;
import uk.ac.abdn.foodsafety.provenance.ProvenanceReasoner;
import uk.ac.abdn.foodsafety.sensordata.TimedTemperatureReading;
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
public final class SingleTagEngine
    extends CsparqlEngineImpl 
    implements Consumer<TimedTemperatureReading> {
    private final RdfStream rdfStream = new RdfStream("http://foodsafety/parsed");
    private final SingleTagWindowBuilder formatter = new SingleTagWindowBuilder(new ProvenanceReasoner());
    private final Stream.Builder<TimedTemperatureReading> readings = Stream.builder();

    private int numQuadruples = 0;
    private int numReadings = 0;
    
    /**
     * Initializes this engine.
     */
    public SingleTagEngine() {
        this.initialize();
        this.registerStream(this.rdfStream);
        this.registerQueryFromResources("/window.sparql.txt");
    }
    
    /**
     * Call this once all streams have been processed.
     */
    public void done() {
        this.readings.build()
            .sorted(Comparator.comparing(r -> r.time))
            .map(SingleTagWindowBuilder::quadruples)
            .reduce(Stream.empty(), Stream::concat)
            .forEachOrdered(tuple -> this.put(tuple));
        ;
        Logging.info(String.format("Total number of readings: %d", this.numReadings));
        Logging.info(String.format("Total number of quadruples put into C-Sparql: %d", this.numQuadruples));
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
        final InputStream resourceAsStream = SingleTagEngine.class.getResourceAsStream(path);
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
     * Saves the reading, to be processed when done() is called.
     * @param A reading from a wireless tag or the meat probe.
     */
    public void accept(final TimedTemperatureReading reading) {
        this.numReadings += 1;
        this.readings.accept(reading);
    }

    /**
     * Puts the given quadruple into this engine's single stream.
     * @param q An RdfQuadruple
     */
    private void put(final RdfQuadruple q) {
        this.numQuadruples += 1;
        this.rdfStream.put(q);
    }
}
