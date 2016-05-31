package dinidiniz.eggsearcher.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import dinidiniz.eggsearcher.R;

/**
 * Created by leon on 25/05/16.
 */
public class HistoricCal extends BaseAdapter {
    private List<List<Integer>> historic;
    private LayoutInflater inflater;

    @Override
    public int getCount() {
        return historic.size();
    }

    public HistoricCal(List<List<Integer>> historic, Activity ctx){
        this.historic = historic;
        this.inflater = LayoutInflater.from(ctx);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return historic.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // initIfNeed view
        //
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.adapter_historic_cal, null);
            holder = new ViewHolder();
            holder.r = (TextView) convertView.findViewById(R.id.r);
            holder.g = (TextView) convertView.findViewById(R.id.g);
            holder.b = (TextView) convertView.findViewById(R.id.b);
            holder.isegg = (TextView) convertView.findViewById(R.id.isEgg);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // set data
        //
        List<Integer> item = historic.get(position);

        holder.r.setText(item.get(0) + "");
        holder.g.setText(item.get(1) + "");
        holder.b.setText(item.get(2) + "");
        holder.isegg.setText(item.get(3) + "");

        return convertView;
    }


    private static class ViewHolder{
        TextView r;
        TextView g;
        TextView b;
        TextView isegg;
    }
}
