package edu.upvictoria.fpoo;

/**
 * Class to represent the type of some row in the table
 * */
public class TypeBuilder {
    private final String name;
    private final boolean canBeNull;
    private final int length;
    private final String dataType;
    private final boolean primaryKey;

    TypeBuilder(String name, boolean canBeNull, String dataType, int length, boolean primaryKey) {
        this.name = name;
        this.canBeNull = canBeNull;
        this.dataType = dataType;
        this.length = length;
        this.primaryKey = primaryKey;
    }

    public String getName() {
        return name;
    }

    public boolean getCanBeNull(){
        return canBeNull;
    }

    public int getLength() {
        return length;
    }

    public String getDataType() {
        return dataType;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    @Override
    public String toString() {
        return "Registro {" +
                "\n\tName: " + name +
                "\n\tCan be null: " + canBeNull +
                "\n\tLength: " + length +
                "\n\tPrimary key: " + primaryKey+
                "\n}";
    }
}
