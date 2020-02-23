package com.pillpals.pillpals.ui.medications.medication_info

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout

import com.pillpals.pillpals.R
import io.realm.Realm

class MedicationInfoTextFragment : Fragment() {

    public lateinit var layout: LinearLayout
    lateinit var loading: ImageView
    lateinit var loadingAnimation: RotateAnimation

    private lateinit var realm: Realm

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_medication_info_text, container,false)

        layout = view!!.findViewById(R.id.textLayout)
        loading = view.findViewById(R.id.loading)
        loading.visibility = View.GONE

        loadingAnimation = RotateAnimation(0f, 360f, 55f, 55f)
        loadingAnimation.interpolator = LinearInterpolator()
        loadingAnimation.repeatCount = Animation.INFINITE
        loadingAnimation.duration = 700

        realm = Realm.getDefaultInstance()

        return view
    }

    // in the fragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MedicationInfoActivity).tabFragmentLoaded()
    }

    fun setTabLoading(loadingBool: Boolean) {
        if(loadingBool) {
            loading.startAnimation(loadingAnimation)
            loading.setImageResource(R.drawable.loader)
            loading.visibility = View.VISIBLE
        }
        else {
            loading.visibility = View.INVISIBLE
            loading.setImageDrawable(null)
            loading.startAnimation(loadingAnimation)
        }
    }
}
