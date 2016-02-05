package uk.ac.abdn.foodsafety.wirelesstag;

/**
 * A GetEventRawDataResponse represents the data sent in a
 * response from the "/ethLogs.asmx/GetEventRawData" operation.
 * It is intended to be deserialized from JSON by GSON.
 */
public final class GetEventRawDataResponse {
    private Object d;

    /**
     * Temporary implementation for inspecting the responses.
     */
    public String toString() {
        return d.toString();
    }
}
