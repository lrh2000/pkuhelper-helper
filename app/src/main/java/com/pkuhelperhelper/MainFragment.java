package com.pkuhelperhelper;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "PH-Helper";

    private HashMap<String, Object> preferences = new HashMap<>();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Objects.requireNonNull(findPreference("long_click_to_copy")).setOnPreferenceChangeListener(this);
        Objects.requireNonNull(findPreference("long_click_to_save_picture")).setOnPreferenceChangeListener(this);

        preferences.putAll(getPreferenceManager().getSharedPreferences().getAll());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Object oldValue = preferences.get(preference.getKey());
        preferences.put(preference.getKey(), newValue);

        try {
            File dir = Objects.requireNonNull(getContext()).getExternalFilesDir(null);
            File file = new File(dir, "pkuhelperhelper.conf");
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));

            oos.writeObject(preferences);
            oos.close();

            Process proc = Runtime.getRuntime().exec(
                    "su -c mv pkuhelperhelper.conf ../../com.pkuhelper.beta/files/",
                    null, dir);
            if (!proc.waitFor(10, TimeUnit.SECONDS) || proc.exitValue() != 0) {
                throw new Throwable("Commands to copy pkuhelperhelper.conf end with " +
                        "non-zero return code " + Integer.valueOf(proc.exitValue()).toString() + " .");
            }
        } catch (Throwable e) {
            Log.e(TAG, "Failed to apply the preference change.", e);
            Toast.makeText(getActivity(), "Failed to apply the preference change.", Toast.LENGTH_LONG).show();
            preferences.put(preference.getKey(), oldValue);
            return false;
        }

        return true;
    }
}
