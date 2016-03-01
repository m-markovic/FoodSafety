package uk.ac.abdn.foodsafety.simulator.wirelesstag;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A GetStatsRawRequest represents the data sent in a
 * request to the "/ethLogs.asmx/GetStatsRaw" operation.
 * It is intended to be serialized to JSON by GSON.
 */
public final class GetStatsRawRequest {
    /** Example: 3 */
    @SuppressWarnings("unused")
    private final int id;
    
    /** Example: "2/8/2016" for Feb 8 2016. 
     * This is in American style: MM/dd/yyyy */
    @SuppressWarnings("unused")
    private final String fromDate;
    
    /** Example: "2/8/2016" for Feb 8 2016. 
     * This is in American style: MM/dd/yyyy */
    @SuppressWarnings("unused")
    private final String toDate;
    
    /**
     * @param id ID of a wireless tag sensor
     * @param fromDate The first date to get data from
     * @param toDate The last date to get data from - must be after fromDate
     */
    public GetStatsRawRequest(
            final int id,
            final LocalDate fromDate,
            final LocalDate toDate) {
        this.id = id;
        this.fromDate = fromDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        this.toDate = toDate.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }
}
