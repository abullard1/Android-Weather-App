package de.ur.mi.android.wetter.Geocoding;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.ur.mi.android.wetter.MainActivity;
import de.ur.mi.android.wetter.UI.CustomToasts;

/**
 * This class is used to get the users current location (city) by using the built in Geocoder
 * (FOR SOME REASON MULTIPLE CLICKS ON THE LOCATION BUTTON ARE NEEDED AFTER FIRST OPENING THE APP AND
 * DIDN'T WORK ON EMULATOR FOR SOME REASON)
 **/
public class Geocoding {

    String result = "";
    Task<Location> currentLocation;
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private double lat;
    private double lng;
    private final MainActivity mainActivity;

    public Geocoding(MainActivity mainActivity) {
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mainActivity);
        this.mainActivity = mainActivity;
    }

    /**
     * Gets the users Latitude and Longitude
     **/
    //Suppressing permission check since permissions are already checked in MainActivity
    @SuppressLint("MissingPermission")
    public String getCurrentCity() {
        //Setting up request (All values are the default values)
        //https://developers.google.com/android/reference/com/google/android/gms/location/CurrentLocationRequest.Builder
        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setDurationMillis(Long.MAX_VALUE)
                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                .setMaxUpdateAgeMillis(60000)
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .build();

        //Getting the users current location
        currentLocation = fusedLocationProviderClient.getCurrentLocation(request, new CancellationToken() {
            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }

            @Override
            public boolean isCancellationRequested() {
                return false;
            }
        });

        //Listens for location and stores users longitude and latitude into variables
        currentLocation.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    lat = location.getLatitude();
                    lng = location.getLongitude();

                    handleLatLng();

                } else {
                    CustomToasts.error.show();
                }
            }
        });

        //Listeners listens for Failure while getting location
        currentLocation.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Errors while geocoding get handled here
                e.printStackTrace();
                CustomToasts.error.show();
            }
        });

        return result;
    }

    /**
     * This class gets the users current City by handling his Latitude and Longitude
     **/
    //https://stackoverflow.com/questions/43862079/how-to-get-city-name-using-latitude-and-longitude-in-android
    private void handleLatLng() {
        //Initialize Geocoder and setting result language
        Geocoder geocoder = new Geocoder(mainActivity.getApplicationContext(), Locale.ENGLISH);

        //List for storing address components into
        List<Address> addresses = null;
        try {
            //Tries to gather most accurate address components
            addresses = geocoder.getFromLocation(lat, lng, 1);
        } catch (IOException e) {
            //Errors while geocoding get handled here
            e.printStackTrace();
            CustomToasts.error.show();
        }

        /*
        Checking for availability of closest thing to locality if locality is not
        available since locality is not available or the same in all countries.
        Checks for most relevant/accurate types first.
        */
        if (addresses != null && addresses.size() > 0) {
            if (addresses.get(0).getLocality() != null) {
                result = addresses.get(0).getLocality();
            } else if (addresses.get(0).getAdminArea() != null) {
                result = addresses.get(0).getAdminArea();
            } else if (addresses.get(0).getSubLocality() != null) {
                result = addresses.get(0).getSubLocality();
            } else if (addresses.get(0).getSubAdminArea() != null) {
                result = addresses.get(0).getSubAdminArea();
            } else if (addresses.get(0).getLocale() != null) {
                result = addresses.get(0).getLocale().toString();
            }
        }
    }
}