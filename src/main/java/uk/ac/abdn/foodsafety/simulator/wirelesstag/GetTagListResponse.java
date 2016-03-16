package uk.ac.abdn.foodsafety.simulator.wirelesstag;

import java.util.List;

import uk.ac.abdn.foodsafety.common.Logging;

/**
 * A GetTagListResponse represents the data sent in a
 * response from the "/ethLogs.asmx/GetTagList" operation.
 * It is intended to be deserialized from JSON by GSON.
 */
public final class GetTagListResponse {
    /** This operation returns a sequence of tag objects. */
    private List<TagDescription> d;

    /**
     * Utility method for inspecting the responses.
     */
    public void log() {
        d.forEach(TagDescription::log);
    }
    
    /**
     * A TagDescription captures some the data returned
     * for each tag. 
     */
    public static final class TagDescription {
        private String name;
        private String uuid;
        private int slaveId;
        
        /**
         * Utility method for inspecting the responses.
         */
        public void log() {
            Logging.info(String.format(
                    "Wireless tag: name=%s, uuid=%s, slaveId=%d", 
                    name, 
                    uuid, 
                    slaveId));
        }
       
    };

}
