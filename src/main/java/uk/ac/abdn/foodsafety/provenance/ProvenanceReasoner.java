package uk.ac.abdn.foodsafety.provenance;

import java.util.function.Consumer;
import java.util.stream.Stream;

import uk.ac.abdn.foodsafety.sensordata.WindowReading;

/**
 * 
 * @author nhc
 *
 * A ProvenanceReasoner accepts readings, bundled by time windows,
 * then reasons about the provenance.
 * TODO: This is only a stub.
 */
public final class ProvenanceReasoner
    implements Consumer<Stream<WindowReading>> {
    public void accept(final Stream<WindowReading> readings) {
        SingleWindowReasoner swr = new SingleWindowReasoner();
        // TODO: Make this a reduce() operation
        readings.forEach(swr);
        swr.log();
    }
}
