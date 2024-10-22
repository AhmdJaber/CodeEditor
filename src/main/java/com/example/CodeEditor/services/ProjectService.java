package com.example.CodeEditor.services;

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

    public List<Project> getSharedEditProjects(Client client) {
        List<String> projects = storageService.getSharedEditProjects(client);
        return getSharedProjects(projects);
    }

    public List<Project> getShareViewProjects(Client client) {
        List<String> projects = storageService.getSharedViewProjects(client);
        return getSharedProjects(projects);
    }

    private List<Project> getSharedProjects(List<String> projects) {
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

    public void deleteProjectById(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new IllegalArgumentException("Project does not exist");
        }
        projectRepository.deleteById(id);
    }
//    public List<Long> getSharedWith(Long id){
//        Project project = getProjectById(id);
//        Client client = clientRepository.findById(project.getClient().getId()).orElseThrow();
//        if (!projectRepository.existsById(id)) {
//            throw new IllegalArgumentException("Project does not exist");
//        }
//        return storageService.getSharedWith(client, id);
//    }
    //TODO: other services
}
