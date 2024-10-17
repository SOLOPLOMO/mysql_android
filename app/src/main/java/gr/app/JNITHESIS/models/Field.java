package gr.app.JNITHESIS.models;

import java.io.Serializable;

public class Field implements Serializable {
    public String name;
    public String type;
    public boolean nullable = false;
    public boolean primary_key = false;
    public boolean isUnique = false;

    public String toString(){
        return name + "\n" + type + "\n" + (nullable?"NULL\n":"NOT NULL\n") + (primary_key?"PRIMARY KEY\n":"") + (isUnique?"UNIQUE\n":"");
    }

}
