package octacode.allblue.code.sunshine.data;

import android.content.ContentUris;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.DateFormat;
import java.util.Date;

import static android.provider.Settings.System.DATE_FORMAT;

/**
 * Created by shasha on 14/12/16.
 */

public class WeatherContract {

    public static final String PATH_WEATHER="weather";

    public static final String PATH_LOCATION="location";

    public static final String CONTENT_AUTHORITY = "octacode.allblue.code.sunshine";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class WeatherEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY +"/"+ PATH_WEATHER;

        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY +"/"+ PATH_WEATHER;

        public static final String TABLE_NAME = "weather";

        public static final String _ID = "_id";

        public static final String COLUMN_LOCATION_KEY = "location_key_id";

        public static final String COLUMN_WEATHER_ID = "weather_id";

        public static final String COLUMN_DATE_TEXT = "date";

        public static final String COLUMN_DESC_TEXT = "short_desc";

        public static final String COLUMN_MIN_TEMP = "min_temp";

        public static final String COLUMN_MAX_TEMP = "max_temp";

        public static final String COLUMN_HUMIDITY = "humidity";

        public static final String COLUMN_PRESSURE = "pressure";

        public static final String COLUMN_WIND_SPEED = "wind";

        public static final String COLUMN_DEGREE = "degree";

        public static Uri buildWeatherUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWeatherLocation(String locationSetting) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static Uri buildWeatherLocationWithStartDate(
                String locationSetting, String startDate) {

            return CONTENT_URI.buildUpon().appendPath(locationSetting)
                    .appendQueryParameter(COLUMN_DATE_TEXT, startDate).build();
        }

        public static Uri buildWeatherLocationWithDate(
                String locationSetting, String date) {

            return CONTENT_URI.buildUpon().appendPath(locationSetting)
                    .appendPath(date).build();
        }

        public static String getLocationSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getStartDateFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_DATE_TEXT);
        }

    }





        public static final class LocationEntry implements BaseColumns {

            public static final Uri CONTENT_URI =
                    BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

            public static final String CONTENT_TYPE =
                    "vnd.android.cursor.dir/" + CONTENT_AUTHORITY +"/"+ PATH_LOCATION;

            public static final String CONTENT_ITEM_TYPE =
                    "vnd.android.cursor.item/" + CONTENT_AUTHORITY +"/"+ PATH_LOCATION;


            public static final String TABLE_NAME="location";

            public static final String _ID="_id";

            public static final String COLUMN_LOCATION_SETTING="location_setting";

            public static final String COLOUMN_CITY_NAME="city_name";

            public static final String COLUMN_LATITUDE="latitude";

            public static final String COLUMN_LONGITUDE="longitude";

            public static Uri buildLocationUri(long id) {
                return ContentUris.withAppendedId(CONTENT_URI, id);
            }
    }

}