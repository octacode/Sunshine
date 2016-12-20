package octacode.allblue.code.sunshine.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
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
import java.util.Vector;

import octacode.allblue.code.sunshine.MainActivity;
import octacode.allblue.code.sunshine.R;
import octacode.allblue.code.sunshine.Utility;
import octacode.allblue.code.sunshine.data.WeatherContract;
import octacode.allblue.code.sunshine.data.WeatherContract.WeatherEntry;

/**
 * Created by shasha on 19/12/16.
 */

public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private boolean DEBUG = true;

    private static final String[] FORECAST_PRJECTION={
            WeatherEntry.COLUMN_WEATHER_ID,
            WeatherEntry.COLUMN_DESC_TEXT,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP
    };

    private static final long DAY_IN_MILLIS=1000*60*60*24;
    private static final int WEATHER_NOTIFICATION_ID=3004;

    private static final int INDEX_WEATHER_ID=0;
    private static final int INDEX_FORECAST=1;
    private static final int INDEX_MAX=2;
    private static final int INDEX_MIN=3;

    public SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }


    @Override
    public void onPerformSync(Account account, Bundle extras,
                              String authority, ContentProviderClient provider,
                              SyncResult syncResult) {

        Log.d("Shashwat", "onPerformSync");
        String postalCode = Utility.getPreferredLocation(getContext());
        if (postalCode == null || postalCode.isEmpty()) {
            return;
        }
        String weatherForecast = getWeatherForecastData(postalCode);

        try {
            getWeatherDataFromJson(weatherForecast, postalCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        Account account=getSyncAccount(context);
        ContentResolver.requestSync(account,
                "octacode.allblue.code.sunshine", bundle);
    }

    private String getWeatherForecastData(String postalCode) {
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
                    .appendQueryParameter(QUERY_PARAM, postalCode)
                    .appendQueryParameter(MODE_PARAM, "json")
                    .appendQueryParameter(UNITS_PARAM, "metric")
                    .appendQueryParameter(COUNT_PARAM, "14")
                    .appendQueryParameter(APPID_PARAM, "2b602d1d73519843d6df028cb7ae473f");
            URL url = new URL(built_uri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            StringBuffer buffer;
            String line;
            try (InputStream inputStream = urlConnection.getInputStream()) {
                buffer = new StringBuffer();
                if (inputStream == null) {
                    forecastJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();

                }
            catch (FileNotFoundException e){
                e.printStackTrace();
            }
        }
            catch (IOException e) {
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
        return forecastJsonStr;
    }

    private static Account getSyncAccount(Context context) {
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(context.getString(R.string.app_name),
                context.getString(R.string.sync_account_type));

        // If password doesn't exist, the account doesn't exist
        if (accountManager.getPassword(newAccount) == null) {
            // If not successful
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            // If you don't set android:syncable="true" in your <provider> element in the manifest
            // then call context.setIsSyncable(account, AUTHORITY, 1) here
            onAccountCreated(newAccount, context);
        }

        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, "octacode.allblue.code.sunshine", true);
        syncImmediately(context);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {

        Account account = getSyncAccount(context);
        String authority = "octacode.allblue.code.sunshine.app";

          ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
    }

    private void getWeatherDataFromJson(String forecastJsonStr, String locationSetting)
            throws JSONException {

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
        if(forecastJsonStr!=null){
        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
        String cityName = cityJson.getString(OWM_CITY_NAME);
        JSONObject coordJSON = cityJson.getJSONObject(OWM_COORD);
        double cityLatitude = coordJSON.getDouble(OWM_COORD_LAT);
        double cityLongitude = coordJSON.getDouble(OWM_COORD_LONG);

        long locationID = insertLocationInDatabase(
                locationSetting, cityName, cityLatitude, cityLongitude
        );

        Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());

        for(int i = 0; i < weatherArray.length(); i++) {

            long dateTime;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;

            int high;
            int low;

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
            high = temperatureObject.getInt(OWM_MAX);
            low = temperatureObject.getInt(OWM_MIN);

            ContentValues weatherValues = new ContentValues();

            weatherValues.put(WeatherEntry.COLUMN_LOCATION_KEY, locationID);
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

        if(cVVector.size()>0){
            ContentValues[] cvarray=new ContentValues[cVVector.size()];
            cVVector.toArray(cvarray);
            getContext().getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI,cvarray);
            notifyWeather();
        }
            insertWeatherIntoDatabase(cVVector);
        }
    }

    private long insertLocationInDatabase(String locationSetting, String cityName, double lat, double lon) {
        long rowId = 0;

        Cursor cursor = getContext().getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[] {WeatherContract.LocationEntry._ID},
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[] {locationSetting},
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {

            int locationIdIndex =  cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            rowId = cursor.getLong(locationIdIndex);

        } else {

            ContentValues locationValues = new ContentValues();
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(WeatherContract.LocationEntry.COLOUMN_CITY_NAME, cityName);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LATITUDE, lat);
            locationValues.put(WeatherContract.LocationEntry.COLUMN_LONGITUDE, lon);

            Uri uri = getContext().getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, locationValues);
            rowId = ContentUris.parseId(uri);

        }

        if (cursor != null) {
            cursor.close();
        }

        return rowId;
    }

    private void insertWeatherIntoDatabase(Vector<ContentValues> CVVector) {
        if (CVVector.size() > 0) {
            ContentValues[] contentValuesArray = new ContentValues[CVVector.size()];
            CVVector.toArray(contentValuesArray);

            int rowsInserted = getContext().getContentResolver().bulkInsert(WeatherEntry.CONTENT_URI, contentValuesArray);

            if (DEBUG) {
                Cursor weatherCursor = getContext().getContentResolver().query(
                        WeatherEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );

                if (weatherCursor.moveToFirst()) {
                    ContentValues resultValues = new ContentValues();
                    DatabaseUtils.cursorRowToContentValues(weatherCursor, resultValues);
                    Log.v("Shashwat", "Query succeeded! **********");
                    for (String key : resultValues.keySet()) {
                        Log.v("Shashwat", key + ": " + resultValues.getAsString(key));
                    }
                } else {
                    Log.v("Shashwat", "Query failed! :( **********");
                }
            }
        }
    }

    private void notifyWeather() {
        Context context = getContext();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean value=prefs.getBoolean(getContext().getString(R.string.pref_notifications_enable),false);
        if(value){
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        long lastSync = prefs.getLong(lastNotificationKey, 0);

        if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {

            String locationQuery = Utility.getPreferredLocation(context);

            Uri weatherUri = WeatherContract.WeatherEntry
                    .buildWeatherLocationWithDate(
                            locationQuery, WeatherEntry.COLUMN_DATE_TEXT);


            Cursor cursor =  context.getContentResolver().query(
                    weatherUri,
                    FORECAST_PRJECTION,
                    null,
                    null,
                    null
            );

            assert cursor != null;
            if (cursor.moveToFirst()) {
                int weatherId = cursor.getInt(INDEX_WEATHER_ID);
                double maxTemp = cursor.getDouble(INDEX_MAX);
                double minTemp = cursor.getDouble(INDEX_MIN);
                String shortDesc = cursor.getString(INDEX_FORECAST);
                int iconId = Utility.getIconResourceForWeatherCondition(weatherId);
                String title = context.getString(R.string.app_name);

                String contentText =
                        "Forecast: "+shortDesc+" Max: "+
                        Utility.getformattedTemp(maxTemp, Utility.isMetric(context))+ " Min: "+
                        Utility.getformattedTemp(minTemp, Utility.isMetric(context));

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(iconId)
                                .setContentTitle(title)
                                .setContentText(contentText);


                Intent resultIntent = new Intent(context, MainActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(resultPendingIntent);

                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(WEATHER_NOTIFICATION_ID, mBuilder.build());

                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(lastNotificationKey, System.currentTimeMillis());
                editor.apply();
                }
            }
        }
    }
}
