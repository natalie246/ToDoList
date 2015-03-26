package com.nataliemaayan.taskmanager.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

import com.nataliemaayan.taskmanager.DialogListener;

public class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener 
{

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour = -1;
        int minute = -1;

        /* if there is data passed */
        if (getArguments() != null){
            try{
                hour    = getArguments().getInt("timeHour");
                minute  = getArguments().getInt("timeMinute");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            hour    = c.get(Calendar.HOUR_OF_DAY);
            minute  = c.get(Calendar.MINUTE);
        }

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        System.out.println("****************************************");
        System.out.println(hourOfDay+":"+minute);
        System.out.println("****************************************");

        // Return input text to activity
        Intent data = new Intent();
        //data.putExtra("timePicker", timePicker);
        data.putExtra("hour", hourOfDay);
        data.putExtra("minute", minute);
        DialogListener activity = (DialogListener) getActivity();
        activity.onFinishEditDialog(data);
        this.dismiss();
    }

//    @Override
//    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//        if (EditorInfo.IME_ACTION_DONE == actionId) {
//            // Return input text to activity
//            EditNameDialogListener activity = (EditNameDialogListener) getActivity();
//            activity.onFinishEditDialog(mEditText.getText().toString());
//            this.dismiss();
//            return true;
//        }
//        return false;
//    }
}




