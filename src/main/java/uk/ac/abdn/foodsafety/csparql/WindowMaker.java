package uk.ac.abdn.foodsafety.csparql;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Observable;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import uk.ac.abdn.foodsafety.sensordata.TimedTemperatureReading;

import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.core.ResultFormatter;

/**
 * 
 * @author nhc
 *
 * A ResultFormatter which knows how to interpret results of our queries
 * and adds them to an internal Jena Model.
 */
final class WindowMaker extends ResultFormatter {
    private final Consumer<Stream<WindowReading>> consumer;

    public WindowMaker(final Consumer<Stream<WindowReading>> consumer) {
        this.consumer = consumer;
    }
    
    public static Stream<RdfQuadruple> quadruples(final TimedTemperatureReading r) {
        final String uuidUri = uri("obs", UUID.randomUUID().toString());
        final long timestamp = r.time.toInstant().toEpochMilli();
        return Arrays.stream(new RdfQuadruple[] {
            new RdfQuadruple(uuidUri, uri("type"), r.observationType(), timestamp),
            new RdfQuadruple(uuidUri, uri("temp"), Double.toString(r.temperature), timestamp),
            new RdfQuadruple(uuidUri, uri("time"), r.time.format(DateTimeFormatter.ISO_DATE_TIME), timestamp)
        });
    }

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

    private static String uri(final String... values) {
        return String.format("http://foodsafety#%s", String.join("-", values));
    }
    
}
