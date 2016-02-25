package uk.ac.abdn.foodsafety.provenance;

import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import uk.ac.abdn.foodsafety.common.Logging;
import uk.ac.abdn.foodsafety.sensordata.WindowReading;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

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
    
    /**
     * 
     * @author nhc
     *
     * A SingleWindowReasoner reasons about provenance for readings
     * in a single time window.
     * TODO: This is only a stub.
     */
    private static final class SingleWindowReasoner
        implements Consumer<WindowReading> {
        private final OntModel model = ModelFactory.createOntologyModel();
        
        @SuppressWarnings("unused")
        public void accept(final WindowReading reading) {
            Individual newObservation = this.model.createIndividual(
                    "http://FoodSafety/observation/mp/" + UUID.randomUUID(),
                    this.model.createClass(Prefix.SSN + "Observation"));
            Individual newSensorOutput = this.model.createIndividual(
                    "http://FoodSafety/sensorOutput/mp/" + UUID.randomUUID(),
                    this.model.createClass(Prefix.SSN + "SensorOutput"));
            Individual newObservationValue = this.model.createIndividual(
                    "http://FoodSafety/observationValue/mp/" + UUID.randomUUID(),
                    this.model.createClass(Prefix.SK + "QuantityObservationValue"));
            // set time of observation
            newObservation.setPropertyValue(this.model.createProperty(
                    Prefix.SSN + "observationSamplingTime"),
                    this.model.createTypedLiteral(GregorianCalendar.from(reading.time)));
            // link  observation value to sensor reading 
            newObservationValue.setPropertyValue(this.model.createProperty(
                    Prefix.SSN + "hasQuantityValue"),
                    this.model.createTypedLiteral(reading.temperature));
        }

        public void log() {
            Logging.info(String.format("Window model had %d triples", model.size()));
        }
    }

    @SuppressWarnings("unused")
    private static class Prefix {
        public static String SSN = "http://purl.oclc.org/NET/ssnx/ssn#";
        public static String PROV = "http://www.w3.org/ns/prov#";
        public static String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
        public static String OWL = "http://www.w3.org/2002/07/owl#";
        public static String XSD = "http://www.w3.org/2001/XMLSchema#";
        public static String METEO = "https://www.w3.org/2005/Incubator/ssn/ssnx/meteo/aws#";
        public static String SK = "http://purl.oclc.org/NET/ssnx/product/smart-knife#";
        public static String FS = "http://example.org/food-safety#";
    }
}
