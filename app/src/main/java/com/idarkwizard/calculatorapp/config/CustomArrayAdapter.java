package com.idarkwizard.calculatorapp.config;

import android.app.Activity;
import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.idarkwizard.calculatorapp.R;

import java.util.ArrayList;
import java.util.List;

public class CustomArrayAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> labels;

    public CustomArrayAdapter(Context context, List<String> labels) {
        super(context, R.layout.single_row, labels);
        this.context = context;
        this.labels = labels;
    }

    static class ViewHolder {
        public TextView textView;
        public View icon;

    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            rowView = inflater.inflate(R.layout.single_row, null, true);
            holder = new ViewHolder();
            holder.textView = rowView.findViewById(R.id.text_view);
            holder.icon = rowView.findViewById(R.id.row_ic);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }
        holder.textView.setText(labels.get(position));
        if(holder.textView.getText().toString().startsWith("/"))
            holder.icon.setBackground(context.getDrawable(R.drawable.ic_folder));
        else
            holder.icon.setBackground(context.getDrawable(R.drawable.ic_file));
        return rowView;
    }
}
