package use_case.exam;

import entity.AcademicRecord;
import entity.ExamResult;
import entity.Player;
import use_case.question.QuestionTopic;

public interface ExamStateGateway {
    AcademicRecord getAcademicRecord();
    Player getPlayer();
    QuestionTopic getQuestionTopic(String courseId);
    void startExam(String courseId, String attemptId, int energyCost);
    void completeExam(String courseId, String attemptId, ExamResult result);
    void abandonExam(String attemptId);
}
