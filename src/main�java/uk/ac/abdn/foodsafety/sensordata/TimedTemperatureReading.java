package uk.ac.abdn.foodsafety.sensordata;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

import eu.larkc.csparql.cep.api.RdfQuadruple;

public abstract class TimedTemperatureReading {
    /** Timestamp of the reading */
    public final ZonedDateTime time;
    
    /** Temperature in Celsius, e.g. 19.946533012390137 */
    public final Double temperature;

    TimedTemperatureReading(
            final ZonedDateTime time,
            final Double temperature) {
        this.time = time;
        this.temperature = temperature;
    }
    
    protected abstract String observationType();
    
    private static String uri(final String... values) {
        return String.format("http://foodsafety#%s", String.join("-", values));
    }
    
    public Stream<RdfQuadruple> toRdf() {
        final String uuidUri = uri("obs", UUID.randomUUID().toString());
        final long timestamp = this.time.toInstant().toEpochMilli();
        return Arrays.stream(new RdfQuadruple[] {
            new RdfQuadruple(uuidUri, uri("type"), this.observationType(), timestamp),
            new RdfQuadruple(uuidUri, uri("temp"), Double.toString(this.temperature), timestamp),
            new RdfQuadruple(uuidUri, uri("time"), this.time.format(DateTimeFormatter.ISO_DATE_TIME), timestamp)
        });
    }
}
