package uk.ac.abdn.foodsafety.csparql;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Observable;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import uk.ac.abdn.foodsafety.sensordata.TimedTemperatureReading;
import uk.ac.abdn.foodsafety.sensordata.WindowReading;
import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.core.ResultFormatter;

/**
 * 
 * @author nhc
 *
 * A SingleTagWindowBuilder reads/writes sensor readings from/to C-Sparql.
 * In companionship with the C-SPARQL query, this causes readings
 * to be bundled into time windows.
 */
final class SingleTagWindowBuilder extends ResultFormatter {
    /** Each window of readings is provided to this object */
    private final Consumer<Stream<WindowReading>> consumer;

    /**
     * Registers the consumer of bundled readings
     * @param consumer each window of readings will be provided to this object
     */
    public SingleTagWindowBuilder(final Consumer<Stream<WindowReading>> consumer) {
        this.consumer = consumer;
    }
    
    /**
     * Converts the given reading into tuples for C-Sparql
     * @param r A temperature reading from a wireless tag or meat probe
     * @return A Stream of tuples designed for our C-Sparql query
     */
    public static Stream<RdfQuadruple> quadruples(final TimedTemperatureReading r) {
        final String uuidUri = uri("obs", UUID.randomUUID().toString());
        final long timestamp = r.time.toInstant().toEpochMilli();
        return Arrays.stream(new RdfQuadruple[] {
            new RdfQuadruple(uuidUri, uri("type"), r.observationType(), timestamp),
            new RdfQuadruple(uuidUri, uri("temp"), Double.toString(r.temperature), timestamp),
            new RdfQuadruple(uuidUri, uri("time"), r.time.format(DateTimeFormatter.ISO_DATE_TIME), timestamp)
        });
    }
    
    /**
     * Utility method for creating URIs in the "fs" namespace of our
     * C-Sparql query.
     * @param values Elements to include in the URI, e.g. "a", "b", "c"
     * @return e.g. "http://foodsafety#a-b-c"
     */
    private static String uri(final String... values) {
        return String.format("http://foodsafety#%s", String.join("-", values));
    }


    /**
     * Called when C-Sparql emits a window.
     * The readings captured by our C-Sparql query for this window
     * are converted to a WindowReading, then passed on to
     * this object's internal consumer.
     */
    @Override
    public void update(final Observable ignored, final Object rdfTableUntyped) {
        final RDFTable rdfTable = (RDFTable) rdfTableUntyped;
        final Stream<WindowReading> readingStream = rdfTable.stream()
            .map(tuple -> new WindowReading(
                    tuple.get(0), 
                    Double.parseDouble(tuple.get(1)), 
                    ZonedDateTime.parse(tuple.get(2))));
        this.consumer.accept(readingStream);
    }    
}
