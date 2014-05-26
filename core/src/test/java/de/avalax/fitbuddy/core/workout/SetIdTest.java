package de.avalax.fitbuddy.core.workout;

import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class SetIdTest {

    @Test
    public void testSameIdentity() throws Exception {
        assertThat(new SetId(42),equalTo(new SetId(42)));
        assertThat(new SetId(42).hashCode(),equalTo(new SetId(42).hashCode()));
    }
}