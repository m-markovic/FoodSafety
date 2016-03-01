package uk.ac.abdn.foodsafety.simulator.sensordata;

import java.time.ZonedDateTime;

public abstract class TimedTemperatureReading {
    /** Timestamp of the reading */
    public final ZonedDateTime time;
    
    /** Temperature in Celsius, e.g. 19.946533012390137 */
    public final Double temperature;

    /** Feature of interest, e.g. "http://example.org/meatCoreTemp" */
    public String foi;
    
    TimedTemperatureReading(
            final ZonedDateTime time,
            final Double temperature,
            final String foi) {
        this.time = time;
        this.temperature = temperature;
        this.foi = foi;
    }
}
