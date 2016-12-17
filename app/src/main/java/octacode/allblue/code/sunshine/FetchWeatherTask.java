package octacode.allblue.code.sunshine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import octacode.allblue.code.sunshine.data.WeatherContract;
import octacode.allblue.code.sunshine.data.WeatherContract.LocationEntry;
import octacode.allblue.code.sunshine.data.WeatherContract.WeatherEntry;

/**
 * Created by shasha on 17/12/16.
 */
public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

    String forecastJsonStr,lat, lon;
    Context mContext;

    public FetchWeatherTask(Context mContext){
        this.mContext=mContext;
    }

    @Override
    protected Void doInBackground(String... params) {

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
                    .appendQueryParameter(COUNT_PARAM, "14")
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

        final String OWM_CITY = "city";
        final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";
        final String OWM_COORD_LAT = "lat";
        final String OWM_COORD_LONG = "lon";

        final String OWM_LIST = "list";

        final String OWM_DATETIME = "dt";
        final String OWM_PRESSURE = "pressure";
        final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";

        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";

        final String OWM_WEATHER = "weather";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";

        try {
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
            String cityName = cityJson.getString(OWM_CITY_NAME);
            JSONObject coordJSON = cityJson.getJSONObject(OWM_COORD);
            double cityLatitude = coordJSON.getDouble(OWM_COORD_LAT);
            double cityLongitude = coordJSON.getDouble(OWM_COORD_LONG);
            long location_id=addLocation(params[0],cityName,cityLatitude,cityLongitude);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

            for(int i = 0; i < weatherArray.length(); i++) {

                long dateTime;
                double pressure;
                int humidity;
                double windSpeed;
                double windDirection;

                double high;
                double low;

                String description;
                int weatherId;

                JSONObject dayForecast = weatherArray.getJSONObject(i);

                dateTime = dayForecast.getLong(OWM_DATETIME);

                pressure = dayForecast.getDouble(OWM_PRESSURE);
                humidity = dayForecast.getInt(OWM_HUMIDITY);
                windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
                windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);

                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);
                weatherId = weatherObject.getInt(OWM_WEATHER_ID);

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                high = temperatureObject.getDouble(OWM_MAX);
                low = temperatureObject.getDouble(OWM_MIN);

                ContentValues weatherValues = new ContentValues();

                weatherValues.put(WeatherEntry.COLUMN_LOCATION_KEY, location_id);
                weatherValues.put(WeatherEntry.COLUMN_DATE_TEXT, dateTime);
                weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
                weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
                weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
                weatherValues.put(WeatherEntry.COLUMN_DEGREE, windDirection);
                weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
                weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
                weatherValues.put(WeatherEntry.COLUMN_DESC_TEXT, description);
                weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);

                cVVector.add(weatherValues);
            }

            insertWeatherIntoDatabase(cVVector);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void insertWeatherIntoDatabase(Vector<ContentValues> CVVector) {
        if (CVVector.size() > 0) {
            ContentValues[] contentValuesArray = new ContentValues[CVVector.size()];
            CVVector.toArray(contentValuesArray);
            mContext.getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, contentValuesArray);
        }
    }

    private long addLocation(String location_setting,String city_name,double latitude,double longitude){
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                new String[] {LocationEntry._ID},
                LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[] {location_setting},
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {

            int locationIdIndex =  cursor.getColumnIndex(LocationEntry._ID);
            return  cursor.getLong(locationIdIndex);

        }
        else{
            ContentValues location_values = new ContentValues();
            location_values.put(LocationEntry.COLUMN_LOCATION_SETTING, location_setting);
            location_values.put(LocationEntry.COLOUMN_CITY_NAME, city_name);
            location_values.put(LocationEntry.COLUMN_LATITUDE, latitude);
            location_values.put(LocationEntry.COLUMN_LONGITUDE, longitude);
            return ContentUris.parseId(mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, location_values));
        }
    }
}

