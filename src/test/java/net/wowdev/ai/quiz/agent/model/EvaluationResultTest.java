package net.wowdev.ai.quiz.agent.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class EvaluationResultTest {

  @Test
  void recordExposesEvaluationFields() {
    EvaluationResult result = new EvaluationResult(true, "Justification", List.of("Tip"), List.of("Improve"));

    assertThat(result.isCorrect()).isTrue();
    assertThat(result.scoreJustification()).isEqualTo("Justification");
    assertThat(result.tips()).containsExactly("Tip");
    assertThat(result.enhancementPoints()).containsExactly("Improve");
  }
}
