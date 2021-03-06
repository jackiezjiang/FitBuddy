package de.avalax.fitbuddy.presentation.edit.workout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.avalax.fitbuddy.domain.model.exercise.Exercise;
import de.avalax.fitbuddy.presentation.FitbuddyApplication;
import de.avalax.fitbuddy.R;
import de.avalax.fitbuddy.presentation.helper.ExerciseViewHelper;

import javax.inject.Inject;
import java.util.List;

public class ExerciseAdapter extends ArrayAdapter<Exercise> {
    private List<Exercise> exercises;
    @Inject
    ExerciseViewHelper exerciseViewHelper;
    private int textViewResourceId;

    public ExerciseAdapter(Context context, int textViewResourceId, List<Exercise> exercises) {
        super(context, textViewResourceId, exercises);
        this.textViewResourceId = textViewResourceId;
        ((FitbuddyApplication) context.getApplicationContext()).inject(this);
        this.exercises = exercises;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ExerciseViewHolder holder;
        if (convertView == null) {
            String inflaterService = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflaterService);
            convertView = vi.inflate(textViewResourceId, parent, false);
            holder = new ExerciseViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.exercise_title);
            holder.weight = (TextView) convertView.findViewById(R.id.weightTextView);
            holder.reps = (TextView) convertView.findViewById(R.id.repsTextView);
            holder.sets = (TextView) convertView.findViewById(R.id.setsTextView);
            convertView.setTag(holder);
        } else {
            holder = (ExerciseViewHolder) convertView.getTag();
        }

        holder.setFromExercise(exercises.get(position));

        return convertView;
    }

    class ExerciseViewHolder {
        public TextView name;
        public TextView weight;
        public TextView sets;
        public TextView reps;

        void setFromExercise(Exercise exercise) {
            name.setText(exerciseViewHelper.nameOfExercise(exercise));
            weight.setText(exerciseViewHelper.weightOfExercise(exercise));
            reps.setText(String.valueOf(exerciseViewHelper.maxRepsOfExercise(exercise)));
            sets.setText(String.valueOf(exerciseViewHelper.setCountOfExercise(exercise)));
        }
    }
}