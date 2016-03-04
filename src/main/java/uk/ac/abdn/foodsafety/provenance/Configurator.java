package uk.ac.abdn.foodsafety.provenance;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

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

    private static final String QUERY_FILE = "csparql-query.rq";
    private static final String OWL_FILE = "init.owl";
    
    /** Root of all configuration files */
    private static final Path CONFIG_ROOT = Paths.get("config/foodsafety/");
    
    /** The engine to configure */
    private final FoodSafetyEngine engine;

    /** Observer for each query */
    private Map<String, FoodSafetyFormatter> observers = new HashMap<>();
    
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
        final String content = read(file);
        final Path rel = CONFIG_ROOT.relativize(file);
        if ((rel.getNameCount() == 2) && (rel.getFileName().toString().equals(QUERY_FILE))) {
            //A C-SPARQL query
            this.registerQuery(rel.getName(0).toString(), content);
        } if ((rel.getNameCount() == 2) && (rel.getFileName().toString().equals(OWL_FILE))) {
            //An OWL file for initializing our Jena model
            this.observers.get(rel.getName(0).toString()).setOwl(content);
        } else if ((rel.getNameCount() == 3) && (rel.getFileName().toString().endsWith(".rq"))) {
            //A SPARQL update to be executed on the output of a query
            this.observers.get(rel.getName(1).toString()).addSparql(rel.getName(2).toString(), content);
        } else {
            throw FoodSafetyException.configurationError(String.format("Unexpected file %s", file.toString()));
        }
    }
    
    private void registerQuery(final String name, final String content) {
        try {
            this.observers.put(name, new FoodSafetyFormatter(name));
            this.engine.registerQuery(
                    content, 
                    false)
                .addObserver(this.observers.get(name));
        } catch (final ParseException e) {
            throw FoodSafetyException.configurationError(e);
        }
    }
    
    private static String read(final Path file) {
        try {
            return String.join("\n", Files.readAllLines(file, ISO88591));
        } catch (final IOException e) {
            throw FoodSafetyException.configurationError(e);
        }
    }
}
