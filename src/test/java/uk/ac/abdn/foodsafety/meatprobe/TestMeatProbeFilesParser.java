package uk.ac.abdn.foodsafety.meatprobe;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;

public class TestMeatProbeFilesParser {

    @Test
    public void test() {
        final String file = TestMeatProbeFilesParser.class.getResource("/meatprobefiles").getFile();
        final Map<String, String[]> parsed = new MeatProbeFilesParser(file)
            .parse()
            .collect(Collectors.toMap(
                    triple -> triple[0], 
                    Function.identity()));
        assertEquals(5, parsed.size());
        assertEquals("11.1", parsed.get("1")[2]);
        assertEquals("22.2", parsed.get("2")[2]);
        assertEquals("33", parsed.get("3")[2]);
        assertEquals("999", parsed.get("30855")[2]);
        assertEquals("999", parsed.get("30857")[2]);
    }

}
