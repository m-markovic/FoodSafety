package uk.ac.abdn.iotstreams.simulator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.hp.hpl.jena.rdf.model.Model;

import uk.ac.abdn.iotstreams.common.Constants;
import uk.ac.abdn.iotstreams.common.IotStreamsException;
import uk.ac.abdn.iotstreams.common.Logging;
import uk.ac.abdn.iotstreams.simulator.meatprobe.MeatProbeFilesParser;
import uk.ac.abdn.iotstreams.simulator.sensordata.MeatProbeReading;
import uk.ac.abdn.iotstreams.simulator.sensordata.TimedTemperatureReading;
import uk.ac.abdn.iotstreams.simulator.sensordata.WirelessTagReading;
import uk.ac.abdn.iotstreams.simulator.wirelesstag.WirelessTagClient;

/**
 * 
 * @author nhc
 *
 * A ReadingsCompiler picks slices of sensor data and passes
 * these on to a reasoner.
 */
public final class Simulator {
    /** Provide sliced sensor data to this object */
    private final Consumer<TimedTemperatureReading> consumer;
    
    /** Slice sensor data by time of reading: Must be after this time. */
    private final ZonedDateTime fromDateTime;

    /** Slice sensor data by time of reading: Must be before this time. */
    private final ZonedDateTime toDateTime;

    /** Cache for sorting all readings by timestamp */
    private final Stream.Builder<TimedTemperatureReading> readings = Stream.builder();

    /** Number of readings cached so far */
    private long numReadings = 0;
    
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
    public Simulator(
            final String from, 
            final String to, 
            final Function<ZonedDateTime, Consumer<Model>> engine) {
        this.consumer = new SSNModeller(engine);
        this.fromDateTime = Simulator.parse(from, LocalTime.MIN);
        this.toDateTime = Simulator.parse(to, LocalTime.MAX);
    }

    private <T extends TimedTemperatureReading> void filterAndCache(final Stream<T> readings) {
        readings
        //Filter by time of day
        .filter((reading) -> reading.time.isAfter(this.fromDateTime))
        .filter((reading) -> reading.time.isBefore(this.toDateTime))
        //Pass on to the cache
        .forEach(this.readings.andThen(r -> this.numReadings += 1));
    }
    
    /**
     * Gets data from a specific wireless tag, slices that data
     * and provides the result to the registered consumer.
     * @param client Facade to the wireless tag API
     * @param sensorId The ID of the sensor to get data for, e.g. 3
     * @param foiAnnotator object to replace raw fields with manual annotations
     */
    public void add(
            final WirelessTagClient client, 
            final int sensorId, 
            final UnaryOperator<TimedTemperatureReading> foiAnnotator) {
        //Get data for the dates (the API cannot slice on time of day)
        final Stream<WirelessTagReading> readings = client.getStatsRaw(
            sensorId,
            this.fromDateTime.toLocalDate(), 
            this.toDateTime.toLocalDate());
        this.filterAndCache(readings.map(foiAnnotator));
    }
    
    /**
     * Gets data from a directory of meat probe files, slices that data
     * and provides the result to the registered consumer.
     * @param parser parser for the meat probe files
     * @param foiAnnotator object to replace raw fields with manual annotations
     */
    public void add(
            final MeatProbeFilesParser parser, 
            final UnaryOperator<TimedTemperatureReading> foiAnnotator) {
        //Get data
        final Stream<MeatProbeReading> readings = parser.parse();
        this.filterAndCache(readings.map(foiAnnotator));
    }

    /**
     * Call this once all readings have been added.
     */
    public void done() {
        Logging.info(String.format("%d readings in Simulator", this.numReadings));
        this.readings.build()
            .sorted(Comparator.comparing(r -> r.time))
            .forEachOrdered(this.consumer);
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
                throw IotStreamsException.userInputError(userInput, e);
            }
        }
    }
}
