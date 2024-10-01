package com.example.CodeEditor.model.component;

import lombok.Data;

import java.util.List;

@Data
public class CodeLine {
    private int lineNumber;
    private List<Char> characters;
}
