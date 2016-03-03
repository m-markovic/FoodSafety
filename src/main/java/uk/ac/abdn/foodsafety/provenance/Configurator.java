package uk.ac.abdn.foodsafety.provenance;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;

import uk.ac.abdn.foodsafety.common.FoodSafetyException;

/**
 * 
 * @author nhc
 *
 * A Configurator loads configuration from a directory
 * and configures a given FoodSafetyEngine accordingly.
 */
final class Configurator {
    /** Charset used for decoding all meat probe files */
    private static final Charset ISO88591 = Charset.forName("ISO-8859-1");

    //private static final String QUERY_FILE = "csparql-query.rq";
    
    /** Root of all configuration files */
    private static final Path CONFIG_ROOT = Paths.get("config/foodsafety/");
    
    /** The engine to configure */
    private final FoodSafetyEngine engine;

    /**
     * Registers the engine to configure
     * @param foodSafetyEngine The engine to configure
     * @throws IOException 
     */
    public Configurator(final FoodSafetyEngine foodSafetyEngine) {
        this.engine = foodSafetyEngine;
        try {
            Files.walk(CONFIG_ROOT)
                .filter(Files::isRegularFile)
                .forEach(this::addFile);
        } catch (final IOException e) {
            throw FoodSafetyException.configurationError(e);
        }
    }
    
    private void addFile(final Path file) {
        try {
            this.engine.registerQuery(
                    String.join("\n", Files.readAllLines(file, ISO88591)), 
                    false)
                .addObserver(new FoodSafetyEngine.TmpFormatter());
        } catch (final IOException e) {
            throw FoodSafetyException.configurationError(e);
        } catch (final ParseException e) {
            throw FoodSafetyException.configurationError(e);
        }
    }
}
