package octacode.allblue.code.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by shasha on 17/12/16.
 */

public class ForecastAdapter extends CursorAdapter {

    Context mContext;

    private final int VIEW_TYPE_TODAY=0;
    private final int VIEW_TYPE_FUTURE_DAY=1;

    @Override
    public int getItemViewType(int position) {
        if(position==0)
            return VIEW_TYPE_TODAY;
        else
            return VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    public ForecastAdapter(Context context, Cursor c, int autoRequery) {
        super(context, c, autoRequery);
        mContext=context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int view_type=getItemViewType(cursor.getPosition());
        if(view_type==0){

            View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast_today,parent,false);

            if(PreferenceManager.getDefaultSharedPreferences(mContext).getString("theme","0").matches("1")){
                view.setBackground(new ColorDrawable(R.color.SunshineDark));
            }

            ViewHolder viewHolder=new ViewHolder(view);
            view.setTag(viewHolder);
            return view;
        }
        else{
         View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast,parent,false);
            ViewHolder viewHolder=new ViewHolder(view);
            view.setTag(viewHolder);
            return view;
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder=(ViewHolder)view.getTag();
        int view_type=getItemViewType(cursor.getPosition());
        int weather_id=cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int image_resource;
        if(view_type==0)
        image_resource=Utility.getArtResourceForWeatherCondition(weather_id);
        else
        image_resource=Utility.getIconResourceForWeatherCondition(weather_id);

        boolean isMetric=Utility.isMetric(mContext);
        viewHolder.iconView.setImageResource(image_resource);

        String date_string=cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        TextView date_text=(TextView)view.findViewById(R.id.list_item_date_textview);
        date_text.setText(Utility.formatDate(Long.parseLong(date_string)*1000));

        double max=cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        TextView max_tv=(TextView)view.findViewById(R.id.list_item_max_textview);
        String max_str=Utility.getformattedTemp(max,isMetric);
        max_tv.setText(max_str+"°");

        double min=cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        TextView min_tv=(TextView)view.findViewById(R.id.list_item_min_textview);
        String min_str=Utility.getformattedTemp(min,isMetric);
        min_tv.setText(min_str+"°");

        String desc=cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.desc_tv.setText(desc);
    }

}

class ViewHolder{
    public final ImageView iconView;
    public final TextView date_tv;
    public final TextView desc_tv;
    public final TextView max_tv;
    public final TextView min_tv;

    public ViewHolder(View view){
        iconView=(ImageView)view.findViewById(R.id.list_item_imageview);
        date_tv=(TextView)view.findViewById(R.id.list_item_date_textview);
        desc_tv=(TextView)view.findViewById(R.id.list_item_forecast_textview);
        max_tv=(TextView)view.findViewById(R.id.list_item_max_textview);
        min_tv=(TextView)view.findViewById(R.id.list_item_min_textview);
    }
}
