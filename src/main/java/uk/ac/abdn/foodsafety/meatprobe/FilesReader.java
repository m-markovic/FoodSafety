package uk.ac.abdn.foodsafety.meatprobe;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public final class FilesReader {
    public FilesReader(final String dir_path) throws IOException {
        Files.walk(Paths.get(dir_path))
            .map(FilesReader::readAllLinesAsUTF8)
            .reduce(Stream.empty(), Stream::concat)
            .filter(line -> !line.contains("MeatProbe"))
            .map(line -> line.split(","))
            .filter(parts -> parts.length == 3);
    }
    
    private static Stream<String> readAllLinesAsUTF8(final Path path) {
        try {
            return Files.readAllLines(path, Charset.forName("UTF-8")).stream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
    }
}
