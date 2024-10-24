package com.example.CodeEditor.model.component;

import com.example.CodeEditor.model.component.files.FileNode;
import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
@ToString
public class ProjectStructure implements Serializable { //TODO: remove this class?
    @Serial
    private static final long serialVersionUID = 1L;
    private Map<Long, FileNode> tree;

    public ProjectStructure() {
        tree = new HashMap<>();
        tree.put(0L, new FileNode("", new ArrayList<>(), null));
    }

    public ProjectStructure(Map<Long, FileNode> tree) {
        this.tree = tree;
    }
}