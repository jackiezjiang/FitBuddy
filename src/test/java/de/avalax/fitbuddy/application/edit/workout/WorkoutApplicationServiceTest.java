package de.avalax.fitbuddy.application.edit.workout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import de.avalax.fitbuddy.application.workout.WorkoutSession;
import de.avalax.fitbuddy.domain.model.exercise.BasicExercise;
import de.avalax.fitbuddy.domain.model.exercise.Exercise;
import de.avalax.fitbuddy.domain.model.exercise.ExerciseId;
import de.avalax.fitbuddy.domain.model.exercise.ExerciseRepository;
import de.avalax.fitbuddy.domain.model.finishedWorkout.FinishedWorkoutRepository;
import de.avalax.fitbuddy.domain.model.set.Set;
import de.avalax.fitbuddy.domain.model.set.SetId;
import de.avalax.fitbuddy.domain.model.set.SetRepository;
import de.avalax.fitbuddy.domain.model.workout.BasicWorkout;
import de.avalax.fitbuddy.domain.model.workout.Workout;
import de.avalax.fitbuddy.domain.model.workout.WorkoutException;
import de.avalax.fitbuddy.domain.model.workout.WorkoutId;
import de.avalax.fitbuddy.domain.model.workout.WorkoutListEntry;
import de.avalax.fitbuddy.domain.model.workout.WorkoutRepository;
import de.avalax.fitbuddy.domain.model.workout.WorkoutService;
import de.bechte.junit.runners.context.HierarchicalContextRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(HierarchicalContextRunner.class)
public class WorkoutApplicationServiceTest {

    @Mock
    private WorkoutRepository workoutRepository;

    @Mock
    private ExerciseRepository exerciseRepository;
    @Mock
    private SetRepository setRepository;

    @Mock
    private WorkoutService workoutService;

    @Mock
    private WorkoutSession workoutSession;

    @Mock
    private FinishedWorkoutRepository finishedWorkoutRepository;

    @InjectMocks
    private EditWorkoutApplicationService editWorkoutApplicationService;

    private Workout workout;

    @Before
    public void setUp() throws Exception {
        workout = new BasicWorkout();
        MockitoAnnotations.initMocks(this);
        workout.setWorkoutId(new WorkoutId("123456"));
    }

    @Test
    public void afterInitialization_shouldHideUnsavedChanges() throws Exception {
        assertThat(editWorkoutApplicationService.hasUnsavedChanges(), equalTo(false));
    }

    @Test
    public void getWorkoutList_shouldWorkoutListFromRepository() throws Exception {
        List<WorkoutListEntry> workoutListEntries = new ArrayList<>();
        when(workoutRepository.getWorkoutList()).thenReturn(workoutListEntries);
        List<WorkoutListEntry> workoutList = editWorkoutApplicationService.getWorkoutList();
        assertThat(workoutList, equalTo(workoutListEntries));
    }

    @Test
    public void createWorkout_shouldPersistTheCreatedWorkout() throws Exception {
        Workout newWorkout = editWorkoutApplicationService.createWorkout();
        verify(workoutRepository).save(newWorkout);
        assertThat(newWorkout, not(equalTo(workout)));
        assertThat(editWorkoutApplicationService.hasUnsavedChanges(), equalTo(false));
    }

    @Test
    public void createWorkoutFromJson_shouldPersistTheCreatedWorkout() throws Exception {
        when(workoutService.workoutFromJson("jsonstring")).thenReturn(workout);
        editWorkoutApplicationService.createWorkoutFromJson("jsonstring");
        verify(workoutRepository).save(workout);
        assertThat(editWorkoutApplicationService.hasUnsavedChanges(), equalTo(false));
    }

    @Test
    public void deleteUnknownWorkout_shouldDoNothing() throws Exception {
        editWorkoutApplicationService.deleteWorkout();

        verifyNoMoreInteractions(workoutRepository);
    }

    public class givenAWorkoutWithOneExercise {
        private WorkoutId workoutId;

        private Exercise exercise;

        @Before
        public void setUp() throws Exception {
            workoutId = new WorkoutId("42");
            workout.setWorkoutId(workoutId);
            when(workoutRepository.load(workoutId)).thenReturn(workout);
            exercise = workout.createExercise();
            exercise.setName("ExerciseOne");

            editWorkoutApplicationService.setWorkout(workoutId);
        }

        @Test
        public void changeWorkoutName_shouldSavePersistence() throws Exception {
            String name = "new name";
            editWorkoutApplicationService.changeName(name);

            assertThat(workout.getName(), equalTo(name));
            verify(workoutRepository).save(workout);
            assertThat(editWorkoutApplicationService.hasUnsavedChanges(), equalTo(false));
        }

        @Test
        public void deleteWorkout_shouldRemoveTheWorkoutFromThePersistence() throws Exception {
            editWorkoutApplicationService.deleteWorkout();

            verify(workoutRepository).delete(workoutId);
            assertThat(editWorkoutApplicationService.getWorkout(), equalTo(null));
            assertThat(editWorkoutApplicationService.hasUnsavedChanges(), equalTo(true));
            assertThat(editWorkoutApplicationService.hasDeletedWorkout(), equalTo(true));
        }

        @Test
        public void undoDeleteWorkout_shouldReinsertTheWorkoutToThePersistence() throws Exception {
            editWorkoutApplicationService.deleteWorkout();

            editWorkoutApplicationService.undoDeleteWorkout();

            verify(workoutRepository).save(workout);
            assertThat(editWorkoutApplicationService.getWorkout(), equalTo(workout));
            assertThat(editWorkoutApplicationService.hasUnsavedChanges(), equalTo(false));
            assertThat(editWorkoutApplicationService.hasDeletedWorkout(), equalTo(false));
        }

        @Test
        public void createExercise_shouldPersistTheExercise() throws Exception {
            editWorkoutApplicationService.createExercise();

            verify(exerciseRepository).save(workout.getWorkoutId(), 1, workout.exerciseAtPosition(1));
            assertThat(editWorkoutApplicationService.hasUnsavedChanges(), equalTo(false));
        }

        @Test
        public void saveExercise_shouldSaveExerciseInRepository() throws Exception {
            Exercise exercise = workout.createExercise();
            exercise.setExerciseId(new ExerciseId("42"));
            Exercise changedExercise = new BasicExercise();
            changedExercise.setName("changed exercise");
            changedExercise.setExerciseId(exercise.getExerciseId());

            editWorkoutApplicationService.saveExercise(changedExercise, 1);

            assertThat(workout.exerciseAtPosition(1).getName(), equalTo("changed exercise"));
            verify(exerciseRepository).save(workout.getWorkoutId(), 1, changedExercise);
            assertThat(editWorkoutApplicationService.hasUnsavedChanges(), equalTo(false));
        }

        @Test
        public void switchWorkout_shouldSetWorkout() throws Exception {
            when(workoutSession.getWorkout()).thenThrow(new WorkoutException());
            editWorkoutApplicationService.switchWorkout();

            verify(workoutSession).switchWorkout(workout);
            assertThat(editWorkoutApplicationService.hasUnsavedChanges(), equalTo(false));
        }

        @Test
        public void switchWorkout_shouldPersistCurrentWorkout() throws Exception {
            BasicWorkout currentWorkoutToPersist = new BasicWorkout();
            currentWorkoutToPersist.setName("currentWorkoutToPersist");
            when(workoutSession.getWorkout()).thenReturn(currentWorkoutToPersist);

            editWorkoutApplicationService.switchWorkout();

            verify(finishedWorkoutRepository).saveWorkout(currentWorkoutToPersist);
        }

        public class moveExercises {
            @Test
            public void moveFirstExerciseAtPositionUp_shouldDoNothing() throws Exception {
                editWorkoutApplicationService.moveExerciseAtPositionUp(0);

                verify(workoutRepository, never()).save(workout);
                assertThat(workout.exerciseAtPosition(0), equalTo(exercise));
                assertThat(editWorkoutApplicationService.hasUnsavedChanges(), equalTo(false));
            }

            @Test
            public void moveExerciseAtPositionUp_shouldPlaceTheExerciseAtPosition0() throws Exception {
                Exercise exerciseToMove = workout.createExercise();
                exerciseToMove.setName("ExerciseTwo");

                editWorkoutApplicationService.moveExerciseAtPositionUp(1);

                verify(workoutRepository).save(workout);
                assertThat(workout.exerciseAtPosition(0), equalTo(exerciseToMove));
                assertThat(editWorkoutApplicationService.hasUnsavedChanges(), equalTo(false));
            }

            @Test
            public void moveLastExerciseAtPositionDown_shouldDoNothing() throws Exception {
                Exercise lastExercise = workout.createExercise();

                editWorkoutApplicationService.moveExerciseAtPositionDown(1);

                verify(workoutRepository, never()).save(workout);
                assertThat(workout.exerciseAtPosition(1), equalTo(lastExercise));
                assertThat(editWorkoutApplicationService.hasUnsavedChanges(), equalTo(false));
            }

            @Test
            public void moveExerciseAtPositionDown_shouldPlaceTheExerciseAtTheRightPosition() throws Exception {
                Exercise exerciseToMove = workout.createExercise();
                exerciseToMove.setName("ExerciseToMove");
                Exercise lastExercise = workout.createExercise();
                lastExercise.setName("ExerciseLast");

                editWorkoutApplicationService.moveExerciseAtPositionDown(1);

                verify(workoutRepository).save(workout);
                assertThat(workout.exerciseAtPosition(2), equalTo(exerciseToMove));
                assertThat(editWorkoutApplicationService.hasUnsavedChanges(), equalTo(false));
            }
        }

        public class exerciseManipulation {
            private List<Integer> positions;

            @Before
            public void setUp() throws Exception {
                positions = new ArrayList<>();
            }

            @Test
            public void deleteExercise_shouldDeleteThePersistdExercise() throws Exception {
                ExerciseId exerciseId = new ExerciseId("42");
                exercise.setExerciseId(exerciseId);
                positions.add(0);

                editWorkoutApplicationService.deleteExercise(positions);

                assertThat(workout.countOfExercises(), equalTo(0));
                verify(exerciseRepository).delete(exerciseId);
                assertThat(editWorkoutApplicationService.hasUnsavedChanges(), equalTo(true));
                assertThat(editWorkoutApplicationService.hasDeletedExercise(), equalTo(true));
            }

            @Test
            public void undoDeleteExercise_shouldReinsertTheExerciseToThePersistence() throws Exception {
                int size = workout.countOfExercises();
                positions.add(0);
                editWorkoutApplicationService.deleteExercise(positions);

                editWorkoutApplicationService.undoDeleteExercise();

                verify(exerciseRepository).save(workout.getWorkoutId(), 0, exercise);
                assertThat(editWorkoutApplicationService.getWorkout().countOfExercises(), equalTo(size));
                assertThat(editWorkoutApplicationService.hasUnsavedChanges(), equalTo(false));
                assertThat(editWorkoutApplicationService.hasDeletedExercise(), equalTo(false));
            }

            @Test
            public void undoDeleteExercise_shouldReinsertTheExerciseAtOldPosition() throws Exception {
                Exercise exerciseToRestore = workout.createExercise();
                workout.createExercise();
                positions.add(1);

                editWorkoutApplicationService.deleteExercise(positions);
                editWorkoutApplicationService.undoDeleteExercise();

                assertThat(workout.exerciseAtPosition(1), equalTo(exerciseToRestore));
            }

            @Test
            public void undoDeleteExercises_shouldReinsertBothExercisesAtOldPosition() throws Exception {
                Exercise exerciseToRestore = workout.createExercise();
                exerciseToRestore.setName("exericseToRestore");
                Exercise secondExerciseToRestore = workout.createExercise();
                secondExerciseToRestore.setName("secondExercise");
                positions.add(1);
                positions.add(0);

                editWorkoutApplicationService.deleteExercise(positions);
                editWorkoutApplicationService.undoDeleteExercise();

                assertThat(workout.exerciseAtPosition(0), equalTo(exercise));
                assertThat(workout.exerciseAtPosition(1), equalTo(exerciseToRestore));
            }

            @Test
            public void undoDeleteExerciseAfterDeleteAnWorkout_shouldReinsertTheExercise() throws Exception {
                positions.add(0);
                editWorkoutApplicationService.createWorkout();
                editWorkoutApplicationService.deleteWorkout();
                editWorkoutApplicationService.setWorkout(workout.getWorkoutId());
                editWorkoutApplicationService.deleteExercise(positions);

                editWorkoutApplicationService.undoDeleteExercise();

                assertThat(editWorkoutApplicationService.hasDeletedExercise(), equalTo(false));
                assertThat(editWorkoutApplicationService.hasDeletedWorkout(), equalTo(false));
            }

            @Test
            public void undoDeleteWorkoutAfterDeleteAnExercise_shouldReinsertTheWorkout() throws Exception {
                positions.add(0);
                editWorkoutApplicationService.deleteExercise(positions);
                editWorkoutApplicationService.deleteWorkout();

                editWorkoutApplicationService.undoDeleteWorkout();
                assertThat(editWorkoutApplicationService.hasDeletedExercise(), equalTo(false));
                assertThat(editWorkoutApplicationService.hasDeletedWorkout(), equalTo(false));
            }

            @Test
            public void changeSetAmountToSameAmount_shouldDoNothing() throws Exception {
                Exercise exercise = new BasicExercise();
                exercise.createSet();

                editWorkoutApplicationService.changeSetAmount(exercise, 1);

                verifyNoMoreInteractions(setRepository);
            }

            @Test
            public void changeSetAmountWithoutSets_shouldAddOneSet() throws Exception {
                ExerciseId exerciseId = new ExerciseId("21");
                Exercise exercise = new BasicExercise();
                exercise.setExerciseId(exerciseId);

                editWorkoutApplicationService.changeSetAmount(exercise, 1);

                verify(setRepository).save(exerciseId, exercise.setAtPosition(0));
            }

            @Test
            public void changeSetAmountWithoutSets_shouldAddTwoSets() throws Exception {
                ExerciseId exerciseId = new ExerciseId("21");
                Exercise exercise = new BasicExercise();
                exercise.setExerciseId(exerciseId);

                editWorkoutApplicationService.changeSetAmount(exercise, 2);

                assertThat(exercise.countOfSets(), equalTo(2));
            }

            @Test
            public void changeSetAmountToOne_shouldRemoveOneSet() throws Exception {
                ExerciseId exerciseId = new ExerciseId("21");
                Exercise exercise = new BasicExercise();
                exercise.setExerciseId(exerciseId);
                Set set1 = exercise.createSet();
                set1.setSetId(new SetId("42"));
                Set set2 = exercise.createSet();

                editWorkoutApplicationService.changeSetAmount(exercise, 1);

                assertThat(exercise.setAtPosition(0), equalTo(set2));
                verify(setRepository).delete(set1.getSetId());
            }

            @Test
            public void changeSetAmountToOne_shouldRemoveTwoSets() throws Exception {
                ExerciseId exerciseId = new ExerciseId("21");
                Exercise exercise = new BasicExercise();
                exercise.setExerciseId(exerciseId);
                exercise.createSet();
                exercise.createSet();
                exercise.createSet();

                editWorkoutApplicationService.changeSetAmount(exercise, 1);

                assertThat(exercise.countOfSets(), equalTo(1));
            }

            @Test
            public void changeSetAmountWithOneSet_shouldAddASecondEqualSet() throws Exception {
                ExerciseId exerciseId = new ExerciseId("21");
                Exercise exercise = new BasicExercise();
                exercise.setExerciseId(exerciseId);
                Set set = exercise.createSet();
                set.setMaxReps(12);
                set.setWeight(42.0);

                editWorkoutApplicationService.changeSetAmount(exercise, 2);

                Set newSet = exercise.setAtPosition(1);

                assertThat(newSet.getMaxReps(), equalTo(12));
                assertThat(newSet.getWeight(), equalTo(42.0));
                verify(setRepository).save(exerciseId, newSet);
            }
        }
    }
}