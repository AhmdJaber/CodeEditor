package com.example.CodeEditor.vcs;

public enum Change {
    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE");

    private final String value;

    Change(String value){
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
