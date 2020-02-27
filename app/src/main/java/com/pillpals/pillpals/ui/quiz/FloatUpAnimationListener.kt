package com.pillpals.pillpals.ui.quiz

import android.view.View
import android.view.animation.Animation

class FloatUpAnimationListener: Animation.AnimationListener {
    var mView: View;
    constructor(view: View) {
        mView = view;
    }

    override fun onAnimationStart(animation: Animation) {

    }

    override fun onAnimationEnd(animation: Animation ) {
        mView.setVisibility(View.GONE)
    }

    override fun onAnimationRepeat(animation: Animation) {

    }
}