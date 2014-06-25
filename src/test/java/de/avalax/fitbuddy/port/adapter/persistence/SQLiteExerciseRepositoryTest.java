package de.avalax.fitbuddy.port.adapter.persistence;

import android.app.Activity;
import de.avalax.fitbuddy.application.R;
import de.avalax.fitbuddy.application.manageWorkout.ManageWorkoutActivity;
import de.avalax.fitbuddy.domain.model.exercise.BasicExercise;
import de.avalax.fitbuddy.domain.model.exercise.Exercise;
import de.avalax.fitbuddy.domain.model.exercise.ExerciseId;
import de.avalax.fitbuddy.domain.model.exercise.ExerciseRepository;
import de.avalax.fitbuddy.domain.model.set.BasicSet;
import de.avalax.fitbuddy.domain.model.set.Set;
import de.avalax.fitbuddy.domain.model.set.SetRepository;
import de.avalax.fitbuddy.domain.model.workout.BasicWorkout;
import de.avalax.fitbuddy.domain.model.workout.Workout;
import de.avalax.fitbuddy.domain.model.workout.WorkoutId;
import de.avalax.fitbuddy.domain.model.workout.WorkoutRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class SQLiteExerciseRepositoryTest {

    private ExerciseRepository exerciseRepository;

    private SetRepository setRepository;

    private WorkoutId workoutId;

    private ExerciseId createExercise(int position, String name) {
        Exercise exercise = new BasicExercise(name, new ArrayList<Set>());
        exerciseRepository.save(workoutId, position, exercise);
        return exercise.getExerciseId();
    }

    private void createWorkout(FitbuddySQLiteOpenHelper sqLiteOpenHelper) {
        WorkoutRepository workoutRepository = new SQLiteWorkoutRepository(sqLiteOpenHelper, exerciseRepository);
        Workout workout = new BasicWorkout(new LinkedList<Exercise>());
        workoutRepository.save(workout);
        workoutId = workout.getWorkoutId();
    }

    @Before
    public void setUp() throws Exception {
        Activity activity = Robolectric.buildActivity(ManageWorkoutActivity.class).create().get();
        FitbuddySQLiteOpenHelper sqLiteOpenHelper = new FitbuddySQLiteOpenHelper("SQLiteExerciseRepositoryTest", 1, activity, R.raw.fitbuddy_db);
        setRepository = new SQLiteSetRepository(sqLiteOpenHelper);
        exerciseRepository = new SQLiteExerciseRepository(sqLiteOpenHelper, setRepository);

        createWorkout(sqLiteOpenHelper);
    }

    @Test
    public void saveUnpersistedExercise_shouldAssignNewExerciseId() throws Exception {
        Exercise exercise = new BasicExercise("nam", new ArrayList<Set>());

        assertThat(exercise.getExerciseId(), nullValue());
        exerciseRepository.save(workoutId, 1, exercise);
        assertThat(exercise.getExerciseId(), any(ExerciseId.class));
    }

    @Test
    public void savePersistedExercise_shouldKeepExerciseId() {
        Exercise exercise = new BasicExercise("name", new ArrayList<Set>());
        exerciseRepository.save(workoutId, 1, exercise);
        ExerciseId exerciseId = exercise.getExerciseId();

        exerciseRepository.save(workoutId, 2, exercise);
        assertThat(exercise.getExerciseId(), equalTo(exerciseId));
    }

    @Test
    public void updateExercises_shouldSaveTheCorrectEntity() {
        ExerciseId exerciseId1 = createExercise(0,"name1");
        ExerciseId exerciseId2 = createExercise(1,"name2");

        LinkedList<Exercise> exercises = exerciseRepository.allExercisesBelongsTo(workoutId);
        exercises.get(1).setName("newname2");
        exerciseRepository.save(workoutId, 1, exercises.get(1));

        LinkedList<Exercise> reloadedExercises = exerciseRepository.allExercisesBelongsTo(workoutId);
        assertThat(reloadedExercises.get(0).getExerciseId(), equalTo(exerciseId1));
        assertThat(reloadedExercises.get(0).getName(), equalTo("name1"));
        assertThat(reloadedExercises.get(1).getExerciseId(), equalTo(exerciseId2));
        assertThat(reloadedExercises.get(1).getName(), equalTo("newname2"));
    }

    @Test
    public void saveExercise_shouldAlsoSaveSets() {
        List<Set> sets = new ArrayList<>();
        Set set = new BasicSet(42, 12);
        sets.add(set);
        Exercise exercise = new BasicExercise("name", sets);

        exerciseRepository.save(workoutId, 1, exercise);

        List<Set> loadedSets = setRepository.allSetsBelongsTo(exercise.getExerciseId());
        assertThat(loadedSets.size(), equalTo(1));
        assertThat(loadedSets.get(0), equalTo(set));
    }

    @Test
    public void loadByUnknownWorkoutId_shouldReturnNullValue() {
        LinkedList<Exercise> exercises = exerciseRepository.allExercisesBelongsTo(new WorkoutId("21"));
        assertThat(exercises.size(), equalTo(0));
    }

    @Test
    public void loadAllExercisesBelongsTo_shouldReturnExercisesOfWorkout() throws Exception {
        ExerciseId exerciseId1 = createExercise(1, "name");
        ExerciseId exerciseId2 = createExercise(2, "name");

        LinkedList<Exercise> exercises = exerciseRepository.allExercisesBelongsTo(workoutId);

        assertThat(exercises.size(), equalTo(2));
        assertThat(exercises.get(0).getExerciseId(), equalTo(exerciseId1));
        assertThat(exercises.get(1).getExerciseId(), equalTo(exerciseId2));
    }

    @Test
    public void loadAllExercisesBelongsTo_shouldAddSetsToExercise() throws Exception {
        ArrayList<Set> sets = new ArrayList<>();
        Set set1 = new BasicSet(42, 12);
        sets.add(set1);
        Set set2 = new BasicSet(40, 10);
        sets.add(set2);
        Exercise exercise = new BasicExercise("name", sets);

        exerciseRepository.save(workoutId, 1, exercise);

        LinkedList<Exercise> exercises = exerciseRepository.allExercisesBelongsTo(workoutId);
        assertThat(exercises.get(0).getSets().size(), equalTo(2));
        assertThat(exercises.get(0).getSets().get(0), equalTo(set1));
        assertThat(exercises.get(0).getSets().get(1), equalTo(set2));
    }

    @Test
    public void loadAllExercisesBelongsTo_shouldLoadThemInCorrectOrder() throws Exception {
        ExerciseId exerciseId2 = createExercise(2, "name");
        ExerciseId exerciseId1 = createExercise(1, "name");
        ExerciseId exerciseId3 = createExercise(3, "name");
        ExerciseId exerciseId0 = createExercise(0, "name");

        LinkedList<Exercise> exercises = exerciseRepository.allExercisesBelongsTo(workoutId);

        assertThat(exercises.size(), equalTo(4));
        assertThat(exercises.get(0).getExerciseId(), equalTo(exerciseId0));
        assertThat(exercises.get(1).getExerciseId(), equalTo(exerciseId1));
        assertThat(exercises.get(2).getExerciseId(), equalTo(exerciseId2));
        assertThat(exercises.get(3).getExerciseId(), equalTo(exerciseId3));
    }

    @Test
    public void deleteExerciseWithNull_shouldDoNothing() throws Exception {
        exerciseRepository.delete(null);
    }

    @Test
    public void deleteExerciseByExerciseId_shouldRemoveItFromPersistence() throws Exception {
        ExerciseId exerciseId = createExercise(1, "name");

        exerciseRepository.delete(exerciseId);

        assertThat(exerciseRepository.allExercisesBelongsTo(workoutId).size(), equalTo(0));
    }
}