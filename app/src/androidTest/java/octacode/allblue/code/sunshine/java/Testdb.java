package octacode.allblue.code.sunshine.java;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import org.junit.internal.runners.statements.Fail;

import octacode.allblue.code.sunshine.data.WeatherContract;
import octacode.allblue.code.sunshine.data.WeatherContract.WeatherEntry;
import octacode.allblue.code.sunshine.data.Weatherdb;

/**
 * Created by shasha on 14/12/16.
 */

public class Testdb extends AndroidTestCase {
    public void testCreatedb() throws Throwable {
        mContext.deleteDatabase(Weatherdb.DATABASE_NAME);
        SQLiteDatabase liteDatabase = new Weatherdb(getContext()).getWritableDatabase();
        assertEquals(true, liteDatabase.isOpen());
        liteDatabase.close();
    }

    public void testInsertRead() {
        String testname = "Ghaziabad";
        String testLocationsetting = "20161214";
        double testlat = 145.212;
        double testlon = 21.89;

        ContentValues cv = new ContentValues();
        cv.put(WeatherContract.LocationEntry.COLOUMN_CITY_NAME, testname);
        cv.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, testLocationsetting);
        cv.put(WeatherContract.LocationEntry.COLUMN_LATITUDE, testlat);
        cv.put(WeatherContract.LocationEntry.COLUMN_LONGITUDE, testlon);
        SQLiteDatabase liteDatabase = new Weatherdb(getContext()).getWritableDatabase();
        long location_row_id = liteDatabase.insert(WeatherContract.LocationEntry.TABLE_NAME, null, cv);
        assertTrue(location_row_id != -1);
        Log.d("EVERYTHING IS AWESOME->", "The row_id is: " + location_row_id);

        String[] columns = {
                WeatherContract.LocationEntry._ID,
                WeatherContract.LocationEntry.COLOUMN_CITY_NAME,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
                WeatherContract.LocationEntry.COLUMN_LATITUDE,
                WeatherContract.LocationEntry.COLUMN_LONGITUDE
        };

        Cursor cursor = liteDatabase.query(WeatherContract.LocationEntry.TABLE_NAME,
                columns,
                null,
                null,
                null,
                null,
                null,
                null);


        if (cursor.moveToFirst()) {
            int id_index = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
            String id = cursor.getString(id_index);

            int cityname_index = cursor.getColumnIndex(WeatherContract.LocationEntry.COLOUMN_CITY_NAME);
            String city_name = cursor.getString(cityname_index);

            int location_settings_index = cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);
            String location_setting = cursor.getString(location_settings_index);

            int latitude_index = cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_LATITUDE);
            double latitude = cursor.getDouble(latitude_index);

            int longitude_index = cursor.getColumnIndex(WeatherContract.LocationEntry.COLUMN_LONGITUDE);
            double longitude = cursor.getDouble(longitude_index);

            assertEquals(testname, city_name);
            assertEquals(testlat, latitude);
            assertEquals(testlon, longitude);
            assertEquals(testLocationsetting, location_setting);
        } else {
            fail("The test failed. Kuch aata hai bhai aapko.");
        }
        cursor.close();

        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOCATION_KEY,location_row_id);
        weatherValues.put(WeatherEntry.COLUMN_DATE_TEXT, "20161215");
        weatherValues.put(WeatherEntry.COLUMN_DEGREE, 1.13);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 5.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 8.6);
        weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 89.2);
        weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_DESC_TEXT, "Earthquake");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 2.1);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

        long weather_row = liteDatabase.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weather_row != -1);

        Log.d("EVERYTHING IS AWESOME->", "The Row id returned is: " + weather_row);

        String[] columns_weather={
            WeatherEntry.COLUMN_LOCATION_KEY,
            WeatherEntry.COLUMN_DATE_TEXT,
            WeatherEntry.COLUMN_DEGREE,
            WeatherEntry.COLUMN_HUMIDITY,
            WeatherEntry.COLUMN_PRESSURE,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_DESC_TEXT,
            WeatherEntry.COLUMN_WIND_SPEED,
            WeatherEntry.COLUMN_WEATHER_ID
        };
        cursor=liteDatabase.query(WeatherEntry.TABLE_NAME,
                columns_weather,
                null,
                null,
                null,
                null,
                null
                );
        if(cursor.moveToFirst()){
            int column_location_key_index=cursor.getColumnIndex(WeatherEntry.COLUMN_LOCATION_KEY);
            int location_key=cursor.getInt(column_location_key_index);
            int column_date_text_index=cursor.getColumnIndex(WeatherEntry.COLUMN_DATE_TEXT);
            String date_text=cursor.getString(column_date_text_index);
            int column_degree_index=cursor.getColumnIndex(WeatherEntry.COLUMN_DEGREE);
            double degree=cursor.getDouble(column_degree_index);
            int column_humidity_index=cursor.getColumnIndex(WeatherEntry.COLUMN_HUMIDITY);
            double humidity=cursor.getDouble(column_humidity_index);
            int column_pressure=cursor.getColumnIndex(WeatherEntry.COLUMN_PRESSURE);
            double pressure=cursor.getDouble(column_pressure);
            int column_max_index=cursor.getColumnIndex(WeatherEntry.COLUMN_MAX_TEMP);
            double max=cursor.getDouble(column_max_index);
            int column_min_index=cursor.getColumnIndex(WeatherEntry.COLUMN_MIN_TEMP);
            double min=cursor.getDouble(column_min_index);
            int column_desc_index=cursor.getColumnIndex(WeatherEntry.COLUMN_DESC_TEXT);
            String desc=cursor.getString(column_desc_index);
            int column_wind_speed_index=cursor.getColumnIndex(WeatherEntry.COLUMN_WIND_SPEED);
            double wind_speed=cursor.getDouble(column_wind_speed_index);
            int column_weather_id=cursor.getColumnIndex(WeatherEntry.COLUMN_WEATHER_ID);
            int weather_id=cursor.getInt(column_weather_id);
            assertEquals(1, location_key);
            assertEquals("20161215", date_text);
            assertEquals(1.13, degree);
            assertEquals(5.2, humidity);
            assertEquals(8.6, pressure);
            assertEquals(89.2, max);
            assertEquals(1.2, min);
            assertEquals("Earthquake", desc);
            assertEquals(2.1, wind_speed);
            assertEquals(321, weather_id);
        }
        else{
            fail("Kuch aata hai Bhai aapko.");
        }
    }
}

