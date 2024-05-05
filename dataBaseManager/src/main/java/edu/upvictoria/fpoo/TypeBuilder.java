package edu.upvictoria.fpoo;

/**
 * Class to represent the type of some row in the table
 * */
public class TypeBuilder {
    private final String name;
    private final boolean isNull;
    private final int length;
    private final boolean primaryKey;

    TypeBuilder(String name, boolean isNull, int length, boolean primaryKey) {
        this.name = name;
        this.isNull = isNull;
        this.length = length;
        this.primaryKey = primaryKey;
    }

    public String getName() {
        return name;
    }

    public boolean getIsNull(){
        return isNull;
    }

    public int getLength() {
        return length;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }
}
