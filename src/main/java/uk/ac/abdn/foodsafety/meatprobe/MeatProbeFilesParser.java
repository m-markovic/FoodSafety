package uk.ac.abdn.foodsafety.meatprobe;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import uk.ac.abdn.foodsafety.common.FoodSafetyException;

public final class MeatProbeFilesParser {
    /** Charset used for decoding all meat probe files */
    private static final Charset UTF8 = Charset.forName("UTF-8");
    
    /** The directory containing the meat probe files */
    private Path dir_path;
    
    /**
     * Converts the directory into a java.nio.file.Path and stores it
     * @param directory Path to the directory containing the meat probe files,
     * e.g. "mydata/meatprobefiles/"
     */
    public MeatProbeFilesParser(final String directory) {
        this.dir_path = Paths.get(directory); 
    }
    
    /**
     * Parses all files in the directory containing the meat probe files
     * @return A Stream containing every meat probe reading
     */
    public Stream<String[]> parse() {
        try {
            return 
                    //For each file in the given directory
                    Files.walk(this.dir_path)
                    //Read all lines of the file
                    .map(MeatProbeFilesParser::readAllLines)
                    //Concatenate the lines of all the files
                    .reduce(Stream.empty(), Stream::concat)
                    //Remove the header lines
                    .filter(line -> !line.contains("MeatProbe"))
                    //Split remaining lines by the commas
                    .map(line -> line.split(","))
                    //Skip any line that did not have exactly 2 commas (i.e. 3 parts)
                    .filter(parts -> parts.length == 3);
        } catch (final IOException e) {
            throw FoodSafetyException.meatProbeIOfailed(e);
        }
    }
    
    /**
     * Reads and decodes all lines of the file on the given path
     * @param path Path to a meat probe file
     * @return a Stream with one element per line in the file
     */
    private static Stream<String> readAllLines(final Path path) {
        try {
            return Files.readAllLines(path, UTF8).stream();
        } catch (final IOException e) {
            throw FoodSafetyException.meatProbeIOfailed(e);
        }
    }
}
