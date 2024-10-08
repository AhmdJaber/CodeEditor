package com.example.CodeEditor.services;

import com.example.CodeEditor.model.component.files.File;
import com.example.CodeEditor.model.component.files.Snippet;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.model.users.editor.ProjectDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SnippetService {
    @Autowired
    private StorageService storageService;

    @Autowired
    private FileService fileService;

    public Long createSnippet(Client editor, Snippet snippet, Long projectId) throws IOException {
        Long snippetId = fileService.createFile(new File(snippet.getName(), snippet.getParentId())).getId();
        snippet.setId(snippetId);
        ProjectDirectory projectDirectory = storageService.loadEditorDirObj(editor, projectId);
        projectDirectory.getTree().get(snippet.getParentId()).getFiles().add(snippet);
        storageService.saveProjectDirectory(editor, projectDirectory, projectId);
        storageService.createSnippet(editor, snippet, projectId);
        return snippetId;
    }

    public void removeSnippet(Client editor, Snippet snippet, Long projectId) throws IOException {
        ProjectDirectory projectDirectory = storageService.loadEditorDirObj(editor, projectId);
        projectDirectory.getTree().get(snippet.getParentId()).getFiles().remove(snippet); // TODO: it deletes the object?
        storageService.saveProjectDirectory(editor, projectDirectory, projectId);
        storageService.deleteSnippet(editor, snippet, projectId);
    }

    public void updateSnippet(Client editor, Long id, String name, String updatedContent, Long projectId) throws IOException {
        storageService.updateSnippet(editor, id, name, updatedContent, projectId);
        System.out.println("Snippet " + id + "_" + name + " has been updated");
    }

    public String loadSnippet(Client editor, Long id, String name, Long projectId) throws IOException {
        return storageService.loadSnippet(editor, id, name, projectId);
    }
}
