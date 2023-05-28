package de.ur.mi.android.wetter;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import java.util.ArrayList;

import de.ur.mi.android.wetter.GooglePlacesAPI.PlaceItem;

/**Adapter class used to populate the popup menus city Search RecyclerView**/
//https://stackoverflow.com/questions/30284067/handle-button-click-inside-a-row-in-recyclerview
public class PlacesRecyclerAdapter extends RecyclerView.Adapter<PlacesRecyclerAdapter.PlacesViewHolder> {

    private final ClickListener listener;
    private final ArrayList<PlaceItem> placeItemsList;

    //Limit of shown items
    private final int maxItemLimit = 4;

    public PlacesRecyclerAdapter(ArrayList<PlaceItem> placeItemList, ClickListener listener) {
        this.placeItemsList = placeItemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlacesRecyclerAdapter.PlacesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_item, parent, false);
        PlacesViewHolder placesViewHolder = new PlacesViewHolder(itemView, listener);

        return placesViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PlacesRecyclerAdapter.PlacesViewHolder holder, int position) {
        PlaceItem currentPlaceItem = placeItemsList.get(position);
        //Sets the text to the cities name
        holder.placeText.setText(currentPlaceItem.getPlaceName());
        //Set selected needs to be true for marquee to animate
        holder.placeText.setSelected(true);

        //Resets the button visibilities on bind
        if (holder.addCityButton.getVisibility() == View.GONE) {
            holder.addCityButton.setVisibility(View.VISIBLE);
            holder.addCityCheckMark.setVisibility(View.GONE);
        }

        //Sets up the Checkmark animation that is shown when the user adds a new city
        Drawable checkMark = holder.addCityCheckMark.getDrawable();
        AnimatedVectorDrawableCompat avd;
        AnimatedVectorDrawable avd2;

        for (CityWeatherCardItem cityWeatherCardItem : MainActivity.cityWeatherCardItems) {
            /*If the name of the city in the popup recyclerview already matches one of the added cities,
            make the button invisible and the checkmark visible.*/
            if (cityWeatherCardItem.getCityName().equals(holder.placeText.getText().toString())) {
                holder.addCityButton.setVisibility(View.GONE);
                holder.addCityCheckMark.setVisibility(View.VISIBLE);

                //Sets and starts the checkmark vector animation if city already added
                if (checkMark instanceof AnimatedVectorDrawableCompat) {
                    avd = (AnimatedVectorDrawableCompat) checkMark;
                    avd.start();
                } else if (checkMark instanceof AnimatedVectorDrawable) {
                    avd2 = (AnimatedVectorDrawable) checkMark;
                    avd2.start();
                }
            }
        }
    }

    public static class PlacesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView placeText;
        public ImageButton addCityButton;
        public ImageView addCityCheckMark;
        private final ClickListener listener;
        private final Drawable checkMark;

        public PlacesViewHolder(View view, ClickListener listener) {
            super(view);
            this.listener = listener;

            //Finds the views in the layout
            placeText = view.findViewById(R.id.cityText);
            addCityButton = view.findViewById(R.id.addCityButton);
            addCityCheckMark = view.findViewById(R.id.addCityCheckMark);

            checkMark = addCityCheckMark.getDrawable();

            //Sets a Click Listener on the Add City Plus button
            addCityButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //Gets items adapter position
            int position = getAdapterPosition();
            //Checks for NO_POSITION so that array indexes are consistent
            if (position != RecyclerView.NO_POSITION) {
                //Passes click to listener in MainActivity
                listener.onPositionClicked(getAdapterPosition());
                //Performs light haptic feedback on click
                addCityButton.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);

                //Delay until checkMark Animation plays so that the addCityButton StateAnimation has time to play
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    private AnimatedVectorDrawableCompat avd;
                    private AnimatedVectorDrawable avd2;

                    @Override
                    public void run() {
                        //If clicked makes the addCity Plus Button to invisible and checkmark visible
                        addCityButton.setVisibility(View.GONE);
                        addCityCheckMark.setVisibility(View.VISIBLE);

                        //Sets and starts the checkmark vector animation
                        if (checkMark instanceof AnimatedVectorDrawableCompat) {
                            avd = (AnimatedVectorDrawableCompat) checkMark;
                            avd.start();
                        } else if (checkMark instanceof AnimatedVectorDrawable) {
                            avd2 = (AnimatedVectorDrawable) checkMark;
                            avd2.start();
                        }
                    }
                }, 130); //130 ms delay corresponds to animation addCityButton state animation duration
            }
        }
    }

    @Override
    public int getItemCount() {
        //Sets limit on how many items can be returned
        if (placeItemsList.size() > maxItemLimit) {
            return maxItemLimit;
        } else {
            return placeItemsList.size();
        }
    }

    public interface ClickListener {
        void onPositionClicked(int position);
    }
}
