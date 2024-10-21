package com.example.CodeEditor.model.component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@ToString
@AllArgsConstructor
@Builder
public class Comment implements Serializable {
    private String editorName;
    private String editorEmail;
    private Integer start;
    private Integer end;
    private LocalDate date;
    private String content;
}
