package de.ur.mi.android.wetter.GooglePlacesAPI;

//Represents a GooglePlaces AutocompletePrediction API prediction result
public class PlaceItem {
    private final String placeName;

    public PlaceItem(String placeName) {
        this.placeName = placeName;
    }

    public String getPlaceName() {
        return placeName;
    }
}
