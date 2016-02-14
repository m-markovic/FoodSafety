package uk.ac.abdn.foodsafety.sensordata;

import java.time.ZonedDateTime;

public class Reading {
    /** Timestamp of the reading */
    public final ZonedDateTime time;
    
    Reading(final ZonedDateTime time) {
        this.time = time;
    }
}
