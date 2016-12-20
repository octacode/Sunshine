package octacode.allblue.code.sunshine;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
public class DetailActivity extends AppCompatActivity implements DetailFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_in_detail,new DetailFragment()).commit();
    }

    public void themeApplier(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getString("theme","0").matches("1")) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(R.color.SunshineDark));
            getSupportActionBar().setLogo(R.drawable.ic_logo);
        }
    }
    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
