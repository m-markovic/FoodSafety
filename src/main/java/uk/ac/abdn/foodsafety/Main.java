package uk.ac.abdn.foodsafety;

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
        new WirelessTagClient().getEventRawData(3);
    }
}
