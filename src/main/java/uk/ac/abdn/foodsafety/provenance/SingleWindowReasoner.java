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
    
    private Individual anotateSingleSensorData(
            OntModel modelToWriteTo, 
            Individual sensor, 
            String sensorType,
            Literal dateTime, 
            String value, 
            String observedProperty,
            String foi, 
            Individual oldObservation)
    {
        // ---- individuals 
        Individual newObservation = modelToWriteTo.createIndividual("http://FoodSafety/observation/"+sensorType+"/" + UUID.randomUUID(),
                modelToWriteTo.createClass(Prefix.SSN + "Observation"));
        Individual newSensorOutput = modelToWriteTo.createIndividual("http://FoodSafety/sensorOutput/"+sensorType+"/" + UUID.randomUUID(),
                modelToWriteTo.createClass(Prefix.SSN + "SensorOutput"));
        Individual newObservationValue = modelToWriteTo.createIndividual("http://FoodSafety/observationValue/"+sensorType+"/" + UUID.randomUUID(),
                modelToWriteTo.createClass(Prefix.SK + "QuantityObservationValue"));
        Individual property = modelToWriteTo.createIndividual(observedProperty,
                modelToWriteTo.createClass(Prefix.SSN + "Property"));
        Individual fetureOfInterest = modelToWriteTo.createIndividual(foi,
                modelToWriteTo.createClass(Prefix.SSN + "FeatureOfInterest"));
        value = value.replace ("\"","");
        Literal sensorReading = modelToWriteTo.createTypedLiteral(Double.parseDouble(value));
        // ---- properties
        // set time of observation
        newObservation.setPropertyValue(modelToWriteTo.createProperty(Prefix.SSN + "observationSamplingTime"),
                dateTime);
        // link sensor and sensor output
        newSensorOutput.setPropertyValue(modelToWriteTo.createProperty(Prefix.SSN + "isProducedBy"),
                sensor);
        // link observation and sensor output
        newObservation.setPropertyValue(modelToWriteTo.createProperty(Prefix.SSN + "observationResult"),
                newSensorOutput);
        // link sensor output and quantity observation value 
        newSensorOutput.setPropertyValue(modelToWriteTo.createProperty(Prefix.SSN + "hasValue"),
                newObservationValue);
        // link  observation value to sensor reading 
        newObservationValue.setPropertyValue(modelToWriteTo.createProperty(Prefix.SK + "hasQuantityValue"),
                sensorReading);
        // link  observation to foi
        newObservation.setPropertyValue(modelToWriteTo.createProperty(Prefix.SSN + "featureOfInterest"),
                fetureOfInterest);        
        // link  foi and property
        fetureOfInterest.setPropertyValue(modelToWriteTo.createProperty(Prefix.SSN + "hasProperty"),
                property);
        // link  property and sensor
        sensor.setPropertyValue(modelToWriteTo.createProperty(Prefix.SSN + "observes"),
                        property);
        if (oldObservation != null) {
        // link  new observation to the previous one 
        newObservation.setPropertyValue(modelToWriteTo.createProperty(Prefix.FS + "follows"),
                        oldObservation);
        }
        // ---- properties
       return newObservation;
    }

    public void log() {
        Logging.info(String.format("Window model had %d triples", model.size()));
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