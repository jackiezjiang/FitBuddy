package de.avalax.fitbuddy.presentation.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import de.avalax.fitbuddy.R;
import de.avalax.fitbuddy.application.dialog.WeightDecimalPlaces;

public class EditWeightDialogFragment extends DialogFragment {

    private static final String ARGS_WEIGHT = "weight";
    private NumberPicker numberPicker;
    private NumberPicker decimalPlacesNumberPicker;
    private DialogListener listener;
    private WeightDecimalPlaces weightDecimalPlaces = new WeightDecimalPlaces();

    public static EditWeightDialogFragment newInstance(double weight) {
        EditWeightDialogFragment fragment = new EditWeightDialogFragment();
        Bundle args = new Bundle();
        args.putDouble(ARGS_WEIGHT, weight);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (DialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement EditWeightDialogFragment.DialogListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_weight, container, false);
        Button button = (Button) view.findViewById(R.id.done_button);
        Double weight = getArguments().getDouble(ARGS_WEIGHT);
        getDialog().setTitle(R.string.dialog_change_weight);

        numberPicker = (NumberPicker) view.findViewById(R.id.numberPicker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(999);
        numberPicker.setValue((int) Math.floor(weight));

        String[] labels = weightDecimalPlaces.getLabels();
        int decimalPlaces = weightDecimalPlaces.getPosition(weight - Math.floor(weight));
        decimalPlacesNumberPicker = (NumberPicker) view.findViewById(R.id.decimalPlaces);
        decimalPlacesNumberPicker.setMinValue(0);
        decimalPlacesNumberPicker.setMaxValue(labels.length - 1);
        decimalPlacesNumberPicker.setValue(decimalPlaces);
        decimalPlacesNumberPicker.setDisplayedValues(labels);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                listener.onDialogPositiveClick(EditWeightDialogFragment.this);
                getDialog().dismiss();
            }
        });

        return view;
    }

    public double getWeight() {
        int decimalPlaces = decimalPlacesNumberPicker.getValue();
        return numberPicker.getValue() + weightDecimalPlaces.getWeight(decimalPlaces);
    }

    public interface DialogListener {
        void onDialogPositiveClick(EditWeightDialogFragment editWeightDialogFragment);
    }
}
