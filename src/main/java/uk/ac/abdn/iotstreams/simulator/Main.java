package uk.ac.abdn.iotstreams.simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import uk.ac.abdn.iotstreams.csparql.IotStreamsEngine;
import uk.ac.abdn.iotstreams.simulator.meatprobe.MeatProbeFilesParser;
import uk.ac.abdn.iotstreams.simulator.sensordata.TimedTemperatureReading;
import uk.ac.abdn.iotstreams.simulator.wirelesstag.WirelessTagClient;
import uk.ac.abdn.iotstreams.util.IotStreamsException;

import com.google.gson.Gson;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * 
 * @author nhc
 *
 * Command-line interface for the FoodSafety project.
 */
public final class Main {
    /**
     * Creates a C-SPARQL engine for FoodSafety and provides it with simulated live data.
     * The Simulator will read meat probe files
     * and download wireless tag data from the internet.
     * The readings will contain temperature from fromDate to toDate
     * 
     * This application requires its input to be provided in a file at Input.INPUT_PATH
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
        //Jena Model collecting all inferences
        final Model persistentModel = ModelFactory.createDefaultModel();
        //Connect to wireless tag site
        final WirelessTagClient client = new WirelessTagClient();
        client.logTagList();
        final IotStreamsEngine engine = IotStreamsEngine.forRecordedData(persistentModel::add);
        final Simulator simulator = new Simulator(input.from, input.to, engine);
        //Get meat probe data
        simulator
            .add(new MeatProbeFilesParser(),
                 new FoiAnnotator());
        //Get wireless tag data
        input.foi2wirelessTagID.forEach(
                (foi, id) -> simulator.add(
                                client, 
                                id,
                                r -> setFoi(foi, r)));
        //Run the queries and inferences
        simulator.done();
        //Log stats from engine
        engine.log();
        //Output all inferred data on System.out
        persistentModel.write(System.out, "N3");
    }
    
    /**
     * Sets r.foi to foi
     * @param foi Any feature of interest
     * @param r Any reading
     * @return r after update
     */
    private static TimedTemperatureReading setFoi(
            final String foi, 
            final TimedTemperatureReading r) {
        r.foi = foi;
        return r;
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
        private HashMap<String, Integer> foi2wirelessTagID;
        
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
                throw IotStreamsException.userInputError(INPUT_PATH.toString(), e);
            }
        }
    }
}
