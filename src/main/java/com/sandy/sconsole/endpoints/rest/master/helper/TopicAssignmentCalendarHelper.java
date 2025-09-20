package com.sandy.sconsole.endpoints.rest.master.helper;

import com.sandy.sconsole.dao.master.Topic;
import com.sandy.sconsole.dao.master.TopicTrackAssignment;
import com.sandy.sconsole.dao.master.repo.TopicRepo;
import com.sandy.sconsole.dao.master.repo.TopicTrackAssignmentRepo;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.sandy.sconsole.core.util.SConsoleUtil.durationDays;

@Component()
@Scope( "prototype" )
public class TopicAssignmentCalendarHelper {
    
    private final SimpleDateFormat DT_DF      = new SimpleDateFormat( "yyyyMMdd" ) ;
    private final SimpleDateFormat DTSTAMP_DF = new SimpleDateFormat( "yyyyMMdd'T'HHmmss'Z'" ) ;
    
    @Autowired
    private TopicTrackAssignmentRepo ttaRepo = null ;
    
    @Autowired
    private TopicRepo topicRepo = null ;
    
    public TopicAssignmentCalendarHelper() {
        DTSTAMP_DF.setTimeZone( java.util.TimeZone.getTimeZone( "UTC" ) ) ;
    }
    
    public String getCalendarEntries( String syllabusName ) {
        
        List<TopicTrackAssignment> ttaList = ttaRepo.findBySyllabus( syllabusName );
        StringBuilder ics     = new StringBuilder();
        ics.append( "BEGIN:VCALENDAR\n" );
        ics.append( "VERSION:2.0\n" );
        ics.append( "PRODID:-//SConsole//EN\n" );
        
        for( TopicTrackAssignment tta : ttaList ) {
            Topic topic = topicRepo.findById( tta.getTopicId() ).get() ;
            TopicAssignment ta = new TopicAssignment( tta, topic ) ;
            
            ics.append( ta.getCoachingCalendarEntry() );
            ics.append( ta.getSelfStudyCalendarEntry() );
            ics.append( ta.getExerciseCalendarEntry() );
            ics.append( ta.getConsolidationCalendarEntry() );
        }
        
        ics.append( "END:VCALENDAR\n" );
        return ics.toString();
    }
    
    // =============================================================================================
    private class TopicAssignment {
        
        public static final String ICON_COACHING_PERSON = "\uD83E\uDDD1\u200D\uD83C\uDFEB"; // 🧑‍🏫 (gender-neutral)
        public static final String ICON_SELF_STUDY = "\uD83D\uDCD6";                        // 📖
        public static final String ICON_EXERCISE = "\u270D\uFE0F";                          // ✍️
        public static final String ICON_CONSOLIDATION = "\uD83D\uDDC2";                     // 🗂
        
        private final TopicTrackAssignment tta ;
        private final Topic topic ;
        
        private final Date startDate ;
        private final Date endDate ;
        
        // Derived information from the topic plan
        private final Date coachingStartDate ;
        private final Date coachingEndDate ;
        private final Date selfStudyStartDate ;
        private final Date selfStudyEndDate ;
        private final Date exerciseStartDate ;
        private final Date exerciseEndDate ;
        private final Date consolidationStartDate ;
        private final Date consolidationEndDate ;
        
        private final Date creationTimeStamp ;
        
        TopicAssignment( TopicTrackAssignment tta, Topic topic ) {
            this.creationTimeStamp = new Date() ;
            
            this.tta = tta ;
            this.topic = topic ;
            this.startDate = tta.getStartDate() ;
            this.endDate   = tta.getEndDate() ; // End time is 23:59:59 of the end date
            
            int numTotalDays         = durationDays( startDate, endDate ) ;
            int coachingNumDays      = tta.getCoachingNumDays() ;
            int selfStudyNumDays     = tta.getSelfStudyNumDays() ;
            int consolidationNumDays = tta.getConsolidationNumDays() ;
            int numExerciseDays      = numTotalDays - coachingNumDays - selfStudyNumDays - consolidationNumDays ;
            
            coachingStartDate      = startDate ;
            coachingEndDate        = DateUtils.addDays( startDate, coachingNumDays ) ;
            selfStudyStartDate     = DateUtils.addDays( startDate, coachingNumDays ) ;
            selfStudyEndDate       = DateUtils.addDays( startDate, coachingNumDays + selfStudyNumDays ) ;
            exerciseStartDate      = DateUtils.addDays( startDate, coachingNumDays + selfStudyNumDays ) ;
            exerciseEndDate        = DateUtils.addDays( exerciseStartDate, numExerciseDays ) ;
            consolidationStartDate = DateUtils.addDays( endDate, -consolidationNumDays ) ;
            consolidationEndDate   = DateUtils.addDays( endDate, 1 ) ; ;
        }
        
        StringBuilder getCoachingCalendarEntry() {
            return getCalendarEntry( "Coaching", ICON_COACHING_PERSON, coachingStartDate, coachingEndDate ) ;
        }
        
        StringBuilder getSelfStudyCalendarEntry() {
            return getCalendarEntry( "Self-Study", ICON_SELF_STUDY, selfStudyStartDate, selfStudyEndDate ) ;
        }
        
        StringBuilder getExerciseCalendarEntry() {
            return getCalendarEntry( "Exercise", ICON_EXERCISE, exerciseStartDate, exerciseEndDate ) ;
        }
        
        StringBuilder getConsolidationCalendarEntry() {
            return getCalendarEntry( "Wrap-Up", ICON_CONSOLIDATION, consolidationStartDate, consolidationEndDate ) ;
        }
        
        private String generateUID( String phase ) {
            return topic.getId() + "-" + phase + "@sconsole" ;
        }
        
        private String generateSummary( String phase, String icon ) {
            return icon + " " + topic.getTopicName() + " [" + phase + "]" ;
        }
        
        private StringBuilder getCalendarEntry( String phase, String icon, Date startDate, Date endDate ) {
            StringBuilder ics = new StringBuilder();
            ics.append( "BEGIN:VEVENT" ).append( "\n" ) ;
            ics.append( "UID:" ).append( generateUID( phase ) ).append( "\n" ) ;
            ics.append( "DTSTAMP:" ).append( DTSTAMP_DF.format( creationTimeStamp ) ).append( "\n" ) ;
            ics.append( "DTSTART;VALUE=DATE:" ).append( DT_DF.format( startDate ) ).append( "\n" ) ;
            ics.append( "DTEND;VALUE=DATE:" ).append( DT_DF.format( endDate ) ).append( "\n" ) ;
            ics.append( "SUMMARY:" ).append( generateSummary( phase, icon ) ).append( "\n" ) ;
            ics.append( "TRANSP:TRANSPARENT" ).append( "\n" ) ;
            ics.append( "END:VEVENT" ).append( "\n" ) ;
            return ics ;
        }
    }
}
