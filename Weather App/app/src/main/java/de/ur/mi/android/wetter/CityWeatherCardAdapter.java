package de.ur.mi.android.wetter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.matteobattilana.weather.PrecipType;
import com.github.matteobattilana.weather.WeatherView;

import java.util.ArrayList;

//https://www.youtube.com/watch?v=bhhs4bwYyhc&ab_channel=CodinginFlow
/**Adapter Class used to populate the CityCard RecyclerView**/
public class CityWeatherCardAdapter extends RecyclerView.Adapter<CityWeatherCardAdapter.CityWeatherCardViewHolder> {
    private final ArrayList<CityWeatherCardItem> cityWeatherCardItems;
    private final MainActivity mMainActivity;

    public CityWeatherCardAdapter(ArrayList<CityWeatherCardItem> cityWeatherCardItems, MainActivity mainActivity) {
        this.cityWeatherCardItems = cityWeatherCardItems;
        mMainActivity = mainActivity;
    }

    @NonNull
    @Override
    public CityWeatherCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.city_card, parent, false);
        CityWeatherCardViewHolder cityWeatherCardViewHolder = new CityWeatherCardViewHolder(v);
        return cityWeatherCardViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CityWeatherCardViewHolder holder, int position) {
        CityWeatherCardItem cityWeatherCardItem = cityWeatherCardItems.get(position);

        //Sets cityName (setSelected(true) is needed for marquee to animate)
        holder.cityName.setText(cityWeatherCardItem.getCityName());
        holder.cityName.setSelected(true);

        //Sets the city weather description (e.g. Sunn, Rainy etc...)
        holder.description.setText(cityWeatherCardItem.getDescription());
        holder.description.setSelected(true);

        //Sets the city temperature
        holder.temperature.setText(cityWeatherCardItem.getTemperature());

        //Sets the cities image
        holder.cityImage.setImageBitmap(cityWeatherCardItem.getCityImage());

        //Changes the weather of the weather view according to the weather description
        changeWeather(filterDescription(holder.description.getText().toString()), holder);
        /*
        Causes performance issues when refreshing data but if not set to true
        the weather animation and image will disappear once the item goes of screen.
        Gets set corresponding to the onRefresh Boolean in main so that the items can get Recycled
        when they are being updated to the new Text and so on.
        */
        holder.setIsRecyclable(mMainActivity.onRefresh);

        //Sets a long Click listener on the the recyclerview item so they can be removed on long click
        holder.touchOverlayDummy.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition();
                //Checks for NO_POSITION so that array indexes are consistent
                if (position != RecyclerView.NO_POSITION) {
                    //sets recyclable to true so that the items can be removed
                    holder.setIsRecyclable(true);
                    //Passes the position to the listener in MainActivity
                    mMainActivity.onItemLongClicked(position);
                    //Returns true to notify that this longLickListener is responsible for click
                    return true;
                }
                return false;
            }
        });
    }

    public static class CityWeatherCardViewHolder extends RecyclerView.ViewHolder {
        public View v;
        TextView cityName;
        TextView description;
        TextView temperature;
        ImageView cityImage;
        ImageView touchOverlayDummy;
        WeatherView weatherView;

        public CityWeatherCardViewHolder(View itemView) {
            super(itemView);
            this.v = itemView;

            //Finds the views in the layout
            cityName = itemView.findViewById(R.id.cardCityName);
            description = itemView.findViewById(R.id.cardWeatherDescription);
            temperature = itemView.findViewById(R.id.cardCityTemperature);
            cityImage = itemView.findViewById(R.id.cardCityImage);
            touchOverlayDummy = itemView.findViewById(R.id.touchOverlayDummy);
            weatherView = itemView.findViewById(R.id.cardWeatherView);
        }
    }

    @Override
    public int getItemCount() {
        return cityWeatherCardItems.size();
    }

    //Method to change the weather of the WeatherView according to the passed in WeatherType
    private void changeWeather(String weatherType, CityWeatherCardViewHolder holder) {
        int weatherSpeed = 0;
        int weatherParticles = 0;
        PrecipType weather = null;
        int angle = 5;

        if (weatherType.matches("Sunny")) {
            weather = PrecipType.CLEAR;
        } else if (weatherType.matches("Rainy")) {
            weather = PrecipType.RAIN;
            weatherParticles = 60;
            weatherSpeed = 700;
            angle = 30;
        } else if (weatherType.matches("Heavy Rain")) {
            weather = PrecipType.RAIN;
            weatherParticles = 100;
            weatherSpeed = 800;
            angle = 40;
        } else if (weatherType.matches("Light Rain") || weatherType.matches("Drizzle")) {
            weather = PrecipType.RAIN;
            weatherParticles = 30;
            weatherSpeed = 600;
            angle = 20;
        } else if (weatherType.matches("Snowy")) {
            weather = PrecipType.SNOW;
            weatherParticles = 25;
            weatherSpeed = 200;
        }

        //Sets the precipitation Type (e.g. Snow, Rain etc...)
        holder.weatherView.setPrecipType(weather);
        //Sets particle speed
        holder.weatherView.setSpeed(weatherSpeed);
        //Sets particle emission rate
        holder.weatherView.setEmissionRate(weatherParticles);
        //Sets the percentage of where on the screen the particles should fade out
        holder.weatherView.setFadeOutPercent(0.95f);
        //Sets the angle of the particles trajectory
        holder.weatherView.setAngle(angle);
    }

    //Filters the weather Description Strings
    private String filterDescription(String description) {
        if (description.toLowerCase().contains("snow")) {
            return "Snowy";
        } else if (description.toLowerCase().contains("light rain")) {
            return "Light Rain";
        } else if (description.toLowerCase().contains("heavy rain")) {
            return "Heavy Rain";
        } else if (description.toLowerCase().contains("rain")) {
            return "Rainy";
        } else if (description.toLowerCase().contains("drizzle")) {
            return "Drizzle";
        }
        return "Sunny";
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClicked(int position);
    }
}
