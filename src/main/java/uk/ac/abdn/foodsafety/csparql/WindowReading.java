package uk.ac.abdn.foodsafety.csparql;

import java.time.ZonedDateTime;

final class WindowReading {
    /** E.g. "meatprobe" or "tag" */
    public final String type;
    
    /** Temperature in Celsius, e.g. 19.946533012390137 */
    public final Double temperature;

    /** Timestamp of the reading */
    public final ZonedDateTime time;
    
    WindowReading(
            final String type,
            final Double temperature,
            final ZonedDateTime time) {
        this.type = type;
        this.temperature = temperature;
        this.time = time;
    }
}
