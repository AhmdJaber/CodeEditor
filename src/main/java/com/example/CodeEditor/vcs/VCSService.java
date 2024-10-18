package com.example.CodeEditor.vcs;

import com.example.CodeEditor.model.component.files.FileItem;
import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.component.files.Snippet;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.repository.ProjectRepository;
import com.example.CodeEditor.services.FileItemService;
import com.example.CodeEditor.services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class VCSService {
    @Autowired
    private StorageService storageService;

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private FileItemService fileItemService;

    public void initVCS(Long projectId) throws IOException {
        Project project = projectRepository.findById(projectId).orElseThrow();
        storageService.vcsInit(project);
    }

    public void deleteVCS(Long projectId){
        Project project = projectRepository.findById(projectId).orElseThrow();
        storageService.vcsDelete(project);
    }

    public Map<String, List<String>> status(Long projectId) throws IOException {
        Project project = projectRepository.findById(projectId).orElseThrow();
        String branchName = storageService.getCurrentBranch(project);
        Map<Long, ChangeHolder> untrackedChanges = storageService.vcsReadChanges(project, branchName);
        Map<Long, ChangeHolder> trackedChanges = storageService.vcsReadTracked(project, branchName);
        Map<String, List<String>> status = new HashMap<>();
        status.put("untracked", new ArrayList<>());
        status.put("tracked", new ArrayList<>());
        for(Long id : untrackedChanges.keySet()){
            FileItem fileItem = fileItemService.getFileById(id);
            status.get("untracked").add(fileItem.getName());
        }
        for(Long id : trackedChanges.keySet()){
            FileItem fileItem = fileItemService.getFileById(id);
            status.get("tracked").add(fileItem.getName());
        }
        return status; //TODO: everything is working?
    }

    public List<String> add(Long projectId, List<String> files) throws IOException {
        Project project = projectRepository.findById(projectId).orElseThrow();
        String branchName = storageService.getCurrentBranch(project);
        if (files.contains(".")) {
            storageService.vcsTrackAllChanges(project, branchName);
            return new ArrayList<>(); //TODO: change this to be list of strings of all the changes
        }

        List<Long> filesIds = new ArrayList<>();
        for(String filePath : files){
            Long fileId = storageService.getFileIdByPath(project, filePath);
            filesIds.add(fileId);
        }
        storageService.vcsTrackChanges(project, branchName, filesIds);
        return new ArrayList<>(); //TODO: change this to be list of strings of the changes (Files)
    }

    public String log(Long projectId) throws IOException {
        Project project = projectRepository.findById(projectId).orElseThrow();
        String branchName = storageService.getCurrentBranch(project);
        List<String> logs = storageService.log(project, branchName);
        StringBuilder log = new StringBuilder();
        for (String currentLog : logs) {
            log.append(currentLog).append("\n\n");
        }
        return log.toString();
    }

    public List<String> commit(Long projectId, Client client, String message) throws IOException {
        Project project = projectRepository.findById(projectId).orElseThrow();
        String branchName = storageService.getCurrentBranch(project);
        String currentCommit = storageService.getCurrentCommit(project, branchName);
        String commitId = storageService.vcsCommitTracked(project, branchName, client, message, currentCommit);
        return new ArrayList<>(); //TODO: make it return a list of String with all the tracked changes that have been commited
    }

    public void revert(Long projectId, String commitId) throws IOException {
        Project project = projectRepository.findById(projectId).orElseThrow();
        String branchName = storageService.getCurrentBranch(project);
        storageService.revert(project, branchName, commitId);
    }

    // TODO: -------------------------------- :TODO
    // TODO:      T      O      D      O      :TODO
    // TODO: -------------------------------- :TODO
    public List<String> push(Project project){
        return null; //TODO: create the commit inside the branch (the snapshot that we created)
    }

    public boolean checkVCSDirectory(Project project){
        return true; //TODO: from the filesystem, check if there is a .vcs directory
    }
}