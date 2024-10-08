package com.quiz.order.repository;

import com.quiz.order.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("SELECT q FROM Question q WHERE q.active IN :statuses ORDER BY " +
        "CASE q.active WHEN 'P' THEN 1 WHEN 'Y' THEN 2 WHEN 'C' THEN 3 WHEN 'N' THEN 4 END, " +
        "CASE WHEN q.active = 'C' THEN q.questionId END DESC, " +
        "CASE WHEN q.active <> 'C' THEN q.questionId END ASC")
    List<Question> findOrderedQuestions(@Param("statuses") List<Character> statuses);

    @Modifying
    @Transactional
    @Query("UPDATE Question q SET q.timeLeft = q.timeLeft - 1 WHERE q.active IN :statuses AND q.timeLeft > 0")
    void decrementTimeLeftForActiveQuestions(@Param("statuses") List<Character> statuses);

    // Query to update 'P' to 'Y' where timeLeft is 0
    @Modifying
    @Transactional
    @Query("UPDATE Question q SET q.active = 'Y', q.timeLeft = 60 WHERE q.active = 'P' AND q.timeLeft = 0")
    void activatePendingQuestions();

    // Query to update 'Y' to 'C' where timeLeft is 0
    @Modifying
    @Transactional
    @Query("UPDATE Question q SET q.active = 'C', q.timeLeft = 10 WHERE q.active = 'Y' AND q.timeLeft = 0")
    void completeActiveQuestions();

    @Query("SELECT q FROM Question q WHERE q.active = 'N' ORDER BY q.questionId ASC")
    List<Question> findInactiveQuestions();

    // Query to update status to 'P' and timeLeft to 45
    @Modifying
    @Transactional
    @Query("UPDATE Question q SET q.active = 'P', q.timeLeft = 45 WHERE q.questionId = :questionId")
    void activateNextInactiveQuestion(@Param("questionId") Long questionId);

    // Reset all questions to Incomplete
    @Modifying
    @Transactional
    @Query("UPDATE Question q SET q.active = 'N'")
    void resetAllQuestions();
}
