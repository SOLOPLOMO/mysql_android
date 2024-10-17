package gr.app.JNITHESIS.models;

import java.io.Serializable;

public class Database implements Serializable {
    public String name;
    public String collation;
    public String getName() {
        return name;
    }
}
