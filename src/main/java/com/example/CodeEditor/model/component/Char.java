package com.example.CodeEditor.model.component;

import lombok.Data;

@Data
public class Char {
    private Double id;
    private char character;

    public Char(Double id, char character) {
        this.id = id;
        this.character = character;
    }
}
