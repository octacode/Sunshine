package octacode.allblue.code.sunshine.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import octacode.allblue.code.sunshine.data.WeatherContract.WeatherEntry;
import octacode.allblue.code.sunshine.data.WeatherContract.LocationEntry;

/**
 * Created by shasha on 14/12/16.
 */

public class Weatherdb extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION=1;
    public static final String DATABASE_NAME="weatherdb";

    public Weatherdb(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public Weatherdb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_WEATHER_TABLE="CREATE TABLE "+ WeatherEntry.TABLE_NAME+" ("
                +WeatherEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "

                +WeatherEntry.COLUMN_WEATHER_ID+" INTEGER NOT NULL, "
                +WeatherEntry.COLUMN_DATE_TEXT+" TEXT NOT NULL, "
                +WeatherEntry.COLUMN_DESC_TEXT+" TEXT NOT NULL, "
                +WeatherEntry.COLUMN_MAX_TEMP+" REAL NOT NULL, "
                +WeatherEntry.COLUMN_MIN_TEMP+" REAL NOT NULL, "
                +WeatherEntry.COLUMN_LOCATION_KEY+" INTEGER NOT NULL, "
                +WeatherEntry.COLUMN_DEGREE+" REAL NOT NULL, "
                +WeatherEntry.COLUMN_HUMIDITY+" REAL NOT NULL, "
                +WeatherEntry.COLUMN_WIND_SPEED+" REAL NOT NULL, "
                +WeatherEntry.COLUMN_PRESSURE+" REAL NOT NULL, "

                +" FOREIGN KEY ( "+WeatherEntry.COLUMN_LOCATION_KEY+" ) REFERENCES "
                    +LocationEntry.TABLE_NAME+" ( "+LocationEntry._ID+" ), "

                +" UNIQUE ("+ WeatherEntry.COLUMN_DATE_TEXT+", "+
                WeatherEntry.COLUMN_LOCATION_KEY+" ) ON CONFLICT REPLACE);";



        final String SQL_CREATE_LOCATION_TABLE="CREATE TABLE "+LocationEntry.TABLE_NAME+" ("
                +LocationEntry._ID+" INTEGER PRIMARY KEY, "

                +LocationEntry.COLOUMN_CITY_NAME+" TEXT NOT NULL, "
                +LocationEntry.COLUMN_LOCATION_SETTING+" TEXT UNIQUE NOT NULL, "
                +LocationEntry.COLUMN_LATITUDE+" REAL NOT NULL, "
                +LocationEntry.COLUMN_LONGITUDE+" REAL NOT NULL, "

                +" UNIQUE ("+LocationEntry.COLUMN_LOCATION_SETTING+") ON CONFLICT IGNORE"
                +");";
        db.execSQL(SQL_CREATE_WEATHER_TABLE);
        db.execSQL(SQL_CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+WeatherEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+LocationEntry.TABLE_NAME);
        onCreate(db);
    }
}
