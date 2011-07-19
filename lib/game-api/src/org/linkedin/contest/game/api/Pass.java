package org.linkedin.contest.game.api;

/**
 * Represents a pass move.
 * 
 * As there is no data associated with this move, there is only a single instance accessible 
 * via Pass.INSTANCE that can be used by a player at any time
 */
public final class Pass implements Move
{
  public static final Pass INSTANCE = new Pass();

  private Pass()
  {
  }
  
  @Override
  public String toString()
  {
    return "Pass";
  }
}
