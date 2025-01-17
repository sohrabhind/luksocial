package com.investokar.poppi.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.investokar.poppi.R;
import com.investokar.poppi.constants.Constants;

public class PhotoActionDialog extends DialogFragment implements Constants {

    private int position;

    /** Declaring the interface, to invoke a callback function in the implementing activity class */
    AlertPositiveListener alertPositiveListener;

    /** An interface to be implemented in the hosting activity for "OK" button click listener */
    public interface AlertPositiveListener {

        void onPhotoReportDialog(int position);
    }

    /** This is a callback method executed when this fragment is attached to an activity.
     *  This function ensures that, the hosting activity implements the interface AlertPositiveListener
     * */
    public void onAttach(android.app.Activity activity) {

        super.onAttach(activity);

        try {

            alertPositiveListener = (AlertPositiveListener) activity;

        } catch(ClassCastException e){

            // The hosting activity does not implemented the interface AlertPositiveListener
            throw new ClassCastException(activity.toString() + " must implement AlertPositiveListener");
        }
    }

    /** This is a callback method which will be executed
     *  on creating this fragment
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();

        position = bundle.getInt("position");

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(requireActivity());

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(requireActivity(), android.R.layout.simple_list_item_1);
        arrayAdapter.add(getString(R.string.action_report));

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch (which) {

                    case 0: {

                        alertPositiveListener.onPhotoReportDialog(position);

                        break;
                    }

                    default: {

                        break;
                    }
                }

            }
        });

        /** Creating the alert dialog window using the builder class */
        AlertDialog d = builderSingle.create();

        /** Return the alert dialog window */
        return d;
    }
}