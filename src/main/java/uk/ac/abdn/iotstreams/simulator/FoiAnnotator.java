package uk.ac.abdn.iotstreams.simulator;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import uk.ac.abdn.iotstreams.simulator.sensordata.TimedTemperatureReading;
import uk.ac.abdn.iotstreams.util.Constants;
import uk.ac.abdn.iotstreams.util.IotStreamsException;

/**
 * 
 * @author nhc
 *
 * A FoiAnnotator reads a JSON file mapping datetimes to feature-of-interest-URIs.
 * If a reading matches the time stamp, its FOI is updated to the given value; otherwise
 * it is returned unchanged.
 * Example input file content:
 * {"2016-12-240T12:34:56+00:00": "http://example.org/meatItem345"}
 */
public final class FoiAnnotator
    implements UnaryOperator<TimedTemperatureReading> {
    /** Path to the input JSON file */
    private static final Path INPUT_PATH = Paths.get("config/simulator/annotations.json.txt");
    
    /** Internal cache of the annotations */
    private Map<ZonedDateTime, String> time2foi = 
            new HashMap<ZonedDateTime, String>();
    
    /**
     * Reads and parses the input JSON file
     */
    public FoiAnnotator() {
        try {
            new Gson().fromJson(
                Files.newBufferedReader(
                    INPUT_PATH,
                    Charset.forName("UTF-8")), 
                Annotations.class)
            .entrySet()
            .stream()
            .forEach(entry -> time2foi.put(
                    LocalDateTime.parse(entry.getKey(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        .atZone(Constants.UK), 
                    entry.getValue()));
        } catch (JsonSyntaxException e) {
            throw IotStreamsException.userInputError(String.format("File '%s'", INPUT_PATH.toString()), e);
        } catch (JsonIOException e) {
            throw IotStreamsException.userInputError(String.format("File '%s'", INPUT_PATH.toString()), e);
        } catch (IOException e) {
            throw IotStreamsException.annotationIOfailed(e);
        }
    }
    
    /**
     * If the reading matches a time stamp in the input JSON annotations,
     * the reading's FOI is updated to the given value; otherwise
     * it is left unchanged.
     */
    @Override
    public TimedTemperatureReading apply(final TimedTemperatureReading r) {
        r.foi = this.time2foi.getOrDefault(r.time, r.foi);
        return r;
    }
    
    @SuppressWarnings("serial")
    private static class Annotations extends HashMap<String,String> {
        // This is merely an alias to pass to GSON
    }
}
