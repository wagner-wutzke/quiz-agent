package net.wowdev.ai.quiz.agent.service;

import java.util.List;
import net.wowdev.ai.quiz.agent.entity.EvaluationResultEntity;
import net.wowdev.ai.quiz.agent.model.EvaluationResult;
import net.wowdev.ai.quiz.agent.repository.EvaluationResultRepository;
import org.springframework.stereotype.Service;

@Service
public class QuizHistoryService {

  private final EvaluationResultRepository repository;

  public QuizHistoryService(EvaluationResultRepository repository) {
    this.repository = repository;
  }

  public EvaluationResultEntity save(
      String sessionId,
      String question,
      String expectedAnswer,
      String answer,
      EvaluationResult evaluation) {
    return repository.save(
        new EvaluationResultEntity(sessionId, question, expectedAnswer, answer, evaluation));
  }

  public List<EvaluationResultEntity> findBySessionId(String sessionId) {
    return repository.findHistoryBySessionId(sessionId);
  }

  public void deleteBySessionId(String sessionId) {
    repository.deleteBySessionId(sessionId);
  }
}
