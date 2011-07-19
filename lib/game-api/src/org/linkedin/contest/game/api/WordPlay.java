/**
 * $Id:$
 */
package org.linkedin.contest.game.api;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a move where a word is played on to the board
 */
public final class WordPlay implements Move
{
  private final List<LetterPlay> _play = new ArrayList<LetterPlay>();

  public void setLetter(Letter letter, Coordinate coord)
  {
    LetterPlay letterPlay = new LetterPlay(letter, coord);
    _play.add(letterPlay);
  }

  public List<Letter> getLetters()
  {
    List<Letter> letters = new ArrayList<Letter>();
    for(LetterPlay letterPlay : _play)
    {
      letters.add(letterPlay._letter);
    }
    return letters;
  }

  public Letter getLetter(int index)
  {
    return _play.get(index)._letter;
  }

  public Letter getLetter(Coordinate coord)
  {
    for (int cntr = 0; cntr < _play.size(); cntr++)
    {
      LetterPlay letterPlay = _play.get(cntr);
      if (letterPlay._coord.equals(coord))
      {
        return letterPlay._letter;
      }
    }
    return null;
  }

  public Coordinate getCoordinate(int index)
  {
    return _play.get(index)._coord;
  }

  public int getLetterCount()
  {
    return _play.size();
  }

  @Override
  public String toString()
  {
    StringBuilder sb = new StringBuilder();
    
    sb.append("Play: ");
    for(LetterPlay letterPlay : _play)
    {
      sb.append(letterPlay._letter);
    }
    return sb.toString();
  }

  private static class LetterPlay
  {
    private final Letter _letter;
    private final Coordinate _coord;

    private LetterPlay(Letter letter, Coordinate coord)
    {
      _letter = letter;
      _coord = coord;
    }
  }
}
