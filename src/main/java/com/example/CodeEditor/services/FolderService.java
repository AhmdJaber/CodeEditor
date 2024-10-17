package com.example.CodeEditor.services;

import com.example.CodeEditor.model.component.files.FileItem;
import com.example.CodeEditor.model.component.files.FileNode;
import com.example.CodeEditor.model.component.files.Folder;
import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.model.users.editor.ProjectDirectory;
import com.example.CodeEditor.repository.ProjectRepository;
import com.example.CodeEditor.vcs.Change;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

@Service
public class FolderService {
    @Autowired
    private StorageService storageService;

    @Autowired
    private FileItemService fileItemService;

    @Autowired
    private ProjectRepository projectRepository;

    public Long createFolder(Client editor, Folder folder, Long projectId) throws IOException {
        String branchName = "main"; //TODO: get the branch name !!
        Long folderId = fileItemService.createFile(new FileItem(folder.getName(), folder.getParentId())).getId();
        folder.setId(folderId);
        ProjectDirectory projectDirectory = storageService.loadEditorDirObj(editor, projectId);
        projectDirectory.getTree().get(folder.getParentId()).getFileItems().add(folder);
        projectDirectory.getTree().put(folderId, new FileNode(folder.getName(), new ArrayList<>(), folder.getParentId()));
        storageService.saveProjectDirectory(editor, projectDirectory, projectId);
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new NoSuchElementException("No Project with id " + projectId)
        );
        storageService.vcsMakeChange(project, branchName, 'd', Change.CREATE, folder);
        return folderId;
    }

    public void removeFolder(Client editor, Folder folder, Long projectId) throws IOException {
        String branchName = "main"; //TODO: get the branch name !!
        ProjectDirectory projectDirectory = storageService.loadEditorDirObj(editor, projectId);
        System.out.println(projectDirectory);
        projectDirectory.getTree().get(folder.getParentId()).getFileItems().remove(folder);
        projectDirectory.getTree().remove(folder.getId());
        storageService.saveProjectDirectory(editor, projectDirectory, projectId);
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new NoSuchElementException("No Project with id " + projectId)
        );
        storageService.vcsMakeChange(project, branchName, 'd', Change.DELETE, folder);
    }
}