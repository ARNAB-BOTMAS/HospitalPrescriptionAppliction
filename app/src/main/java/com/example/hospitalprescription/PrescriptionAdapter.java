package com.example.hospitalprescription;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.hospitalprescription.Prescription;
import com.example.hospitalprescription.R;

import java.util.List;

public class PrescriptionAdapter extends ArrayAdapter<Prescription> {

    public PrescriptionAdapter(@NonNull Context context, List<Prescription> prescriptions) {
        super(context, 0, prescriptions);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.prescription_item, parent, false);
        }

        Prescription prescription = getItem(position);
        TextView prescriptionDateTextView = convertView.findViewById(R.id.prescriptionDate);

        if (prescription != null) {
            prescriptionDateTextView.setText("Date: " + prescription.getDate());
        }

        return convertView;
    }
}
