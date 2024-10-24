package com.example.CodeEditor.controllers;

import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.clients.Client;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.services.ClientService;
import com.example.CodeEditor.services.ProjectService;
import com.example.CodeEditor.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ClientService clientService;

    @Autowired
    private StorageService storageService;

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
        Client client = clientRepository.findById(editorId).orElseThrow();
        return projectService.getClientProjects(client);
    }

    @DeleteMapping("/remvoe-project/{editorId}/{projectId}")
    public void removeEditorProject(@PathVariable Long editorId, @PathVariable Long projectId){
        Client client = clientRepository.findById(editorId).orElseThrow();
        projectService.deleteProjectById(projectId);
        storageService.deleteProject(client, projectId);
    }

    @GetMapping("/get-shared/{editorId}")
    public List<Project> getSharedProjects(@PathVariable Long editorId){
        Client client = clientRepository.findById(editorId).orElseThrow();
        return projectService.getSharedEditProjects(client);
    }

    @DeleteMapping("/remove-shared-project/{editorId}/{projectId}")
    public void removeSharedProject(@PathVariable Long editorId, @PathVariable Long projectId){
        Client client = clientRepository.findById(editorId).orElseThrow();
        storageService.removesharedProject(client, projectId);
    }

    @GetMapping("/get-shared/{projectId}/{ownerId}")
    public List<Client> getSharedWith(@PathVariable Long projectId, @PathVariable Long ownerId){
        return storageService.getAllSharedWith(projectId, ownerId);
    }

}
