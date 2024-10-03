package com.example.CodeEditor.services;

import com.example.CodeEditor.model.component.files.File;
import com.example.CodeEditor.model.component.files.FileNode;
import com.example.CodeEditor.model.component.files.Folder;
import com.example.CodeEditor.model.component.files.Snippet;
import com.example.CodeEditor.model.users.editor.Editor;
import com.example.CodeEditor.model.users.editor.EditorDirectory;
import com.example.CodeEditor.services.fileSystem.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;

@Service
public class SnippetService {
    @Autowired
    private StorageService storageService;

    @Autowired
    private FileService fileService;

    public Long createSnippet(Editor editor, Snippet snippet) throws IOException {
        Long snippetId = fileService.createFile(new File(snippet.getName(), snippet.getParentId())).getId();
        snippet.setId(snippetId);
        EditorDirectory editorDirectory = storageService.loadEditorDirObj(editor);
        editorDirectory.getTree().get(snippet.getParentId()).getFiles().add(snippet);
        storageService.saveEditorDirObj(editor, editorDirectory);
        return snippetId;
    }

    public void removeSnippet(Editor editor, Snippet snippet) throws IOException {
        EditorDirectory editorDirectory = storageService.loadEditorDirObj(editor);
        System.out.println("here is the info: "); //TODO: remove
        System.out.println(snippet);
        System.out.println(editorDirectory);
        editorDirectory.getTree().get(snippet.getParentId()).getFiles().remove(snippet); // TODO: is it really deletes the object?
        storageService.saveEditorDirObj(editor, editorDirectory);
    }
}
