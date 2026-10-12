package net.wowdev.ai.quiz.agent.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;

@Configuration
public class ChatConfiguration {

  @Value("classpath:/prompts/system-prompt.st")
  private Resource systemPrompt;

  @Bean
  ChatClient chatClient(ChatClient.Builder builder) {
    return builder
        .defaultSystem(systemPrompt)
        .defaultAdvisors(new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE))
        .build();
  }
}
