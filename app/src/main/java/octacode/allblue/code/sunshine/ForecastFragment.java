package octacode.allblue.code.sunshine;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import octacode.allblue.code.sunshine.data.WeatherContract;
import octacode.allblue.code.sunshine.data.WeatherContract.LocationEntry;
import octacode.allblue.code.sunshine.data.WeatherContract.WeatherEntry;
import octacode.allblue.code.sunshine.sync.SunshineSyncAdapter;


public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public interface titleUpdateEventListener{
        public void updateEvent(String name);
    }

    private String mlocation;
    private titleUpdateEventListener titleUpdateEventListener;

    public SwipeRefreshLayout swipeRefreshLayout;
    private static int FORECAST_LOADER=0;
    private static final String[] FORECAST_COLUMNS = {
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATE_TEXT,
            WeatherEntry.COLUMN_DESC_TEXT,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_WEATHER_ID,
            LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherEntry.COLUMN_DEGREE,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            LocationEntry.COLUMN_LATITUDE,
            LocationEntry.COLUMN_LONGITUDE,
            LocationEntry.COLOUMN_CITY_NAME
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
    public static final int COL_LOCATION_LATITUDE=11;
    public static final int COL_LOCATION_LONGITUDE=12;
    public static final int COL_LOCATION_CITY_NAME=13;

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
        Uri weatherLocationUri = WeatherEntry.buildWeatherLocation(Utility.getPreferredLocation(getContext()));
        Cursor cursor=getContext().getContentResolver().query(
                weatherLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                null);
        String city_name= "";
        assert cursor != null;
        while (cursor.moveToNext()){
            city_name=cursor.getString(COL_LOCATION_CITY_NAME);
        }
        this.city_name=city_name;
        swipeRefreshLayout=(SwipeRefreshLayout)getActivity().findViewById(R.id.swipe);
    }

    String city_name;

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
                Cursor cursor=aAdapter.getCursor();
                double menu_lat=cursor.getDouble(COL_LOCATION_LATITUDE);
                double menu_lon=cursor.getDouble(COL_LOCATION_LONGITUDE);
                Intent intent,chooser;
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("geo:"+menu_lat+","+menu_lon));
                chooser = Intent.createChooser(intent,"Your Location");
                startActivity(chooser);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void updateweather() {
        SunshineSyncAdapter.syncImmediately(getActivity());
        ((MainActivity)getActivity()).getSupportActionBar().setTitle(city_name);
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
        }
        updateweather();
    }

    ForecastAdapter aAdapter;
    View rootview;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        aAdapter=new ForecastAdapter(getContext(),null,0);
        rootview = inflater.inflate(R.layout.fragment_main, container, false);
        swipeRefreshLayout=(SwipeRefreshLayout)rootview.findViewById(R.id.swipe);
        final ListView lv = (ListView) rootview.findViewById(R.id.list_view_forecast);
        lv.setAdapter(aAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                aAdapter=(ForecastAdapter)parent.getAdapter();
                Cursor cursor=aAdapter.getCursor();
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

        swipeRefreshLayout = (SwipeRefreshLayout) rootview.findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Utility.hasNetworkConnection(getActivity())) {
                    updateweather();
                } else {
                    Toast.makeText(getContext(), "Network Not Available!", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        return rootview;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
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
        swipeRefreshLayout.setRefreshing(false);
        TextView no_data=(TextView)getActivity().findViewById(R.id.empty);
        aAdapter.swapCursor(data);
        try{
            if (aAdapter.getCount() == 0) {
                no_data.setText("No data fetched.");
                no_data.setVisibility(View.VISIBLE);
            }
            else {
                no_data.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        aAdapter.swapCursor(null);
    }

    public interface OnFragmentInteractionListener {
    }

}
