/**
 * $Id:$
 */
package org.linkedin.contest.game.api;

/**
 * Represents a single letter than can be played on the board.
 * 
 * Captures the score as well as the number of instances of the letter in the game
 */
public enum Letter
{
  A('A', 9, 1), 
  B('B', 2, 3), 
  C('C', 2, 3), 
  D('D', 4, 2), 
  E('E', 12, 1), 
  F('F', 2, 4), 
  G('G', 3, 2), 
  H('H', 2, 4), 
  I('I', 9, 1), 
  J('J', 1, 8), 
  K('K', 1, 5), 
  L('L', 4, 1),
  M('M', 2, 3),
  N('N', 6, 1),
  O('O', 8, 1),
  P('P', 2, 3),
  Q('Q', 1, 10),
  R('R', 6, 1),
  S('S', 4, 1),
  T('T', 6, 1),
  U('U', 4, 1),
  V('V', 2, 4),
  W('W', 2, 4),
  X('X', 1, 8),
  Y('Y', 2, 4),
  Z('Z', 1, 10);
  
  private final char _letter;
  private final int _occurences;
  private final int _score;

  private Letter(char letter, int occurences, int score)
  {
    _letter = letter;
    _occurences = occurences;
    _score = score;
  }

  public char getLetter()
  {
    return _letter;
  }

  public int getScore()
  {
    return _score;
  }

  public int getOccurences()
  {
    return _occurences;
  }

  @Override
  public String toString()
  {
    return Character.toString(_letter);
  }
  
  public static Letter getLetter(char letter)
  {
    return Letter.valueOf(Character.toString(Character.toUpperCase(letter)));
  }
}
