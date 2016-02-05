package uk.ac.abdn.foodsafety;

import java.io.IOException;
import java.net.URL;

/**
 * 
 * @author nhc
 *
 * The only Exception expected from the FoodSafety application.
 */
@SuppressWarnings("serial")
final class FoodSafetyException extends RuntimeException {
    /**
     * Constructor.
     * @param msg The error message
     * @param cause The Exception behind this situation 
     */
    private FoodSafetyException(
        final String msg,
        final Exception cause) {
      super(String.format("%s, caused by %s", msg, cause));
    }    

    /**
     * @param e IOException caught while communicating with mywirelesstag.com
     * @return An FoodSafetyException representing the known facts about the situation
     */
    static final FoodSafetyException wirelessTagConnectionFailed(final IOException e) {
        return new FoodSafetyException("Failed to connect to mywirelesstag.com", e);
    }

    /**
     * @param responseCode A non-200 HTTP response from mywirelesstag.com
     * @param url The url that returned the response
     * @return An FoodSafetyException representing the known facts about the situation
     */
    static final FoodSafetyException wirelessTagSentError(
            final int responseCode, 
            final URL url) {
        return new FoodSafetyException(
                String.format(
                    "Got HTTP response code %d on request for %s", 
                    responseCode, 
                    url), 
                null);
    }
}
