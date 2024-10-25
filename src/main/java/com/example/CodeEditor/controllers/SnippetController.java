package com.example.CodeEditor.controllers;

import com.example.CodeEditor.model.clients.Client;
import com.example.CodeEditor.model.component.Comment;
import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.component.files.Snippet;
import com.example.CodeEditor.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/snippet")
@PreAuthorize("hasAnyRole('ADMIN', 'EDITOR')")
public class SnippetController {
    @Autowired
    private SnippetService snippetService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CodeExecutionService codeExecutionService;

    @PostMapping("/create/{ownerId}/{projectId}")
    public Snippet addSnippet(@RequestBody Snippet snippet, @PathVariable Long projectId, @PathVariable Long ownerId) throws Exception {
        Client editor = clientService.getClientById(ownerId);
        snippet.setId(snippetService.createSnippet(editor, snippet, projectId));
        return snippet;
    }

    @DeleteMapping("/delete/{ownerId}/{projectId}")
    public void deleteSnippet(@RequestBody Snippet snippet, @PathVariable Long projectId, @PathVariable Long ownerId) throws Exception {
        Client editor = clientService.getClientById(ownerId);
        snippetService.removeSnippet(editor, snippet, projectId);
    }

    @GetMapping("/content/{id}/{name}/{ownerId}/{projectId}")
    public String getSnippetContent(@PathVariable Long id, @PathVariable String name, @PathVariable Long projectId, @PathVariable Long ownerId) throws Exception {
        Client editor = clientService.getClientById(ownerId);
        return snippetService.loadSnippet(editor, id, name, projectId);
    }

    @PutMapping("/update/{id}/{name}/{ownerId}/{projectId}")
    public void updateSnippet(@PathVariable Long id, @PathVariable String name, @RequestBody String content, @PathVariable Long projectId, @PathVariable Long ownerId) throws IOException {
        Client editor = clientService.getClientById(ownerId);
        snippetService.updateSnippet(editor, id, name, content, projectId);
    }

    @PostMapping("/comment/{projectId}/{snippetId}")
    public void comment(@PathVariable Long projectId, @PathVariable Long snippetId, @RequestBody Map<String, String> body,  @RequestHeader("Authorization") String reqToken) {
        String senderEmail = jwtService.extractUsername(reqToken.replace("Bearer ", ""));
        Client editor = clientService.getClientByEmail(senderEmail);
        Project project = projectService.getProjectById(projectId);
        String comment = body.get("comment");
        Integer startLine = Integer.parseInt(body.get("start"));
        Integer endLine = Integer.parseInt(body.get("end"));
        snippetService.comment(editor, project, snippetId, comment, startLine, endLine);
    }

    @GetMapping("/get-comments/{projectId}/{snippetId}")
    public List<Comment> getSnippetComments(@PathVariable Long projectId, @PathVariable Long snippetId)  {
        Project project = projectService.getProjectById(projectId);
        return snippetService.getSnippetComments(project, snippetId);
    }

    @PostMapping("/execute")
    public String executeCode(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String language = body.get("language");

        System.out.println(language);
        return codeExecutionService.executeCode(code, language);
    }
}
