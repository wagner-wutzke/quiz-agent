package net.wowdev.ai.quiz.agent.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.wowdev.ai.quiz.agent.entity.FlashCardEntity;
import net.wowdev.ai.quiz.agent.model.EvaluationResult;
import net.wowdev.ai.quiz.agent.repository.FlashCardRepository;
import net.wowdev.ai.quiz.agent.repository.EvaluationResultRepository;
import net.wowdev.ai.quiz.agent.service.QuizAgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class QuizControllerTest {

  @Mock private FlashCardRepository repository;
  @Mock private QuizAgentService agentService;
  @Mock private EvaluationResultRepository evaluationRepository;

  private QuizController controller;

  @BeforeEach
  void setUp() {
    controller = new QuizController(repository, agentService, evaluationRepository);
  }

  @Test
  void nextQuestionReturnsNoContentWhenRepositoryIsEmpty() {
    when(repository.findRandomFlashcard()).thenReturn(null);

    ResponseEntity<QuizController.QuestionResponse> response = controller.nextQuestion();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  void nextQuestionReturnsQuestionAndTotal() {
    FlashCardEntity card = new FlashCardEntity(7L, "Question", "Answer");
    when(repository.findRandomFlashcard()).thenReturn(card);
    when(repository.count()).thenReturn(12L);

    ResponseEntity<QuizController.QuestionResponse> response = controller.nextQuestion();

    assertThat(response.getBody())
        .isEqualTo(new QuizController.QuestionResponse(7L, "Question", "Answer", 12L));
    verify(repository).count();
  }

  @Test
  void evaluateDelegatesToAgentService() {
    QuizController.EvaluationRequest request =
        new QuizController.EvaluationRequest("Question", "Expected", "Answer");
    EvaluationResult result = new EvaluationResult(true, "Good", java.util.List.of(), java.util.List.of());
    when(agentService.evaluateAnswer("Question", "Expected", "Answer")).thenReturn(result);

    assertThat(controller.evaluate(request)).isSameAs(result);
  }
}
