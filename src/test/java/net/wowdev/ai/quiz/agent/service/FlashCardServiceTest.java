package net.wowdev.ai.quiz.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.wowdev.ai.quiz.agent.entity.FlashCardEntity;
import net.wowdev.ai.quiz.agent.repository.FlashCardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FlashCardServiceTest {

  @Mock private FlashCardRepository repository;
  @InjectMocks private FlashCardService service;

  @Test
  void returnsRandomFlashcardFromRepository() {
    FlashCardEntity flashCard = new FlashCardEntity(4L, "Question", "Answer");
    when(repository.findRandomFlashcard()).thenReturn(flashCard);

    assertThat(service.findRandomFlashcard()).isSameAs(flashCard);
    verify(repository).findRandomFlashcard();
  }

  @Test
  void returnsRepositoryFlashcardCount() {
    when(repository.count()).thenReturn(8L);

    assertThat(service.count()).isEqualTo(8L);
    verify(repository).count();
  }
}
