package uk.ac.abdn.foodsafety.wirelesstag;

/**
 * A SignInRequest represents the data sent in a
 * request to the "/ethLogs.asmx/GetEventRawData" operation.
 * It is intended to be serialized to JSON by GSON.
 */
public final class SignInRequest {
    /** Environment variable containing the credentials for mytaglist.com */
    private static final transient String ENV_VARIABLE = "WTCRED";
    
    /** Instructions in case proper credentials were not found in the environment */
    private static final transient String MESSAGE = String.format("Format of environment variable '%s' should be 'me@myemail.com mypasswd'", ENV_VARIABLE);
    
    /** The email to use for signing in */
    @SuppressWarnings("unused")
    private String email;

    /** The password to use for signing in */
    @SuppressWarnings("unused")
    private String password;

    /**
     * Reads credentials from the system environment.
     */
    public SignInRequest() {
        final String credentials = System.getenv(ENV_VARIABLE);
        assert (credentials != null) : MESSAGE;
        final String[] pair = credentials.split(" ");
        assert pair.length == 2 : MESSAGE;
        this.email = pair[0];
        this.password = pair[1];
    }
}
