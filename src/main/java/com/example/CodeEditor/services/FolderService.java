package com.example.CodeEditor.services;

import com.example.CodeEditor.model.component.files.FileItem;
import com.example.CodeEditor.model.component.files.FileNode;
import com.example.CodeEditor.model.component.files.Folder;
import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.clients.Client;
import com.example.CodeEditor.model.component.ProjectStructure;
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
        Long folderId = fileItemService.createFile(new FileItem(folder.getName(), folder.getParentId())).getId();
        folder.setId(folderId);
        ProjectStructure projectStructure = storageService.loadEditorDirObj(editor, projectId);
        projectStructure.getTree().get(folder.getParentId()).getFileItems().add(folder);
        projectStructure.getTree().put(folderId, new FileNode(folder.getName(), new ArrayList<>(), folder.getParentId()));
        storageService.saveProjectStructure(editor, projectStructure, projectId);
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new NoSuchElementException("No Project with id " + projectId)
        );
        if (storageService.checkVCSProject(project)){
            String branchName = storageService.vcsGetCurrentBranch(project);
            storageService.vcsMakeChange(project, branchName, 'd', Change.CREATE, folder);
        }
        return folderId;
    }

    public void removeFolder(Client editor, Folder folder, Long projectId) throws IOException {
        ProjectStructure projectStructure = storageService.loadEditorDirObj(editor, projectId);
        System.out.println(projectStructure);
        projectStructure.getTree().get(folder.getParentId()).getFileItems().remove(folder);
        projectStructure.getTree().remove(folder.getId());
        storageService.saveProjectStructure(editor, projectStructure, projectId);
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new NoSuchElementException("No Project with id " + projectId)
        );
        if (storageService.checkVCSProject(project)) {
            String branchName = storageService.vcsGetCurrentBranch(project);
            storageService.vcsMakeChange(project, branchName, 'd', Change.DELETE, folder);
        }
    }
}