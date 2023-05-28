package de.ur.mi.android.wetter.UI;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import de.ur.mi.android.wetter.MainActivity;
import de.ur.mi.android.wetter.R;

/**Class used for custom made Animations**/
public class CustomAnimations {
    private final MainActivity mainActivity;
    public Animation scaleIncrease;
    public Animation scaleDecrease;
    public Animation alphaIncrease;
    public Animation alphaDecrease;
    public Boolean animationIsPlaying = false;

    public CustomAnimations(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void setupHideShowAnimations() {
        //Setup of scaleIncrease Animation
        scaleIncrease = AnimationUtils.loadAnimation(mainActivity, R.anim.scale_increase);
        scaleIncrease.setInterpolator(new OvershootInterpolator(1.7f));
        scaleIncrease.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animationIsPlaying = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animationIsPlaying = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        //Setup of scaleDecrease Animation
        scaleDecrease = AnimationUtils.loadAnimation(mainActivity, R.anim.scale_decrease);
        scaleDecrease.setInterpolator(new AccelerateInterpolator());
        scaleDecrease.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animationIsPlaying = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //Clears text so it doesnt reappear when popup is shown again
                mainActivity.getAddCityEditText().setText("");
                animationIsPlaying = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        //Setup of alphaIncrease Animation
        alphaIncrease = AnimationUtils.loadAnimation(mainActivity, R.anim.alpha_increase);
        alphaIncrease.setInterpolator(new LinearInterpolator());
        alphaIncrease.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animationIsPlaying = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                animationIsPlaying = false;
            }
        });

        //Setup of alphaDecrease Animation
        alphaDecrease = AnimationUtils.loadAnimation(mainActivity, R.anim.alpha_decrease);
        alphaDecrease.setInterpolator(new LinearInterpolator());
        alphaDecrease.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animationIsPlaying = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                animationIsPlaying = false;
            }
        });
    }
}