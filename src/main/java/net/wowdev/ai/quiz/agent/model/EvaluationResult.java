package net.wowdev.ai.quiz.agent.model;

import java.util.List;

public record EvaluationResult(
    boolean isCorrect,
    String scoreJustification,
    List<String> tips,
    List<String> enhancementPoints
) {}
