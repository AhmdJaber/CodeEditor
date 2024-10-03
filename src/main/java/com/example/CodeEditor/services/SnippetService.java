package com.example.CodeEditor.services;

import com.example.CodeEditor.model.component.files.File;
import com.example.CodeEditor.model.component.files.Snippet;
import com.example.CodeEditor.model.users.editor.Editor;
import com.example.CodeEditor.model.users.editor.EditorDirectory;
import com.example.CodeEditor.services.fileSystem.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

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
        storageService.createSnippet(editor, snippet);
        return snippetId;
    }

    public void removeSnippet(Editor editor, Snippet snippet) throws IOException {
        EditorDirectory editorDirectory = storageService.loadEditorDirObj(editor);
        editorDirectory.getTree().get(snippet.getParentId()).getFiles().remove(snippet); // TODO: it deletes the object?
        storageService.saveEditorDirObj(editor, editorDirectory);
        storageService.deleteSnippet(editor, snippet);
    }

    public void updateSnippet(Editor editor, Long id, String name, String updatedContent) throws IOException {
        storageService.updateSnippet(editor, id, name, updatedContent);
        System.out.println("Snippet " + id + "_" + name + " has been updated");
    }

    public String loadSnippet(Editor editor, Long id, String name) throws IOException {
        return storageService.loadSnippet(editor, id, name);
    }
}
