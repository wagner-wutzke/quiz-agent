package net.wowdev.ai.quiz.agent.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;

class ChatConfigurationTest {

  @Test
  void createsChatClientWithSystemPromptAndAdvisor() {
    ChatClient.Builder builder = mock(ChatClient.Builder.class);
    ChatClient chatClient = mock(ChatClient.class);
    when(builder.defaultSystem(any(org.springframework.core.io.Resource.class)))
        .thenReturn(builder);
    when(builder.defaultAdvisors(any(org.springframework.ai.chat.client.advisor.api.Advisor.class)))
        .thenReturn(builder);
    when(builder.build()).thenReturn(chatClient);
    ChatConfiguration configuration = new ChatConfiguration();
    ReflectionTestUtils.setField(
        configuration, "systemPrompt", new ByteArrayResource("prompt".getBytes()));

    assertThat(configuration.chatClient(builder)).isSameAs(chatClient);
  }
}
