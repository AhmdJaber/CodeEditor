package com.example.CodeEditor.model.component;

import lombok.Data;

import java.util.List;

@Data
public class Folder {
    private String name;
    private String path;
    private List<Snippet> snippets;
}
