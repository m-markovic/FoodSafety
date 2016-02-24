package uk.ac.abdn.foodsafety;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;

import com.google.gson.Gson;

import uk.ac.abdn.foodsafety.csparql.FoodSafetyEngine;
import uk.ac.abdn.foodsafety.meatprobe.MeatProbeFilesParser;
import uk.ac.abdn.foodsafety.wirelesstag.WirelessTagClient;

/**
 * 
 * @author nhc
 *
 * Command-line interface for the FoodSafety project.
 */
public class Main {
    /**
     * Creates a Csparql engine and provides it with downloaded readings
     * from wireless tags.
     * The readings will contain temperature and humidity from fromDate to toDate
     * for the given sensors.
     * 
     * This application requires its input to be provided on stdin. Example input:
     * {"from": "2016-01-29T15:30:00", "to": "2016-01-29T15:40:00", "meatProbeDir": "./mydata/", "wirelessTags": [2,3]}
     * @param args not used
     */
    public static void main(final String[] args) {
        //Parse input
        final Input input = Input.parseFromStdIn();
        //Create a reasoner and wrap it in an object for slicing data according to time
        final FoodSafetyEngine engine = new FoodSafetyEngine();
        final DataSlicer dataSlicer = new DataSlicer(
                input.from, input.to, 
                engine);
        //Get meat probe data
        dataSlicer.add(new MeatProbeFilesParser(input.meatProbeDir));
        //Get wireless tag data
        if (input.wirelessTags.size() > 0) {
            final WirelessTagClient client = new WirelessTagClient();
            for (Integer id : input.wirelessTags) {
                dataSlicer.add(client, id);
            }            
        }
        engine.done();
    }
    
    /**
     * 
     * @author nhc
     *
     * Defines the input to the command-line application.
     * This class is instantiated by the GSON library, parsed from JSON input.
     */
    private static final class Input {
        private String from;
        private String to;
        private String meatProbeDir;
        private List<Integer> wirelessTags;
        
        /**
         * Read and parse JSON from stdin
         * @return The parsed Input
         */
        static Input parseFromStdIn() {
            final BufferedReader responseReader = 
                    new BufferedReader(
                            new InputStreamReader(
                                    System.in,
                                    Charset.forName("UTF-8")));
            return new Gson().fromJson(responseReader, Input.class);
        }
    }
}
