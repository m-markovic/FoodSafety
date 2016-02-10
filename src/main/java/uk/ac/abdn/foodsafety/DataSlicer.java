package uk.ac.abdn.foodsafety;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import uk.ac.abdn.foodsafety.csparql.FoodSafetyEngine;

/**
 * 
 * @author nhc
 *
 * A DataSlicer picks slices of sensor data and passes
 * these on to a FoodSafetyEngine.
 */
final class DataSlicer {
    /** Provide sliced sensor data to this object */
    private final FoodSafetyEngine dataConsumer;
    
    /** Slice sensor data by time of reading: Must be after this time. */
    private final LocalDateTime fromDateTime;

    /** Slice sensor data by time of reading: Must be before this time. */
    private final LocalDateTime toDateTime;

    /**
     * Parses from and to as LocalDate/LocalDateTime in the ISO format
     * and registers both of these along with the dataConsumer.
     * @param from Slice sensor data by only providing readings after this time.
     * LocalDate/LocalDateTime in the ISO format, e.g. "2016-01-31T15:36:59"
     * @param to Slice sensor data by only providing readings before this time.
     * LocalDate/LocalDateTime in the ISO format, e.g. "2016-01-31T15:36:59"
     * @param dataConsumer The object to provide sliced data to
     */
    DataSlicer(final String from, final String to, final FoodSafetyEngine dataConsumer) {
        this.dataConsumer = dataConsumer;
        this.fromDateTime = DataSlicer.parse(from, LocalTime.MIN);
        this.toDateTime = DataSlicer.parse(to, LocalTime.MAX);
    }
    
    void add(final WirelessTagClient client, final int sensorId) {
        client.getStatsRaw(
                sensorId, 
                this.fromDateTime.toLocalDate(), 
                this.toDateTime.toLocalDate(), 
                this.dataConsumer);
    }
    
    /**
     * Parses the given userInput as a LocalDateTime or LocalDate.
     * @param userInput An ISO date or datetime, e.g. "2016-01-31T15:36:59"
     * @param defaultTime time to use if userInput was a date and not a datetime.
     * @return The parsed LocalDateTime
     */
    private static LocalDateTime parse(final String userInput, final LocalTime defaultTime) {
        try {
            return LocalDateTime.parse(userInput);
        } catch (final DateTimeParseException eLDT) {
            try {
                return LocalDate.parse(userInput).atTime(defaultTime);
            } catch (final DateTimeParseException e) {
                throw FoodSafetyException.userInputError(userInput, e);
            }
        }
    }
}
