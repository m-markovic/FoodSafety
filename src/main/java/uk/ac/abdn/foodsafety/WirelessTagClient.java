package uk.ac.abdn.foodsafety;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

import javax.net.ssl.HttpsURLConnection;

import uk.ac.abdn.foodsafety.csparql.FoodSafetyEngine;
import uk.ac.abdn.foodsafety.wirelesstag.GetStatsRawRequest;
import uk.ac.abdn.foodsafety.wirelesstag.GetStatsRawResponse;
import uk.ac.abdn.foodsafety.wirelesstag.SignInRequest;
import uk.ac.abdn.foodsafety.wirelesstag.GetEventRawDataRequest;
import uk.ac.abdn.foodsafety.wirelesstag.GetEventRawDataResponse;

import com.google.gson.Gson;

/**
 * An HTTP Client for mywirelesstag.com
 * 
 * Credentials must be given in an environment variable,
 * @see uk.ac.abdn.foodsafety.wirelesstag.SignInRequest
 */
class WirelessTagClient {
    /** Base URL for all requests made */
    private static final String BASE_URL = "https://www.mytaglist.com";

    /** Have the credentials from the environment been used to sign in? */
    private static boolean signedIn = false;

    /** See https://github.com/google/gson */
    private Gson gson = new Gson();

    /**
     * Signs in to mywirelesstag.com if this has not been
     * done previously by another instance.
     */
    WirelessTagClient() {
        WirelessTagClient.ensureSignedIn(this);
    }

    /**
     * Signs in to mywirelesstag.com if this has not been
     * done previously by another instance.
     * @param instance client object to use for the SignIn operation.
     */
    private static synchronized void ensureSignedIn(final WirelessTagClient instance) {
        if (!WirelessTagClient.signedIn) {
            CookieHandler.setDefault(new CookieManager());
            instance.signIn();
            WirelessTagClient.signedIn = true;
        }
    }
    
    /**
     * Performs a POST to "/ethLogs.asmx/GetEventRawData", retrieving
     * event data from a specified sensor.
     * @param sensorId The ID of the sensor to get data for, e.g. 3.
     */
    public void getEventRawData(final int sensorId) {
        try {
            final HttpsURLConnection urlConnection = 
              this.post(
                  "/ethLogs.asmx/GetEventRawData",
                  gson.toJson(new GetEventRawDataRequest(sensorId)));
            final GetEventRawDataResponse response = 
                    this.parseJsonResponse(
                        urlConnection, 
                        GetEventRawDataResponse.class);
            //TODO do stuff
            System.out.println(response.toString());
          } catch (final IOException e) {
              throw FoodSafetyException.wirelessTagConnectionFailed(e);
          }
    }

    /**
     * Performs a POST to "/ethLogs.asmx/GetStatsRaw", retrieving
     * event data from a specified sensor between two dates.
     * @param sensorId The ID of the sensor to get data for, e.g. 3.
     * @param fromDate The first date to get data from
     * @param toDate The last date to get data from - must be after fromDate
     */
    public void getStatsRaw(
            final int sensorId,
            final LocalDate fromDate,
            final LocalDate toDate,
            final FoodSafetyEngine engine) {
        assert fromDate.isBefore(toDate) : String.format(
                "Cannot get data for this period because fromDate %s is not before toDate %s",
                fromDate,
                toDate);
        try {
            final HttpsURLConnection urlConnection = 
              this.post(
                  "/ethLogs.asmx/GetStatsRaw",
                  gson.toJson(new GetStatsRawRequest(sensorId, fromDate, toDate)));
            final GetStatsRawResponse response = 
                    this.parseJsonResponse(
                        urlConnection, 
                        GetStatsRawResponse.class);
            response.addReadingsTo(engine);
          } catch (final IOException e) {
              throw FoodSafetyException.wirelessTagConnectionFailed(e);
          }
    }

    /**
     * Performs a POST to "/ethAccount.asmx/SignIn", signing in.
     * The expected result of this operation is that a login cookie named "WTAG"
     * will be set in the default java.net CookieHandler and will be passed
     * to mywirelesstag.com in subsequent HTTP requests. 
     */
    private synchronized void signIn() {
        try {
            final HttpsURLConnection urlConnection = this.post(
                    "/ethAccount.asmx/SignIn", 
                    gson.toJson(new SignInRequest()));
            final int responseCode = urlConnection.getResponseCode();
            if (responseCode != 200) {
                throw FoodSafetyException.wirelessTagSentError(
                        responseCode,
                        urlConnection.getURL());
            }
        } catch (final IOException e) {
            throw FoodSafetyException.wirelessTagConnectionFailed(e);
        }
    }

    /**
     * Creates an UrlConnection to a URL based on BASE_URL.
     * @param path the URL path to add, e.g. "/ethAccount.asmx/SignIn" 
     * @return the newly created connection
     * @throws IOException if the URL was malformed or the connection
     * could not be created.
     */
    private HttpsURLConnection createConnection(final String path) 
            throws IOException {
        final HttpsURLConnection result =  
                (HttpsURLConnection) new URL(WirelessTagClient.BASE_URL + path)
        .openConnection();
        //X-Requested-With to avoid CORS issues
        result.setRequestProperty("X-Requested-With", "java.net");
        //Done
        return result;
    }

    /**
     * Creates an UrlConnection to a URL based on BASE_URL
     * and POSTs a text body to that URL, insisting on JSON request and response.
     * @param path the URL path to add, e.g. "/ethAccount.asmx/SignIn" 
     * @return the newly created connection
     * @throws IOException if the URL was malformed or the connection
     * could not be created.
     */
    private HttpsURLConnection post(
            final String path, 
            final String body) throws IOException {
        final HttpsURLConnection urlConnection = 
                this.createConnection(path);
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept","application/json");
        urlConnection.setDoOutput(true);
        final OutputStreamWriter writer = new OutputStreamWriter(urlConnection.getOutputStream());
        writer.write(body);
        writer.flush();
        writer.close();
        return urlConnection;
    }

    /**
     * Checks that the response on the connection was 200, then parses
     * the JSON in its body into an object of the specified class.
     * @param urlConnection the connection to get data from.
     * @param classOfT intended class of the response, a GSON-deserializable class
     * @return the parsed response
     * @throws IOException if the IO with the tool failed.
     */
    private <T> T parseJsonResponse(
            final HttpURLConnection urlConnection,
            final Class<T> classOfT) throws IOException {
        final int responseCode = urlConnection.getResponseCode();
        if (responseCode != 200) {
            throw FoodSafetyException.wirelessTagSentError(
                    responseCode,
                    urlConnection.getURL());
        }
        final BufferedReader responseReader = 
                new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
        final T response = gson.fromJson(responseReader, classOfT);
        responseReader.close();
        return response;
    }
}
