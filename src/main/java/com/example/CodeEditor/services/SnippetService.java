package com.example.CodeEditor.services;

import com.example.CodeEditor.model.component.Comment;
import com.example.CodeEditor.model.component.files.FileItem;
import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.component.files.Snippet;
import com.example.CodeEditor.model.clients.Client;
import com.example.CodeEditor.model.component.ProjectStructure;
import com.example.CodeEditor.repository.ProjectRepository;
import com.example.CodeEditor.vcs.Change;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
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
        Long snippetId = fileItemService.createFile(new FileItem(snippet.getName(), snippet.getParentId())).getId();
        snippet.setId(snippetId);
        ProjectStructure projectStructure = storageService.loadEditorDirObj(editor, projectId);
        projectStructure.getTree().get(snippet.getParentId()).getFileItems().add(snippet);
        storageService.saveProjectStructure(editor, projectStructure, projectId);
        storageService.createSnippet(editor, snippet, projectId);
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new NoSuchElementException("No Project with id " + projectId)
        );
        if (storageService.checkVCSProject(project)) {
            String branchName = storageService.vcsGetCurrentBranch(project);
            storageService.vcsMakeChange(project, branchName, '-', Change.CREATE, snippet);
        }
        return snippetId;
    }

    public void removeSnippet(Client editor, Snippet snippet, Long projectId) throws IOException {
        ProjectStructure projectStructure = storageService.loadEditorDirObj(editor, projectId);
        projectStructure.getTree().get(snippet.getParentId()).getFileItems().remove(snippet); // TODO: it deletes the object?
        storageService.saveProjectStructure(editor, projectStructure, projectId);
        storageService.deleteSnippet(editor, snippet, projectId);
        fileItemService.removeFile(snippet.getId()); // TODO: added, fine?
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new NoSuchElementException("No Project with id " + projectId)
        );
        if (storageService.checkVCSProject(project)) {
            String branchName = storageService.vcsGetCurrentBranch(project);
            storageService.vcsMakeChange(project, branchName, '-', Change.DELETE, snippet);
        }
    }

    public void updateSnippet(Client editor, Long id, String name, String updatedContent, Long projectId) throws IOException {
        storageService.updateSnippet(editor, id, name, updatedContent, projectId);
        System.out.println("Snippet " + id + "_" + name + " has been updated");
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new NoSuchElementException("No Project with id " + projectId)
        );
        FileItem snippet = fileItemService.getFileById(id);
        if (storageService.checkVCSProject(project)) {
            String branchName = storageService.vcsGetCurrentBranch(project);
            storageService.vcsMakeChange(project, branchName, '-', Change.UPDATE, snippet);
        }
    }

    public String loadSnippet(Client editor, Long id, String name, Long projectId) throws Exception {
        return storageService.loadSnippet(editor, id, name, projectId);
    }

    public void comment(Client editor, Project project, Long snippetId, String comment, Integer start, Integer end) {
        storageService.comment(editor, project, snippetId, comment, start, end);
    }

    public List<Comment> getSnippetComments(Project project, Long snippetId) {
        return storageService.getSnippetComments(project, snippetId);
    }
}
