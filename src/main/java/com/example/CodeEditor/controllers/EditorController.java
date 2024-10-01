package com.example.CodeEditor.controllers;

import com.example.CodeEditor.model.users.editor.Editor;
import com.example.CodeEditor.security.jwt.JWTService;
import com.example.CodeEditor.services.EditorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/editor")
@CrossOrigin("http://localhost:5000")
public class EditorController {
    @Autowired
    private EditorService editorService;

    @Autowired
    private JWTService jwtService;

    @GetMapping("/get")
    public List<Editor> getAllEditors(){
        return editorService.getAllEditors();
    }

    @PostMapping("/login")
    public String login(@RequestBody Editor editor) {
        if (editorService.authenticate(editor)){
            return jwtService.getToken(editor.getEmail(), "editor");
        }
        return "";
    }
}
