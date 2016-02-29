package uk.ac.abdn.foodsafety.provenance;

import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.function.Consumer;

import uk.ac.abdn.foodsafety.common.Logging;
import uk.ac.abdn.foodsafety.sensordata.WindowReading;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * 
 * @author nhc
 *
 * A SingleWindowReasoner reasons about provenance for readings
 * in a single time window.
 * TODO: This is only a stub.
 */
final class SingleWindowReasoner
    implements Consumer<WindowReading> {
    private final OntModel model = ModelFactory.createOntologyModel();
    
    @SuppressWarnings("unused")
    public void accept(final WindowReading reading) {
        Individual newObservation = this.model.createIndividual(
                "http://FoodSafety/observation/mp/" + UUID.randomUUID(),
                this.model.createClass(SingleWindowReasoner.Prefix.SSN + "Observation"));
    }
    
    private Individual annotateSingleSensorData(
            final WindowReading reading,
            final Individual sensor, 
            final String sensorType,
            final String observedProperty,
            final Individual oldObservation)
    {
        // ---- individuals 
        Individual newObservation = model.createIndividual("http://FoodSafety/observation/"+sensorType+"/" + UUID.randomUUID(),
                model.createClass(Prefix.SSN + "Observation"));
        Individual newSensorOutput = model.createIndividual("http://FoodSafety/sensorOutput/"+sensorType+"/" + UUID.randomUUID(),
                model.createClass(Prefix.SSN + "SensorOutput"));
        Individual newObservationValue = model.createIndividual("http://FoodSafety/observationValue/"+sensorType+"/" + UUID.randomUUID(),
                model.createClass(Prefix.SK + "QuantityObservationValue"));
        Individual property = model.createIndividual(observedProperty,
                model.createClass(Prefix.SSN + "Property"));
        Individual fetureOfInterest = model.createIndividual(
                reading.foi,
                model.createClass(Prefix.SSN + "FeatureOfInterest"));
        Literal sensorReading = model.createTypedLiteral(reading.temperature);
        // ---- properties
        // set time of observation
        newObservation.setPropertyValue(
                model.createProperty(Prefix.SSN + "observationSamplingTime"),
                this.model.createTypedLiteral(GregorianCalendar.from(reading.time)));
        // link sensor and sensor output
        newSensorOutput.setPropertyValue(model.createProperty(Prefix.SSN + "isProducedBy"),
                sensor);
        // link observation and sensor output
        newObservation.setPropertyValue(model.createProperty(Prefix.SSN + "observationResult"),
                newSensorOutput);
        // link sensor output and quantity observation value 
        newSensorOutput.setPropertyValue(model.createProperty(Prefix.SSN + "hasValue"),
                newObservationValue);
        // link  observation value to sensor reading 
        newObservationValue.setPropertyValue(model.createProperty(Prefix.SK + "hasQuantityValue"),
                sensorReading);
        // link  observation to foi
        newObservation.setPropertyValue(model.createProperty(Prefix.SSN + "featureOfInterest"),
                fetureOfInterest);        
        // link  foi and property
        fetureOfInterest.setPropertyValue(model.createProperty(Prefix.SSN + "hasProperty"),
                property);
        // link  property and sensor
        sensor.setPropertyValue(model.createProperty(Prefix.SSN + "observes"),
                        property);
        if (oldObservation != null) {
        // link  new observation to the previous one 
        newObservation.setPropertyValue(model.createProperty(Prefix.FS + "follows"),
                        oldObservation);
        }
        // ---- properties
       return newObservation;
    }
    
    // create instances of individual sensors for temp, humidity, movement and meat probe (note Sensing Device is subclass of ssn:Sensor)
    public Individual sensorDescribtion (
            final Individual mainSystem, 
            final String sensorType) {
        final Individual sensor = model.createIndividual(
                "http://FoodSafety/sensor/sensingDevice/"+UUID.randomUUID(),
                model.createClass(Prefix.SSN+"SensingDevice"));
        sensor.addRDFType(
                model.getResource(sensorType));
        mainSystem.setPropertyValue(
                model.createProperty(Prefix.SSN+ "hasSubsystem"),
                sensor);
        return sensor; 
    }

    /**
     * @param r Reading to make provenance model for
     * @param sensorID
     * @param oldObservation Latest observation
     * @return New observation (which is already added to this.model)
     */
    public Individual generateMeatProbeAnnotations(
            final WindowReading r, 
            final String sensorID, 
            final Individual oldObservation) {
        final Individual meatprobe = model.createIndividual(
                "http://FoodSafety/sensor/meatProbe/"+sensorID,
                model.createClass(Prefix.SSN+"System"));
        // create instances of individual sensors for temp, humidity and movement (note Sensing Device is subclass of ssn:Sensor)
        final Individual meatprobeSensor = sensorDescribtion(
                meatprobe,
                Prefix.METEO+"TmeperatureSensor");
        return annotateSingleSensorData(
                r, 
                meatprobeSensor, 
                "temperature", 
                Prefix.FS_EXT+"meatCoreTemp",
                oldObservation);
    }

    public void log() {
        Logging.info(String.format("Window model had %d triples", model.size()));
    }

    @SuppressWarnings("unused")
    private static class Prefix {
        public static final String ALL_AS_STRING = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + 
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
                "PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#>\n" + 
                "PREFIX sk: <http://purl.oclc.org/NET/ssnx/product/smart-knife#>\n" + 
                "PREFIX fs:<https://raw.githubusercontent.com/m-markovic/FS-PROV-Ontology/master/fso.owl#>\n" + 
                "PREFIX prov:<http://www.w3.org/ns/prov#>\n" + 
                "PREFIX fs-ex:<https://raw.githubusercontent.com/m-markovic/FS-PROV-Ontology/master/fso_extended.owl#>\n" + 
                "PREFIX sc-prov:<https://w3id.org/abdn/socialcomp/sc-prov#>";
        
        
        public static String SSN = "http://purl.oclc.org/NET/ssnx/ssn#";
        public static String PROV = "http://www.w3.org/ns/prov#";
        public static String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
        public static String OWL = "http://www.w3.org/2002/07/owl#";
        public static String XSD = "http://www.w3.org/2001/XMLSchema#";
        public static String METEO = "https://www.w3.org/2005/Incubator/ssn/ssnx/meteo/aws#";
        public static String SK = "http://purl.oclc.org/NET/ssnx/product/smart-knife#";
        public static String FS = "https://raw.githubusercontent.com/m-markovic/FS-PROV-Ontology/master/fso.owl#";
        public static String FS_EXT = "https://raw.githubusercontent.com/m-markovic/FS-PROV-Ontology/master/fso_extended.owl#";
        public static String SC = "http://example.org/sc-prov#";
    }
}