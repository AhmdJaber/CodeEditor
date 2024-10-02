package com.example.CodeEditor.services.fileSystem;

import com.example.CodeEditor.model.users.editor.Editor;
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

    public void createUser(Editor editor){ // TODO: change it to general class like "User" or something
        String userPath = path + editor.getId();
        try{
            fileUtil.createFolder(userPath);
            fileUtil.createFolder(userPath + "\\tree"); //TODO: any better names?
            fileUtil.createFolder(userPath + "\\0");
        } catch (Exception e){
            throw new IllegalStateException("Failed to create folder " + userPath, e);
        }
    }

    public void saveEditorDirObj(Editor editor, EditorDirectory editorDirectory) throws IOException {
        String dirPath = path + "\\" + editor.getId() + "\\tree\\";
        File dir = new File(dirPath);
        if (!dir.exists()){
            if (!dir.mkdirs()){
                System.out.println("Directory " + dirPath + " already exists!");
            } else{
                System.out.println("Directory " + dirPath + " created");
            }
        }

        File file = new File(dirPath + editor.getId() + "_treeObject.ser");
        try (FileOutputStream fileOutStream = new FileOutputStream(file);
             ObjectOutputStream objectOutStream = new ObjectOutputStream(fileOutStream)){
            objectOutStream.writeObject(editorDirectory);
            System.out.println("Object written to " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public EditorDirectory loadEditorDirObj(Editor editor) throws IOException {
        String filePath = path + "\\" + editor.getId() + "\\tree\\" + editor.getId() + "_treeObject.ser";

        File file = new File(filePath);
        if (!file.exists()){
            saveEditorDirObj(editor, new EditorDirectory());
        }

        try(FileInputStream fileInStream = new FileInputStream(file);
                ObjectInputStream objectInStream = new ObjectInputStream(fileInStream)) {

            Object obj = objectInStream.readObject();
            EditorDirectory editorDirectory = (EditorDirectory) obj;
            return editorDirectory;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}