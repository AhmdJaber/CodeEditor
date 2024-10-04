package com.example.CodeEditor.services.fileSystem;

import com.example.CodeEditor.model.component.files.Snippet;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.model.users.editor.EditorDirectory;
import com.example.CodeEditor.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class StorageService {
    private final String path = "C:\\Users\\ahmad\\OneDrive\\Desktop\\Atypon\\Capstone Project\\filesystem\\users";
    @Autowired
    private FileUtil fileUtil;

    public void createUser(Client editor){ // TODO: change it to general class like "User" or something
        String userPath = path + "\\" + editor.getId();
        try{
            fileUtil.createFolder(userPath);
            fileUtil.createFolder(userPath + "\\tree"); //TODO: any better names?
            fileUtil.createFolder(userPath + "\\snippets");
        } catch (Exception e){
            throw new IllegalStateException("Failed to create folder " + userPath, e);
        }
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

    public void createSnippet(Client editor, Snippet snippet) throws IOException {
        String snippetsPath = path + "\\" + editor.getId() + "\\snippets";
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

    public void deleteSnippet(Client editor, Snippet snippet) throws IOException {// TODO: Move to SnippetStorageService
        String snippetsPath = path + "\\" + editor.getId() + "\\snippets";
        createFolderIfNotExists(snippetsPath);

        String fileName = snippet.getId() + "_" + snippet.getName();
        Path path = Paths.get(snippetsPath + "\\" + fileName);
        Files.delete(path);
    }

    public String loadSnippet(Client editor, Long id, String name) throws IOException {
        String snippetsPath = path + "\\" + editor.getId() + "\\snippets";
        createFolderIfNotExists(snippetsPath);

        String fileName = id + "_" + name;
        Path path = Paths.get(snippetsPath + "\\" + fileName);
        return Files.readString(path);
    }

    public void updateSnippet(Client editor, Long id, String name, String updatedContent) throws IOException {
        String snippetsPath = path + "\\" + editor.getId() + "\\snippets";
        createFolderIfNotExists(snippetsPath);

        String fileName = id + "_" + name;
        Path path = Paths.get(snippetsPath + "\\" + fileName);
        Files.write(path, updatedContent.getBytes());
    }

    public void saveEditorDirObj(Client editor, EditorDirectory editorDirectory) throws IOException {
        String dirPath = path + "\\" + editor.getId() + "\\tree\\";
        createFolderIfNotExists(dirPath);

        File file = new File(dirPath + editor.getId() + "_treeObject.ser");
        try (FileOutputStream fileOutStream = new FileOutputStream(file);
             ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream)){
            objectOutStream.writeObject(editorDirectory);
            System.out.println("Object written to " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public EditorDirectory loadEditorDirObj(Client editor) throws IOException {
        String filePath = path + "\\" + editor.getId() + "\\tree\\" + editor.getId() + "_treeObject.ser";

        File file = new File(filePath);
        if (!file.exists()){
            saveEditorDirObj(editor, new EditorDirectory());
        }

        try(FileInputStream fileInStream = new FileInputStream(file);
                ObjectInputStream objectInStream = new ObjectInputStream(fileInStream)) {

            return (EditorDirectory) objectInStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}