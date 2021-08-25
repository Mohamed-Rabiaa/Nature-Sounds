package com.relaxation.naturesounds;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class TimerDialogFragment extends DialogFragment {

    private String tenMinutesLabel = "";

    private String twentyMinutesLabel = "";

    private String thirtyMinutesLabel = "";

    private String fourtyMinutesLabel = "";

    private String fiftyMinutesLabel = "";

    private String cancelTimerLabel = "";

    private String oneHourLabel = "";

    private String twoHoursLabel = "";

    private String fourHoursLabel = "";

    private String eightHoursLabel = "";

    TimerDialogFragmentListener listener;

    MainActivity mainActivity;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        tenMinutesLabel = getString(R.string.TEN_MINUTES_LABEL);

        twentyMinutesLabel = getString(R.string.TWENTY_MINUTES_LABEL);

        thirtyMinutesLabel = getString(R.string.THIRTY_MINUTES_LABEL);

        fourtyMinutesLabel = getString(R.string.FOURTY_MINUTES_LABEL);

        fiftyMinutesLabel = getString(R.string.FIFTY_MINUTES_LABEL);

        cancelTimerLabel = getString(R.string.CANCEL_TIMER_LABEL);

        oneHourLabel =  getString(R.string.ONE_HOUR_LABEL);

        twoHoursLabel = getString(R.string.TWO_HOURS_LABEL);

        fourHoursLabel = getString(R.string.FOUR_HOURS_LABEL);

        eightHoursLabel = getString(R.string.EIGHT_HOURS_LABEL);

        CharSequence timerDurations [] = {cancelTimerLabel, tenMinutesLabel, twentyMinutesLabel, thirtyMinutesLabel,
        fourtyMinutesLabel, fiftyMinutesLabel, oneHourLabel, twoHoursLabel,fourHoursLabel,eightHoursLabel};

        mainActivity = (MainActivity) getActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.TIMER_DIALOG_TITLE)

        .setSingleChoiceItems(timerDurations, mainActivity.getWhich() , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                listener.singleChoiceItemsOnClick(TimerDialogFragment.this,which);
            }
        })

        .setPositiveButton(R.string.OK_BUTTON_LABEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                listener.positiveButtonOnClick(TimerDialogFragment.this);
            }
        })

        .setNegativeButton(R.string.CANCEL_BUTTON_LABEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                listener.negativeButtonOnclick(TimerDialogFragment.this);
            }
        });

        return  builder.create();
    }

    public interface  TimerDialogFragmentListener {

        public void singleChoiceItemsOnClick(DialogFragment dialogFragment,int which);

        public void  negativeButtonOnclick(DialogFragment dialogFragment);

        public void positiveButtonOnClick(DialogFragment dialogFragment);
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        try{

            listener = (TimerDialogFragmentListener) context;
        }
        catch (ClassCastException exception)
        {
            exception.printStackTrace();
        }
    }
}