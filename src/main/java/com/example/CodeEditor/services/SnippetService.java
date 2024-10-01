package com.example.CodeEditor.services;

import com.example.CodeEditor.model.component.Snippet;
import com.example.CodeEditor.repository.SnippetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class SnippetService {
//    private final SnippetRepository snippetRepository;
//
//    @Autowired
//    public SnippetService(SnippetRepository snippetRepository){
//        this.snippetRepository = snippetRepository;
//    }
//
//    public Snippet getSnippet(Long id){
//        return snippetRepository.findById(id).orElseThrow(
//                () -> new NoSuchElementException("No Snippet found with id: " + id)
//        );
//    }
//
//    public List<Snippet> getAllSnippets(){
//        return snippetRepository.findAll();
//    }
//
//    public Snippet addSnippet(Snippet snippet){
//        return snippetRepository.save(snippet);
//    }
//
//    public Snippet updateSnippet(Long id, Snippet updatedSnippet){
//        return snippetRepository.findById(id).map(snippet -> {
//            if (updatedSnippet.getName() != null) snippet.setName(updatedSnippet.getName());
//            if (updatedSnippet.getPath() != null) snippet.setPath(updatedSnippet.getPath());
//            if (updatedSnippet.getExtension() != null) snippet.setExtension(updatedSnippet.getExtension());
//            if (updatedSnippet.getContent() != null) snippet.setContent(updatedSnippet.getContent());
//            return snippetRepository.save(snippet);
//        }).orElseThrow(
//                () -> new NoSuchElementException("No Snippet found with id: " + updatedSnippet.getId())
//        );
//    }
//
//    public boolean deleteSnippet(Long id){
//        if (!snippetRepository.existsById(id)) {
//            return false;
//        }
//
//        snippetRepository.deleteById(id);
//        return true;
//    }
}
