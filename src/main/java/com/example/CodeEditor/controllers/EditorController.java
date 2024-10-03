package com.example.CodeEditor.controllers;

import com.example.CodeEditor.model.users.editor.Editor;
import com.example.CodeEditor.model.users.editor.EditorDirectory;
import com.example.CodeEditor.security.jwt.JWTService;
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
    private JWTService jwtService;

    @Autowired
    private StorageService storageService;

    @GetMapping("/get")
    public List<Editor> getAllEditors(){
        return editorService.getAllEditors();
    }

    @PostMapping("/login")
    public String login(@RequestBody Editor editor) { //TODO: is it better to return a ResponseEntity<> ??
        if (editorService.authenticate(editor)){
            return jwtService.getToken(editor.getEmail(), "editor");
        }
        return "";
    }

    @GetMapping("/directory")
    public EditorDirectory getEditorDirectory(@RequestHeader("Authorization") String token) throws IOException {
        String email = jwtService.getName(token.replace("Bearer ", ""));
        Editor editor = editorService.getEditorByEmail(email);
        if (editor == null) {
            throw new FileNotFoundException();
        }
        EditorDirectory editorDirectory = storageService.loadEditorDirObj(editor);
        System.out.println(editorDirectory);
        return editorDirectory;
    }
}
