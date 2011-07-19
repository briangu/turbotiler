/**
 * $Id:$
 */
package org.linkedin.contest.game.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.linkedin.contest.game.api.Coordinate;
import org.linkedin.contest.game.api.Dictionary;
import org.linkedin.contest.game.api.Discard;
import org.linkedin.contest.game.api.Letter;
import org.linkedin.contest.game.api.Move;
import org.linkedin.contest.game.api.Pass;
import org.linkedin.contest.game.api.Board;
import org.linkedin.contest.game.api.WordPlay;
import org.linkedin.contest.game.player.Player;

/**
 * TBD: Add documentation for this class.
 */
public class SamplePlayer implements Player
{
	public enum Direction
	{
		NORTH,
		EAST,
		SOUTH,
		WEST,
		UNKNOWN
	}

	private Random _random;
    private Pattern _allVowels;
    private Pattern _allConsonants;
    private Dictionary _dictionary;
    private int _discards;

    public void init()
    {
        _random = new Random(System.currentTimeMillis());
        _allVowels = Pattern.compile("[AEIOU]+");
        _allConsonants = Pattern.compile("[^AEIOU]+");
        _dictionary = Dictionary.getInstance();
    }

    public Move move(Board board, List<Letter> letters, int myScore, int opponentScore)
    {
      Move result = allVowels(letters);
      if (result != null)
      {
        _discards++;
        return result;
      }
      result = allConsonants(letters);
      if (result != null)
      {
        _discards++;
        return result;
      }

      List<WordPlay> played = board.getPlayedWords();
      if (played.isEmpty())
      {
        // We are the first player, play at the start spot+
        result = findWord(letters, null, board.getStart(), Direction.EAST);
      }
      else
      {
        // We are on a subsequent play so find somewhere to play.
        boolean found = false;
        Move attempt = null;
        for (WordPlay word : played)
        {
          for (int counter = 0; counter < word.getLetterCount(); counter++)
          {
            // Check if this is a reasonable place to play.
            Coordinate coord = word.getCoordinate(counter);
            if (checkNorth(board, coord, 2))
            {
              Letter boardLetter = board.getLetter(coord);
              attempt = findWord(letters, boardLetter, coord, Direction.NORTH);
              if (attempt instanceof WordPlay && board.checkWordPlay((WordPlay) attempt))
              {
                found = true;
                break;
              }
            }
            if (checkSouth(board, coord, 2))
            {
              Letter boardLetter = board.getLetter(coord);
              attempt = findWord(letters, boardLetter, coord, Direction.SOUTH);
              if (attempt instanceof WordPlay && board.checkWordPlay((WordPlay) attempt))
              {
                found = true;
                break;
              }
            }
            if (checkEast(board, coord, 2))
            {
              Letter boardLetter = board.getLetter(coord);
              attempt = findWord(letters, boardLetter, coord, Direction.EAST);
              if (attempt instanceof WordPlay && board.checkWordPlay((WordPlay) attempt))
              {
                found = true;
                break;
              }
            }
            if (checkWest(board, coord, 2))
            {
              Letter boardLetter = board.getLetter(coord);
              attempt = findWord(letters, boardLetter, coord, Direction.WEST);
              if (attempt instanceof WordPlay && board.checkWordPlay((WordPlay) attempt))
              {
                found = true;
                break;
              }
            }
          }
          if (found)
          {
            _discards = 0;
            result = attempt;
            break;
          }
        }
        if (!found)
        {
          if (result == null)
          {
            result = Pass.INSTANCE;
          }
          if (result instanceof Pass && letters.size() == 7)
          {
            // Throw them all and try again
            if (_discards > 5)
            {
              // Something is wrong just pass
            }
            else
            {
              _discards++;
              result = new Discard(letters);
            }
          }
          else if (result instanceof Pass)
          {
            System.out.println("Could not find anything to play and no point discarding.");
          }
        }
      }
      return result;
	}

    private Move allVowels(List<Letter> letters)
    {
        // Demonstrate the PASS and DISCARD options
        // If we have nothing but vowels AND there are letters left in the deck trade some in.
        // If we have nothing but vowels AND there are no letters left in the deck pass.
        // Otherwise return null to indicate do something else.
        return patternCheckDiscard(letters, _allVowels);
    }

    private Move allConsonants(List<Letter> letters)
    {
        return patternCheckDiscard(letters, _allConsonants);
    }

    private Move patternCheckDiscard(List<Letter> letters, Pattern pattern)
    {
        StringBuffer letterBuffer = new StringBuffer();
        for (Letter letter : letters)
        {
            letterBuffer.append(letter);
        }

        Matcher matcher = pattern.matcher(letterBuffer.toString());
        if (!matcher.matches())
        {
            return null;
        }
        if (letters.size() < 7)
        {
            return Pass.INSTANCE;
        }
        // Randomly pick either 3 or 4 as the number of tiles to trade in.
        int tradeSize = 3 + _random.nextInt(2);
        List<Letter> discards = new ArrayList<Letter>();
        for (int counter = 0; counter < tradeSize; counter++)
        {
            Letter discard = letters.get(counter);
            discards.add(discard);
        }
        return new Discard(discards);
    }

    private Move findWord(List<Letter> letters, Letter boardLetter, Coordinate coord, Direction direction)
    {
      // The boardLetter must be part of the word IFF its not null
      // If we have less than 3 letters we cannot make a word.
      int size = letters.size() + (boardLetter != null ? 1 : 0);
      if (size < 3)
      {
        return Pass.INSTANCE;
      }
      String test = null;
      for (int indexSize = size - 1 ; indexSize >= 3; indexSize--)
      {
        // Make sure the word fits before we waste time
        switch (direction)
        {
        case NORTH:
          if (coord.getNorth(indexSize) == null)
          {
            continue;
          }
          break;
        case SOUTH:
          if (coord.getSouth(indexSize) == null)
          {
            continue;
          }
          break;
        case EAST:
          if (coord.getEast(indexSize) == null)
          {
            continue;
          }
          break;
        case WEST:
          if (coord.getWest(indexSize) == null)
          {
            continue;
          }
          break;
        }

        // Trying to find a word of size counter
        int[] testIndices = new int[indexSize];
        for (int counter = 0; counter < indexSize; counter++)
        {
          if (direction == Direction.NORTH || direction == Direction.WEST)
          {
            if (counter >= indexSize - 1)
            {
              testIndices[counter] = 0;
            }
            else
            {
              testIndices[counter] = counter + 1;
            }
          }
          else
          {
            testIndices[counter] = counter;
          }
        }
        test = getTestWordFromIndices(testIndices, letters, boardLetter);
        while (!_dictionary.checkWord(test))
        {
          if (!findNextIndices(testIndices, size, direction, boardLetter != null))
          {
            test = null;
            break;
          }
          if (direction == Direction.SOUTH || direction == Direction.EAST)
          {
            if (testIndices[0] > 0)
            {
              // Dumb client AI only looks for words STARTING with the board letter.
              test = null;
              break;
            }
          }
          else
          {
            if (testIndices[indexSize - 1] > 0)
            {
              test = null;
              continue;
            }
            if (testIndices[0] > size - 1)
            {
              test = null;
              break;
            }
          }
          test = getTestWordFromIndices(testIndices, letters, boardLetter);
        }
        if (test != null)
        {
          break;
        }
        else
        {
          // System.out.println("Could not find a word of length " + indexSize);
        }
      }
      if (test == null)
      {
        // We did not find anything; We're going to pass.
        return Pass.INSTANCE;
      }
      WordPlay result = new WordPlay();
      setLettersInWordPlay(result, boardLetter, coord, test, direction);
      return result;
    }

    private void setLettersInWordPlay(WordPlay move, Letter boardLetter, Coordinate coord, String word, Direction direction)
    {
        if (direction == Direction.NORTH)
        {
          coord = coord.getNorth(word.length() - 1);
        }
        if (direction == Direction.WEST)
        {
          coord = coord.getWest(word.length() - 1);
        }
        for (int counter = 0; counter < word.length(); counter++)
        {
            if (boardLetter != null)
            {
                if (direction == Direction.SOUTH || direction == Direction.EAST)
                {
                    // We are in the ascending direction - Board letter comes first
                    if (counter == 0)
                    {
                        continue;
                    }
                }
                else
                {
                    // We are in the descending direction - Board letter comes last
                    if (counter >= word.length()  - 1)
                    {
                        continue;
                    }
                }
            }
            char character = word.charAt(counter);
            int xoffset = 0;
            int yoffset = 0;
            switch (direction)
            {
            case NORTH:
            case SOUTH:
                yoffset = counter;
                break;
            case EAST:
            case WEST:
                xoffset = counter;
                break;
            }
            move.setLetter(Letter.getLetter(character), coord.applyOffset(xoffset, yoffset));
        }
    }

    private String getTestWordFromIndices(int[] indices, List<Letter> letters, Letter boardLetter)
    {
        StringBuffer testBuffer = new StringBuffer();
        for (int counter = 0; counter < indices.length; counter++)
        {
            if (boardLetter == null)
            {
                testBuffer.append(letters.get(indices[counter]));
            }
            else if (indices[counter] == 0)
            {
                testBuffer.append(boardLetter);
            }
            else
            {
                testBuffer.append(letters.get(indices[counter] - 1));
            }
        }
        return testBuffer.toString();
    }

    private boolean findNextIndices(int[] indices, int max, Direction direction, boolean requireZero)
    {
        boolean invalidIndices = true;
        while (invalidIndices)
        {
            invalidIndices = false;
            incrementIndices(indices, max);
            if (direction == Direction.SOUTH || direction == Direction.EAST)
            {
                if ((requireZero && indices[0] > 0) || indices[0] >= max - 1)
                {
                    // We have tried every combination of words
                    // For requiresZero we are "stupid" and require it start with the board letter.
                    return false;
                }
                if (requireZero && indices[0] != 0)
                {
                    invalidIndices = true;
                }
            }
            else
            {
                if (indices[0] >= max - 1)
                {
                    return false;
                }
                if (requireZero && indices[indices.length - 1] != 0)
                {
                    invalidIndices = true;
                }
            }
        }
        return true;
    }

    private boolean hasDuplicateIndices(int[] indices)
    {
        for (int first = 0; first < indices.length - 1; first++)
        {
            for (int second = first + 1; second < indices.length; second++)
            {
                if (indices[first] == indices[second])
                {
                    return true;
                }
            }
        }
        return false;
    }

    private void incrementIndices(int[] indices, int max)
    {
        boolean first = true;
        while (first || hasDuplicateIndices(indices))
        {
            int index = indices.length - 1;
            while (indices[index] >= max - 1)
            {
                if (index <= 0)
                {
                    break;
                }
                indices[index] = 0;
                index--;
            }
            indices[index]++;
            first = false;
        }
    }

	private boolean checkNorth(Board board, Coordinate coord, int length)
	{
	  Coordinate c = coord;
	  for (int counter = 1; counter <= length; counter++)
	  {
	    c = c.getNorth();
	    if (c == null)
	    {
	      return false;
	    }
	    if (board.getLetter(c) != null)
	    {
	      return false;
	    }
	  }
	  c = coord.getNorthWest();
	  if (c != null && board.getLetter(c) != null)
	  {
	    return false;
	  }
      c = coord.getNorthEast();
	  if (c != null && board.getLetter(c) != null)
	  {
	    return false;
	  }
	  c = coord.getSouth();
	  if (c != null && board.getLetter(c) != null)
	  {
	    return false;
	  }
	  return true;
	}

	private boolean checkSouth(Board board, Coordinate coord, int length)
	{
	  Coordinate c = coord;
	  for (int counter = 1; counter <= length; counter++)
	  {
	    c = c.getSouth();
	    if (c == null)
	    {
	      // Not enough space
	      return false;
	    }
        if (board.getLetter(c) != null)
        {
          return false;
        }
	  }
	  
	  c = coord.getNorthEast();
	  if (c != null && board.getLetter(c) != null)
	  {
	    return false;
	  }

	  c = coord.getSouthEast();
      if (c != null && board.getLetter(c) != null)
	  {
	    return false;
	  }
      
      c = coord.getNorth();
      if (c != null && board.getLetter(c) != null)
      {
        return false;
      }
      
      return true;
	}

	private boolean checkEast(Board board, Coordinate coord, int length)
	{
	  Coordinate c = coord;
	  
	  for (int counter = 1; counter <= length; counter++)
	  {
	    c = coord.getEast();
        if (c == null)
        {
          // Not enough space
          return false;
        }
        if (board.getLetter(c) != null)
        {
          return false;
        }
	  }
	  
	  c = coord.getNorthEast();
      if (c != null && board.getLetter(c) != null)
      {
        return false;
      }
      
      c = coord.getSouthEast();
      if (c != null && board.getLetter(c) != null)
      {
        return false;
      }
      
      c = coord.getWest();
      if (c != null && board.getLetter(c) != null)
      {
        return false;
      }
      return true;
	}

	private boolean checkWest(Board board, Coordinate coord, int length)
	{
	  Coordinate c = coord;
	  for (int counter = 1; counter <= length; counter++)
	  {
	    c = c.getWest();
        if (c == null)
        {
          // Not enough space
          return false;
        }
        if (board.getLetter(c) != null)
        {
          return false;
        }
	  }
	  
	  c = coord.getNorthWest();
      if (c != null && board.getLetter(c) != null)
      {
        return false;
      }
      
      c = coord.getSouthWest();
      if (c != null && board.getLetter(c) != null)
      {
        return false;
      }
      
      c = coord.getEast();
      if (c != null && board.getLetter(c) != null)
      {
        return false;
      }
      return true;
	}

}
