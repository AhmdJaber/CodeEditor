package com.example.CodeEditor.services;

import com.example.CodeEditor.enums.Change;
import com.example.CodeEditor.model.clients.Client;
import com.example.CodeEditor.model.component.Comment;
import com.example.CodeEditor.model.component.ProjectStructure;
import com.example.CodeEditor.model.component.files.FileItem;
import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.component.files.Snippet;
import com.example.CodeEditor.repository.ProjectRepository;
import com.example.CodeEditor.services.storage.ProjectStorageService;
import com.example.CodeEditor.services.storage.SnippetStorageService;
import com.example.CodeEditor.services.storage.VCSStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SnippetService {
    @Autowired
    private VCSStorageService storageService;

    @Autowired
    private FileItemService fileItemService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SnippetStorageService snippetStorageService;

    @Autowired
    private ProjectStorageService projectStorageService;

    public Long createSnippet(Client editor, Snippet snippet, Long projectId) throws IOException {
        Long snippetId = fileItemService.createFile(new FileItem(snippet.getName(), snippet.getParentId())).getId();
        snippet.setId(snippetId);
        ProjectStructure projectStructure = projectStorageService.loadProjectStructure(editor, projectId);
        projectStructure.getTree().get(snippet.getParentId()).getFileItems().add(snippet);
        projectStorageService.saveProjectStructure(editor, projectStructure, projectId);
        snippetStorageService.createSnippet(editor, snippet, projectId);
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new NoSuchElementException("No Project with id " + projectId)
        );
        if (storageService.checkVCSProject(project)) {
            String branchName = storageService.getCurrentBranch(project);
            storageService.makeChange(project, branchName, '-', Change.CREATE, snippet);
        }
        return snippetId;
    }

    public void removeSnippet(Client editor, Snippet snippet, Long projectId) throws IOException {
        ProjectStructure projectStructure = projectStorageService.loadProjectStructure(editor, projectId);
        projectStructure.getTree().get(snippet.getParentId()).getFileItems().remove(snippet);
        projectStorageService.saveProjectStructure(editor, projectStructure, projectId);
        snippetStorageService.deleteSnippet(editor, snippet, projectId);
        fileItemService.removeFile(snippet.getId());
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new NoSuchElementException("No Project with id " + projectId)
        );
        if (storageService.checkVCSProject(project)) {
            String branchName = storageService.getCurrentBranch(project);
            storageService.makeChange(project, branchName, '-', Change.DELETE, snippet);
        }
    }

    public void updateSnippet(Client editor, Long id, String name, String updatedContent, Long projectId) {
        snippetStorageService.updateSnippet(editor, id, name, updatedContent, projectId);
        System.out.println("Snippet " + id + "_" + name + " has been updated");
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new NoSuchElementException("No Project with id " + projectId)
        );
        FileItem snippet = fileItemService.getFileById(id);
        if (storageService.checkVCSProject(project)) {
            String branchName = storageService.getCurrentBranch(project);
            storageService.makeChange(project, branchName, '-', Change.UPDATE, snippet);
        }
    }

    public String loadSnippet(Client editor, Long id, String name, Long projectId) throws Exception {
        return snippetStorageService.loadSnippet(editor, id, name, projectId);
    }

    public void comment(Client editor, Project project, Long snippetId, String comment, Integer start, Integer end) {
        snippetStorageService.comment(editor, project, snippetId, comment, start, end);
    }

    public List<Comment> getSnippetComments(Project project, Long snippetId) {
        return snippetStorageService.getSnippetComments(project, snippetId);
    }
}
