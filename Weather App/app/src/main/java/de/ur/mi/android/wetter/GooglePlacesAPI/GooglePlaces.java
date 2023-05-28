package de.ur.mi.android.wetter.GooglePlacesAPI;

import android.graphics.Bitmap;
import android.view.View;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.ur.mi.android.wetter.CityWeatherCardItem;
import de.ur.mi.android.wetter.MainActivity;
import de.ur.mi.android.wetter.PlacesRecyclerAdapter;
import de.ur.mi.android.wetter.R;
import de.ur.mi.android.wetter.UI.CustomToasts;

/**This class is used to retrieve Google Autocomplete API predictions
 * and Google Places Photos for a given inout and city**/
//https://developers.google.com/maps/documentation/places/android-sdk/autocomplete
public class GooglePlaces {
    private final MainActivity mainActivity;
    private final ArrayList<AutocompletePrediction> predictionList;
    private PlacesClient placesClient;
    private AutocompleteSessionToken token;

    public GooglePlaces(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        //Sets up the prediction ArrayList
        predictionList = new ArrayList<>();
    }

    public void setupPlacesClient() {
        //Generates a Google GooglePlaces API Session Token to reduce api costs
        token = AutocompleteSessionToken.newInstance();
        initializePlacesClient();

        //Some code was refactored weirdly when creating class delegates
        placesClient = com.google.android.libraries.places.api.Places.createClient(mainActivity);
    }

    //Method to make the actual prediction request
    public void makePredictionsRequest(String query, PlacesRecyclerAdapter adapter) {

        //Setting up of the autocomplete request
        FindAutocompletePredictionsRequest autocompleteRequest = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.CITIES) //Filtering for cities
                .setSessionToken(token) //Sets the session token
                .setQuery(query) //Sets the autocomplete Query (The users input)
                .build();

        //Finds the corresponding predictions
        placesClient.findAutocompletePredictions(autocompleteRequest).addOnSuccessListener((response) -> {
            //Clears the old predictions list so new ones can be added
            predictionList.clear();

            //List of received responses responses
            List<AutocompletePrediction> responsePredictions = response.getAutocompletePredictions();

            //Loops through the received predictions
            for (AutocompletePrediction prediction : responsePredictions) {
                //Adds a new PlaceItem to the getItemsList used by the Popup City Search Recyclerview
                mainActivity.getPlaceItemsList().add(new PlaceItem(prediction.getPrimaryText(null).toString()));
                //Adds the prediction to the preditions List (used to get the cityId so we can get the city photo)
                predictionList.add(prediction);

                //If there are no results then make the textview that tells the user to search for a city invisible
                if(mainActivity.placeItemsList.size() > 0) {
                    mainActivity.popupStubInflated.findViewById(R.id.noItemsPopup).setVisibility(View.GONE);
                }
                //If there are results then make the textview that tells the user to search for a city visible
                else {
                    mainActivity.popupStubInflated.findViewById(R.id.noItemsPopup).setVisibility(View.VISIBLE);
                }

                //Notifies the adapter that the dataset has changed (updates the popup recyclerview)
                adapter.notifyDataSetChanged();
            }

            //Errors while making autocomplete request get handled here
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                //ApiException apiException = (ApiException) exception;
                CustomToasts.error.show();
            }
        });
    }

    //Method to initialize the placesClient
    public void initializePlacesClient() {
        if (!com.google.android.libraries.places.api.Places.isInitialized()) {
            com.google.android.libraries.places.api.Places.initialize(mainActivity.getApplicationContext(), mainActivity.getString(R.string.places_api_key));
        }
    }

    //Getter for the predictionList used to get cityIds in MainActivity
    public ArrayList<AutocompletePrediction> getPredictionList() {
        return predictionList;
    }


    //Method to get the photo to the corresponding city
    public void getPlacePhoto(String placeId, CityWeatherCardItem cityWeatherCardItem, int addedItemArrayPosition) {
        //Specifies fields for request
        final List<Place.Field> fields = Collections.singletonList(Place.Field.PHOTO_METADATAS);

        //Creates new FetchPlaceRequest for getting Place Object
        final FetchPlaceRequest placeRequest = FetchPlaceRequest.newInstance(placeId, fields);

        //Makes request for Place
        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            final Place place = response.getPlace();

            // Get the photo metadata.
            final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
            if (metadata == null || metadata.isEmpty()) {
                return;
            }
            final PhotoMetadata photoMetadata = metadata.get(0);

            // Create a FetchPhotoRequest.
            final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                    .setMaxWidth(700) // Optional. Sets the maximum width resolution
                    .setMaxHeight(700) // Optional. Sets the maximum height resolution
                    .build();

            //Makes the photo request
            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                Bitmap bitmap = fetchPhotoResponse.getBitmap();

                //Sets the photo to the passed in cityWeatherCardItem
                mainActivity.cityWeatherCardRecyclerView.scheduleLayoutAnimation();
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cityWeatherCardItem.setCityImage(bitmap);

                        //If there is a city weather card added then the textview telling the user to add a city becomes invisible
                        if(MainActivity.cityWeatherCardItems.size() >= 1) {
                            mainActivity.noItemsTextview.setVisibility(View.GONE);
                        }

                        //Updates the cityWeatherCard Recyclerview
                        mainActivity.cityWeatherCardAdapter.notifyItemInserted(addedItemArrayPosition);
                    }
                });

                //Errors while fetching photo get handled here
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    //final ApiException apiException = (ApiException) exception;
                    //Log.e("PlacesPhotoError", "Place not found: " + exception.getMessage());
                    //final int statusCode = apiException.getStatusCode();
                    CustomToasts.error.show();
                }
            });
        });
    }
}