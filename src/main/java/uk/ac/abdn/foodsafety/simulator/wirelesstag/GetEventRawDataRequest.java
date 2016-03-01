package uk.ac.abdn.foodsafety.simulator.wirelesstag;

/**
 * A GetEventRawDataRequest represents the data sent in a
 * request to the "/ethLogs.asmx/GetEventRawData" operation.
 * It is intended to be serialized to JSON by GSON.
 */
public final class GetEventRawDataRequest {
    /** Example: 3 */
    @SuppressWarnings("unused")
    private final int id;
    
    /**
     * @param id ID of a wireless tag sensor, e.g. 3
     */
    public GetEventRawDataRequest(final int id) {
        this.id = id;
    }
}
