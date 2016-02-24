package uk.ac.abdn.foodsafety.sensordata;

import java.time.ZonedDateTime;

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
    
    public abstract String observationType();
    
}
