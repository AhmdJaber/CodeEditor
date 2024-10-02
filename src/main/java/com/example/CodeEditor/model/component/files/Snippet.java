package com.example.CodeEditor.model.component.files;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
public class Snippet extends File{
    private String content;

    public Snippet(String name, Long parentId, String content) {
        super.setName(name);
        super.setParentId(parentId);
        this.content = content;
    }
}