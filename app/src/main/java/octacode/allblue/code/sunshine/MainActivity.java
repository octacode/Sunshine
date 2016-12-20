package octacode.allblue.code.sunshine;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements ForecastFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_in_main,new ForecastFragment()).commit();
    }

    public void themeApplier(){
        SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getString("theme","0").matches("1")) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(R.color.SunshineDark));
            getSupportActionBar().setLogo(R.drawable.ic_logo);
        }
    }

    public void action_name(String title){
        getSupportActionBar().setTitle(title);
    }
}
