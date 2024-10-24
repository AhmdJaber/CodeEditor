package com.example.CodeEditor.controllers;

import com.example.CodeEditor.model.component.files.Folder;
import com.example.CodeEditor.model.clients.Client;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.services.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/folder")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class FolderController {
    @Autowired
    private FolderService folderService;

    @Autowired
    private ClientRepository clientRepository;

    @PostMapping("/create/{ownerId}/{projectId}")
    public Long addFolder(@RequestBody Folder folder, @PathVariable Long projectId, @PathVariable Long ownerId) throws IOException {
        Client editor = clientRepository.findById(ownerId).orElseThrow();
        return folderService.createFolder(editor, folder, projectId);
    }

    @DeleteMapping("/delete/{ownerId}/{projectId}")
    public void deleteFolder(@RequestBody Folder folder, @PathVariable Long projectId, @PathVariable Long ownerId) throws IOException {
        Client editor = clientRepository.findById(ownerId).orElseThrow();
        folderService.removeFolder(editor, folder, projectId);
    }
}