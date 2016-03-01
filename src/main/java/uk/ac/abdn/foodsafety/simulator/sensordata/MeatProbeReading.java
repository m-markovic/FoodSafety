package uk.ac.abdn.foodsafety.simulator.sensordata;

import java.time.ZonedDateTime;

/**
 * 
 * @author nhc
 *
 * Represents one ID'ed and timestamped temperature reading from a meat probe.
 */
public final class MeatProbeReading extends TimedTemperatureReading {
    /** ID from the meat probe */
    public final int id;
    
    /**
     * @param id ID from the meat probe
     * @param time Timestamp of the reading
     * @param temperature e.g. 31.3
     */
    public MeatProbeReading(
            final int id,
            final ZonedDateTime time,
            final Double temperature) {
        super(time, temperature, "http://example.org/meatCoreTemp");
        this.id = id;
    }
}