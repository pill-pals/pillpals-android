package com.pillpals.pillpals.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.webkit.WebView
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.pillpals.pillpals.R

class SettingsFragment: PreferenceFragmentCompat() {
    private lateinit var privacyPolicy: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        privacyPolicy = findPreference("privacy_policy")
        privacyPolicy.setOnPreferenceClickListener {
            var webView = WebView(context)
            webView.loadUrl("file:///android_asset/privacy_policy.html")

            var dialogBuilder = AlertDialog.Builder(context)
            dialogBuilder.setTitle("PillPals Privacy Policy")
                .setView(webView)
                .setNeutralButton("Close") { dialog, which ->
                    //Do nothing but close
                }
                .show()

            true
        }
    }
}
