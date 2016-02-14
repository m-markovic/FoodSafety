package uk.ac.abdn.foodsafety;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

import uk.ac.abdn.foodsafety.common.Constants;
import uk.ac.abdn.foodsafety.common.FoodSafetyException;
import uk.ac.abdn.foodsafety.meatprobe.MeatProbeFilesParser;
import uk.ac.abdn.foodsafety.sensordata.MeatProbeReading;
import uk.ac.abdn.foodsafety.sensordata.TemperatureHumidityReading;
import uk.ac.abdn.foodsafety.wirelesstag.WirelessTagClient;

/**
 * 
 * @author nhc
 *
 * A DataSlicer picks slices of sensor data and passes
 * these on to a reasoner.
 */
final class DataSlicer {
    /** Provide sliced sensor data to these objects */
    private final Consumer<TemperatureHumidityReading> thConsumer;
    private final Consumer<MeatProbeReading> mpConsumer;
    
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
     * @param dataConsumer The object to provide sliced data to
     */
    DataSlicer(
            final String from, 
            final String to, 
            final Consumer<TemperatureHumidityReading> thConsumer,
            final Consumer<MeatProbeReading> mpConsumer) {
        this.thConsumer = thConsumer;
        this.mpConsumer = mpConsumer;
        this.fromDateTime = DataSlicer.parse(from, LocalTime.MIN);
        this.toDateTime = DataSlicer.parse(to, LocalTime.MAX);
    }

    /**
     * Gets data from a specific wireless tag, slices that data
     * and provides the result to the registered consumer.
     * @param client Facade to the wireless tag API
     * @param sensorId The ID of the sensor to get data for, e.g. 3
     */
    void add(final WirelessTagClient client, final int sensorId) {
        //Get data for the dates (the API cannot slice on time of day
        client.getStatsRaw(
                sensorId, 
                this.fromDateTime.toLocalDate(), 
                this.toDateTime.toLocalDate())
        //Filter by time of day
          .filter((reading) -> reading.time.isAfter(this.fromDateTime))
          .filter((reading) -> reading.time.isBefore(this.toDateTime))
        //Pass on to the consumer
          .forEach(this.thConsumer);
    }
    
    /**
     * Gets data from a directory of meat probe files, slices that data
     * and provides the result to the registered consumer.
     * @param parser parser for the meat probe files
     */
    void add(final MeatProbeFilesParser parser) {
        //Get data
        parser.parse()
        //Filter by time of day
          .filter((reading) -> reading.time.isAfter(this.fromDateTime))
          .filter((reading) -> reading.time.isBefore(this.toDateTime))
        //Pass on to the consumer
          .forEach(this.mpConsumer);
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
