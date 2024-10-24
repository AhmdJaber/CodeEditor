package com.example.CodeEditor.services;

import com.example.CodeEditor.enums.Role;
import com.example.CodeEditor.model.component.files.Project;
import com.example.CodeEditor.model.clients.Client;
import com.example.CodeEditor.model.component.Token;
import com.example.CodeEditor.repository.ClientRepository;
import com.example.CodeEditor.repository.ProjectRepository;
import com.example.CodeEditor.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ClientService {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private StorageService storageService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TokenRepository tokenRepository;

    public Client getClientById(Long id) {
        return clientRepository.findById(id).orElseThrow(
                () -> new RuntimeException("No client found with id: " + id)
        );
    }

    public void addClient(Client client) {
        storageService.createUser(client);
        clientRepository.save(client);
    }

    public Client updateClient(Long id, Client updatedClient) {
        return clientRepository.findById(id).map(editor -> {
            editor.setName(updatedClient.getName());
            editor.setPassword(updatedClient.getPassword());
            return clientRepository.save(editor);
        }).orElseThrow(() -> new NoSuchElementException("Client with ID " + id + " not found"));

    }

    public void deleteEditor(Long id) {
        if (clientRepository.existsById(id)) {
            Client client = clientRepository.findById(id).orElseThrow();
            List< Project> projects = projectRepository.findByClient(client);
            for (Project project : projects) {
                List<Client> allSharedWith = storageService.getAllSharedWith(project.getClient().getId(), project.getId());
                for (Client sharedWith : allSharedWith) {
                    if (sharedWith != null) {
                        storageService.removesharedProject(sharedWith, project.getId());
                    }
                }
                projectRepository.delete(project);
            }
            List<Token> tokens = tokenRepository.findAllValidTokenClient(id);
            tokenRepository.deleteAll(tokens);
            storageService.deleteUser(id);
            clientRepository.deleteById(id);
        }
    }

    public void deleteClient(Long id){
        if (!clientRepository.existsById(id)) {
            throw new NoSuchElementException("Client with ID " + id + " not found");
        }
        clientRepository.deleteById(id);
    }

    public List<Client> getAllEditors(){
        List<Client> clients = clientRepository.findAll();
        List<Client> editors = new ArrayList<>();
        for (Client client : clients) {
            if (client.getRole() == Role.EDITOR){
                editors.add(client);
            }
        }
        return editors;
    }

    public List<Client> getAllClients(){
        return clientRepository.findAll();
    }
}