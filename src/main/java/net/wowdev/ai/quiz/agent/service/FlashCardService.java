package net.wowdev.ai.quiz.agent.service;

import net.wowdev.ai.quiz.agent.entity.FlashCardEntity;
import net.wowdev.ai.quiz.agent.repository.FlashCardRepository;
import org.springframework.stereotype.Service;

@Service
public class FlashCardService {

  private final FlashCardRepository repository;

  public FlashCardService(FlashCardRepository repository) {
    this.repository = repository;
  }

  public FlashCardEntity findRandomFlashcard() {
    return repository.findRandomFlashcard();
  }

  public long count() {
    return repository.count();
  }
}
