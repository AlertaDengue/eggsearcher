package dinidiniz.eggsearcher.adapters;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dinidiniz.eggsearcher.R;

/**
 * Created by leon on 12/04/17.
 */
public class HistoricAdapter extends BaseAdapter {
    private ArrayList<ArrayList<String>> historic;
    private LayoutInflater inflater;
    private String TAG = HistoricAdapter.class.getName();

    @Override
    public int getCount() {
        return historic.size();
    }

    public HistoricAdapter(ArrayList<ArrayList<String>> historic, Activity ctx){
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
        final ViewHolder holder;

        // initIfNeed view
        //
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.adapter_historic, null);
            holder = new ViewHolder();
            holder.eggsAdapter = (TextView) convertView.findViewById(R.id.eggsAdapter);
            holder.codeAdapter = (TextView) convertView.findViewById(R.id.codeAdapter);
            holder.idAdapter = (TextView) convertView.findViewById(R.id.idAdapter);
            holder.descriptionAdapter = (TextView) convertView.findViewById(R.id.descriptionAdapter);
            holder.addressAdapter = (TextView) convertView.findViewById(R.id.addressAdapter);
            holder.adapterHistoricLinearLayout = (LinearLayout) convertView.findViewById(R.id.adapterHistoricLinearLayout);
            holder.adapterInvisibleLinearLayout = (LinearLayout) convertView.findViewById(R.id.adapterInvisibleLinearLayout);
            holder.dateAdapter = (TextView) convertView.findViewById(R.id.dateAdapter);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // set data
        //
        ArrayList<String> item = historic.get(position);

        holder.eggsAdapter.setText(item.get(2));
        holder.codeAdapter.setText(item.get(1));
        holder.idAdapter.setText(item.get(0));
        holder.descriptionAdapter.setText(item.get(3));
        holder.addressAdapter.setText(item.get(4));
        holder.dateAdapter.setText(item.get(5));

        holder.adapterHistoricLinearLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i(TAG, "hello click");
                holder.adapterInvisibleLinearLayout.setVisibility(View.VISIBLE);

                holder.adapterInvisibleLinearLayout.setMinimumHeight(holder.eggsAdapter.getHeight() + holder.descriptionAdapter.getHeight());
            } }

        );

        return convertView;
    }


    private static class ViewHolder{
        TextView eggsAdapter;
        TextView codeAdapter;
        TextView idAdapter;
        TextView descriptionAdapter;
        TextView addressAdapter;
        LinearLayout adapterHistoricLinearLayout;
        LinearLayout adapterInvisibleLinearLayout;
        TextView dateAdapter;
    }
}

