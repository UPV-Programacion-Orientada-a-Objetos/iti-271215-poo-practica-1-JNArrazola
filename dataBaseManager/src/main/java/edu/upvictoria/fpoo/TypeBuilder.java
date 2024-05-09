package edu.upvictoria.fpoo;

/**
 * Class to represent the type of some row in the table
 * */
public class TypeBuilder {
    private String name;
    private boolean canBeNull;
    private int length;
    private String dataType;
    private boolean primaryKey;

    TypeBuilder(){};

    TypeBuilder(String name, boolean canBeNull, String dataType, int length, boolean primaryKey) {
        this.name = name;
        this.canBeNull = canBeNull;
        this.dataType = dataType.toLowerCase();
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

    public void setCanBeNull(boolean canBeNull) {
        this.canBeNull = canBeNull;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
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
