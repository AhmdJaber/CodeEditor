package com.example.CodeEditor.services;

import com.example.CodeEditor.model.component.files.File;
import com.example.CodeEditor.model.component.files.FileNode;
import com.example.CodeEditor.model.component.files.Folder;
import com.example.CodeEditor.model.users.editor.Editor;
import com.example.CodeEditor.model.users.editor.EditorDirectory;
import com.example.CodeEditor.services.fileSystem.StorageService;
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

    public Long createFolder(Editor editor, Folder folder) throws IOException {
        Long folderId = fileService.createFile(new File(folder.getName(), folder.getParentId())).getId();
        folder.setId(folderId);
        EditorDirectory editorDirectory = storageService.loadEditorDirObj(editor);
        editorDirectory.getTree().get(folder.getParentId()).getFiles().add(folder);
        editorDirectory.getTree().put(folderId, new FileNode(folder.getName(), new ArrayList<>(), folder.getParentId()));
        storageService.saveEditorDirObj(editor, editorDirectory);
        return folderId;
    }

    public void removeFolder(Editor editor, Folder folder) throws IOException {
        EditorDirectory editorDirectory = storageService.loadEditorDirObj(editor);
        System.out.println("here is the info: "); //TODO: remove
        System.out.println(folder);
        System.out.println(editorDirectory);
        editorDirectory.getTree().get(folder.getParentId()).getFiles().remove(folder); // TODO: delete the object?
        editorDirectory.getTree().remove(folder.getId());
        storageService.saveEditorDirObj(editor, editorDirectory);
    }
}