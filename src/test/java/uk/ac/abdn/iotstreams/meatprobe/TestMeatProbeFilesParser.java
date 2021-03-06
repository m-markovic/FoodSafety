package uk.ac.abdn.iotstreams.meatprobe;

import static org.junit.Assert.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

import uk.ac.abdn.iotstreams.simulator.meatprobe.MeatProbeFilesParser;
import uk.ac.abdn.iotstreams.simulator.sensordata.MeatProbeReading;
import uk.ac.abdn.iotstreams.util.Constants;

public class TestMeatProbeFilesParser {

    @Test
    public void test() {
        final Map<Integer, MeatProbeReading> parsed = new TestParser()
            .parse()
            .collect(Collectors.toMap(
                    reading -> reading.id, 
                    Function.identity()));
        //Expect 5 readings
        assertEquals(5, parsed.size());
        //Check temperatures
        assertEquals(11.1, parsed.get(1).temperature, 0.01);
        assertEquals(22.2, parsed.get(2).temperature, 0.01);
        assertEquals(33, parsed.get(3).temperature, 0.01);
        assertEquals(999, parsed.get(30855).temperature, 0.01);
        assertEquals(999, parsed.get(30857).temperature, 0.01);
        //Check timestamps
        final LocalDate dec14 = LocalDate.of(2015, 12, 14);
        final LocalDate dec15 = LocalDate.of(2015, 12, 15);
        assertEquals(
                dec14.atTime(12, 34, 01).atZone(Constants.UK), 
                parsed.get(1).time);
        assertEquals(
                dec14.atTime(12, 34, 11).atZone(Constants.UK), 
                parsed.get(2).time);
        assertEquals(
                dec14.atTime(12, 34, 21).atZone(Constants.UK), 
                parsed.get(3).time);
        assertEquals(
                dec15.atTime(2, 16, 14).atZone(Constants.UK), 
                parsed.get(30855).time);
        assertEquals(
                dec15.atTime(2, 16, 34).atZone(Constants.UK), 
                parsed.get(30857).time);
    }
    
    /**
     * 
     * @author nhc
     * 
     * MeatProbeFilesParser reading its input from src/test/resources
     */
    private static final class TestParser extends MeatProbeFilesParser {
        protected Path getPath() {
            return Paths.get(TestMeatProbeFilesParser.class.getResource("/meatprobefiles").getFile());
        }
    }
}
