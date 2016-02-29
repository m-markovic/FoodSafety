package uk.ac.abdn.foodsafety.meatprobe;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import uk.ac.abdn.foodsafety.common.Constants;
import uk.ac.abdn.foodsafety.common.FoodSafetyException;
import uk.ac.abdn.foodsafety.sensordata.TimedTemperatureReading;

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
    implements Consumer<TimedTemperatureReading> {
    private Map<ZonedDateTime, String> time2foi = 
            new HashMap<ZonedDateTime, String>();
    
    /**
     * Reads and parses the input JSON file
     * @param pathAsString path to the input JSON file
     */
    public FoiAnnotator(final String pathAsString) {
        try {
            new Gson().fromJson(
                Files.newBufferedReader(
                    Paths.get(pathAsString),
                    Charset.forName("UTF-8")), 
                Annotations.class)
            .entrySet()
            .stream()
            .forEach(entry -> time2foi.put(
                    LocalDateTime.parse(entry.getKey(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        .atZone(Constants.UK), 
                    entry.getValue()));
            ;
        } catch (JsonSyntaxException e) {
            throw FoodSafetyException.userInputError(String.format("File '%s'", pathAsString), e);
        } catch (JsonIOException e) {
            throw FoodSafetyException.userInputError(String.format("File '%s'", pathAsString), e);
        } catch (IOException e) {
            throw FoodSafetyException.annotationIOfailed(e);
        }
    }
    
    /**
     * If the reading matches a time stamp in the input JSON annotations,
     * the reading's FOI is updated to the given value; otherwise
     * it is left unchanged.
     */
    @Override
    public void accept(final TimedTemperatureReading r) {
        r.foi = this.time2foi.getOrDefault(r.time, r.foi);
    }
    
    @SuppressWarnings("serial")
    private static class Annotations extends HashMap<String,String> {
        // This is merely an alias to pass to GSON
    }
}
