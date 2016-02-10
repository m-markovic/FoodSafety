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
public final class FoodSafetyException extends RuntimeException {
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
     * Wraps an Exception caught during communication with mywirelesstag.com 
     * @param e IOException caught while communicating with mywirelesstag.com
     * @return An FoodSafetyException representing the known facts about the situation
     */
    public static FoodSafetyException wirelessTagConnectionFailed(final IOException e) {
        return new FoodSafetyException("Failed to connect to mywirelesstag.com", e);
    }

    /**
     * Creates an Exception for HTTP errors during communication with mywirelesstag.com
     * @param responseCode A non-200 HTTP response from mywirelesstag.com
     * @param url The url that returned the response
     * @return An FoodSafetyException representing the known facts about the situation
     */
    public static FoodSafetyException wirelessTagSentError(
            final int responseCode, 
            final URL url) {
        return new FoodSafetyException(
                String.format(
                    "Got HTTP response code %d on request for %s", 
                    responseCode, 
                    url), 
                null);
    }
    
    /**
     * Exception to use when an internal error caused a caught Exception
     * @param e The caught Exception
     * @return Wrapping Exception
     */
    public static FoodSafetyException internalError(
            final Exception e) {
        return new FoodSafetyException("Internal FoodSafety appliction error", e);
    }

    /**
     * Constructs an Exception to throw when some user input was malformed.
     * @param userInput The malformed user input.
     * @param e The Exception caught when processing the malformed input.
     * @return The wrapping Exception
     */
    public static FoodSafetyException userInputError(
            final String userInput,
            final Exception e) {
        return new FoodSafetyException(String.format("Malformed input: '%s'", userInput), e);
    }
}
