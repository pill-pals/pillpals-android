package com.pillpals.pillbuddies.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.pillpals.pillbuddies.R

class SettingsFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}