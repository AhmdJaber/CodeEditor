package com.example.CodeEditor.services;

import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.component.files.Snippet;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.model.users.editor.ProjectDirectory;
import com.example.CodeEditor.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class StorageService { // TODO: split the storage service && Debugging && Exception Handling
    private final String path = "C:\\Users\\ahmad\\OneDrive\\Desktop\\Atypon\\Capstone Project\\filesystem\\users";
    @Autowired
    private FileUtil fileUtil;

    public void createUser(Client client){ // TODO: change it to general class like "User" or something
        String userPath = path + "\\" + client.getId();
        try{
            fileUtil.createFolder(userPath);
            fileUtil.createFolder(userPath + "\\projects");
            fileUtil.createFolder(userPath + "\\shared");
        } catch (Exception e){
            throw new IllegalStateException("Failed to create folder " + userPath, e);
        }
    }

    public void createProject(Client client, Project project){
        String projectPath = path + "\\" + client.getId() + "\\projects\\" + project.getId();
        try{
            fileUtil.createFolder(projectPath);
            fileUtil.createFolder(projectPath + "\\tree");
            fileUtil.createFolder(projectPath + "\\snippets");
            saveProjectDirectory(client, new ProjectDirectory(), project.getId());
        } catch (Exception e){
            throw new IllegalStateException("Failed to create folders on " + projectPath, e);
        }
    }

    public void shareProject(Client clientToShareWith, Long projectId, Long ownerId){
        String sharedPath = path + "\\" + clientToShareWith.getId() + "\\shared\\";
        createFolderIfNotExists(sharedPath);
        try {
            fileUtil.createFile(sharedPath + "\\" + ownerId + "_" + projectId, "");
        } catch (Exception e){
            throw new IllegalStateException("Failed to create folder " + sharedPath, e);
        }
    }

    public List<String> getSharedProjects(Client client){
        String sharedPath = path + "\\" + client.getId() + "\\shared\\";
        createFolderIfNotExists(sharedPath);
        File[] files = fileUtil.getSubFiles(sharedPath);
        List<String> projects = new ArrayList<>();
        for (File file : files){
            projects.add(file.getName());
        }
        return projects;
    }

    public void createFolderIfNotExists(String path){
        File file = new File(path);
        if (!file.exists()){
            if (!file.mkdir()){
                throw new IllegalStateException("Failed to create folder " + path);
            } else {
                System.out.println("Folder " + path + " created!");
            }
        }
    }

    public void createSnippet(Client client, Snippet snippet, Long projectId) throws IOException {
        String snippetsPath = path + "\\" + client.getId() + "\\projects\\" + projectId + "\\snippets";
        createFolderIfNotExists(snippetsPath);

        String fileName = snippet.getId() + "_" + snippet.getName();
        Path path = Paths.get(snippetsPath + "\\" + fileName);
        Files.createFile(path);
        String extension = snippet.getName().substring(snippet.getName().lastIndexOf(".") + 1);
        String content = getContent(extension);

        Files.write(path, content.getBytes());
    }

    private static String getContent(String extension) { // TODO: move it to utils?
        if (extension.equals("cpp")) {
            return "#include <bits/stdc++.h>\nusing namespace std;\n\nint main() {\n\t//Start Coding\n\tcout << \"Hello World!\";\n}";
        }
        else if (extension.equals("java")) {
            return "public class HelloWorld {\n\n\tpublic static void main(String[] args) {\n\t\t//Start Coding\n\t\tSystem.out.println(\"Hello World!\");\n\t}\n}";
        }
        else if (extension.equals("py")) {
            return "#Start Coding\nprint(\"Hello World!\")";
        }
        return "Nothing\n\n Extension " + extension + " not allowed";
    }

    public void deleteSnippet(Client client, Snippet snippet, Long projectId) throws IOException {// TODO: Move to SnippetStorageService
        String snippetsPath = path + "\\" + client.getId() + "\\projects\\" + projectId + "\\snippets";
        createFolderIfNotExists(snippetsPath);

        String fileName = snippet.getId() + "_" + snippet.getName();
        Path path = Paths.get(snippetsPath + "\\" + fileName);
        Files.delete(path);
    }

    public String loadSnippet(Client client, Long id, String name, Long projectId) throws IOException {
        String snippetsPath = path + "\\" + client.getId() + "\\projects\\" + projectId + "\\snippets";
        createFolderIfNotExists(snippetsPath);

        String fileName = id + "_" + name;
        Path path = Paths.get(snippetsPath + "\\" + fileName);
        return Files.readString(path);
    }

    public void updateSnippet(Client client, Long id, String name, String updatedContent, Long projectId) throws IOException {
        String snippetsPath = path + "\\" + client.getId() + "\\projects\\" + projectId + "\\snippets";
        createFolderIfNotExists(snippetsPath);

        String fileName = id + "_" + name;
        Path path = Paths.get(snippetsPath + "\\" + fileName);
        Files.write(path, updatedContent.getBytes());
    }

    public void saveProjectDirectory(Client client, ProjectDirectory projectDirectory, Long projectId) throws IOException {
        String dirPath = path + "\\" + client.getId() + "\\projects\\" + projectId + "\\tree\\";
        createFolderIfNotExists(dirPath);

        File file = new File(dirPath + client.getId() + "_treeObject.ser");
        try (FileOutputStream fileOutStream = new FileOutputStream(file);
             ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream)){
            objectOutStream.writeObject(projectDirectory);
            System.out.println("Object written to " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ProjectDirectory loadEditorDirObj(Client client, Long projectId) throws IOException {
        String filePath = path + "\\" + client.getId() + "\\projects\\" + projectId + "\\tree\\" + client.getId() + "_treeObject.ser";

        File file = new File(filePath);
        if (!file.exists()){
            saveProjectDirectory(client, new ProjectDirectory(), projectId);
        }

        try(FileInputStream fileInStream = new FileInputStream(file);
                ObjectInputStream objectInStream = new ObjectInputStream(fileInStream)) {

            return (ProjectDirectory) objectInStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}