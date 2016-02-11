package uk.ac.abdn.foodsafety.sensordata;

import java.time.ZonedDateTime;

/**
 * 
 * @author nhc
 *
 * Represents one ID'ed and timestamped temperature reading from a meat probe.
 */
public final class MeatProbeReading {
    /** ID from the meat probe */
    public final int id;
    
    /** Timestamp of the reading */
    public final ZonedDateTime time;
    
    /** Temperature in Celsius, e.g. 31.3 */
    public final Double temperature;
    
    /**
     * @param id ID from the meat probe
     * @param time Timestamp of the reading
     * @param temperature e.g. 31.3
     */
    public MeatProbeReading(
            final int id,
            final ZonedDateTime time,
            final Double temperature) {
        this.id = id;
        this.time = time;
        this.temperature = temperature;
    }
}