package uk.ac.abdn.foodsafety;

import java.time.LocalDate;

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
    }
}
