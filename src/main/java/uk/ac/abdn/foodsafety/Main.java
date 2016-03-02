package uk.ac.abdn.foodsafety;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import com.google.gson.Gson;

import uk.ac.abdn.foodsafety.provenance.FoodSafetyEngine;
import uk.ac.abdn.foodsafety.simulator.FoiAnnotator;
import uk.ac.abdn.foodsafety.simulator.Simulator;
import uk.ac.abdn.foodsafety.simulator.meatprobe.MeatProbeFilesParser;
import uk.ac.abdn.foodsafety.simulator.wirelesstag.WirelessTagClient;

/**
 * 
 * @author nhc
 *
 * Command-line interface for the FoodSafety project.
 */
public final class Main {
    /**
     * Creates a Csparql engine and provides it with downloaded readings
     * from wireless tags.
     * The readings will contain temperature and humidity from fromDate to toDate
     * for the given sensors.
     * 
     * This application requires its input to be provided on stdin. Example input:
     * {"from": "2016-01-29T15:30:00", "to": "2016-01-29T15:40:00", "meatProbeDir": "mydata/", "wirelessTags": [2,3]}
     * @param args not used
     */
    public static void main(final String[] args) {
        //Parse command-line input
        final Input input = Input.parseFromStdIn();
        try {
            run(input);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Analyzes the readings of each tag in the input paired with the
     * meat probe readings.
     * @param input Parsed input from System.in
     */
    private static void run(final Input input) {
        //Connect to wireless tag site
        final WirelessTagClient client = new WirelessTagClient();
        final FoodSafetyEngine engine = new FoodSafetyEngine();
        final Simulator simulator = new Simulator(input.from, input.to, engine);
        //Get meat probe data
        simulator
            .add(new MeatProbeFilesParser(input.meatProbeDir),
                 new FoiAnnotator(input.annotationsFile));
        //Get wireless tag data
        simulator
            .add(client, input.wirelessTagId);
        simulator.done();
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
        private String annotationsFile;
        private int wirelessTagId;
        
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
