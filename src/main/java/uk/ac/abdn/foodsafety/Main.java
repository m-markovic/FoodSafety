package uk.ac.abdn.foodsafety;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import uk.ac.abdn.foodsafety.common.FoodSafetyException;
import uk.ac.abdn.foodsafety.provenance.FoodSafetyEngine;
import uk.ac.abdn.foodsafety.simulator.FoiAnnotator;
import uk.ac.abdn.foodsafety.simulator.Simulator;
import uk.ac.abdn.foodsafety.simulator.meatprobe.MeatProbeFilesParser;
import uk.ac.abdn.foodsafety.simulator.wirelesstag.WirelessTagClient;

import com.google.gson.Gson;

/**
 * 
 * @author nhc
 *
 * Command-line interface for the FoodSafety project.
 */
public final class Main {
    /**
     * Creates a C-SPARQL engine for FoodSafety and provides it with simulated live data.
     * The Simulator will read meat probe files from config/meatprobe/
     * and will download wireless tag data from the internet.
     * The readings will contain temperature from fromDate to toDate
     * 
     * This application requires its input to be provided in a file at Input.INPUT_PATH
     * Example input:
     * {"from": "2016-01-29T15:30:00", "to": "2016-01-29T15:40:00", "wirelessTagId": 2}
     * @param args not used
     */
    public static void main(final String[] args) {
        //Parse input file
        final Input input = Input.readAndparse();
        try {
            run(input);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Analyzes the readings of each tag in the input paired with the
     * meat probe readings.
     * @param input Parsed from file system
     */
    private static void run(final Input input) {
        //Connect to wireless tag site
        final WirelessTagClient client = new WirelessTagClient();
        final FoodSafetyEngine engine = new FoodSafetyEngine();
        final Simulator simulator = new Simulator(input.from, input.to, engine);
        //Get meat probe data
        simulator
            .add(new MeatProbeFilesParser(),
                 new FoiAnnotator());
        //Get wireless tag data
        simulator
            .add(client, input.wirelessTagId);
        simulator.done();
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
        private static final Path INPUT_PATH = Paths.get("config/simulator/input.json.txt");
        
        private String from;
        private String to;
        private int wirelessTagId;
        
        /**
         * Read and parse JSON from INPUT_PATH
         * @return The parsed Input
         */
        static Input readAndparse() {
            final BufferedReader inputReader;
            try {
                inputReader = Files.newBufferedReader(
                        INPUT_PATH, 
                        StandardCharsets.UTF_8);
                return new Gson().fromJson(inputReader, Input.class);
            } catch (final IOException e) {
                throw FoodSafetyException.userInputError(INPUT_PATH.toString(), e);
            }
        }
    }
}
