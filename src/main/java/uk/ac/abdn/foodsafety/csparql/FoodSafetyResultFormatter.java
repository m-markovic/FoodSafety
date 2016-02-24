package uk.ac.abdn.foodsafety.csparql;

import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.Observable;
import java.util.UUID;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import eu.larkc.csparql.common.RDFTable;
import eu.larkc.csparql.common.RDFTuple;
import eu.larkc.csparql.core.ResultFormatter;

/**
 * 
 * @author nhc
 *
 * A ResultFormatter which knows how to interpret results of our queries
 * and adds them to an internal Jena Model.
 */
final class FoodSafetyResultFormatter extends ResultFormatter {
    /** All query results will be added to this model. */
    private OntModel model;
    
    /**
     * Dumps the internal model on System.out
     */
    void dump() {
        this.model.writeAll(System.out, "TURTLE", null);
    }
    
    @Override
    public void update(final Observable ignored, final Object rdfTableUntyped) {
        model = ModelFactory.createOntologyModel();
        final RDFTable rdfTable = (RDFTable) rdfTableUntyped;
        for (RDFTuple tuple : rdfTable) {
                this.addMeatProbeResult(
                        tuple.get(0),
                        tuple.get(1),
                        tuple.get(2));
        }
        this.dump();
    }

    /**
     * Adds one result from meatprobe.sparql.txt to the internal model
     * @param minDate Min date captured by the query
     * @param maxDate Max date captured by the query
     * @param temp Max temperature in the interval
     */
    private void addMeatProbeResult(
            final String type, 
            final String temp,
            final String time) {
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
                this.model.createTypedLiteral(GregorianCalendar.from(ZonedDateTime.parse(time))));
        // link  observation value to sensor reading 
        newObservationValue.setPropertyValue(this.model.createProperty(
                Prefix.SSN + "hasQuantityValue"),
                this.model.createTypedLiteral(Double.parseDouble(temp)));
    }
    
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
