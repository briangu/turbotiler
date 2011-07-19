package org.linkedin.contest.game.api;

import java.util.List;

/**
 * Represents a discard move
 */
public final class Discard implements Move
{
  /**
   * The letters to discard
   */
  private final List<Letter> _discards;
  
  public Discard(List<Letter> discards)
  {
    _discards = discards;
  }
  
  public List<Letter> getDiscards()
  {
    return _discards;
  }
  
  @Override
  public String toString()
  {
    return "Discarding: " + _discards;
  }
}
