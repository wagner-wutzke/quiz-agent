package net.wowdev.ai.quiz.agent.repository;

import net.wowdev.ai.quiz.agent.entity.FlashCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FlashCardRepository extends JpaRepository<FlashCardEntity, Long> {

  @Query(value = "SELECT * FROM flashcard ORDER BY RAND() LIMIT 1", nativeQuery = true)
  FlashCardEntity findRandomFlashcard();
}