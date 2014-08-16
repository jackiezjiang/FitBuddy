package de.avalax.fitbuddy.presentation.edit.exercise;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import butterknife.ButterKnife;
import de.avalax.fitbuddy.presentation.R;
import de.avalax.fitbuddy.application.edit.workout.ManageWorkout;
import de.avalax.fitbuddy.domain.model.exercise.Exercise;
import de.avalax.fitbuddy.domain.model.set.BasicSet;
import de.avalax.fitbuddy.domain.model.set.Set;
import de.avalax.fitbuddy.presentation.FitbuddyApplication;
import de.avalax.fitbuddy.presentation.dialog.EditNameDialogFragment;
import de.avalax.fitbuddy.presentation.dialog.EditRepsDialogFragment;
import de.avalax.fitbuddy.presentation.dialog.EditSetsDialogFragment;
import de.avalax.fitbuddy.presentation.dialog.EditWeightDialogFragment;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class EditExerciseActivity extends FragmentActivity implements EditWeightDialogFragment.DialogListener, EditSetsDialogFragment.DialogListener, EditRepsDialogFragment.DialogListener, EditNameDialogFragment.DialogListener {

    @Inject
    protected ManageWorkout manageWorkout;

    private Exercise exercise;

    private EditExerciseDialogFragment editExerciseDialogFragment;
    private int position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_workout);
        ButterKnife.inject(this);
        ((FitbuddyApplication) getApplication()).inject(this);
        exercise = (Exercise) getIntent().getSerializableExtra("exercise");
        position = getIntent().getIntExtra("position", -1);
        editExerciseDialogFragment = EditExerciseDialogFragment.newInstance(exercise);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editExerciseDialogFragment).commit();
        getActionBar().setTitle(exercise.getName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_exercise_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete_exercise) {
            manageWorkout.deleteExercise(exercise);
            setResult(RESULT_OK);
            finish();
        } else if (item.getItemId() == R.id.action_save_exercise) {
            manageWorkout.saveExercise(exercise);
            setResult(RESULT_OK);
            finish();
        } else if (item.getItemId() == R.id.action_add_exercise) {
            manageWorkout.createExerciseBefore(exercise);
            setResult(RESULT_OK);
            finish();
        } else if (item.getItemId() == R.id.action_add_exercise_after) {
            manageWorkout.createExerciseAfter(exercise);
            setResult(RESULT_OK);
            finish();
        }
        return true;
    }

    @Override
    public void onDialogPositiveClick(EditWeightDialogFragment editWeightDialogFragment) {
        double weight = editWeightDialogFragment.getWeight();
        for (Set set : exercise.getSets()) {
            set.setWeight(weight);
        }
        editExerciseDialogFragment.init();
    }

    @Override
    public void onDialogPositiveClick(EditSetsDialogFragment editSetsDialogFragment) {
        int newSetAmount = editSetsDialogFragment.getSets();
        //TODO: update sets, instead of replacing them
        int maxReps = exercise.getSets().isEmpty() ? 0 : exercise.getCurrentSet().getMaxReps();
        double weight = exercise.getSets().isEmpty() ? 0 : exercise.getCurrentSet().getWeight();
        List<Set> sets = new ArrayList<>();
        for (int i = 0; i < newSetAmount; i++) {
            Set set = new BasicSet();
            set.setWeight(weight);
            set.setMaxReps(maxReps);
            sets.add(set);
        }
        manageWorkout.replaceSets(exercise, sets);
        editExerciseDialogFragment.init();
    }

    @Override
    public void onDialogPositiveClick(EditRepsDialogFragment editRepsDialogFragment) {
        int reps = editRepsDialogFragment.getReps();
        for (Set set : exercise.getSets()) {
            set.setMaxReps(reps);
        }
        editExerciseDialogFragment.init();
    }

    @Override
    public void onDialogPositiveClick(EditNameDialogFragment editNameDialogFragment) {
        String name = editNameDialogFragment.getName();
        exercise.setName(name);
        editExerciseDialogFragment.init();
        getActionBar().setTitle(exercise.getName());
    }
}