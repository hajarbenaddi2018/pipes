package com.github.users.dmoagx.pipes.model;

public enum FieldType {
    FORBIDDEN("X","0",0,false),
    MUST("!",">0",-1,false),
    EMPTY("_","?",0,false),
    PIPE_1("1","1",1,true),
    PIPE_2("2","2",2,true),
    PIPE_3("3","3",3,true),
    PIPE_4("4","4",4,true);

    private String serialization;
    private String display;
    private int value;
    private boolean isNumeric;

    private FieldType(String serValue,String displayValue, int intValue, boolean isNumeric) {
        this.serialization = serValue;
        this.display = displayValue;
        this.value = intValue;
        this.isNumeric = isNumeric;
    }

    public static FieldType FromString(String s) {
        if(s != null) {
            for (FieldType ft : FieldType.values()) {
                if(ft.serialization.equals(s))
                    return ft;
            }
        }
        throw new IllegalArgumentException("Unhandled FieldType=<"+s+">");
    }

    public int getIntValue() {
        if(isReal())
            return value;

        throw new RuntimeException("Asking a not-pipe field for it's intVal()!");
    }

    public String getStringValue() {
        return display;
    }

    public boolean isReal() {
        return isNumeric;
    }

    /**
     * Vergleicht die Anzahl der Rohre auf diesem Feld mit einer Zahl.
     * @param otherNum Eine beliebige Zahl.
     * @return Wenn auf diesem Feld so viele Rohre liegen wie otherNum true, sonst false.
     *
     * Die Funktion kann mit Feldern vom Typ EMPTY und FORBIDDEN benutzt werden, aber
     * MUST liefert immer false!
     */
    public boolean comparePipesTo(int otherNum) {
        return (value >= 0 && otherNum == value);
    }

    @Override
    public String toString() {
        return serialization;
    }
}
