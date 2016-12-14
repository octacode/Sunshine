package octacode.allblue.code.sunshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
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


public class ForecastFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    public ForecastFragment() {
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
                Intent intent=null, chooser=null;
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("geo:"+fetchWeatherTask.lat+","+fetchWeatherTask.lon));
                chooser = Intent.createChooser(intent,"Launch Maps");
                startActivity(chooser);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    FetchWeatherTask fetchWeatherTask;
    private void updateweather() {
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(getContext());
        String data=sharedPreferences.getString(getString(R.string.ZIP_pref),"94043");
        fetchWeatherTask=new FetchWeatherTask();
        this.fetchWeatherTask=fetchWeatherTask;
        fetchWeatherTask.execute(data);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateweather();
    }

    ArrayAdapter<String> aAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_main, container, false);

        ArrayList<String> fakeData=new ArrayList<>();
        aAdapter = new ArrayAdapter<String>(getContext(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, fakeData);
        final ListView lv = (ListView) rootview.findViewById(R.id.list_view_forecast);
        lv.setAdapter(aAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            startActivity(new Intent(getContext(),DetailActivity.class).putExtra("data",lv.getItemAtPosition(position).toString()));
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        String forecastJsonStr,lat, lon;

        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            this.forecastJsonStr=forecastJsonStr;
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DATETIME = "dt";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                String day;
                String description;
                String highAndLow;

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                long dateTime = dayForecast.getLong(OWM_DATETIME);
                day = getReadableDateString(dateTime);

                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);
                SharedPreferences shared_pref=PreferenceManager.getDefaultSharedPreferences(getContext());
                String number=shared_pref.getString(getString(R.string.UNIT_pref),"0");
                if(number.equals("1")){
                    high=1.8*high+32;
                    low=1.8*low+32;
                }
                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            return resultStrs;
        }

        private String getReadableDateString(long time){
            Date date = new Date(time * 1000);
            SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
            return format.format(date).toString();
        }

        private String formatHighLows(double high, double low) {
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);
            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }
        @Override
        protected String[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String forecastJsonStr = null;
            final String BASE_PARAM = "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String MODE_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String COUNT_PARAM = "cnt";
            final String APPID_PARAM = "appid";

            try {
                Uri.Builder built_uri = Uri.parse(BASE_PARAM).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(MODE_PARAM, "json")
                        .appendQueryParameter(UNITS_PARAM, "metric")
                        .appendQueryParameter(COUNT_PARAM, "7")
                        .appendQueryParameter(APPID_PARAM, "2b602d1d73519843d6df028cb7ae473f");
                URL url = new URL(built_uri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                forecastJsonStr = null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                return getWeatherDataFromJson(forecastJsonStr,7);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if(strings!=null){

                aAdapter.clear();
                for(String dayForcast:strings)
                    aAdapter.add(dayForcast);
                aAdapter.notifyDataSetChanged();

                try {
                    JSONObject weather=new JSONObject(forecastJsonStr);
                    JSONObject city=weather.getJSONObject("city");
                    String city_name=city.getString("name");
                    JSONObject coordinates=city.getJSONObject("coord");
                    this.lat=coordinates.get("lat").toString();
                    this.lon=coordinates.get("lon").toString();
                    Toast.makeText(getContext(),lat+"  "+city_name+"  "+lon,Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
