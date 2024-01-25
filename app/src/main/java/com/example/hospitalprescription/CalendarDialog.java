package com.example.hospitalprescription;// CalendarDialog.java

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CalendarDialog extends Dialog {

    private CalendarView calendarView;
    private Button btnSelectDate;

    public interface OnDateSelectedListener {
        void onDateSelected(String selectedDate);
    }

    private OnDateSelectedListener onDateSelectedListener;

    public void setOnDateSelectedListener(OnDateSelectedListener listener) {
        this.onDateSelectedListener = listener;
    }

    public CalendarDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_calendar);

        calendarView = findViewById(R.id.calendarView);
        btnSelectDate = findViewById(R.id.btnSelectDate);

        btnSelectDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleDateSelection();
            }
        });

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                updateCalendar(year, month, dayOfMonth);
            }
        });
    }

    private void handleDateSelection() {
        final Calendar calendar = Calendar.getInstance();
        long selectedDateMillis = calendarView.getDate();
        calendar.setTimeInMillis(selectedDateMillis);

        // Format the selected date
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String selectedDate = sdf.format(calendar.getTime());

        // Notify the listener with the selected date
        if (onDateSelectedListener != null) {
            onDateSelectedListener.onDateSelected(selectedDate);
        }

        dismiss(); // Close the dialog
    }

    private void updateCalendar(int year, int month, int dayOfMonth) {
        // Update the calendar when the user selects a different date
        final Calendar selectedDate = Calendar.getInstance();
        selectedDate.set(year, month, dayOfMonth);
        calendarView.setDate(selectedDate.getTimeInMillis(), true, true);
    }
}
