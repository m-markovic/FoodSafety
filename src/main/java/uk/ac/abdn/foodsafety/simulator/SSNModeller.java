package uk.ac.abdn.foodsafety.simulator;

import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import uk.ac.abdn.foodsafety.common.FoodSafetyException;
import uk.ac.abdn.foodsafety.simulator.sensordata.TimedTemperatureReading;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * 
 * @author nhc
 *
 * An SSNModeller expresses a parsed sensor reading in the SSN ontology,
 * then passes the resulting model on to a C-SPARQL engine.
 */
final class SSNModeller
    implements Consumer<TimedTemperatureReading> {
    private OntModel model;
    private Individual lastWirelessObservation = null;
    private Individual lastMeatProbeObservation = null;
    private final Function<ZonedDateTime, Consumer<Model>> modelConsumer;
    
    //TODO: Add URLs to use as "last observation" when there hasn't been any observations yet?
    SSNModeller(final Function<ZonedDateTime, Consumer<Model>> engine) {
        this.modelConsumer = engine;
    }
    
    @Override
    public void accept(final TimedTemperatureReading reading) {
        this.model = ModelFactory.createOntologyModel();
        if (reading.sensorType == TimedTemperatureReading.SensorType.WIRELESS_TAG) {
            this.lastWirelessObservation =
                    this.generateWirelessTagSensorAnnotations(
                            reading, 
                            this.lastWirelessObservation);
        } else if (reading.sensorType == TimedTemperatureReading.SensorType.MEAT_PROBE) {
            this.lastMeatProbeObservation =
                    this.generateMeatProbeAnnotations(
                            reading, 
                            this.lastMeatProbeObservation);
        } else {
            throw FoodSafetyException.internalError(String.format(
                    "The SSNModeller needs to handle the new sensor type %s", 
                    reading.sensorType));
        }
        modelConsumer.apply(reading.time).accept(this.model);
        this.model = null;
    }
    
    /**
     * @param r Reading to make provenance model for
     * @param sensorID
     * @param oldObservation Latest observation
     * @return New observation (which is already added to this.model)
     */
    private Individual generateMeatProbeAnnotations(
            final TimedTemperatureReading r,
            final Individual oldObservation) {
        final Individual meatprobe = model.createIndividual(
                "http://FoodSafety/sensor/meatProbe/mp",
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
    
    /**
     * @param r Reading to make provenance model for
     * @param sensorID
     * @param oldObservation Latest observation
     * @return New observation (which is already added to this.model)
     */
    private Individual generateWirelessTagSensorAnnotations(
            final TimedTemperatureReading r,
            final Individual oldObservation) {
        //static ssn info relevant to all data coming from the same sensor 
        //create instance of main System that will represent a single wirelesstag
        final Individual  wirelessTagSystem = 
                model.createIndividual(
                        "http://FoodSafety/system/wirelesstag/" + r.sensorId,
                        model.createClass(Prefix.SSN+"System"));
        // create instances of individual sensors for temp, humidity and movement (note Sensing Device is subclass of ssn:Sensor)
        final Individual  temperatureSensor = 
                sensorDescribtion(
                        wirelessTagSystem,
                        Prefix.METEO+"TmeperatureSensor");
        // annotate data from temperature sensor 
        return annotateSingleSensorData(
                r, 
                temperatureSensor, 
                "temperature", 
                Prefix.FS_EXT+"meatSurfaceTemp", 
                oldObservation);
    }

    private Individual annotateSingleSensorData(
            final TimedTemperatureReading reading,
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
    private Individual sensorDescribtion (
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

    @SuppressWarnings("unused")
    private static class Prefix {
        public static String SSN = "http://purl.oclc.org/NET/ssnx/ssn#";
        public static String PROV = "http://www.w3.org/ns/prov#";
        public static String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
        public static String OWL = "http://www.w3.org/2002/07/owl#";
        public static String XSD = "http://www.w3.org/2001/XMLSchema#";
        public static String METEO = "https://www.w3.org/2005/Incubator/ssn/ssnx/meteo/aws#";
        public static String SK = "http://purl.oclc.org/NET/ssnx/product/smart-knife#";
        public static String FS = "https://raw.githubusercontent.com/m-markovic/FS-PROV-Ontology/master/fso#";
        public static String FS_EXT = "https://raw.githubusercontent.com/m-markovic/FS-PROV-Ontology/master/fso_extended#";
        public static String SC = "https://w3id.org/abdn/socialcomp/sc-prov#";
    }
}