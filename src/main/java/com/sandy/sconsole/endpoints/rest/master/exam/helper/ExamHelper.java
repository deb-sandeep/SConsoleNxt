package com.sandy.sconsole.endpoints.rest.master.exam.helper;

import com.sandy.sconsole.dao.exam.Exam;
import com.sandy.sconsole.dao.exam.ExamQuestion;
import com.sandy.sconsole.dao.exam.ExamSection;
import com.sandy.sconsole.dao.exam.Question;
import com.sandy.sconsole.dao.exam.repo.ExamRepo;
import com.sandy.sconsole.dao.exam.repo.QuestionRepo;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.ExamQuestionVO;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.ExamSectionVO;
import com.sandy.sconsole.endpoints.rest.master.exam.vo.ExamVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
@Scope( "prototype" )
public class ExamHelper {
    
    @Autowired
    private ExamRepo examRepo ;

    @Autowired
    private QuestionRepo questionRepo ;
    
    @Transactional
    public int createExam( ExamVO examVO ) {
        Exam exam = new Exam( examVO ) ;
        Exam savedExam = examRepo.saveAndFlush( exam ) ;
        return savedExam.getId() ;
    }
    
    public List<ExamVO> getListOfExams() {
        List<Exam> exams = examRepo.findAll() ;
        List<ExamVO> examVoList = new ArrayList<>() ;
        exams.forEach( e -> examVoList.add( new ExamVO( e, false ) ) ) ;
        return examVoList ;
    }
    
    public ExamVO getExamConfig( int examId ) {
        Exam exam = examRepo.findById( examId ).get() ;
        return new ExamVO( exam, true ) ;
    }
    
    @Transactional
    public int updateExam( ExamVO examVO ) {
        
        Exam exam = examRepo.findById( examVO.getId() ).get() ;

        int questionSequence = 1 ;
        List<ExamSection> entitySections = exam.getSections().stream()
                                               .sorted( Comparator.comparing( ExamSection::getExamSequence ) )
                                               .toList() ;

        Map<ExamSection, ExamSectionVO> twinSections = new LinkedHashMap<>() ;
        for( ExamSection entitySection : entitySections ) {
            ExamSectionVO twinSectionVO = findTwinSection( entitySection, examVO.getSections() ) ;
            twinSections.put( entitySection, twinSectionVO ) ;
        }

        for( ExamSection entitySection : entitySections ) {
            entitySection.getQuestions().clear() ;
        }

        // Force orphan-removal deletes before inserting replacement rows.
        examRepo.flush() ;

        for( Map.Entry<ExamSection, ExamSectionVO> twinSection : twinSections.entrySet() ) {

            ExamSection entitySection = twinSection.getKey() ;
            ExamSectionVO twinSectionVO = twinSection.getValue() ;

            for( ExamQuestionVO questionVO : twinSectionVO.getQuestions() ) {
                
                Question question = questionRepo.findById( questionVO.getQuestionId() ).get() ;
                ExamQuestion examQuestion = new ExamQuestion(
                        null, question, entitySection, questionSequence++
                ) ;
                entitySection.getQuestions().add( examQuestion ) ;
            }
        }
        Exam savedExam = examRepo.saveAndFlush( exam ) ;
        return savedExam.getId() ;
    }

    private ExamSectionVO findTwinSection( ExamSection entitySection,
                                           List<ExamSectionVO> sectionVOs ) {
        
        int examSequence = entitySection.getExamSequence() ;
        String syllabusName = entitySection.getSyllabus().getSyllabusName() ;
        String problemType = entitySection.getProblemType().getProblemType() ;
        
        List<ExamSectionVO> matches = sectionVOs.stream()
                                                .filter( sectionVO -> isTwinSection( entitySection, sectionVO ) )
                                                .toList() ;

        if( matches.isEmpty() ) {
            throw new IllegalArgumentException(
                    "No matching section found for examSequence=" + examSequence +
                    ", syllabusName=" + syllabusName + ", problemType=" + problemType ) ;
        }
        if( matches.size() > 1 ) {
            throw new IllegalArgumentException(
                    "Multiple matching sections found for examSequence=" + examSequence +
                    ", syllabusName=" + syllabusName + ", problemType=" + problemType ) ;
        }
        return matches.get( 0 ) ;
    }
    
    private boolean isTwinSection( ExamSection entitySection, ExamSectionVO sectionVO ) {
        int examSequence = entitySection.getExamSequence() ;
        String syllabusName = entitySection.getSyllabus().getSyllabusName() ;
        String problemType = entitySection.getProblemType().getProblemType() ;

        return Objects.equals( examSequence, sectionVO.getExamSequence() ) &&
                Objects.equals( syllabusName, sectionVO.getSyllabusName() ) &&
                Objects.equals( problemType, sectionVO.getProblemType() ) ;
    }
}
