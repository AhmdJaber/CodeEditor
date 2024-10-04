package com.example.CodeEditor.controllers;

import com.example.CodeEditor.model.component.files.Folder;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.model.users.editor.Editor;
import com.example.CodeEditor.newSecurity.jwt.JwtService;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.services.EditorService;
import com.example.CodeEditor.services.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/folder")
@CrossOrigin("http://localhost:5000")
public class FolderController {
    @Autowired
    private FolderService folderService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EditorService editorService;
    @Autowired
    private ClientRepository clientRepository;

    @PostMapping("/create")
    public Long addFolder(@RequestBody Folder folder, @RequestHeader("Authorization") String reqToken) throws IOException {
        String token = reqToken.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        Client editor = clientRepository.findByEmail(email).orElseThrow();
        return folderService.createFolder(editor, folder);
    }

    @DeleteMapping("/delete")
    public void deleteFolder(@RequestBody Folder folder, @RequestHeader("Authorization") String reqToken) throws IOException {
        System.out.println(folder);
        String token = reqToken.replace("Bearer ", "");
        String email = jwtService.extractUsername(token);
        Client editor = clientRepository.findByEmail(email).orElseThrow();
        folderService.removeFolder(editor, folder);
    }
}