package com.pillpals.pillpals.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.pillpals.pillpals.R

class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}