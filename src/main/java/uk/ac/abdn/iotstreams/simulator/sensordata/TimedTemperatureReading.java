package uk.ac.abdn.iotstreams.simulator.sensordata;

import java.time.ZonedDateTime;

public abstract class TimedTemperatureReading {
    /** Timestamp of the reading */
    public final ZonedDateTime time;
    
    /** Temperature in Celsius, e.g. 19.946533012390137 */
    public final Double temperature;

    /** Feature of interest, e.g. "http://example.org/meatCoreTemp" */
    public String foi;

    /** Wireless tag or meat probe? */
    public final SensorType sensorType;

    /** the ID of the sensor that made this reading */
    public final int sensorId;
    
    TimedTemperatureReading(
            final ZonedDateTime time,
            final Double temperature,
            final SensorType sensorType,
            final String foi,
            final int sensorId) {
        this.time = time;
        this.temperature = temperature;
        this.sensorType = sensorType;
        this.foi = foi;
        this.sensorId = sensorId;
    }
    
    /**
     * Wireless tag or meat probe?
     */
    public enum SensorType {
        WIRELESS_TAG,
        MEAT_PROBE
    }
}
