package com.example.CodeEditor.services;

import com.example.CodeEditor.model.component.files.File;
import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private StorageService storageService;

    public Project create(Project project) {
        return projectRepository.save(project);
    }

    public List<Project> getClientProjects(Client client){
        return projectRepository.findByClient(client);
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    public List<Project> getSharedProjects(Client client) {
        List<String> projects = storageService.getSharedProjects(client);
        List<Project> sharedProjects = new ArrayList<>();
        for (String shared : projects) {
            Long projectId = Long.parseLong(shared.split("_")[1]);
            Project project = getProjectById(projectId);
            if (project != null) {
                sharedProjects.add(project);
            }
        }
        return sharedProjects;
    }

    public Project updateProject(Long id, Project project) {
        Project old = getProjectById(id);
        if (project.getName() != null){
            old.setName(project.getName());
        }
        return projectRepository.save(old);
    }

    public Project getProjectByNameAndOwner(String name, Client client) {
        return projectRepository.findByNameAndClient(name, client).orElseThrow();
    }
    //TODO: other services
}
