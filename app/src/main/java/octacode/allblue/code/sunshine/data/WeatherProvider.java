package octacode.allblue.code.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.widget.Switch;

/**
 * Created by shasha on 17/12/16.
 */

public class WeatherProvider extends ContentProvider {

    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;

    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private static final UriMatcher sUrimatcher=buildUriMatcher();

    private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

    private static Weatherdb mOpenHelper;

    private static final String sLocationSettingSelection;
    private static final String sLocationSettingWithStartDateSelection;
    private static final String sLocationSettingWithDaySelection;

    static{
        sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        sWeatherByLocationSettingQueryBuilder.setTables(
                WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN " +
                        WeatherContract.LocationEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +  // Specify the column for _ID
                        "." + WeatherContract.WeatherEntry.COLUMN_LOCATION_KEY + // with the table name to differentiate
                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry._ID);

        sLocationSettingSelection =
                WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING +
                        " = ? ";

        sLocationSettingWithStartDateSelection =
                WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING +
                        " = ? AND " + WeatherContract.WeatherEntry.COLUMN_DATE_TEXT + " >= ? ";

        sLocationSettingWithDaySelection =
                WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING +
                        " = ? AND " + WeatherContract.WeatherEntry.COLUMN_DATE_TEXT + " = ? ";
    }

    private Cursor getWeatherByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == null) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[] {locationSetting};
        } else {
            selection = sLocationSettingWithStartDateSelection;
            selectionArgs = new String[] {locationSetting, startDate};
        }

        return sWeatherByLocationSettingQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getWeatherByLocationSettingwithDate(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = WeatherContract.WeatherEntry.getLocationSettingFromUri(uri);
        String day = WeatherContract.WeatherEntry.getDateFromUri(uri);

        return sWeatherByLocationSettingQueryBuilder.query(
                mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingWithDaySelection,
                new String[] {locationSetting, day},
                null,
                null,
                sortOrder
        );
    }

    private static UriMatcher buildUriMatcher(){

        final String authority=WeatherContract.CONTENT_AUTHORITY;
        final UriMatcher matcher=new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER, WEATHER);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
        matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*/*", WEATHER_WITH_LOCATION_AND_DATE);

        matcher.addURI(authority, WeatherContract.PATH_LOCATION, LOCATION);
        matcher.addURI(authority, WeatherContract.PATH_LOCATION + "/#", LOCATION_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper=new Weatherdb(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch(sUrimatcher.match(uri)){

            case WEATHER:
                retCursor=mOpenHelper.getWritableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case WEATHER_WITH_LOCATION:
                retCursor=getWeatherByLocationSetting(uri,projection,sortOrder);
                break;

            case WEATHER_WITH_LOCATION_AND_DATE:
                retCursor=getWeatherByLocationSettingwithDate(uri,projection,sortOrder);
                break;

            case LOCATION:
                retCursor=mOpenHelper.getWritableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            case LOCATION_ID:
                retCursor=mOpenHelper.getWritableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection,
                        WeatherContract.LocationEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            default:
                throw new UnsupportedOperationException("Unkown Uri: "+uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(),uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUrimatcher.match(uri);

        switch (match) {
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case LOCATION_ID:
                return WeatherContract.LocationEntry.CONTENT_ITEM_TYPE;
            case LOCATION:
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri baseUri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUrimatcher.match(baseUri);
        Uri returnUri;
        long _id;

        switch (match) {
            case WEATHER:
                _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + baseUri);
                }
                break;

            case LOCATION:
                _id = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = WeatherContract.LocationEntry.buildLocationUri(_id);
                } else {
                    throw new SQLException("Failed to insert row into " + baseUri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + baseUri);
        }

        getContext().getContentResolver().notifyChange(baseUri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUrimatcher.match(uri);
        int rowDeleted;

        switch (match) {
            case WEATHER:
                rowDeleted = db.delete(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            case LOCATION:
                rowDeleted = db.delete(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (selection == null || rowDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUrimatcher.match(uri);
        int rowUpdated;

        switch (match) {
            case WEATHER:
                rowUpdated = db.update(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            case LOCATION:
                rowUpdated = db.update(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: "  + uri);
        }

        if (rowUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUrimatcher.match(uri);
        int numRowsInsert = 0;
        switch (match) {
            case WEATHER:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            numRowsInsert++;
                        }
                    }
                    // To commit the transaction
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            default:
                return super.bulkInsert(uri, values);
        }

        if (numRowsInsert > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsInsert;
    }
}
