package com.sandy.sconsole.dao.exam;

import com.sandy.sconsole.dao.master.TagMaster;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table( name = "tag_question_map" )
public class TagQuestionMap {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;

    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "question_id", nullable = false )
    private Question question;

    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "tag_id", nullable = false )
    private TagMaster tag;
}
