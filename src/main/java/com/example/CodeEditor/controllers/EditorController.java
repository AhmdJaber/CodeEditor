package com.example.CodeEditor.controllers;

import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.model.users.editor.Editor;
import com.example.CodeEditor.model.users.editor.EditorDirectory;
import com.example.CodeEditor.newSecurity.jwt.JwtService;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.services.EditorService;
import com.example.CodeEditor.services.fileSystem.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/editor")
@CrossOrigin("http://localhost:5000")
public class EditorController {
    @Autowired
    private EditorService editorService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private StorageService storageService;
    @Autowired
    private ClientRepository clientRepository;

    @GetMapping("/get")
    public List<Editor> getAllEditors(){
        return editorService.getAllEditors();
    }

    @GetMapping("/directory")
    public EditorDirectory getEditorDirectory(@RequestHeader("Authorization") String token) throws IOException { // TODO: clean "security"?
        String email = jwtService.extractUsername(token.replace("Bearer ", ""));
        Client editor = clientRepository.findByEmail(email).orElseThrow();
        EditorDirectory editorDirectory = storageService.loadEditorDirObj(editor);
        System.out.println(editorDirectory);
        return editorDirectory;
    }
}
