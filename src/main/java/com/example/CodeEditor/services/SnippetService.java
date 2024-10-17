package com.example.CodeEditor.services;

import com.example.CodeEditor.model.component.files.FileItem;
import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.component.files.Snippet;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.model.users.editor.ProjectDirectory;
import com.example.CodeEditor.repository.ProjectRepository;
import com.example.CodeEditor.vcs.Change;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class SnippetService {
    @Autowired
    private StorageService storageService;

    @Autowired
    private FileItemService fileItemService;

    @Autowired
    private ProjectRepository projectRepository;

    public Long createSnippet(Client editor, Snippet snippet, Long projectId) throws IOException { //TODO: extract method for duplicates
        String branchName = "main"; //TODO: get the branch name!!
        Long snippetId = fileItemService.createFile(new FileItem(snippet.getName(), snippet.getParentId())).getId();
        snippet.setId(snippetId);
        ProjectDirectory projectDirectory = storageService.loadEditorDirObj(editor, projectId);
        projectDirectory.getTree().get(snippet.getParentId()).getFileItems().add(snippet);
        storageService.saveProjectDirectory(editor, projectDirectory, projectId);
        storageService.createSnippet(editor, snippet, projectId);
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new NoSuchElementException("No Project with id " + projectId)
        );
        storageService.vcsMakeChange(project, branchName, '-', Change.CREATE, snippet);
        return snippetId;
    }

    public void removeSnippet(Client editor, Snippet snippet, Long projectId) throws IOException {
        String branchName = "main"; //TODO: get the branch name!!
        ProjectDirectory projectDirectory = storageService.loadEditorDirObj(editor, projectId);
        projectDirectory.getTree().get(snippet.getParentId()).getFileItems().remove(snippet); // TODO: it deletes the object?
        storageService.saveProjectDirectory(editor, projectDirectory, projectId);
        storageService.deleteSnippet(editor, snippet, projectId);
        fileItemService.removeFile(snippet.getId()); // TODO: added, fine?
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new NoSuchElementException("No Project with id " + projectId)
        );
        storageService.vcsMakeChange(project, branchName, '-', Change.DELETE, snippet);
    }

    public void updateSnippet(Client editor, Long id, String name, Map<String, Object> updatedContent, Long projectId) throws IOException {
        String branchName = "main"; //TODO: get the branch name!!
        storageService.updateSnippet(editor, id, name, updatedContent, projectId);
        System.out.println("Snippet " + id + "_" + name + " has been updated");
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new NoSuchElementException("No Project with id " + projectId)
        );
        FileItem snippet = fileItemService.getFileById(id);
        storageService.vcsMakeChange(project, branchName, '-', Change.UPDATE, snippet);
    }

    public String loadSnippet(Client editor, Long id, String name, Long projectId) throws IOException {
        return storageService.loadSnippet(editor, id, name, projectId);
    }
}
