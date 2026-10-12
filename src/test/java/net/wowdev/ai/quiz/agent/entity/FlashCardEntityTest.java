package net.wowdev.ai.quiz.agent.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FlashCardEntityTest {

  @Test
  void noArgConstructorAndAccessorsWork() {
    FlashCardEntity entity = new FlashCardEntity();

    entity.setId(1L);
    entity.setQuestion("Question");
    entity.setCorrectAnswer("Answer");

    assertThat(entity.getId()).isEqualTo(1L);
    assertThat(entity.getQuestion()).isEqualTo("Question");
    assertThat(entity.getCorrectAnswer()).isEqualTo("Answer");
    assertThat(entity.toString()).contains("Question", "Answer");
  }

  @Test
  void allArgsConstructorSetsFields() {
    FlashCardEntity entity = new FlashCardEntity(2L, "Question", "Answer");

    assertThat(entity.getId()).isEqualTo(2L);
    assertThat(entity.getQuestion()).isEqualTo("Question");
    assertThat(entity.getCorrectAnswer()).isEqualTo("Answer");
  }
}
