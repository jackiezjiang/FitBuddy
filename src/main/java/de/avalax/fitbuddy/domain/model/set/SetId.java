package de.avalax.fitbuddy.domain.model.set;

import java.io.Serializable;

public class SetId implements Serializable {
    private String id;

    public SetId(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SetId && id.equals(((SetId) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "SetId [id=" + id + "]";
    }
}
