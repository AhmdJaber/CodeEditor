package com.example.CodeEditor.services;

import com.example.CodeEditor.model.component.files.File;
import com.example.CodeEditor.model.component.files.FileNode;
import com.example.CodeEditor.model.component.files.Folder;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.model.users.editor.ProjectDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;

@Service
public class FolderService {
    @Autowired
    private StorageService storageService;

    @Autowired
    private FileService fileService;

    public Long createFolder(Client editor, Folder folder, Long projectId) throws IOException {
        Long folderId = fileService.createFile(new File(folder.getName(), folder.getParentId())).getId();
        folder.setId(folderId);
        ProjectDirectory projectDirectory = storageService.loadEditorDirObj(editor, projectId);
        projectDirectory.getTree().get(folder.getParentId()).getFiles().add(folder);
        projectDirectory.getTree().put(folderId, new FileNode(folder.getName(), new ArrayList<>(), folder.getParentId()));
        storageService.saveProjectDirectory(editor, projectDirectory, projectId);
        return folderId;
    }

    public void removeFolder(Client editor, Folder folder, Long projectId) throws IOException {
        ProjectDirectory projectDirectory = storageService.loadEditorDirObj(editor, projectId);
        System.out.println("here is the info: "); //TODO: remove
        System.out.println(folder);
        System.out.println(projectDirectory);
        projectDirectory.getTree().get(folder.getParentId()).getFiles().remove(folder); // TODO: delete the object?
        projectDirectory.getTree().remove(folder.getId());
        storageService.saveProjectDirectory(editor, projectDirectory, projectId);
    }
}