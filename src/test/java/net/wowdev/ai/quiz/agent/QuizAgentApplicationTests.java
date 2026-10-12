package net.wowdev.ai.quiz.agent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:quiz-agent-test",
      "spring.datasource.driver-class-name=org.h2.Driver",
      "spring.jpa.hibernate.ddl-auto=create-drop"
    })
class QuizAgentApplicationTests {

  @Test
  void contextLoads() {
  }

}
