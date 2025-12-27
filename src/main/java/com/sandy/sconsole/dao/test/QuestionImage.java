package com.sandy.sconsole.dao.test;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table( name = "question_image" )
public class QuestionImage {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "id", nullable = false )
    private Integer id;
    
    @ManyToOne( fetch = FetchType.LAZY, optional = false )
    @JoinColumn( name = "question_id", nullable = false )
    private Question question;
    
    @Column( name = "sequence", nullable = false )
    private Integer sequence;
    
    @Column( name = "page_number", nullable = false )
    private Integer pageNumber;
    
    @Column( name = "file_name", nullable = false, length = 128 )
    private String fileName;
    
    @Column( name = "lct_ctx_image" )
    private Boolean lctCtxImage;
    
    @Column( name = "part_number" )
    private Integer partNumber;
    
    @Column( name = "img_width" )
    private Integer imgWidth;
    
    @Column( name = "img_height" )
    private Integer imgHeight;
}
