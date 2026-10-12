package net.wowdev.ai.quiz.agent;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class QuizAgentApplicationTest {

  @Test
  void mainStartsSpringApplication() {
    try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
      QuizAgentApplication.main(new String[] {"--test"});

      springApplication.verify(
          () -> SpringApplication.run(QuizAgentApplication.class, new String[] {"--test"}));
    }
  }
}
