package net.wowdev.ai.quiz.agent.repository;

import java.util.List;
import net.wowdev.ai.quiz.agent.entity.EvaluationResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface EvaluationResultRepository extends JpaRepository<EvaluationResultEntity, Long> {

  @Query(
      "select distinct result from EvaluationResultEntity result "
          + "left join fetch result.tips "
          + "left join fetch result.enhancementPoints "
          + "where result.sessionId = :sessionId order by result.id")
  List<EvaluationResultEntity> findHistoryBySessionId(@Param("sessionId") String sessionId);

  @Modifying
  @Transactional
  @Query("delete from EvaluationResultEntity result where result.sessionId = :sessionId")
  void deleteBySessionId(@Param("sessionId") String sessionId);
}
