package com.sandy.sconsole.dao.master.repo;

import com.sandy.sconsole.dao.master.Book;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepo extends CrudRepository<Book, Integer> {
    
    interface BookSummary {
        
        Integer getId() ;
        String getSubjectName() ;
        String getSyllabusName() ;
        String getSeriesName() ;
        String getBookName() ;
        String getAuthor() ;
        String getBookShortName() ;
        boolean isTopicMappingDone() ;
        int getNumChapters() ;
        int getNumProblems() ;
    }
    
    interface ProblemTypeCount {
        int getChapterNum() ;
        String getChapterName() ;
        int getExerciseNum() ;
        String getExerciseName() ;
        String getProblemType() ;
        int getNumProblems() ;
    }
    
    List<Book> findBySubject_SubjectNameIgnoreCase( String subjectName ) ;
    
    @Query( """
        select b
        from Book b
        where
            upper(b.subject.subjectName) = upper(?1) and
            upper(b.bookName) = upper(?2) and
            upper(b.author) = upper(?3)
    """ )
    Book findBook( String subjectName, String bookName, String author ) ;
    
    @Query( """
        select b.id as id,
               b.subject.subjectName as subjectName,
               sbm.syllabus.syllabusName as syllabusName,
               b.seriesName as seriesName,
               b.bookName as bookName,
               b.author as author,
               b.bookShortName as bookShortName,
               b.topicMappingDone as topicMappingDone,
               count( distinct( c ) ) as numChapters,
               count( p ) as numProblems
        from Book b
            left outer join Chapter c
                on c.book = b
            left outer join Problem p
                on p.chapter.book = b and
                   p.chapter = c
            left outer join SyllabusBookMap sbm
                on sbm.book = b
        group by
            b.id, sbm.syllabus.syllabusName
        order by
            b.subject.subjectName asc,
            b.seriesName asc,
            b.id asc
    """ )
    List<BookSummary> findAllBooks() ;
    
    @Query( """
        select
            c.id.chapterNum as chapterNum,
            c.chapterName as chapterName,
            p.exerciseNum as exerciseNum,
            p.exerciseName as exerciseName,
            p.problemType.problemType as problemType,
            count( p.problemKey ) as numProblems
        from Book b
            left outer join Chapter c
                on c.book = b
            left outer join Problem p
                on p.chapter.book = b and
                   p.chapter = c
            where
                b.id = :bookId
        group by
            c.id.chapterNum,
            c.chapterName,
            p.exerciseNum,
            p.exerciseName,
            p.problemType
        order by
            c.id.chapterNum asc,
            p.exerciseNum asc,
            p.problemType.problemType asc
    """ )
    List<ProblemTypeCount> getProblemSummariesForChapter( @Param( "bookId" ) int bookId ) ;
}
