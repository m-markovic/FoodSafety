package uk.ac.abdn.foodsafety.wirelesstag;

import java.util.List;

/**
 * A GetStatsRawResponse represents the data sent in a
 * response from the "/ethLogs.asmx/GetStatsRaw" operation.
 * It is intended to be deserialized from JSON by GSON.
 */
public final class GetStatsRawResponse {
    /** This operation returns a sequence of readings for raw temperature/battery/humidity data. */
    private List<TemperatureBatteryHumidityReading> d;

    /**
     * Temporary implementation for inspecting the responses.
     */
    public String toString() {
        return d.toString();
    }
    
    /**
     * A TemperatureBatteryHumidityReading contains three sequences of readings
     * for a given date.
     */
    public static final class TemperatureBatteryHumidityReading {
        /** Example: "2/8/2016" for Feb 8 2016. 
          * This is in American style: MM/dd/yyyy */
        private String date;
        
        /** Example: 66471 */
        private List<Integer> tods;
        
        /** Examples: 19.946533012390137 */
        private List<Double> temps;
        
        /** Example: 24.85736083984375 */
        private List<Double> caps;

        /**
         * Temporary implementation for inspecting the responses.
         */
        public String toString() {
            return String.join("\n", date, tods.toString(), temps.toString(), caps.toString());
        }
       
    };

}
