package com.example.hospitalprescription;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class DoctorListAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> doctorNames;

    public DoctorListAdapter(Context context, List<String> doctorNames) {
        super(context, 0, doctorNames);
        this.context = context;
        this.doctorNames = doctorNames;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.list_items, parent, false);
        }

        String currentDoctor = doctorNames.get(position);

        TextView drNameTextView = listItemView.findViewById(R.id.drNames);
        drNameTextView.setText(currentDoctor);

        return listItemView;
    }
}
