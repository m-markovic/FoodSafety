package uk.ac.abdn.foodsafety.wirelesstag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import uk.ac.abdn.foodsafety.csparql.FoodSafetyEngine;

/**
 * A GetStatsRawResponse represents the data sent in a
 * response from the "/ethLogs.asmx/GetStatsRaw" operation.
 * It is intended to be deserialized from JSON by GSON.
 */
public final class GetStatsRawResponse {
    /** This operation returns a sequence of readings for raw temperature/battery/humidity data. */
    private List<TemperatureBatteryHumidityReading> d;

    public void addReadingsTo(final FoodSafetyEngine engine) {
        for (final TemperatureBatteryHumidityReading day : d) {
            day.addTo(engine);
        };
    }
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
        
        /** TimeOfDay. Seconds since midnight. Example: 66471 */
        private List<Integer> tods;
        
        /** Temperature in Celsius. Examples: 19.946533012390137 */
        private List<Double> temps;
        
        /** Humidity. Example: 24.85736083984375 */
        private List<Double> caps;

        /**
         * Temporary implementation for inspecting the responses.
         */
        public String toString() {
            return String.join("\n", date, tods.toString(), temps.toString(), caps.toString());
        }

        public void addTo(final FoodSafetyEngine engine) {
            for (int i = 0; i < tods.size(); i++) {
                final String[] mdy = date.split("/");
                final LocalDateTime localDateTime = LocalDateTime.of(
                        LocalDate.of(
                                Integer.parseInt(mdy[2]), 
                                Integer.parseInt(mdy[0]), 
                                Integer.parseInt(mdy[1])), 
                        LocalTime.ofSecondOfDay(tods.get(i)));
                final ZonedDateTime zdt = localDateTime.atZone(ZoneId.of("America/Los_Angeles"));
                engine.addReading(
                        zdt.toInstant().toEpochMilli(), 
                        Double.toString(temps.get(i)), 
                        Double.toString(caps.get(i)));
            }
        }
       
    };

}
