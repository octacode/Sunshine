package octacode.allblue.code.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.prefs.Preferences;

import octacode.allblue.code.sunshine.data.WeatherContract;
import octacode.allblue.code.sunshine.data.WeatherContract.WeatherEntry;
import octacode.allblue.code.sunshine.data.Weatherdb;


public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private String mlocation;
    private static int FORECAST_LOADER=0;
    private static final String[] FORECAST_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE_TEXT,
            WeatherEntry.COLUMN_DESC_TEXT,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherEntry.COLUMN_DEGREE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE
    };

    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_CONDITION_ID = 5;
    public static final int COL_LOCATION_SETTING = 6;
    public static final int COL_WEATHER_DEGREE = 7;
    public static final int COL_WEATHER_WIND_SPEED = 8;
    public static final int COL_WEATHER_HUMIDITY = 9;
    public static final int COL_WEATHER_PRESSURE = 10;

    private OnFragmentInteractionListener mListener;

    public ForecastFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(FORECAST_LOADER,null,this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(getActivity(),SettingsActivity.class));
                return true;
            case R.id.action_location:
                /*
                Intent intent=null, chooser=null;
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("geo:"+fetchWeatherTask.lat+","+fetchWeatherTask.lon));
                chooser = Intent.createChooser(intent,"Launch Maps");
                startActivity(chooser);
                */
                updateweather();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    FetchWeatherTask fetchWeatherTask;

    private void updateweather() {
        String data=Utility.getPreferredLocation(getContext());
        fetchWeatherTask=new FetchWeatherTask(getContext());
        fetchWeatherTask.execute(data);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateweather();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mlocation != null && !Utility.getPreferredLocation(getContext()).equals(mlocation)){
            getLoaderManager().restartLoader(FORECAST_LOADER,null,this);
            updateweather();
        }
    }

    ForecastAdapter aAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        aAdapter=new ForecastAdapter(getContext(),null,0);
        View rootview = inflater.inflate(R.layout.fragment_main, container, false);

        final ListView lv = (ListView) rootview.findViewById(R.id.list_view_forecast);
        lv.setAdapter(aAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ForecastAdapter adapter=(ForecastAdapter)parent.getAdapter();
                Cursor cursor=adapter.getCursor();
                int weather_condition_id=cursor.getInt(COL_WEATHER_CONDITION_ID);
                String date=cursor.getString(COL_WEATHER_DATE);
                String desc=cursor.getString(COL_WEATHER_DESC);
                double max=cursor.getDouble(COL_WEATHER_MAX_TEMP);
                double min=cursor.getDouble(COL_WEATHER_MIN_TEMP);
                double degree=cursor.getDouble(COL_WEATHER_DEGREE);
                double wind_speed=cursor.getDouble(COL_WEATHER_WIND_SPEED);
                double humidity=cursor.getDouble(COL_WEATHER_HUMIDITY);
                double pressure=cursor.getDouble(COL_WEATHER_PRESSURE);

                Intent i=new Intent(getContext(),DetailActivity.class);
                String[] data_transfer={
                        date,
                        desc,
                        String.valueOf(min),
                        String.valueOf(max),
                        String.valueOf(degree),
                        String.valueOf(wind_speed),
                        String.valueOf(humidity),
                        String.valueOf(pressure),
                        String.valueOf(weather_condition_id)
                };
                i.putExtra(Intent.EXTRA_TEXT,data_transfer);
                startActivity(i);
            }
        });
        return rootview;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortOrder = WeatherEntry.COLUMN_DATE_TEXT + " ASC";

        mlocation = Utility.getPreferredLocation(getActivity());
        Uri weatherLocationUri = WeatherEntry.buildWeatherLocation(mlocation);

        return new CursorLoader(
                getActivity(),
                weatherLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        aAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        aAdapter.swapCursor(null);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

}
