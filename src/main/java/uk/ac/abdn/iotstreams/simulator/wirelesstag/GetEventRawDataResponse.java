package uk.ac.abdn.iotstreams.simulator.wirelesstag;

import java.util.List;

/**
 * A GetEventRawDataResponse represents the data sent in a
 * response from the "/ethLogs.asmx/GetEventRawData" operation.
 * It is intended to be deserialized from JSON by GSON.
 */
public final class GetEventRawDataResponse {
    /** This operation returns a sequence of motion sensor events. */
    private List<MotionEvent> d;

    /**
     * Temporary implementation for inspecting the responses.
     */
    public String toString() {
        return d.toString();
    }
    
    /**
     * A MotionEvent captures the data returned
     * for each sensor motion event. 
     */
    public static final class MotionEvent {
        /** Example: "2/8/2016" for Feb 8 2016. 
          * This is in American style: MM/dd/yyyy */
        private String date;
        
        /** Example: "10:09:40.867". TODO: How to determine time zone? */
        private String time;
        
        /** Examples: "Armed", "Moved", "TimedOut", "Disarmed" */
        private String eventType;
        
        /** Example: 0 */
        private int durationSec;

        /**
         * Temporary implementation for inspecting the responses.
         */
        public String toString() {
            return String.join(",", date, time, eventType, Integer.toString(durationSec));
        }
       
    };

}
