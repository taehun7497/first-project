package com.example.personal.project.question;

import com.example.personal.project.answer.Answer;
import com.example.personal.project.category.Category;
import com.example.personal.project.comment.Comment;
import com.example.personal.project.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@DynamicInsert
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private Category category;

    @ManyToOne
    private SiteUser author;

    @Column(length = 200)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
    private List<Answer> answerList;

    @OneToMany(mappedBy = "question", cascade = CascadeType.REMOVE)
    private List<Comment> commentList;

    @ManyToMany
    Set<SiteUser> voter;

    @Column(columnDefinition = "integer default 0", nullable = false)
    private int view;

    private LocalDateTime createDate;

    private LocalDateTime modifyDate;
}