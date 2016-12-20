package octacode.allblue.code.sunshine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import octacode.allblue.code.sunshine.data.WeatherContract;
import octacode.allblue.code.sunshine.data.WeatherContract.WeatherEntry;
import octacode.allblue.code.sunshine.data.Weatherdb;


public class DetailFragment extends Fragment{

    private TextView date_tv,max_tv,min_tv,desc_tv,humidity_tv,wind_speed_tv,pressure_tv,day_tv;
    private ImageView icon_iv;



    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final String[] data_fetched=getActivity().getIntent().getStringArrayExtra(Intent.EXTRA_TEXT);

        String date=data_fetched[0];
        String desc=data_fetched[1];
        String min=data_fetched[2];
        min=min+"°";
        String max=data_fetched[3];
        max=max+"°";
        float degree= Float.parseFloat(data_fetched[4]);
        float wind_speed= Float.parseFloat(data_fetched[5]);
        float humidity= Float.parseFloat(data_fetched[6]);
        float pressure= Float.parseFloat(data_fetched[7]);
        int weather_condition_id= Integer.parseInt(data_fetched[8]);

        View rootview = inflater.inflate(R.layout.fragment_detail, container, false);

        date_tv=(TextView)rootview.findViewById(R.id.detail_item_date);
        date_tv.setText(Utility.formatDate(Long.parseLong(date)*1000));
        day_tv=(TextView)rootview.findViewById(R.id.detail_item_day);
        max_tv=(TextView)rootview.findViewById(R.id.detail_item_max);
        max_tv.setText(max);
        min_tv=(TextView)rootview.findViewById(R.id.detail_item_min);
        min_tv.setText(min);
        desc_tv=(TextView)rootview.findViewById(R.id.detail_item_forecast);
        desc_tv.setText(desc);
        humidity_tv=(TextView)rootview.findViewById(R.id.detail_item_humidity);
        humidity_tv.setText("Humidity: "+humidity+"%");
        wind_speed_tv=(TextView)rootview.findViewById(R.id.detail_item_wind_speed);
        wind_speed_tv.setText(Utility.getFormattedWind(getContext(),wind_speed,degree));
        pressure_tv=(TextView)rootview.findViewById(R.id.detail_item_pressure);
        pressure_tv.setText("Pressure: "+pressure+"hPa");
        icon_iv=(ImageView)rootview.findViewById(R.id.detail_item_icon);
        icon_iv.setImageResource(Utility.getArtResourceForWeatherCondition(weather_condition_id));
        return rootview;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detailfragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_settings:
                startActivity(new Intent(getContext(),SettingsActivity.class));
                return true;
            case R.id.action_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, desc_tv.getText().toString()+"#Sunshine");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}
