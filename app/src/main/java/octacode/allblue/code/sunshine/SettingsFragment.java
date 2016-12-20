package octacode.allblue.code.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import octacode.allblue.code.sunshine.data.WeatherContract;



public class SettingsFragment extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener{

    public SettingsFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_settings);
        bindPreferenceSummaryToValue(findPreference(getString(R.string.ZIP_pref)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.UNIT_pref)));
    }


    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(this);
        onPreferenceChange(preference,
                PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(),""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String string_Value=newValue.toString();

        if(preference.getKey().equals(getString(R.string.ZIP_pref))){
            String location=newValue.toString();
        }
        else{
            getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI,null);
        }
        if(preference instanceof ListPreference){
            ListPreference listPreference=(ListPreference)preference;
            int prefIndex=listPreference.findIndexOfValue(string_Value);
            if(prefIndex>=0)
                preference.setSummary(listPreference.getEntries()[prefIndex]);
        }
        else{
            preference.setSummary(string_Value);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this,MainActivity.class));
    }
}