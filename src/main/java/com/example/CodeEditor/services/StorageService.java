package com.example.CodeEditor.services;

import com.example.CodeEditor.model.component.Comment;
import com.example.CodeEditor.model.component.files.FileItem;
import com.example.CodeEditor.model.component.files.FileNode;
import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.component.files.Snippet;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.model.users.editor.ProjectDirectory;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.repository.FileItemRepository;
import com.example.CodeEditor.repository.ProjectRepository;
import com.example.CodeEditor.utils.EncryptionUtil;
import com.example.CodeEditor.utils.FileUtil;
import com.example.CodeEditor.vcs.Change;
import com.example.CodeEditor.vcs.ChangeHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
public class StorageService { // TODO: split the storage service && Exception Handling && Clean Code
    private final String path = "C:\\Users\\ahmad\\OneDrive\\Desktop\\Atypon\\Capstone Project\\filesystem\\users";
    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private FileItemRepository fileItemRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ProjectRepository projectRepository;

    public Long getFileIdByPath(Project project, String path) throws IOException {
        Map<Long, FileNode> editorDir = loadEditorDirObj(project.getClient(), project.getId()).getTree();
        if (path.charAt(0) == '/'){
            path = path.substring(1);
        }
        String[] files = path.split("/");
        for (int i = 0; i < files.length; i++){
            System.out.println(files[i]);
        }
        return getIdFromEditorDir(editorDir, files, 0, 0L);
    }

    public Long getIdFromEditorDir(Map<Long, FileNode> editorDir, String[] files, int currentFile, Long currentId){
        for(FileItem fileItem: editorDir.get(currentId).getFileItems()){
            if (fileItem.getName().equals(files[currentFile])){
                if (currentFile == files.length - 1) {
                    return fileItem.getId();
                }
                return getIdFromEditorDir(editorDir, files, currentFile + 1, fileItem.getId());
            }
        }
        throw new IllegalArgumentException("No file with name " + files[currentFile] + "in the directory " + editorDir.get(currentId).getName());
    }

    public void createUser(Client client){ // TODO: change it to general class like "User" or something
        String userPath = path + "\\" + client.getId();
        try{
            fileUtil.createFolder(userPath);
            fileUtil.createFolder(userPath + "\\projects");
            fileUtil.createFolder(userPath + "\\shared");
            fileUtil.createFolder(userPath + "\\shared_view");
            fileUtil.writeObjectOnFile(new ArrayList<>(), userPath + "\\public");
        } catch (Exception e){
            throw new IllegalStateException("Failed to create folder " + userPath, e);
        }
    }

    public void deleteUser(Long clientId){
        String userPath = path + "\\" + clientId;
        fileUtil.deleteFolder(userPath);
    }

    public void createProject(Client client, Project project){
        String projectPath = path + "\\" + client.getId() + "\\projects\\" + project.getId();
        try{
            fileUtil.createFolder(projectPath);
            fileUtil.createFolder(projectPath + "\\tree");
            fileUtil.createFolder(projectPath + "\\snippets");
            fileUtil.createFolder(projectPath + "\\comments");
            fileUtil.createFile(projectPath + "\\shared", ""); //TODO: remove?
            fileUtil.writeObjectOnFile(new ArrayList<>(), projectPath + "\\shared");
            fileUtil.writeObjectOnFile(new ArrayList<>(), projectPath + "\\shared_view");
            saveProjectDirectory(client, new ProjectDirectory(), project.getId());
        } catch (Exception e){
            throw new IllegalStateException("Failed to create folders on " + projectPath, e);
        }
    }

    public void deleteProject(Client client, long projectId){
        String projectPath = path + "\\" + client.getId() + "\\projects\\" + projectId;
        try{
            List<Long> sharedWith = (List<Long>) fileUtil.readObjectFromFile(projectPath + "\\shared");
            for(Long sharedWithId : sharedWith){
                fileUtil.deleteFile(path + "\\" + sharedWithId + "\\shared\\" + "\\" + client.getId() + "_" + projectId);
            }
            removeProjectFromPublic(projectId);
            fileUtil.deleteFolder(projectPath);
        } catch (Exception e){
            throw new IllegalStateException("Failed to create folders on " + projectPath, e);
        }
    }

    public void shareProjectToPublic(Long projectId){
        Project project = projectRepository.findById(projectId).orElseThrow();
        String userPublic = path + "\\" + project.getClient().getId() + "\\public";
        List<Long> publicProjects = (List<Long>) fileUtil.readObjectFromFile(userPublic);
        publicProjects.add(projectId);
        fileUtil.writeObjectOnFile(publicProjects, userPublic);
    }

    public void removeProjectFromPublic(Long projectId){
        Project project = projectRepository.findById(projectId).orElseThrow();
        String userPublic = path + "\\" + project.getClient().getId() + "\\public";
        List<Long> publicProjects = (List<Long>) fileUtil.readObjectFromFile(userPublic);
        publicProjects.remove(projectId);
        fileUtil.writeObjectOnFile(publicProjects, userPublic);
    }

    public List<Project> getPublicProjects(Long clientId){
        Client client = clientRepository.findById(clientId).orElseThrow();
        String userPublic = path + "\\" + client.getId() + "\\public";
        List<Project> publicProjets = new ArrayList<>();
        List<Long> projectsIds = (List<Long>) fileUtil.readObjectFromFile(userPublic);
        for (Long projectId : projectsIds){
            Project project = projectRepository.findById(projectId).orElse(null);
            if (project != null){
                publicProjets.add(project);
            }
        }
        return publicProjets;
    }

    public boolean checkProjectPublic(Long projectId){
        Project project = projectRepository.findById(projectId).orElseThrow();
        String userPublic = path + "\\" + project.getClient().getId() + "\\public";
        List<Long> publicProjects = (List<Long>) fileUtil.readObjectFromFile(userPublic);
        return publicProjects.contains(projectId);
    }

    public void shareProjectWithEdit(Client clientToShareWith, Long projectId, Long ownerId){
        String listOfSharedPath = path + "\\" + ownerId + "\\projects\\" + projectId + "\\shared";
        String sharedPath = path + "\\" + clientToShareWith.getId() + "\\shared\\";
        shareProject(clientToShareWith, projectId, ownerId, listOfSharedPath, sharedPath);
    }

    public void shareProjectWithView(Client clientToShareWith, Long projectId, Long ownerId) {
        String listOfSharedPath = path + "\\" + ownerId + "\\projects\\" + projectId + "\\shared_view";
        String sharedPath = path + "\\" + clientToShareWith.getId() + "\\shared_view\\";
        shareProject(clientToShareWith, projectId, ownerId, listOfSharedPath, sharedPath);
    }

    private void shareProject(Client clientToShareWith, Long projectId, Long ownerId, String listOfSharedPath, String sharedPath) {
        createFolderIfNotExists(sharedPath);
        try {
            fileUtil.createFile(sharedPath + "\\" + ownerId + "_" + projectId, "");
            List<Long> sharedWith = (List<Long>) fileUtil.readObjectFromFile(listOfSharedPath);
            sharedWith.add(clientToShareWith.getId());
            fileUtil.writeObjectOnFile(sharedWith, listOfSharedPath);
        } catch (Exception e){
            throw new IllegalStateException("Failed to create folder " + sharedPath, e);
        }
    }

    public void removesharedProject(Client clientToShareWith, Long projectId){
        Project project = projectRepository.findById(projectId).orElseThrow();
        Long ownerId = project.getClient().getId();
        String listOfSharedPath = path + "\\" + ownerId + "\\projects\\" + projectId + "\\shared";
        String sharedPath = path + "\\" + clientToShareWith.getId() + "\\shared\\";
        createFolderIfNotExists(sharedPath);
        try {
            fileUtil.deleteFile(sharedPath + "\\" + ownerId + "_" + projectId);
            List<Long> sharedWith = (List<Long>) fileUtil.readObjectFromFile(listOfSharedPath);
            sharedWith.remove(clientToShareWith.getId());
            fileUtil.writeObjectOnFile(sharedWith, listOfSharedPath);
        } catch (Exception e){
            throw new IllegalStateException("Failed to delete shared project " + sharedPath, e);
        }
    }

    public List<Client> getAllSharedWith(Long ownerId, Long projectId){
        String listOfSharedPath = path + "\\" + ownerId + "\\projects\\" + projectId + "\\shared";
        List<Client> clients = new ArrayList<>();
        List<Long> sharedWith = (List<Long>) fileUtil.readObjectFromFile(listOfSharedPath);
        for (Long sharedWithId : sharedWith){
            Client client = clientRepository.findById(sharedWithId).orElse(null);
            clients.add(client);
        }
        return clients;
    }

    public List<String> getSharedEditProjects(Client client){
        String sharedPath = path + "\\" + client.getId() + "\\shared\\";
        return getSharedProjects(sharedPath);
    }

    public List<String> getSharedViewProjects(Client client) {
        String sharedPath = path + "\\" + client.getId() + "\\shared_view\\";
        return getSharedProjects(sharedPath);
    }

    private List<String> getSharedProjects(String sharedPath) {
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
        Project project = projectRepository.findById(projectId).orElseThrow();
        String snippetsPath = path + "\\" + client.getId() + "\\projects\\" + project.getId() + "\\snippets";
        createFolderIfNotExists(snippetsPath);

        String fileName = snippet.getId() + "_" + snippet.getName();
        Path snippetPath = Paths.get(snippetsPath + "\\" + fileName);
        Files.createFile(snippetPath);
        String extension = snippet.getName().substring(snippet.getName().lastIndexOf(".") + 1);
        String content = getCodeContent(extension);

        Files.write(snippetPath, content.getBytes());
        String commentPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\comments\\" + snippet.getId();
        fileUtil.writeObjectOnFile(new ArrayList<>(), commentPath);
    }

    private static String getCodeContent(String extension) { // TODO: move it to utils?
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

    public String loadSnippet(Client client, Long id, String name, Long projectId) throws Exception {
        String snippetsPath = path + "\\" + client.getId() + "\\projects\\" + projectId + "\\snippets";
        createFolderIfNotExists(snippetsPath);

        String fileName = id + "_" + name;
        Path path = Paths.get(snippetsPath + "\\" + fileName);
        if (Files.exists(path)) {
            return Files.readString(path);
        } else {
            path = Paths.get(path.toString().split("\\.")[0]);
            if (Files.exists(path)) {
                return fileUtil.readFileContents(encryptionUtil.decrypt(fileUtil.readFileContents(path.toString())));
            } else {
                throw new RuntimeException("File not found");
            }
        }
    }

    public synchronized void updateSnippet(Client client, Long id, String name, String updatedContent, Long projectId) throws IOException {
        String snippetsPath = path + "\\" + client.getId() + "\\projects\\" + projectId + "\\snippets";
        createFolderIfNotExists(snippetsPath);

        String fileName = id + "_" + name;
        Path path = Paths.get(snippetsPath + "\\" + fileName);
        try {
            Files.write(path, updatedContent.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void comment(Client editor, Project project, Long snippetId, String comment, Integer start, Integer end) {
        String commentPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\comments\\" + snippetId;
        List<Comment> comments = (List<Comment>) fileUtil.readObjectFromFile(commentPath);
        Comment currentComment = Comment.builder()
                .editorName(editor.getName())
                .editorEmail(editor.getEmail())
                .content(comment)
                .date(LocalDate.now())
                .start(start)
                .end(end)
                .build();
        comments.add(currentComment);
        System.out.println(comments);
        fileUtil.writeObjectOnFile(comments, commentPath);
    }

    public List<Comment> getSnippetComments(Project project, Long snippetId) {
        String commentPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\comments\\" + snippetId;
        return (List<Comment>) fileUtil.readObjectFromFile(commentPath);
    }

    public void saveProjectDirectory(Client client, ProjectDirectory projectDirectory, Long projectId) throws IOException {
        String dirPath = path + "\\" + client.getId() + "\\projects\\" + projectId + "\\tree\\";
        createFolderIfNotExists(dirPath);

        File file = new File(dirPath + "_treeObject.ser");
        try (FileOutputStream fileOutStream = new FileOutputStream(file);
             ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream)){
            objectOutStream.writeObject(projectDirectory);
            System.out.println("Object written to " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ProjectDirectory loadEditorDirObj(Client client, Long projectId) {
        String filePath = path + "\\" + client.getId() + "\\projects\\" + projectId + "\\tree\\" + "_treeObject.ser";
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            return (ProjectDirectory) fileUtil.readObjectFromFile(filePath);
        } else {
            path = Paths.get(path.toString().split("\\.")[0]);
            if (Files.exists(path)) {
                try{
                    return (ProjectDirectory) fileUtil.readObjectFromFile(encryptionUtil.decrypt(fileUtil.readFileContents(path.toString())));
                } catch (Exception e){
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("Project directory not found " + path);
            }
        }
    }

                        // TODO    :    VVVV   CCCC    SSSS
    public void vcsInit(Project project) throws IOException {
        long ownerId = project.getClient().getId();
        String vcsPath = path + "\\" + ownerId + "\\projects\\" + project.getId() + "\\.vcs";
        createFolderIfNotExists(vcsPath);
        fileUtil.createFile(vcsPath + "\\HEAD", "main");
        fileUtil.writeObjectOnFile(new HashMap<>(), vcsPath + "\\config");
        vcsCreateBranch(vcsPath, "main");
        createInitialCommit(project);
        //TODO: create the rest of the files and directories
    }

    public boolean checkVCSProject(Project project) {
        String vcsPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\.vcs";
        return fileUtil.fileExists(vcsPath);
    }

    private void createInitialCommit(Project project) throws IOException {
        String commitsPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\.vcs\\branches\\main\\commits";
        String commitId = project.getClient().getId().toString() + "%" + Instant.now().getEpochSecond() + "%" + "initial commit".hashCode();
        Map<Long, FileNode> projectStructure = loadEditorDirObj(project.getClient(), project.getId()).getTree();
        ProjectDirectory projectDirectory = new ProjectDirectory(projectStructure);
        File[] allSnippets = fileUtil.getSubFiles(path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\snippets");
        Map<Long, String> filesContent = new HashMap<>();
        for(File snippet : allSnippets){
            String name = snippet.getName();
            Long id = Long.parseLong(name.split("_")[0]);
            String content = fileUtil.readFileContents(snippet.getAbsolutePath());
            filesContent.put(id, content);
        }

        fileUtil.createFolder(commitsPath + "\\" + commitId);
        fileUtil.createFolder(commitsPath + "\\" + commitId + "\\snippets");
        fileUtil.createFolder(commitsPath + "\\" + commitId + "\\tree");
        fileUtil.writeObjectOnFile(projectDirectory, commitsPath + "\\" + commitId + "\\tree\\" + "_treeObject.ser");
        for(File snippet: allSnippets){
            Long id = Long.parseLong(snippet.getName().split("_")[0]);
            fileUtil.createFile(commitsPath + "\\" + commitId + "\\snippets\\" + snippet.getName(), filesContent.get(id));
        }
        setCurrentCommit(project, "main", commitId);
        writeOnLog(project, project.getClient(), commitId, "main", "Initial commit");
    }

    public void vcsCreateBranch(String vcsPath, String branchName) { //TODO: create the files and folders inside based on the cut from branch
        createFolderIfNotExists(vcsPath + "\\branches");
        File file = new File(vcsPath + "\\branches\\" + branchName);
        if (file.exists()){
            throw new RuntimeException("Branch " + branchName + " already exists");
        }
        createFolderIfNotExists(vcsPath + "\\branches\\" + branchName);
        createFolderIfNotExists(vcsPath + "\\branches\\" + branchName + "\\commits");
        //TODO: move the file Current (that represent the current commit) to here, each branch will have a current commit!
        fileUtil.writeObjectOnFile(new HashMap<>(), vcsPath + "\\branches\\" + branchName + "\\changes");
        fileUtil.writeObjectOnFile(new HashMap<>(), vcsPath + "\\branches\\" + branchName + "\\tracked");
        fileUtil.writeObjectOnFile(new ArrayList<>(), vcsPath + "\\branches\\" + branchName + "\\log");
        fileUtil.createFile(vcsPath + "\\branches\\" + branchName + "\\currentCommit", "");
        //TODO: create the rest of the files and directories (if any)
    }

    public void vcsDelete(Project project) {
        long ownerId = project.getClient().getId();
        String vcsPath = path + "\\" + ownerId + "\\projects\\" + project.getId() + "\\.vcs";
        fileUtil.deleteFolder(vcsPath);
    }

    public String getCurrentBranch(Project project) throws IOException {
        String branchPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\.vcs\\HEAD";
        return fileUtil.readFileContents(branchPath);
    }

    public String getCurrentCommit(Project project, String branchName) {
        String currentCommitPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\.vcs\\branches\\" + branchName + "\\currentCommit";
        return fileUtil.readFileContents(currentCommitPath);
    }

    public Map<Long, ChangeHolder> vcsReadChanges(Project project, String branchName) {
        String changesPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\.vcs\\branches\\" + branchName + "\\changes";
        return (Map<Long, ChangeHolder>) fileUtil.readObjectFromFile(changesPath);
    }


    private void vcsWriteChanges(Project project, String branchName, Map<Long, ChangeHolder> changes){
        String changesPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\.vcs\\branches\\" + branchName + "\\changes";
        fileUtil.writeObjectOnFile(changes, changesPath); // TODO: does this line overwrites or appends?
    }

    public void vcsMakeChange(Project project, String branchName, char fileType, Change changeType, FileItem fileItem) throws IOException {
        if (!checkVCSExistance(project)){
            System.out.println("Not a .vcs project");
            return;
        }
        Map<Long, ChangeHolder> changes = vcsReadChanges(project, branchName);
        Map<Long, ChangeHolder> tracked = vcsReadTracked(project, branchName);
        String filePath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\snippets\\" + fileItem.getId() + "_" + fileItem.getName();
        String content = null;
        if (fileType == '-'){
            content = fileUtil.readFileContents(filePath);
        }
        if (changeType == Change.UPDATE){
            tracked.remove(fileItem.getId());
            String currentCommit = getCurrentCommit(project, branchName);
            String currentSnippetPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\.vcs\\branches\\" + branchName + "\\commits\\" + currentCommit + "\\snippets\\" + fileItem.getId() + "_" + fileItem.getName();
            String currentSnippetContent = fileUtil.readFileContents(currentSnippetPath);
            if (currentSnippetContent.equals(content)){
                changes.remove(fileItem.getId());
                vcsWriteChanges(project, branchName, changes);
                return;
            }
        }
        ChangeHolder change = ChangeHolder.builder()
                .change(changeType)
                .fileType(fileType)
                .content(content)
                .build();
        changes.put(fileItem.getId(), change);

        tracked.remove(fileItem.getId());
        vcsWriteChanges(project, branchName, changes);
        vcsWriteTracked(project, branchName, tracked);
    }

    private boolean checkVCSExistance(Project project) {
        String vcsPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\.vcs";
        return fileUtil.fileExists(vcsPath);
    }

    public Map<Long, ChangeHolder> vcsReadTracked(Project project, String branchName) {
        String changesPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\.vcs\\branches\\" + branchName + "\\tracked";
        return (Map<Long, ChangeHolder>) fileUtil.readObjectFromFile(changesPath);
    }

    private void vcsWriteTracked(Project project, String branchName, Map<Long, ChangeHolder> tracked){
        String changesPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\.vcs\\branches\\" + branchName + "\\tracked";
        fileUtil.writeObjectOnFile(tracked, changesPath);
    }

    public void vcsTrackChanges(Project project, String branchName, List<Long> filesIds) {
        Map<Long, ChangeHolder> tracked = vcsReadTracked(project, branchName);
        Map<Long, ChangeHolder> changes = vcsReadChanges(project, branchName);

        for(Long id : filesIds){
            FileItem file = fileItemRepository.findById(id).orElseThrow(
                    () -> new NoSuchElementException("No such file with id: " + id)
            );
            tracked.put(file.getId(), changes.get(file.getId()));
            changes.remove(file.getId());
        }
        vcsWriteChanges(project, branchName, changes);
        vcsWriteTracked(project, branchName, tracked);
    }

    public void vcsTrackAllChanges(Project project, String branchName) {
        Map<Long, ChangeHolder> tracked = vcsReadTracked(project, branchName);
        Map<Long, ChangeHolder> changes = vcsReadChanges(project, branchName);
        for(Long id: changes.keySet()){
            tracked.put(id, changes.get(id));
        }
        vcsWriteChanges(project, branchName, new HashMap<>());
        vcsWriteTracked(project, branchName, tracked);
    }

    //TODO: check this:
    public String vcsCommitTracked(Project project, String branchName, Client client, String message, String prevCommitId) throws Exception { //TODO: remove the folders from the changes and tracked
        String commitId = client.getId().toString() + "%" + Instant.now().getEpochSecond() + "%" + message.hashCode();
        String commitsPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\.vcs\\branches\\" + branchName + "\\commits";
        Map<Long, ChangeHolder> tracked = vcsReadTracked(project, branchName);
        ProjectDirectory projectStructure = null;
        boolean structureChanged = false;
        for(Long id : tracked.keySet()){
            ChangeHolder changeHolder = tracked.get(id);
            if (changeHolder.getChange() != Change.UPDATE){
                structureChanged = true;
                break;
            }
        }
        if (structureChanged){
            projectStructure = loadEditorDirObj(project.getClient(), project.getId());
        }
        File[] allSnippets = fileUtil.getSubFiles(path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\snippets");
        Map<Long, String> filesContent = new HashMap<>();
        for(File snippet : allSnippets){
            String name = snippet.getName();
            Long id = Long.parseLong(name.split("_")[0]);
            if (tracked.containsKey(id)){ //TODO: when updating the newly created file, does it takes it's content? yes, make sure
                String content = tracked.get(id).getContent();
                filesContent.put(id, content);
            } else {
                filesContent.put(id, null); // TODO: means we will take it from the previous commit (reference)
            }
        }

        String prevCommitPath = commitsPath + "\\" + prevCommitId;
        System.out.println(prevCommitPath);
        fileUtil.createFolder(commitsPath + "\\" + commitId);
        fileUtil.createFolder(commitsPath + "\\" + commitId + "\\snippets");
        fileUtil.createFolder(commitsPath + "\\" + commitId + "\\tree");
        if (structureChanged){
            fileUtil.writeObjectOnFile(projectStructure, commitsPath + "\\" + commitId + "\\tree\\" + "_treeObject.ser");
        } else {
            File prevProj = fileUtil.getSubFiles(prevCommitPath + "\\tree")[0];
            if (!prevProj.getName().endsWith(".ser")){
                fileUtil.createFile(commitsPath + "\\" + commitId + "\\tree\\" + "_treeObject", fileUtil.readFileContents(prevProj.getAbsolutePath()));
            } else {
                fileUtil.createLinkFile(prevProj.getAbsolutePath(), commitsPath + "\\" + commitId + "\\tree\\" + "_treeObject");
            }
        }
        for(File snippet: allSnippets){
            Long id = Long.parseLong(snippet.getName().split("_")[0]);
            if (filesContent.get(id) == null){
                String prevFilePath = prevCommitPath + "\\snippets\\" + snippet.getName();
                if (!Files.exists(Paths.get(prevFilePath))){
                    prevFilePath = prevCommitPath + "\\snippets\\" + snippet.getName().split("\\.")[0];
                }
                if (!(prevFilePath.endsWith(".cpp") || prevFilePath.endsWith(".java") || prevFilePath.endsWith(".py"))){
                    fileUtil.createFile( commitsPath + "\\" + commitId + "\\snippets\\" + snippet.getName().split("\\.")[0], fileUtil.readFileContents(prevFilePath));
                } else {
                    fileUtil.createLinkFile(prevFilePath, commitsPath + "\\" + commitId + "\\snippets\\" + snippet.getName().split("\\.")[0]);
                }
            } else {
                fileUtil.createFile(commitsPath + "\\" + commitId + "\\snippets\\" + snippet.getName(), filesContent.get(id));
            }
        }
        vcsWriteTracked(project, branchName, new HashMap<>());
        writeOnLog(project, client, commitId, branchName, message);
        setCurrentCommit(project, "main", commitId);
        return commitId;
        //TODO: think of scenarios
    }

    public void setCurrentCommit(Project project, String branchName, String commitId){
        String currentPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\.vcs\\branches\\" + branchName + "\\currentCommit";
        fileUtil.writeOnFile(Paths.get(currentPath), commitId);
    }

    public List<String> log(Project project, String branchName){
        String logPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\.vcs\\branches\\" + branchName + "\\log";
        return (List<String>) fileUtil.readObjectFromFile(logPath);
    }

    public void writeOnLog(Project project, Client client, String commitId, String branchName, String message){
        String newLog = "Commit Id:\t" + commitId + "\nAuthor   :\t" + client.getName() + " " + client.getEmail() + "\nDate     :\t\t" + Instant.now() + "\n\tMessage: " + message;
        String logPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\.vcs\\branches\\" + branchName + "\\log";
        List<String> log = (List<String>) fileUtil.readObjectFromFile(logPath);
        log.add(newLog);
        fileUtil.writeObjectOnFile(log, logPath);
    }

    public void revert(Project project, String branchName, String commitId) throws Exception {
        String projectPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId();
        fileUtil.deleteFolder(projectPath + "\\snippets");
        fileUtil.deleteFile(projectPath + "\\tree\\" + "_treeObject.ser");
        fileUtil.createFolder(projectPath + "\\snippets");

        String commitPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId() + "\\.vcs\\branches\\" + branchName + "\\commits\\" + commitId;
        String projectDirPath = commitPath + "\\tree";
        File projectDir = fileUtil.getSubFiles(projectDirPath)[0];
        if (!projectDir.getName().endsWith(".ser")){
            fileUtil.createFile(projectPath + "\\tree\\" + "_treeObject", fileUtil.readFileContents(projectDir.getAbsolutePath()));
        } else {
            fileUtil.writeObjectOnFile(fileUtil.readObjectFromFile(projectDir.getAbsolutePath()), projectPath + "\\tree\\" + "_treeObject.ser");
        }
        File[] snippets = fileUtil.getSubFiles(commitPath + "\\snippets");
        for(File snippet: snippets){
            String content = fileUtil.readFileContents(snippet.getAbsolutePath());
            fileUtil.createFile(projectPath + "\\snippets\\" + snippet.getName(), content);
        }
        setCurrentCommit(project, branchName, commitId);
        vcsWriteTracked(project, branchName, new HashMap<>());
        vcsWriteChanges(project, branchName, new HashMap<>());
    }

    public void fork(Project project, Client client) {
        String projectPath = path + "\\" + project.getClient().getId() + "\\projects\\" + project.getId();
        Project buildProject = Project.builder()
                .name(project.getName())
                .client(client)
                .build();
        Project newProject = projectRepository.save(buildProject);
        String clientProjectsPath = path + "\\" + client.getId() + "\\projects\\" + newProject.getId();
        createProject(client, newProject);
        fileUtil.copyDirectory(projectPath + "\\tree", clientProjectsPath + "\\tree");
        fileUtil.copyDirectory(projectPath + "\\snippets", clientProjectsPath + "\\snippets");
    }
}