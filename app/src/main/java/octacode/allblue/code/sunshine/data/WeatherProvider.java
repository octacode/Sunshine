package octacode.allblue.code.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import octacode.allblue.code.sunshine.data.WeatherContract.LocationEntry;
import octacode.allblue.code.sunshine.data.WeatherContract.WeatherEntry;

/**
 * Created by shasha on 14/12/16.
 */

public class WeatherProvider extends ContentProvider {
    public static final int WEATHER=100;
    public static final int WEATHER_WITH_LOCATION=101;
    public static final int WEATHER_WITH_LOCATION_DATE=102;

    public static final int LOCATION=300;
    public static final int LOCATION_WITH_ID=301;

    private static final UriMatcher sUriMatcher=buildUriMatcher();

    private Weatherdb mWeatherdbHelper;

    private static UriMatcher buildUriMatcher(){

        final UriMatcher matcher=new UriMatcher(UriMatcher.NO_MATCH);
        final String authority=WeatherContract.CONTENT_AUTHORITY;


        matcher.addURI(authority,WeatherContract.PATH_WEATHER,WEATHER);
        matcher.addURI(authority,WeatherContract.PATH_WEATHER+ "/*",WEATHER_WITH_LOCATION);
        matcher.addURI(authority,WeatherContract.PATH_WEATHER+"/*/*",WEATHER_WITH_LOCATION_DATE);

        matcher.addURI(authority,WeatherContract.PATH_LOCATION,LOCATION);
        matcher.addURI(authority,WeatherContract.PATH_LOCATION+"/#",LOCATION_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mWeatherdbHelper=new Weatherdb(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
