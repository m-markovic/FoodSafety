package uk.ac.abdn.foodsafety.common;

import java.time.ZoneId;

/**
 * 
 * @author nhc
 * 
 * Shared constants between FoodSafety packages.
 */
public final class Constants {
    /** All date/time stamps without time zone are assigned to this one: */
    public static final ZoneId UK = ZoneId.of("Europe/London");
}
