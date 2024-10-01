package com.example.CodeEditor.model.users.editor;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table
@Data
@NoArgsConstructor
public class Editor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    @Column(unique = true, nullable = false)
    private String email;
    private String password;

    public Editor(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
}
