package de.ur.mi.android.wetter.UI;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import de.ur.mi.android.wetter.R;
import es.dmoral.toasty.Toasty;

/**Class used for custom Toasts**/
//https://github.com/GrenderG/Toasty
public abstract class CustomToasts {
    public static Toast noWeatherDataAvailable;
    public static Toast error;
    public static Toast gps;
    public static Toast refreshed;

    //Sets up the Toast
    public static void setupToasts(Context context) {
        Toasty.Config.getInstance()
                .tintIcon(true) //Tints the icon to the color of the text
                .setTextSize(16) //Sets the textSize
                .allowQueue(false) //Disables multiple toasts getting shown directly after another
                .supportDarkTheme(true)
                .setRTL(false) //Sets RTL Layout to false
                .apply();

        //Icon used for when weather data of a city could not be retrieved
        noWeatherDataAvailable = Toasty.custom(context, getFormattedToastMessage(context.getString(R.string.no_weather_data_found), context), R.drawable.ic_weather, R.color.granite_grey,
                Toasty.LENGTH_SHORT, true, true);
        noWeatherDataAvailable.setGravity(Gravity.TOP, 0, 150);

        //Icon used for when an error occures
        error = Toasty.custom(context, getFormattedToastMessage(context.getString(R.string.error), context), R.drawable.ic_error, R.color.granite_grey,
                Toasty.LENGTH_SHORT, true, true);
        error.setGravity(Gravity.TOP, 0, 150);

        //Icon used fot when GPS feature is not enabled
        gps = Toasty.custom(context, getFormattedToastMessage(context.getString(R.string.gps), context), R.drawable.ic_gps_off, R.color.granite_grey,
                Toasty.LENGTH_SHORT, true, true);
        gps.setGravity(Gravity.TOP, 0, 150);

        //Icon used for when the user refreshes the data by swiping down (SwipeRefreshLayout)
        refreshed = Toasty.custom(context, getFormattedToastMessage(context.getString(R.string.refreshed), context), R.drawable.ic_refresh, R.color.granite_grey,
                Toasty.LENGTH_SHORT, true, true);
        refreshed.setGravity(Gravity.TOP, 0, 150);
    }

    //Used for setting the toasts text color and font
    private static CharSequence getFormattedToastMessage(String message, Context context) {
        final String noResult = message;
        Typeface typeface = ResourcesCompat.getFont(context, R.font.chivo);
        SpannableStringBuilder ssb = new SpannableStringBuilder(noResult);
        ssb.setSpan(new CustomTypefaceSpan("", typeface), 0, noResult.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        ssb.setSpan(new ForegroundColorSpan(context.getColor(R.color.bright_gray)), 0, noResult.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        return ssb;
    }
}