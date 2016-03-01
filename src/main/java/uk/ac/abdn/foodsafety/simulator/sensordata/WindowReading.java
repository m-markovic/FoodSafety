package uk.ac.abdn.foodsafety.simulator.sensordata;

import java.time.ZonedDateTime;

public final class WindowReading {
    /** E.g. "http://example.org/wirelessTag" or "http://example.org/meatCoreTemp" or "http://example.org/meatItem345" */
    public final String foi;
    
    /** Temperature in Celsius, e.g. 19.946533012390137 */
    public final Double temperature;

    /** Timestamp of the reading */
    public final ZonedDateTime time;
    
    public WindowReading(
            final String foi,
            final Double temperature,
            final ZonedDateTime time) {
        this.foi = foi;
        this.temperature = temperature;
        this.time = time;
    }
}
