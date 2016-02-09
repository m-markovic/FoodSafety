package uk.ac.abdn.foodsafety;

import java.time.LocalDate;

import eu.larkc.csparql.core.engine.CsparqlEngineImpl;

/**
 * 
 * @author nhc
 *
 * Command-line interface for the FoodSafety project.
 */
public class Main {
    /**
     * Proof-of-concept - downloads and prints
     * an event log for sensor 3.
     * @param args Not used.
     */
    public static void main(final String[] args) {
        final WirelessTagClient client = new WirelessTagClient();
        client.getEventRawData(3);
        client.getStatsRaw(
                3,
                LocalDate.of(2016, 1, 28), 
                LocalDate.of(2016, 1, 29));
        //Create csparql engine instance
        CsparqlEngineImpl engine = new CsparqlEngineImpl();
        //Initialize the engine instance
        //The initialization creates the static engine (SPARQL) and the stream engine (CEP)
        engine.initialize();

    }
}
