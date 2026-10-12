package net.wowdev.ai.quiz.agent.controller;

import net.wowdev.ai.quiz.agent.entity.EvaluationResultEntity;
import net.wowdev.ai.quiz.agent.entity.FlashCardEntity;
import net.wowdev.ai.quiz.agent.model.EvaluationResult;
import net.wowdev.ai.quiz.agent.repository.EvaluationResultRepository;
import net.wowdev.ai.quiz.agent.repository.FlashCardRepository;
import net.wowdev.ai.quiz.agent.service.QuizAgentService;
import org.springframework.http.ResponseEntity;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

  private final FlashCardRepository repository;
  private final QuizAgentService agentService;
  private final EvaluationResultRepository evaluationRepository;
  private final String interviewTopic;

  public QuizController(
      FlashCardRepository repository,
      QuizAgentService agentService,
      EvaluationResultRepository evaluationRepository,
      @Value("${spring.application.interview-topic:Java}") String interviewTopic) {
    this.repository = repository;
    this.agentService = agentService;
    this.evaluationRepository = evaluationRepository;
    this.interviewTopic = interviewTopic;
  }

  @GetMapping("/topic")
  public TopicResponse topic() {
    return new TopicResponse(interviewTopic);
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
  public EvaluationResultEntity evaluate(@RequestBody EvaluationRequest request,
      @RequestHeader("X-Quiz-Session-Id") String sessionId) {
    EvaluationResult result = agentService.evaluateAnswer(
        request.question(), request.expectedAnswer(), request.answer());
    return evaluationRepository.save(new EvaluationResultEntity(sessionId,
        request.question(), request.expectedAnswer(), request.answer(), result));
  }

  @GetMapping("/history")
  public List<HistoryEntry> history(
      @RequestHeader("X-Quiz-Session-Id") String sessionId) {
    return evaluationRepository.findHistoryBySessionId(sessionId).stream()
        .map(entry -> new HistoryEntry(
            entry.getId(),
            entry.getQuestion(),
            entry.getExpectedAnswer(),
            entry.getAnswer(),
            entry.toEvaluationResult()))
        .collect(Collectors.toList());
  }

  @DeleteMapping("/history")
  public ResponseEntity<Void> clearHistory(
      @RequestHeader("X-Quiz-Session-Id") String sessionId) {
    evaluationRepository.deleteBySessionId(sessionId);
    return ResponseEntity.noContent().build();
  }

  public record QuestionResponse(
      Long id, String question, String expectedAnswer, long totalQuestions) {}

  public record EvaluationRequest(String question, String expectedAnswer, String answer) {}

  public record HistoryEntry(
      Long id, String question, String expectedAnswer, String answer, EvaluationResult result) {}

  public record TopicResponse(String interviewTopic) {}
}
