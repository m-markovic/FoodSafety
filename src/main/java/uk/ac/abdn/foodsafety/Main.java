package uk.ac.abdn.foodsafety;

import java.time.LocalDate;

import uk.ac.abdn.foodsafety.csparql.FoodSafetyEngine;

/**
 * 
 * @author nhc
 *
 * Command-line interface for the FoodSafety project.
 */
public class Main {
    /**
     * Proof-of-concept - pushes some data for sensor 3 to a Csparql engine.
     * @param args Not used.
     */
    public static void main(final String[] args) {
        final WirelessTagClient client = new WirelessTagClient();
        client.getStatsRaw(
                3,
                LocalDate.of(2016, 1, 28), 
                LocalDate.of(2016, 1, 29),
                new FoodSafetyEngine());
    }
}
