package uk.ac.abdn.foodsafety.simulator.sensordata;

import java.time.ZonedDateTime;

/**
 * 
 * @author nhc
 *
 * Represents one timestamped temperature/humidity reading from a sensor.
 */
public final class TemperatureHumidityReading extends TimedTemperatureReading {
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
        super(time, temperature, "http://example.org/wirelessTag");
        this.humidity = humidity;
    }
}