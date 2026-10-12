package net.wowdev.ai.quiz.agent.repository;

import java.util.List;
import net.wowdev.ai.quiz.agent.entity.EvaluationResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvaluationResultRepository extends JpaRepository<EvaluationResultEntity, Long> {
  List<EvaluationResultEntity> findBySessionIdOrderByIdAsc(String sessionId);
  void deleteBySessionId(String sessionId);
}
