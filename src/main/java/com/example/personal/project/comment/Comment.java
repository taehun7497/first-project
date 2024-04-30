package com.example.personal.project.comment;

import com.example.personal.project.answer.Answer;
import com.example.personal.project.question.Question;
import com.example.personal.project.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Answer answer;

    @ManyToOne
    private Question question;

    @ManyToOne
    private SiteUser author;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createDate;

    private String text;
}
