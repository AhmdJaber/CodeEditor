package com.example.CodeEditor.model.users.editor;

import com.example.CodeEditor.model.component.files.File;
import com.example.CodeEditor.model.component.files.FileNode;
import lombok.Data;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ToString
public class EditorDirectory implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Map<Long, FileNode> tree;

    public EditorDirectory() {
        tree = new HashMap<>();
        tree.put(0L, new FileNode("", new ArrayList<>(), null));
    }

    public List<File> getAllFiles() {
        List<File> filesList = new ArrayList<>();
        for (FileNode node : tree.values()) {
            filesList.addAll(node.getFiles());
        }
        return filesList;
    }
}