package de.avalax.fitbuddy.port.adapter.persistence;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import de.avalax.fitbuddy.domain.model.exercise.Exercise;
import de.avalax.fitbuddy.domain.model.finishedExercise.FinishedExercise;
import de.avalax.fitbuddy.domain.model.finishedExercise.FinishedExerciseRepository;
import de.avalax.fitbuddy.domain.model.finishedWorkout.BasicFinishedWorkout;
import de.avalax.fitbuddy.domain.model.finishedWorkout.FinishedWorkout;
import de.avalax.fitbuddy.domain.model.finishedWorkout.FinishedWorkoutId;
import de.avalax.fitbuddy.domain.model.finishedWorkout.FinishedWorkoutException;
import de.avalax.fitbuddy.domain.model.finishedWorkout.FinishedWorkoutRepository;
import de.avalax.fitbuddy.domain.model.workout.Workout;
import de.avalax.fitbuddy.domain.model.workout.WorkoutId;

public class SQLiteFinishedWorkoutRepository implements FinishedWorkoutRepository {
    private SQLiteOpenHelper sqLiteOpenHelper;
    private FinishedExerciseRepository finishedExerciseRepository;

    public SQLiteFinishedWorkoutRepository(
            SQLiteOpenHelper sqLiteOpenHelper,
            FinishedExerciseRepository finishedExerciseRepository) {
        this.sqLiteOpenHelper = sqLiteOpenHelper;
        this.finishedExerciseRepository = finishedExerciseRepository;
    }

    @Override
    public FinishedWorkoutId saveWorkout(Workout workout) {
        SQLiteDatabase database = sqLiteOpenHelper.getWritableDatabase();
        long id = database.insertOrThrow("finished_workout", null, getContentValues(workout));
        FinishedWorkoutId finishedWorkoutId = new FinishedWorkoutId(String.valueOf(id));
        for (Exercise exercise : workout.exercisesOfWorkout()) {
            finishedExerciseRepository.save(finishedWorkoutId, exercise);
        }
        database.close();
        return finishedWorkoutId;
    }

    @Override
    public FinishedWorkout load(FinishedWorkoutId finishedWorkoutId)
            throws FinishedWorkoutException {
        if (finishedWorkoutId == null) {
            throw new FinishedWorkoutException();
        }
        SQLiteDatabase database = sqLiteOpenHelper.getReadableDatabase();
        String[] columns = {"id", "workout_id", "name", "created"};
        String[] args = {finishedWorkoutId.id()};
        Cursor cursor = database.query("finished_workout", columns,
                "id=?", args, null, null, null);
        if (cursor.moveToFirst()) {
            FinishedWorkout finishedWorkout = createFinishedWorkout(cursor);
            cursor.close();
            database.close();
            return finishedWorkout;
        } else {
            cursor.close();
            database.close();
            throw new FinishedWorkoutException();
        }
    }

    @Override
    public List<FinishedWorkout> loadAll() {
        List<FinishedWorkout> finishedWorkouts = new ArrayList<>();
        SQLiteDatabase database = sqLiteOpenHelper.getReadableDatabase();
        String[] columns = {"id", "workout_id", "name", "created"};
        Cursor cursor = database.query("finished_workout", columns,
                null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                finishedWorkouts.add(createFinishedWorkout(cursor));
            } while (cursor.moveToNext());
            cursor.close();
            database.close();
        }
        return finishedWorkouts;
    }

    private FinishedWorkout createFinishedWorkout(Cursor cursor) {
        FinishedWorkoutId finishedWorkoutId = new FinishedWorkoutId(cursor.getString(0));
        WorkoutId workoutId = new WorkoutId(cursor.getString(1));
        List<FinishedExercise> exercises = addFinishedExercises(finishedWorkoutId);
        String name = cursor.getString(2);
        String created = cursor.getString(3);

        return new BasicFinishedWorkout(finishedWorkoutId, workoutId, name, created, exercises);
    }

    private List<FinishedExercise> addFinishedExercises(FinishedWorkoutId finishedWorkoutId) {
        return finishedExerciseRepository.allSetsBelongsTo(finishedWorkoutId);
    }

    private ContentValues getContentValues(Workout workout) {
        ContentValues values = new ContentValues();
        values.put("name", workout.getName());
        String workoutId = workout.getWorkoutId() != null ? workout.getWorkoutId().id() : null;
        values.put("workout_id", workoutId);
        return values;
    }
}
