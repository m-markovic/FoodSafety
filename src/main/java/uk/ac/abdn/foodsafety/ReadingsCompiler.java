package uk.ac.abdn.foodsafety;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import uk.ac.abdn.foodsafety.common.Constants;
import uk.ac.abdn.foodsafety.common.FoodSafetyException;
import uk.ac.abdn.foodsafety.meatprobe.MeatProbeFilesParser;
import uk.ac.abdn.foodsafety.sensordata.MeatProbeReading;
import uk.ac.abdn.foodsafety.sensordata.TemperatureHumidityReading;
import uk.ac.abdn.foodsafety.sensordata.TimedTemperatureReading;
import uk.ac.abdn.foodsafety.wirelesstag.WirelessTagClient;

/**
 * 
 * @author nhc
 *
 * A ReadingsCompiler picks slices of sensor data and passes
 * these on to a reasoner.
 */
final class ReadingsCompiler {
    /** Provide sliced sensor data to this object */
    private final Consumer<TimedTemperatureReading> consumer;
    
    /** Slice sensor data by time of reading: Must be after this time. */
    private final ZonedDateTime fromDateTime;

    /** Slice sensor data by time of reading: Must be before this time. */
    private final ZonedDateTime toDateTime;

    /**
     * Parses from and to as LocalDate/LocalDateTime in the ISO format
     * and registers both of these along with the dataConsumer.
     * @param from Slice sensor data by only providing readings after this time.
     * LocalDate/LocalDateTime in the ISO format, e.g. "2016-01-31T15:36:59"
     * @param to Slice sensor data by only providing readings before this time.
     * LocalDate/LocalDateTime in the ISO format, e.g. "2016-01-31T15:36:59"
     * @param annotator Pass every  
     * @param dataConsumer The object to provide sliced data to
     */
    ReadingsCompiler(
            final String from, 
            final String to, 
            final Consumer<TimedTemperatureReading> consumer) {
        this.consumer = consumer;
        this.fromDateTime = ReadingsCompiler.parse(from, LocalTime.MIN);
        this.toDateTime = ReadingsCompiler.parse(to, LocalTime.MAX);
    }

    private <T extends TimedTemperatureReading> void filterAndConsume(final Stream<T> readings) {
        readings
        //Filter by time of day
        .filter((reading) -> reading.time.isAfter(this.fromDateTime))
        .filter((reading) -> reading.time.isBefore(this.toDateTime))
        //Pass on to the consumer
        .forEach(this.consumer);
    }
    
    /**
     * Gets data from a specific wireless tag, slices that data
     * and provides the result to the registered consumer.
     * @param client Facade to the wireless tag API
     * @param sensorId The ID of the sensor to get data for, e.g. 3
     */
    void add(final WirelessTagClient client, final int sensorId) {
        //Get data for the dates (the API cannot slice on time of day)
        final Stream<TemperatureHumidityReading> readings = client.getStatsRaw(
            sensorId,
            this.fromDateTime.toLocalDate(), 
            this.toDateTime.toLocalDate());
        this.filterAndConsume(readings);
    }
    
    /**
     * Gets data from a directory of meat probe files, slices that data
     * and provides the result to the registered consumer.
     * @param parser parser for the meat probe files
     */
    void add(final MeatProbeFilesParser parser) {
        //Get data
        final Stream<MeatProbeReading> readings = parser.parse();
        this.filterAndConsume(readings);
    }

    /**
     * Parses the given userInput as a LocalDateTime or LocalDate.
     * @param userInput An ISO date or datetime, e.g. "2016-01-31T15:36:59"
     * @param defaultTime time to use if userInput was a date and not a datetime.
     * @return The parsed LocalDateTime
     */
    private static ZonedDateTime parse(final String userInput, final LocalTime defaultTime) {
        try {
            return LocalDateTime.parse(userInput).atZone(Constants.UK);
        } catch (final DateTimeParseException eLDT) {
            try {
                return LocalDate.parse(userInput).atTime(defaultTime).atZone(Constants.UK);
            } catch (final DateTimeParseException e) {
                throw FoodSafetyException.userInputError(userInput, e);
            }
        }
    }
}
