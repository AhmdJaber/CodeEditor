package com.example.CodeEditor.vcs;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeHolder implements Serializable {
    private Change change;
    private char fileType;
    private String content;
}
