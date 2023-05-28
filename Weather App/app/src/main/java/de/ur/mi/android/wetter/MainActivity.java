package de.ur.mi.android.wetter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import de.ur.mi.android.wetter.Geocoding.Geocoding;
import de.ur.mi.android.wetter.GooglePlacesAPI.GooglePlaces;
import de.ur.mi.android.wetter.GooglePlacesAPI.PlaceItem;
import de.ur.mi.android.wetter.OpenWeatherMapAPI.WeatherData;
import de.ur.mi.android.wetter.OpenWeatherMapAPI.WeatherDataRequest;
import de.ur.mi.android.wetter.UI.CustomAnimations;
import de.ur.mi.android.wetter.UI.CustomToasts;
import de.ur.mi.android.wetter.UI.TranslucentFullscreenManager;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;

public class MainActivity extends AppCompatActivity implements WeatherDataRequest.DataRequestListener, PlacesRecyclerAdapter.ClickListener, CityWeatherCardAdapter.OnItemLongClickListener {

    public static ArrayList<CityWeatherCardItem> cityWeatherCardItems;
    private final CustomAnimations customAnimations = new CustomAnimations(this);
    private final GooglePlaces places = new GooglePlaces(this);
    public ArrayList<PlaceItem> placeItemsList;
    public RecyclerView cityWeatherCardRecyclerView;
    public CityWeatherCardAdapter cityWeatherCardAdapter;
    public boolean onRefresh = false;
    private FloatingActionButton addCityFab;
    private View blurWrapLayout;
    private EditText addCityEditText;
    private View popupOutsideArea;
    private CardView popUpCardView;
    private RenderEffect blurEffect;
    private ViewStub popupStub;
    public View popupStubInflated;
    private RecyclerView citySearchRecyclerView;
    private PlacesRecyclerAdapter citySearchAdapter;
    private boolean editTextHasBeenSetup = false;
    private ImageView locationButton;
    private ImageView locationCancel;
    private boolean locationPermissionWasGrantedAlready = false;
    private Geocoding geocoder;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String cityName;
    private String cityId;
    private ArrayList<String> bitmapStrings;
    public TextView noItemsTextview;
    private Bitmap currentRefreshBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Sets Blur effect by using Android 12 RenderEffects
        blurEffect = RenderEffect.createBlurEffect(35, 35, Shader.TileMode.MIRROR);
        blurWrapLayout = findViewById(R.id.blurWrapConstraintLayout);

        //Button to add a new City to the horizontal RecyclerView
        addCityFab = findViewById(R.id.addCityFab);

        //Sets up the Google GooglePlaces API Client
        places.setupPlacesClient();

        //Sets up all the actions associated with opening the popup menu
        setupAddCityFAB();

        //Makes the status and navigation bar translucent
        TranslucentFullscreenManager.makeStatusBarTranslucent(MainActivity.this);

        //Finds the ViewStub later used to lazily inflate the popup menu
        popupStub = findViewById(R.id.stub);

        //Sets up custom Animations
        customAnimations.setupHideShowAnimations();

        //Initializes the placeItems ArrayList
        placeItemsList = new ArrayList<>();

        //Initializes the Geocoder to get the users last know location;
        geocoder = new Geocoding(this);

        //Sets up the CityWeatherCardAdapter
        setupCityWeatherCardRecyclerView();

        //Sets up the swipeRefreshLayout
        setupSwipeRefreshLayout();

        //Sets up the customToasts
        CustomToasts.setupToasts(getApplicationContext());

        /*Initializes the ArrayList used for storing the city image bitmap byte Arrays
        as Strings to then store in shared preferences */
        bitmapStrings = new ArrayList<>();

        //Textview used for telling the user to add a new city if none has been added yet
        noItemsTextview = findViewById(R.id.noItems);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Saves the data if the app is paused
        saveData();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*If there are no CityWeatherCards added then show the
        textview telling the user to add a new city*/
        if(cityWeatherCardItems.size() != 0) {
            noItemsTextview.setVisibility(View.GONE);
        }

        //Inflates the popup Menu Layout to the ViewStub if it is null
        if (popupStubInflated == null) {
            popupStubInflated = popupStub.inflate();
            popupStubInflated.setVisibility(View.GONE);
        }

        //Finds the location button used to find the users current location
        if (locationButton == null) {
            locationButton = popupStubInflated.findViewById(R.id.locationButton);
        }

        //Finds the location Cancel ImageView that shows that location permission was denied
        if (locationCancel == null) {
            locationCancel = popupStubInflated.findViewById(R.id.locationCancel);
        }

        //Checks for location permission and sets the locationButton Button and locationCancel Image accordingly
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionWasGrantedAlready = true;

            locationButton.setImageResource(R.drawable.ic_location_enabled);
            locationCancel.setVisibility(View.INVISIBLE);
        } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            locationPermissionWasGrantedAlready = false;

            locationButton.setImageResource(R.drawable.ic_location_dsabled);
            locationCancel.setVisibility(View.VISIBLE);
        }
    }

    /*
    Unsure how to store bitmaps and assign them back to the correct weathercard.
    In the future I'll do this by storing them in local memory.
     */
    //https://stackoverflow.com/questions/17268519/how-to-store-bitmap-object-in-sharedpreferences-in-android
    //https://www.youtube.com/watch?v=jcliHGR3CHo&ab_channel=CodinginFlow
    //Saves the data
    private void saveData() {
        //Compresses and converts the bitmaps to strings and saves them to the bitmapStrings arrayList
        for (CityWeatherCardItem cityWeatherCardItem : cityWeatherCardItems) {
            bitmapStrings.add(Base64.encodeToString(BitmapCompressor.compressBitmap(cityWeatherCardItem.getCityImage(), 25), Base64.DEFAULT));
            //Sets the cityWeatherCardItems to null because the bitmaps cant be stored in shared preferences
            cityWeatherCardItem.setCityImage(null);
        }

        /*
        Converts the cardWeather Items text and bitmap strings to JSON
        by using Google GSON library and stores them in shared preferences
        */
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String jsonText = gson.toJson(cityWeatherCardItems);
        String jsonBitmap = gson.toJson(bitmapStrings);
        editor.putString("cardList", jsonText);
        editor.putString("bitmapList", jsonBitmap);
        editor.apply();
    }

    //Loads the data
    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonText = sharedPreferences.getString("cardList", null);
        String jsonBitmap = sharedPreferences.getString("bitmapList", null);
        Type cityCards = new TypeToken<ArrayList<CityWeatherCardItem>>() {}.getType();
        Type bitmaps = new TypeToken<ArrayList<String>>() {}.getType();
        cityWeatherCardItems = gson.fromJson(jsonText, cityCards);

        if (cityWeatherCardItems == null) {
            cityWeatherCardItems = new ArrayList<>();
        }

        bitmapStrings = gson.fromJson(jsonBitmap, bitmaps);

        if (bitmapStrings == null) {
            bitmapStrings = new ArrayList<>();
        }

        //Decodes the bitmap Strings and sets the cityWeatherCards Images
        for (int i = 0; i < cityWeatherCardItems.size(); i++) {
            byte[] imageAsBytes = Base64.decode(bitmapStrings.get(i).getBytes(), Base64.DEFAULT);
            cityWeatherCardItems.get(i).setCityImage(BitmapCompressor.decodeByteArray(imageAsBytes));
        }
    }

    //Sets up the swipeRefreshLayout used to refresh the weather data
    private void setupSwipeRefreshLayout() {
        //Finds the swipeRefreshLayout in Layout
        swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);
        //Theming the progress indicator and setting preferences
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.granite_grey);
        swipeRefreshLayout.setColorSchemeColors(getColor(R.color.bright_gray));
        swipeRefreshLayout.setProgressViewEndTarget(false, 350);
        swipeRefreshLayout.setDistanceToTriggerSync(200);
        swipeRefreshLayout.setSlingshotDistance(250);
        swipeRefreshLayout.setProgressViewOffset(false, 0, 350);

        //What happens when refresh is triggered
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //Check if there are any cityWeatherCards to refresh
                if (cityWeatherCardItems.size() > 0) {
                    onRefresh = true;

                    //Starts the weather data request after a short delay
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            for (int i = 0; i < cityWeatherCardItems.size(); i++) {
                                String city = cityWeatherCardItems.get(i).getCityName();
                                //Makes the weather request
                                runDummyRequest(city);
                            }
                        }
                    }, 800);
                /*
                0.8 second delay feels better for the user than an instant update,
                since if it was instance the user would wonder if the refresh was successful.
                */
                } //If cityWeatherCards are there to refresh show the refresh icon for 0.4s (feels better)
                else {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }, 400);
                }
            }
        });
    }

    //Sets up the cityWeatherCard RecyclerView
    private void setupCityWeatherCardRecyclerView() {
        //Loads the weatherCard data from Shared Preferences
        loadData();
        //Initializes the cityWeatherCardAdapter
        cityWeatherCardAdapter = new CityWeatherCardAdapter(cityWeatherCardItems, this);
        //Finds the cityCardRecyclerView
        cityWeatherCardRecyclerView = findViewById(R.id.cityCardRecyclerView);
        //Sets the layoutManager for the cityCardRecyclerView
        RecyclerView.LayoutManager cityWeatherCardLayoutManager = new LinearLayoutManager(this);
        cityWeatherCardRecyclerView.setLayoutManager(cityWeatherCardLayoutManager);
        //Sets the item Animator for the cityWeatherCardRecyclerView
        cityWeatherCardRecyclerView.setItemAnimator(new FadeInAnimator(new DecelerateInterpolator(1f)));
        //Sets fixed mode to false for the cityWeatherCardRecyclerView (notifyItemInserted wont work if set to true)
        cityWeatherCardRecyclerView.setHasFixedSize(false);
        //Sets the cityWeatherCardAdapter to the Recyclerview
        cityWeatherCardRecyclerView.setAdapter(cityWeatherCardAdapter);
    }

    //Sets up the citySuggestionsRecyclerView
    private void setupCitySuggestionsRecyclerView() {
        citySearchAdapter = new PlacesRecyclerAdapter(placeItemsList, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this) {
            //Disables scrolling for popup City Suggestions recyclerview
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        citySearchRecyclerView.setLayoutManager(linearLayoutManager);
        citySearchRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //Improves performance
        citySearchRecyclerView.setHasFixedSize(true);
        citySearchRecyclerView.setAdapter(citySearchAdapter);
    }

    //Onclick listener for the addCityButtons inside the city search popup menu
    @Override
    public void onPositionClicked(int position) {
        //Sets on refresh to false (e.g city is being added not refreshed)
        onRefresh = false;
        //Gets the cityName from the popupRecyclerView Item of which the add City button was clicked
        cityName = placeItemsList.get(position).getPlaceName();
        //Gets the city ID from the Google Autocomplete prediction List (Later used to get the cities photo)
        cityId = places.getPredictionList().get(position).getPlaceId();
        //Makes a request for the weather data for´the clicked on city
        runDummyRequest(cityName);
    }

    //Removes the popup Menu if the user clicks on the area around it
    private void removePopupOnClickOutside() {
        popupOutsideArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupStubInflated != null) {
                    if (!customAnimations.animationIsPlaying) {
                        popupStubInflated.startAnimation(customAnimations.scaleDecrease);
                        popupStubInflated.setVisibility(View.GONE);
                        popupStubInflated.findViewById(R.id.noItemsPopup).setVisibility(View.VISIBLE);
                        if(cityWeatherCardItems.size() > 0) {
                            noItemsTextview.setVisibility(View.INVISIBLE);
                        }
                        else {
                            noItemsTextview.setVisibility(View.VISIBLE);
                        }
                        addCityFab.show();
                        addCityFab.setClickable(true);
                        blurWrapLayout.setRenderEffect(null);
                        placeItemsList.clear();
                        places.getPredictionList().clear();
                        citySearchAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    //Removes the popup Menu if the user taps the back button
    @Override
    public void onBackPressed() {
        //Removes focus from the popup EditText
        View view = getCurrentFocus();
        if (view != null) {
            view.clearFocus();
        }
        //Only do if popup is not null and visible
        if (popupStubInflated != null && popupStubInflated.getVisibility() == View.VISIBLE) {
            if (!customAnimations.animationIsPlaying) {
                //Starts the scale Decrease animation
                popupStubInflated.startAnimation(customAnimations.scaleDecrease);
                //Makes popup invisible if no animation is playing
                if(!customAnimations.animationIsPlaying) {
                    popupStubInflated.setVisibility(View.GONE);
                }
                //Makes the text in the popup which tells users to search for a new city visible again
                popupStubInflated.findViewById(R.id.noItemsPopup).setVisibility(View.VISIBLE);
                if(cityWeatherCardItems.size() > 0) {
                    noItemsTextview.setVisibility(View.INVISIBLE);
                }
                else {
                    noItemsTextview.setVisibility(View.VISIBLE);
                }
                //Clears text input in the popup EditText so it doesn't reappear when popup is shown again
                addCityEditText.setText("");
                //Shows the addCity Fab again
                addCityFab.show();
                //Makes the addCityFab clickable again
                addCityFab.setClickable(true);
                //Removes the blur Render Effect
                blurWrapLayout.setRenderEffect(null);
                //Clears the popups search prediction results recyclerview
                placeItemsList.clear();
                places.getPredictionList().clear();
                citySearchAdapter.notifyDataSetChanged();
            }
        }

    }

    //Sets up the addCityFab and all the actions that happen when it is clicked
    private void setupAddCityFAB() {
        addCityFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Hides fab once clicked (Not using setVisibility because show & hide plays a short Material Design FAB animation)
                addCityFab.setClickable(false);
                addCityFab.hide();

                //Sets a background Blur Effect when the FAB is clicked
                blurWrapLayout.setRenderEffect(blurEffect);

                noItemsTextview.setVisibility(View.GONE);

                //Makes the popup menu visible if the popup menu has been inflated (Probably should have used a fragment instead to have more organized code ._. ).
                if (popupStubInflated == null) {
                    popupStubInflated = popupStub.inflate();
                } else {
                    popupStubInflated.setVisibility(View.VISIBLE);
                }

                //Starts the popup menu scaleIncrease animation if no animation is already playing
                if (!customAnimations.animationIsPlaying) {
                    popupStubInflated.startAnimation(customAnimations.scaleIncrease);

                    //Sets up location button and location cancel image
                    if (locationButton == null) {
                        locationButton = popupStubInflated.findViewById(R.id.locationButton);
                    }
                    if (locationCancel == null) {
                        locationCancel = popupStubInflated.findViewById(R.id.locationCancel);
                    }
                    //Requests the permission to get the users location if the location button is clicked
                    locationButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Perform haptic feedback if location button is clicked
                            locationButton.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);

                            //if location permission has not been granted already, request it
                            if (!locationPermissionWasGrantedAlready) {
                                requestPermission();
                            }
                            //if location feature is not enabled, show the GPS toast
                            if (!checkGpsStatus()) {
                                CustomToasts.gps.show();
                            }
                            /* if location permission has been granted and gps feature is enabled
                            then get the users current city and set the city to the popups EditText */
                            if (locationPermissionWasGrantedAlready && checkGpsStatus()) {
                                addCityEditText.setText(geocoder.getCurrentCity());
                            }
                        }
                    });

                    //Find the Dummy View behind the Popup in the Popup Layout to use in removePopupOnClickOutside()
                    if (popupOutsideArea == null) {
                        popupOutsideArea = popupStubInflated.findViewById(R.id.outSidePopupArea);
                    }

                    //Finds the popupCardView in the popup layout
                    if (popUpCardView == null) {
                        popUpCardView = popupStubInflated.findViewById(R.id.popupCardView);
                    }

                    //Sets up the city Search/Add RecyclerView
                    if (citySearchAdapter == null) {
                        citySearchRecyclerView = popupStubInflated.findViewById(R.id.citySuggestionsRecyclerView);
                        setupCitySuggestionsRecyclerView();
                    }

                    //Sets up the Popup EditText
                    if (!editTextHasBeenSetup) {
                        setupEditText();
                    }

                    //Removes the Popup Menu if the user taps outside of the Menu
                    if (!popupOutsideArea.hasOnClickListeners()) {
                        removePopupOnClickOutside();
                    }
                }
            }
        });
    }

    //Sets up the Popup EditText
    private void setupEditText() {
        //Finds the AutocompleteTextView in the popup Menu
        addCityEditText = popupStubInflated.findViewById(R.id.addCityEditText);

        //Sets the EditTexts visuals depending on its focus state
        addCityEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @SuppressLint("UseCompatTextViewDrawableApis")
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    addCityEditText.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.bright_gray));
                    addCityEditText.setCompoundDrawableTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.bright_gray));
                }
                if (!hasFocus) {
                    addCityEditText.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.granite_grey));
                    addCityEditText.setCompoundDrawableTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.granite_grey));

                    hideSoftKeyboard(v);
                }
            }
        });

        //Clears focus from the editText if the done button is pressed on the softkeyboard
        addCityEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addCityEditText.clearFocus();
                    popUpCardView.requestFocus();
                    return true;
                }
                return false;
            }
        });

        //Listen for text changes
        addCityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Clears the city Suggestion RecyclerView when the text changes
                if (citySearchAdapter != null) {
                    placeItemsList.clear();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //Makes city suggestions prediction request and passes the edittext text as query
                places.makePredictionsRequest(addCityEditText.getText().toString(), citySearchAdapter);
            }
        });
        editTextHasBeenSetup = true;
    }

    //Method used to request location Permission using dexter library
    //https://www.geeksforgeeks.org/easy-runtime-permissions-in-android-with-dexter/
    private void requestPermission() {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted and perform hapticFeedback
                        if (report.areAllPermissionsGranted()) {
                            locationButton.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                        }

                        // check for permanent denial of any permission and performHapticFeedback and set locationCancel Button
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            if (!locationPermissionWasGrantedAlready) {
                                locationButton.performHapticFeedback(HapticFeedbackConstants.REJECT);
                                locationCancel.setVisibility(View.VISIBLE);
                            }
                            //Shows dialog guiding the user to the apps settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        CustomToasts.error.show();
                    }
                })
                .onSameThread()
                .check();
    }

    //Method used for showing the settings dialog when the user denies the location permission request
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions required");
        builder.setMessage("The location feature needs permission to use your current location. You can grant them in the settings.");
        builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    //Navigates the user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    //Checks if GPS feature is enabled
    public boolean checkGpsStatus() {
        LocationManager locationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    /**
     * This method makes a call to the OpenWeatherMapAPI for the current weather data for the passed in location.
     * The received data is then displayed int the UI.
     */
    public void runDummyRequest(String city) {
        // Initialization of a request for the current weather of the passed in city
        WeatherDataRequest dataRequest = new WeatherDataRequest(city, this, this);
        // Makes the request
        //dataRequest.run(MainActivity.this);

        //Runs request in new Thread so that MainThread doesn't get stuck
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                dataRequest.syncCall(MainActivity.this);
            }
        });
        thread.start();

        Log.d("ranExecuted", "Request executed!");
    }

    //Utilizes the weather data once received
    @Override
    public void onResult(WeatherData data) throws InterruptedException {
        String cleanedCityNameGoogleApi;
        String cleanedCityNameWeatherApi;

        //Checks if the weatherData is being retrieved on refresh or not
        if (!onRefresh) {
            /*Cleans both the google Autocomplete City name and weather
            data city name from special characters and whitespace
            (This is done to check if the weatherData is available, a better way
            would probably be to check with a list of cities of which weather data is available
            ,like here: https://bulk.openweathermap.org/sample/)*/
            cleanedCityNameGoogleApi = cleanString(cityName);
            cleanedCityNameWeatherApi = cleanString(data.city);

            //If both cities names match then add the weatherDataCard
            if (cleanedCityNameGoogleApi.matches(cleanedCityNameWeatherApi)) {
                String description = data.description;
                //Remove decimal from temperature
                String temperature = data.temperature.split("\\.")[0];
                //Add city weather card to city weather card recyclerview adapter array
                cityWeatherCardItems.add(new CityWeatherCardItem(this.cityName, description, temperature));
                //Get index of the cityweathercard that was just added by matching data (Probably not a good way to do this.)
                int addedItemArrayPosition = 0;
                for (CityWeatherCardItem cityWeatherCardItem : cityWeatherCardItems) {
                    if (cityWeatherCardItem.getCityName().matches(cityName) && cityWeatherCardItem.getDescription().matches(description) && cityWeatherCardItem.getTemperature().matches(temperature)) {
                        addedItemArrayPosition = cityWeatherCardItems.indexOf(cityWeatherCardItem);
                    }
                }
                //Get the photo for the city weather card that was just added
                places.getPlacePhoto(cityId, cityWeatherCardItems.get(addedItemArrayPosition), addedItemArrayPosition);
            } else {
                //If the cities names don't match then show a "no weather data available" toast
                CustomToasts.noWeatherDataAvailable.show();
            }

            //Checks if the weatherData is being retrieved on refresh or not
        } else if (onRefresh) {
            //Sets the new data for every cityWeatherCard
            for (int i = 0; i < cityWeatherCardItems.size(); i++) {
                if (cityWeatherCardItems.get(i).getCityName().matches(data.city)) {
                    cityWeatherCardItems.get(i).setDescription(data.description);
                    cityWeatherCardItems.get(i).setTemperature(data.temperature.split("\\.")[0]);

                    int finalI = i;
                    final CountDownLatch latch = new CountDownLatch(1);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Sets the cityImage again after the view has been recycled
                            currentRefreshBitmap = cityWeatherCardItems.get(finalI).getCityImage();
                            cityWeatherCardItems.get(finalI).setCityImage(currentRefreshBitmap);
                            cityWeatherCardAdapter.notifyItemChanged(finalI);

                            latch.countDown();
                        }
                    });
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        //Errors while setting images again get handled here
                        e.printStackTrace();
                        CustomToasts.error.show();
                    }
                }
            }
            //Ends the swipeRefreshLayout refresh animation
            swipeRefreshLayout.setRefreshing(false);
            //Show the toast telling the user that the data has been refreshed
            CustomToasts.refreshed.show();
        }
    }

    //The onClickedListener for the cityWeatherCardItems used for deleting cityWeatherCards
    @Override
    public boolean onItemLongClicked(int position) {
        //Removes the cityWeatherCardItem at the passed in position from the recyclerview
        cityWeatherCardItems.remove(position);
        cityWeatherCardAdapter.notifyItemRemoved(position);
        /*If after removing there are no more cards added
        then show the text telling the user to add a new city*/
        if(cityWeatherCardItems.size() == 0) {
            noItemsTextview.setVisibility(View.VISIBLE);
        }
        return true;
    }

    //Method used to hide the soft Keyboard
    private void hideSoftKeyboard(View v) {
        InputMethodManager inputManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //Method used to clean a string from any special characters and whitespace
    private String cleanString(String string) {
        return string.toLowerCase().replaceAll("/[^A-Z0-9]/ig", "");
    }

    //Getters
    public EditText getAddCityEditText() {
        return addCityEditText;
    }

    public ArrayList<PlaceItem> getPlaceItemsList() {
        return placeItemsList;
    }
}