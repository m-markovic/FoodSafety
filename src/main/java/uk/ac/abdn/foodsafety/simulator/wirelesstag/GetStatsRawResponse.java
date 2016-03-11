package uk.ac.abdn.foodsafety.simulator.wirelesstag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import uk.ac.abdn.foodsafety.common.Constants;
import uk.ac.abdn.foodsafety.common.Logging;
import uk.ac.abdn.foodsafety.simulator.sensordata.TemperatureHumidityReading;

/**
 * A GetStatsRawResponse represents the data sent in a
 * response from the "/ethLogs.asmx/GetStatsRaw" operation.
 * It is intended to be deserialized from JSON by GSON.
 */
public final class GetStatsRawResponse {
    /** This operation returns a sequence of readings for raw temperature/battery/humidity data. */
    private List<SingleDayTemperatureHumidityReadings> d;

    public Stream<TemperatureHumidityReading> stream() {
        return d.stream()
                .map((day) -> day.stream())
                .reduce(Stream.empty(), Stream::concat);
    }

    /**
     * A SingleDayTemperatureHumidityReadings contains three sequences of readings
     * for a given date.
     */
    public static final class SingleDayTemperatureHumidityReadings {
        /** Example: "2/8/2016" for Feb 8 2016. 
          * This is in American style: MM/dd/yyyy */
        private String date;
        
        /** TimeOfDay. Seconds since midnight. Example: 66471 */
        private List<Integer> tods;
        
        /** Temperature in Celsius. Examples: 19.946533012390137 */
        private List<Double> temps;
        
        /** Humidity. Example: 24.85736083984375 */
        private List<Double> caps;

        private Stream<TemperatureHumidityReading> stream() {
            final String[] mdy = date.split("/");
            final LocalDate localDate = LocalDate.of(
                            Integer.parseInt(mdy[2]), 
                            Integer.parseInt(mdy[0]), 
                            Integer.parseInt(mdy[1]));
            final Stream.Builder<TemperatureHumidityReading> result = Stream.builder();
            for (int i = 0; i < tods.size(); i++) {
                final LocalDateTime localDateTime = LocalDateTime.of(localDate, LocalTime.ofSecondOfDay(tods.get(i)));
                result.accept(new TemperatureHumidityReading(
                        localDateTime.atZone(Constants.UK),
                        temps.get(i),
                        caps.get(i)));
            }
            return result.build();
        }
    }

    /**
     * Log the total number of readings in the response
     */
    public void log() {
        Logging.info(String.format("Retrieved %d readings", this.d.stream().mapToInt(day -> day.temps.size()).sum()));
    }
}
