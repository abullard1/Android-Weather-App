package de.ur.mi.android.wetter.UI;

import android.app.Activity;
import android.view.WindowManager;

/**Class used for setting the translucency of the navigation and status bar**/
public class TranslucentFullscreenManager {

    ///Method used for setting the translucency of the navigation and status bar
    public static void makeStatusBarTranslucent(Activity activity) {
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }
}