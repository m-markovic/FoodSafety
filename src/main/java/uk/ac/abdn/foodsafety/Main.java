package uk.ac.abdn.foodsafety;

import uk.ac.abdn.foodsafety.csparql.FoodSafetyEngine;
import uk.ac.abdn.foodsafety.wirelesstag.WirelessTagClient;

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
        assert args.length > 2 : "Parameter example: 2016-01-28 2016-01-29T15:34:59 1 2 3 4\nParameters are from to sensorId sensorId ...";
        final DataSlicer dataSlicer = new DataSlicer(args[0], args[1], new FoodSafetyEngine());
        final WirelessTagClient client = new WirelessTagClient();
        for (int i = 2; i < args.length; i++) {
            dataSlicer.add(client, Integer.parseInt(args[i]));
        }
    }
}
