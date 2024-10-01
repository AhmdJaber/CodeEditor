package com.example.CodeEditor.model.component;

import lombok.Data;

import java.util.List;

@Data
public class Snippet {
    private Long id;
    private String name;
    private Folder parent;
    private String path;
    private String extension;
    private List<CodeLine> content;

    public Snippet(String name, Folder parent, String extension, List<CodeLine> content) {
        this.name = name;
        this.parent = parent;
        this.path = parent.getPath() + "/" + name;
        this.extension = extension;
        this.content = content;
    }
}
