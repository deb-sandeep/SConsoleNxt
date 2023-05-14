package com.sandy.sconsole.dao.slide;

import com.sandy.sconsole.daemon.refresher.internal.Path;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SlideRepo extends JpaRepository<Slide, Integer> {

    @NotNull
    @Query( "SELECT s " +
            "FROM Slide s " +
            "ORDER BY " +
            "   s.syllabus ASC, " +
            "   s.subject ASC, " +
            "   s.chapter ASC, " +
            "   s.slideName ASC "
    )
    List<Slide> findAll() ;

    @Query( "SELECT s " +
            "FROM Slide  s " +
            "WHERE s.syllabus  = :#{#path.syllabus} AND " +
            "      s.subject   = :#{#path.subject}  AND " +
            "      s.chapter   = :#{#path.chapter}  AND " +
            "      s.slideName = :#{#path.fileName} "
    )
    Slide findByPath( @Param( "path" ) Path path ) ;

    @Query( "SELECT s " +
            "FROM Slide s " +
            "WHERE s.syllabus = :syllabus " +
            "ORDER BY " +
            "   s.subject ASC, " +
            "   s.chapter ASC, " +
            "   s.slideName ASC "
    )
    List<Slide> findBySyllabus( @Param( "syllabus" ) String syllabus ) ;

    @Query( "SELECT s " +
            "FROM Slide  s " +
            "WHERE " +
            "   s.syllabus = :syllabus AND " +
            "   s.subject = :subject " +
            "ORDER BY " +
            "   s.chapter ASC, " +
            "   s.slideName ASC "
    )
    List<Slide> findBySyllabusAndSubject( @Param( "syllabus" ) String syllabus,
                                          @Param( "subject" ) String subject ) ;

    @Query( "SELECT s " +
            "FROM Slide  s " +
            "WHERE " +
            "   s.syllabus = :syllabus AND " +
            "   s.subject = :subject AND " +
            "   s.chapter = :chapter " +
            "ORDER BY " +
            "   s.slideName ASC "
    )
    List<Slide> findBySyllabusSubjectAndChapter(
                                          @Param( "syllabus" ) String syllabus,
                                          @Param( "subject" ) String subject,
                                          @Param( "chapter" ) String chapter ) ;
}
