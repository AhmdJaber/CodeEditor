package com.example.CodeEditor.services;

import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.users.client.Client;
import com.example.CodeEditor.model.users.client.Token;
import com.example.CodeEditor.model.users.editor.Editor;
import com.example.CodeEditor.model.users.editor.ProjectDirectory;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.repository.EditorRepository;
import com.example.CodeEditor.repository.ProjectRepository;
import com.example.CodeEditor.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class EditorService {
    @Autowired
    private EditorRepository editorRepository;

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private TokenRepository tokenRepository;

    public List<Editor> getAllEditors() {
        return editorRepository.findAll();
    }

    public Editor getEditorById(Long id) {
        return editorRepository.findById(id).orElseThrow(
                () -> new RuntimeException("No editor found with id: " + id)
        );
    }

    public void addEditor(Client editor) {
        storageService.createUser(editor);
        clientRepository.save(editor);
    }

    public Editor updateEditor(Long id, Editor updatedEditor) {
        return editorRepository.findById(id).map(editor -> {
            editor.setName(updatedEditor.getName());
            editor.setPassword(updatedEditor.getPassword());
            return editorRepository.save(editor);
        }).orElseThrow(() -> new NoSuchElementException("Admin with ID " + id + " not found"));

    }

    public void deleteEditor(Long id) {
        if (clientRepository.existsById(id)) {
            Client client = clientRepository.findById(id).orElseThrow();
            List< Project> projects = projectRepository.findByClient(client);
            for (Project project : projects) {
                List<Client> allSharedWith = storageService.getAllSharedWith(project.getClient().getId(), project.getId());
                for (Client sharedWith : allSharedWith) {
                    storageService.removesharedProject(sharedWith, project.getId());
                }
                projectRepository.delete(project);
            }
            List<Token> tokens = tokenRepository.findAllValidTokenClient(id);
            tokenRepository.deleteAll(tokens);
            storageService.deleteUser(id);
            clientRepository.deleteById(id);
        }
    }

    public Editor getEditorByEmail(String email){
        return editorRepository.findByEmail(email); // TODO: return optional
    }

    public boolean authenticate(Editor editor){
        Authentication auth = manager.authenticate(
                new UsernamePasswordAuthenticationToken(editor.getEmail(), editor.getPassword())
        );

        return auth.isAuthenticated();
    }
}