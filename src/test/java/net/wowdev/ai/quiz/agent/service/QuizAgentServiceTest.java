package net.wowdev.ai.quiz.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;
import net.wowdev.ai.quiz.agent.model.EvaluationResult;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

class QuizAgentServiceTest {

  @Test
  void evaluatesAnswerWithTheStructuredEvaluationResult() {
    ChatClient chatClient = mock(ChatClient.class);
    ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
    ChatClient.PromptUserSpec promptUserSpec = mock(ChatClient.PromptUserSpec.class);
    ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
    EvaluationResult expected = new EvaluationResult(true, "Correct", java.util.List.of(), java.util.List.of());
    when(chatClient.prompt()).thenReturn(requestSpec);
    when(requestSpec.user(any(Consumer.class))).thenAnswer(invocation -> {
      Consumer<ChatClient.PromptUserSpec> prompt = invocation.getArgument(0);
      prompt.accept(promptUserSpec);
      return requestSpec;
    });
    when(promptUserSpec.text(any(String.class))).thenReturn(promptUserSpec);
    when(promptUserSpec.params(any(java.util.Map.class))).thenReturn(promptUserSpec);
    when(requestSpec.call()).thenReturn(responseSpec);
    when(responseSpec.entity(EvaluationResult.class)).thenReturn(expected);

    EvaluationResult actual =
        new QuizAgentService(chatClient).evaluateAnswer("Question", "Expected", "Answer");

    assertThat(actual).isSameAs(expected);
  }
}
