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
     * Creates a Csparql engine and provides it with downloaded readings
     * from wireless tags.
     * The readings will contain temperature and humidity from fromDate to toDate
     * for the given sensors
     * @param args fromDate toDate sensorId sensorId ...
     */
    public static void main(final String[] args) {
        assert args.length > 2 : "Parameter example: 2016-01-28 2016-01-29 2 3 4\nParameters are fromDate toDate sensorId sensorId ...";
        final LocalDate fromDate = LocalDate.parse(args[0]);
        final LocalDate toDate = LocalDate.parse(args[1]);
        final WirelessTagClient client = new WirelessTagClient();
        final FoodSafetyEngine engine = new FoodSafetyEngine();
        for (int i = 2; i < args.length; i++) {
            client.getStatsRaw(Integer.parseInt(args[i]), fromDate, toDate, engine);
        }
    }
}
