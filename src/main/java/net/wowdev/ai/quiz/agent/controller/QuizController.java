package net.wowdev.ai.quiz.agent.controller;

import net.wowdev.ai.quiz.agent.entity.FlashCardEntity;
import net.wowdev.ai.quiz.agent.model.EvaluationResult;
import net.wowdev.ai.quiz.agent.repository.FlashCardRepository;
import net.wowdev.ai.quiz.agent.service.QuizAgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

  private final FlashCardRepository repository;
  private final QuizAgentService agentService;

  public QuizController(FlashCardRepository repository, QuizAgentService agentService) {
    this.repository = repository;
    this.agentService = agentService;
  }

  @GetMapping("/question")
  public ResponseEntity<QuestionResponse> nextQuestion() {
    FlashCardEntity flashCard = repository.findRandomFlashcard();
    if (flashCard == null) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.ok(
        new QuestionResponse(
            flashCard.getId(),
            flashCard.getQuestion(),
            flashCard.getCorrectAnswer(),
            repository.count()));
  }

  @PostMapping("/evaluate")
  public EvaluationResult evaluate(@RequestBody EvaluationRequest request) {
    return agentService.evaluateAnswer(
        request.question(), request.expectedAnswer(), request.answer());
  }

  public record QuestionResponse(
      Long id, String question, String expectedAnswer, long totalQuestions) {}

  public record EvaluationRequest(String question, String expectedAnswer, String answer) {}
}
