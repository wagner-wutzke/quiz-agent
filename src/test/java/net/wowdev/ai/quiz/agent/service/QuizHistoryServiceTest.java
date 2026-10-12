package net.wowdev.ai.quiz.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import net.wowdev.ai.quiz.agent.entity.EvaluationResultEntity;
import net.wowdev.ai.quiz.agent.model.EvaluationResult;
import net.wowdev.ai.quiz.agent.repository.EvaluationResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuizHistoryServiceTest {

  @Mock private EvaluationResultRepository repository;
  @InjectMocks private QuizHistoryService service;

  @Test
  void savesEvaluationForSession() {
    EvaluationResult evaluation = new EvaluationResult(true, "Good", List.of(), List.of());
    EvaluationResultEntity saved =
        new EvaluationResultEntity("session", "Question", "Expected", "Answer", evaluation);
    when(repository.save(org.mockito.ArgumentMatchers.any(EvaluationResultEntity.class)))
        .thenReturn(saved);

    EvaluationResultEntity actual =
        service.save("session", "Question", "Expected", "Answer", evaluation);

    assertThat(actual).isSameAs(saved);
    verify(repository).save(org.mockito.ArgumentMatchers.any(EvaluationResultEntity.class));
  }

  @Test
  void findsHistoryForSession() {
    List<EvaluationResultEntity> entries = List.of();
    when(repository.findHistoryBySessionId("session")).thenReturn(entries);

    assertThat(service.findBySessionId("session")).isSameAs(entries);
    verify(repository).findHistoryBySessionId("session");
  }

  @Test
  void deletesHistoryForSession() {
    service.deleteBySessionId("session");

    verify(repository).deleteBySessionId("session");
  }
}
