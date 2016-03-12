package uk.ac.abdn.foodsafety.simulator.sensordata;

import java.time.ZonedDateTime;

/**
 * 
 * @author nhc
 *
 * Represents one timestamped temperature/humidity reading from a wireless tag.
 */
public final class WirelessTagReading extends TimedTemperatureReading {
    /** Humidity e.g. 24.85736083984375 */
    public final Double humidity;
    
    /**
     * @param time Timestamp of the reading
     * @param temperature e.g. 19.946533012390137
     * @param humidity e.g. 24.85736083984375
     */
    public WirelessTagReading(
            final ZonedDateTime time,
            final Double temperature,
            final Double humidity) {
        super(
            time, 
            temperature, 
            TimedTemperatureReading.SensorType.WIRELESS_TAG, 
            "http://example.org/wirelessTag");
        this.humidity = humidity;
    }
}