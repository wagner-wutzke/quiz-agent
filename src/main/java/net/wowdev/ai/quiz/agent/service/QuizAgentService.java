package net.wowdev.ai.quiz.agent.service;

import java.util.Map;
import net.wowdev.ai.quiz.agent.model.EvaluationResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class QuizAgentService {

  private final ChatClient chatClient;

  public QuizAgentService(ChatClient chatClient) {
    this.chatClient = chatClient;
  }

  public EvaluationResult evaluateAnswer(String question, String expected, String userResponse) {
    String userPromptTemplate =
        """
            Question: {question}
            Expected Reference Answer: {expected}
            Student Answer: {userResponse}
            """;

    // Map inputs to the prompt variables
    Map<String, Object> model =
        Map.of(
            "question", question,
            "expected", expected,
            "userResponse", userResponse);

    // Fetch and parse the response strictly as our Java Record
    return chatClient
        .prompt()
        .user(u -> u.text(userPromptTemplate).params(model))
        .call()
        .entity(EvaluationResult.class);
  }
}
