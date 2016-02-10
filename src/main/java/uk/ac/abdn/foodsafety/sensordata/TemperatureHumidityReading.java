package uk.ac.abdn.foodsafety.sensordata;

import java.time.ZonedDateTime;

/**
 * 
 * @author nhc
 *
 * Represents one timestamped temperature/humidity reading from a sensor.
 */
public final class TemperatureHumidityReading {
    /** Timestamp of the reading */
    public final ZonedDateTime time;
    
    /** Temperature in Celsius, e.g. 19.946533012390137 */
    public final Double temperature;
    
    /** Humidity e.g. 24.85736083984375 */
    public final Double humidity;
    
    /**
     * @param time Timestamp of the reading
     * @param temperature e.g. 19.946533012390137
     * @param humidity e.g. 24.85736083984375
     */
    public TemperatureHumidityReading(
            final ZonedDateTime time,
            final Double temperature,
            final Double humidity) {
        this.time = time;
        this.temperature = temperature;
        this.humidity = humidity;
    }
}