package de.ur.mi.android.wetter.OpenWeatherMapAPI;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

import de.ur.mi.android.wetter.MainActivity;
import de.ur.mi.android.wetter.R;
import de.ur.mi.android.wetter.UI.CustomToasts;

/**
 * Instances of this class represent a single request to the Weather API
 * with the goal of getting the current weather for a specific city.
 */
public class WeatherDataRequest {

    // Name of the city of which weather information should be retrieved
    private final String city;
    // Context of Activity in which request is made
    private final Context context;

    // Template for HTTP-Request for Weatherinformation of a specific city.
    private final String REQUEST_URL;
    // API_KEY used for Authentication on every Request
    private final String API_KEY;

    public WeatherDataRequest(String city, Context context, MainActivity activity) {
        this.city = city;
        this.context = context;
        this.REQUEST_URL = activity.getString(R.string.open_weather_request_url);
        this.API_KEY = activity.getString(R.string.open_weather_api_key);
    }

    /**
     * This method executes the API request and returns the result as a WeatherData object to the passed in listener.
     * This is necessary because the request has to be notified about the result in parallel. Future requests so that request for all items
     * on refresh can be performed without the responses not being received in time.
     */
    public void syncCall(DataRequestListener listener) {

        JSONObject response;

        RequestQueue requestQueue = Volley.newRequestQueue(context);

        //Creates new RequestFuture Object
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        //Refactors the API Url
        String url = REQUEST_URL.replace("{city name}", city).replace("{API key}", API_KEY);
        //Creates the JSONObjectRequest
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(), future, future);
        //Adds the request to the requestQueue
        requestQueue.add(request);

        try {
            //Tries to get the response
            response = future.get();
            //Passes the result of the response (after handling)  to the listener in MainActivity
            listener.onResult(getWeatherDataFromResponse(response));

            //Errors while making request are handled here
        } catch (InterruptedException e) {
            e.printStackTrace();
            CustomToasts.noWeatherDataAvailable.show();
        } catch (ExecutionException e) {
            e.printStackTrace();
            CustomToasts.noWeatherDataAvailable.show();
        }
    }

    /**
     * The method searches or the relevant information in the received JSONObject and returns the information
     * packaged in a WeatherData Object.
     */
    private WeatherData getWeatherDataFromResponse(JSONObject response) {
        try {
            //Gets the city name form the JSON
            String city = response.getJSONObject("location").getString("name");
            //Gets the description of that cities weather from the JSON
            String description = response.getJSONObject("current").getJSONObject("condition").getString("text");
            //Gets the temperature of that city from the JSON
            String temperature = response.getJSONObject("current").getString("temp_c");

            //Returns the desired data from the JSON file bundled as a WeatherData object
            return new WeatherData(city, description, temperature);

            //Errors during JSON Parsing get handled here
        } catch (JSONException exception) {
            exception.printStackTrace();
            CustomToasts.error.show();
            return null;
        }
    }

    /**
     * This interface is used to specify how objects have to look that can act as receivers of the
     * request results. (Observer). A corresponding object has to be passed on to the run method of the request.
     */
    public interface DataRequestListener {
        void onResult(WeatherData data) throws InterruptedException;
    }
}


