package com.example.CodeEditor.controllers;

import com.example.CodeEditor.model.clients.Client;
import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.services.ClientService;
import com.example.CodeEditor.services.ProjectService;
import com.example.CodeEditor.services.storage.ProjectStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private ProjectStorageService projectStorageService;

    @GetMapping("/get-editors")
    public List<Client> getAllEditors(){
        return clientService.getAllEditors();
    }

    @DeleteMapping("/remove-editor/{editorId}")
    public void removeEditor(@PathVariable Long editorId){
        clientService.deleteEditor(editorId);
    }

    @GetMapping("/get-editor-projects/{editorId}")
    public List<Project> getEditorProjects(@PathVariable Long editorId){
        Client client = clientService.getClientById(editorId);
        return projectService.getClientProjects(client);
    }

    @DeleteMapping("/remvoe-project/{editorId}/{projectId}")
    public void removeEditorProject(@PathVariable Long editorId, @PathVariable Long projectId){
        Client client = clientService.getClientById(editorId);
        projectService.deleteProjectById(projectId);
        projectStorageService.deleteProject(client, projectId);
    }

    @GetMapping("/get-shared/{editorId}")
    public List<Project> getSharedProjects(@PathVariable Long editorId){
        Client client = clientService.getClientById(editorId);
        return projectService.getSharedEditProjects(client);
    }

    @DeleteMapping("/remove-shared-project/{editorId}/{projectId}")
    public void removeSharedProject(@PathVariable Long editorId, @PathVariable Long projectId){
        Client client = clientService.getClientById(editorId);
        projectStorageService.removesharedProject(client, projectId);
    }

    @GetMapping("/get-shared/{projectId}/{ownerId}")
    public List<Client> getSharedWith(@PathVariable Long projectId, @PathVariable Long ownerId){
        return projectStorageService.getAllSharedWith(projectId, ownerId);
    }

}
