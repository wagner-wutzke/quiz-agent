package net.wowdev.ai.quiz.agent.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.List;
import net.wowdev.ai.quiz.agent.model.EvaluationResult;
import org.junit.jupiter.api.Test;

class EvaluationResultEntityTest {

  private static void setId(EvaluationResultEntity entity, Long id) throws Exception {
    Field idField = EvaluationResultEntity.class.getDeclaredField("id");
    idField.setAccessible(true);
    idField.set(entity, id);
  }

  @Test
  void copiesEvaluationDataAndExposesEntityFields() throws Exception {
    List<String> tips = new java.util.ArrayList<>(List.of("Tip"));
    List<String> enhancements = new java.util.ArrayList<>(List.of("Improve"));
    EvaluationResult result = new EvaluationResult(true, "Good", tips, enhancements);
    EvaluationResultEntity entity =
        new EvaluationResultEntity("session", "Question", "Expected", "Answer", result);
    tips.add("Later tip");
    enhancements.add("Later improvement");
    setId(entity, 9L);

    assertThat(entity.getId()).isEqualTo(9L);
    assertThat(entity.getSessionId()).isEqualTo("session");
    assertThat(entity.getQuestion()).isEqualTo("Question");
    assertThat(entity.getExpectedAnswer()).isEqualTo("Expected");
    assertThat(entity.getAnswer()).isEqualTo("Answer");
    assertThat(entity.isCorrect()).isTrue();
    assertThat(entity.getScoreJustification()).isEqualTo("Good");
    assertThat(entity.getTips()).containsExactly("Tip");
    assertThat(entity.getEnhancementPoints()).containsExactly("Improve");
    EvaluationResult copiedResult =
        new EvaluationResult(true, "Good", List.of("Tip"), List.of("Improve"));
    assertThat(entity.toEvaluationResult()).isEqualTo(copiedResult);
    assertThat(entity.getResult()).isEqualTo(copiedResult);
  }

  @Test
  void replacesNullEvaluationListsWithEmptyLists() {
    EvaluationResult result = new EvaluationResult(false, "Review", null, null);

    EvaluationResultEntity entity =
        new EvaluationResultEntity("session", "Question", "Expected", "Answer", result);

    assertThat(entity.getTips()).isEmpty();
    assertThat(entity.getEnhancementPoints()).isEmpty();
    assertThat(entity.toEvaluationResult().isCorrect()).isFalse();
  }
}
