package net.wowdev.ai.quiz.agent.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import net.wowdev.ai.quiz.agent.model.EvaluationResult;

@Entity
@Table(name = "evaluation_results")
public class EvaluationResultEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "session_id", nullable = false, length = 64)
  private String sessionId;

  @Column(nullable = false, length = 4000)
  private String question;

  @Column(name = "expected_answer", length = 4000)
  private String expectedAnswer;

  @Column(nullable = false, length = 4000)
  private String answer;

  @Column(name = "is_correct", nullable = false)
  private boolean correct;

  @Column(name = "score_justification", length = 4000)
  private String scoreJustification;

  @ElementCollection
  @CollectionTable(name = "evaluation_result_tips", joinColumns = @JoinColumn(name = "evaluation_id"))
  @OrderColumn(name = "item_order")
  @Column(name = "tip", length = 2000)
  private List<String> tips = new ArrayList<>();

  @ElementCollection
  @CollectionTable(name = "evaluation_result_enhancements", joinColumns = @JoinColumn(name = "evaluation_id"))
  @OrderColumn(name = "item_order")
  @Column(name = "enhancement_point", length = 2000)
  private List<String> enhancementPoints = new ArrayList<>();

  protected EvaluationResultEntity() {}

  public EvaluationResultEntity(
      String sessionId,
      String question,
      String expectedAnswer,
      String answer,
      EvaluationResult result) {
    this.sessionId = sessionId;
    this.question = question;
    this.expectedAnswer = expectedAnswer;
    this.answer = answer;
    this.correct = result.isCorrect();
    this.scoreJustification = result.scoreJustification();
    this.tips = result.tips() == null ? new ArrayList<>() : new ArrayList<>(result.tips());
    this.enhancementPoints = result.enhancementPoints() == null
        ? new ArrayList<>() : new ArrayList<>(result.enhancementPoints());
  }

  public Long getId() { return id; }
  public String getSessionId() { return sessionId; }
  public String getQuestion() { return question; }
  public String getExpectedAnswer() { return expectedAnswer; }
  public String getAnswer() { return answer; }
  public boolean isCorrect() { return correct; }
  public String getScoreJustification() { return scoreJustification; }
  public List<String> getTips() { return List.copyOf(tips); }
  public List<String> getEnhancementPoints() { return List.copyOf(enhancementPoints); }

  public EvaluationResult toEvaluationResult() {
    return new EvaluationResult(correct, scoreJustification, getTips(), getEnhancementPoints());
  }

  public EvaluationResult getResult() {
    return toEvaluationResult();
  }
}
