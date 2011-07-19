/**
 * $Id:$
 */
package org.ops5.contest.game;


import com.sun.xml.internal.bind.v2.runtime.Coordinator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
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
import sun.security.util.Password;


public class TurboTiler implements Player
{
  private enum Orientation
  {
    Vertical,
    Horizontal
  }

  private static final Character MATCH_CHAR = '.';

  private Random _random;
  private Pattern _allVowels;
  private Pattern _allConsonants;
  private Dictionary _dictionary;

  private class ScoredMove
  {
    public Move Move;
    public Double Score;
    public ScoredMove(Move move, double score)
    {
      Move = move;
      Score = score;
    }
  }

  public void init()
  {
    _random = new Random(System.currentTimeMillis());
    _allVowels = Pattern.compile("[AEIOU]+");
    _allConsonants = Pattern.compile("[^AEIOU]+");
    _dictionary = Dictionary.getInstance();
  }

  public Move move(Board board, List<Letter> letters, int myScore, int opponentScore)
  {
    System.out.println("Letters: " + letters.toString());

    List<ScoredMove> moves = new ArrayList<ScoredMove>();

    List<WordPlay> played = board.getPlayedWords();

    moves.addAll(
        played.isEmpty()
            ? findAllMoves(board, letters, board.getStart(), Orientation.Horizontal)
            : findAllMoves(board, letters));
    moves.addAll(addDefaultMoves(letters));

    Move result = selectWinningMove(moves);

    System.out.println("letters: " + letters.toString());
    System.out.println("winning Move: " + result);
    if (result instanceof WordPlay)
    {
      System.out.println("board start: " + board.getStart());
      WordPlay wordPlay = (WordPlay)result;
      for (int i = 0; i < wordPlay.getLetters().size(); i++)
      {
        System.out.println(wordPlay.getCoordinate(i));
      }
    }

    return result;
  }

  private Move selectWinningMove(List<ScoredMove> moves)
  {
    if (moves.size() == 0)
    {
      System.out.println("no moves to choose winner from!");
      return Pass.INSTANCE;
    }

    System.out.println("have # moves: " + moves.size());

    Collections.sort(moves, new Comparator<Object>()
    {
      public int compare(Object o, Object o1)
      {
        // descending
        return ((ScoredMove) o1).Score.compareTo(((ScoredMove) o).Score);
      }
    });

    for (ScoredMove move : moves)
    {
      System.out.println("final move: " + move.Move + " score: " + move.Score);
    }

    Move result = moves.get(0).Move;

    return result;
  }

  private List<ScoredMove> addDefaultMoves(List<Letter> letters)
  {
    Move result;

    List<ScoredMove> moves = new ArrayList<ScoredMove>();

    result = allVowels(letters);
    if (result != null)
    {
      moves.add(new ScoredMove(result, 1.0));
    }

    result = allConsonants(letters);
    if (result != null)
    {
      moves.add(new ScoredMove(result, 0.5));
    }

    // Randomly pick either 3 or 4 as the number of tiles to trade in.
    int tradeSize = 3 + _random.nextInt(2);
    List<Letter> discards = new ArrayList<Letter>();
    for (int counter = 0; counter < tradeSize; counter++)
    {
      Letter discard = letters.get(counter);
      discards.add(discard);
      moves.add(new ScoredMove(new Discard(discards), 0.25));
    }

    moves.add(new ScoredMove(Pass.INSTANCE, 0));

    return moves;
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
    if (letters.size() < 6)
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

  private List<ScoredMove> findAllMoves(Board board, List<Letter> letters)
  {
    List<ScoredMove> moves = new ArrayList<ScoredMove>();

    List<WordPlay> played = board.getPlayedWords();
    for (WordPlay word : played)
    {
      for (int counter = 0; counter < word.getLetterCount(); counter++)
      {
        List<ScoredMove> allMoves;

        Coordinate coord = word.getCoordinate(counter);

        allMoves = findAllMoves(board, letters, coord, Orientation.Vertical);
        moves.addAll(allMoves);

        allMoves = findAllMoves(board, letters, coord, Orientation.Horizontal);
        moves.addAll(allMoves);
      }
    }

    return moves;
  }

  private List<ScoredMove> findAllMoves(Board board, List<Letter> letters, Coordinate coord, Orientation orientation)
  {
    List<ScoredMove> moves = new ArrayList<ScoredMove>();

    // move a sliding selection window over the specified coordinate
    for (int windowSize = 1; windowSize <= letters.size(); windowSize++)
    {
      moves.addAll(findAllMovesInWindow(board, letters, coord, orientation, windowSize, -1));
      moves.addAll(findAllMovesInWindow(board, letters, coord, orientation, windowSize, 1));
    }

    return moves;
  }

  private List<ScoredMove> findAllMovesInWindow(Board board, List<Letter> letters, Coordinate coord, Orientation orientation, int windowSize, int side)
  {
    List<ScoredMove> moves = new ArrayList<ScoredMove>();

    Selection selection = createSelect(board, coord, orientation, windowSize, side);
    if (selection == null) return moves;

    // todo: compute neighborhood (strategy) score for query

    List<String> words = executeQuery(_dictionary, selection, letters);

    for (String word : words)
    {
      WordPlay wordPlay = createWordPlayFromQuery(board, selection, orientation, word);
      System.out.println("WordPlay: " + wordPlay.toString());
      if (board.getPlayedWords().size() == 0 || (board.getPlayedWords().size() > 0 && board.checkWordPlay(wordPlay)))
      {
        System.out.println("valid play: " + wordPlay);
        int score = board.computeScore(wordPlay);
        moves.add(new ScoredMove((Move)wordPlay, (double)score));
      }
    }

    return moves;
  }

  public class Selection
  {
    public List<Coordinate> Coords;
    public List<Character> Letters;
    public String Query;
    public Selection(List<Coordinate> coords, List<Character> letters, String query)
    {
      Coords = coords;
      Letters = letters;
      Query = query;
    }
  }

  private Selection createSelect(Board board, Coordinate coord, Orientation orientation, int windowSize, int side)
  {
    List<Coordinate> coords = new ArrayList<Coordinate>();
    List<Character> letters = new ArrayList<Character>();
    StringBuilder sb = new StringBuilder();

    System.out.println("orientation: " + orientation + " windowSize: " + windowSize + " side: " + side);

    if (orientation == Orientation.Vertical)
    {
      Coordinate curCoord = coord;

      while(curCoord != null)
      {
        if (curCoord.getNorth() == null)
        {
          break;
        }
        if (board.getLetter(curCoord) == null)
        {
          if (side > 0 || windowSize == 0)
          {
            curCoord = curCoord.getSouth();
            break;
          }
          windowSize--;
        }

        curCoord = curCoord.getNorth();
      }

      while(curCoord != null && curCoord.getYCoord() < coord.getYCoord())
      {
        coords.add(curCoord);
        Letter letter = board.getLetter(curCoord);
        Character character = letter == null ? MATCH_CHAR : letter.getLetter();
        sb.append(character);
        letters.add(character);
        curCoord = curCoord.getSouth();
      }

      while(curCoord != null)
      {
        if (board.getLetter(curCoord) == null)
        {
          if (side < 0 || windowSize == 0)
          {
            break;
          }
          windowSize--;
        }

        coords.add(curCoord);
        Letter letter = board.getLetter(curCoord);
        Character character = letter == null ? MATCH_CHAR : letter.getLetter();
        sb.append(character);
        letters.add(character);
        curCoord = curCoord.getSouth();
      }
    }
    else
    {
      Coordinate curCoord = coord;

      while(curCoord != null)
      {
        if (curCoord.getWest() == null)
        {
          break;
        }
        if (board.getLetter(curCoord) == null)
        {
          if (side > 0 || windowSize == 0)
          {
            curCoord = curCoord.getEast();
            break;
          }
          windowSize--;
        }

        curCoord = curCoord.getWest();
      }

      while(curCoord != null && curCoord.getXCoord() < coord.getXCoord())
      {
        coords.add(curCoord);
        Letter letter = board.getLetter(curCoord);
        Character character = letter == null ? MATCH_CHAR : letter.getLetter();
        sb.append(character);
        letters.add(character);
        curCoord = curCoord.getEast();
      }

      while(curCoord != null)
      {
        if (board.getLetter(curCoord) == null)
        {
          if (side < 0 || windowSize == 0)
          {
            break;
          }
          windowSize--;
        }

        coords.add(curCoord);
        Letter letter = board.getLetter(curCoord);
        Character character = letter == null ? MATCH_CHAR : letter.getLetter();
        sb.append(character);
        letters.add(character);
        curCoord = curCoord.getEast();
      }
    }

    return new Selection(coords, letters, sb.toString());
  }

  private List<String> executeQuery(Dictionary dictionary, Selection selection, List<Letter> letters)
  {
    List<String> words = new ArrayList<String>();

    Set<String> dict = dictionary.getWords();

    // constrain word to only held letters and board letters
    List<Character> charSet = new ArrayList<Character>();
    for (int i = 0; i < selection.Letters.size(); i++)
    {
      if (selection.Letters.get(i) != MATCH_CHAR)
      {
        charSet.add(selection.Letters.get(i));
      }
    }
    for (Letter letter : letters)
    {
      charSet.add(letter.getLetter());
    }
    Collections.sort(charSet);

    Pattern query = Pattern.compile(String.format("^%s$", selection.Query));
    int queryLength = charSet.size();

    System.out.println("query: " + query);

    for (String candidate : dict)
    {
      if (candidate.length() != queryLength) continue;

      Matcher matcher = query.matcher(candidate);
      if (matcher.matches())
      {
        System.out.println("match 1: "+candidate);
        List<Character> foo = new ArrayList<Character>();
        for (int i = 0; i < candidate.length(); i++)
        {
          foo.add(candidate.charAt(i));
        }
        Collections.sort(foo);
        if (charSet.equals(foo))
        {
          System.out.println("match 2: "+candidate);
          words.add(candidate);
        }
      }
    }

    return words;
  }

  private WordPlay createWordPlayFromQuery(Board board, Selection selection, Orientation orientation, String word)
  {
    WordPlay wordPlay = new WordPlay();

    for (int i = 0; i < selection.Letters.size(); i++)
    {
      if (selection.Letters.get(i) == MATCH_CHAR)
      {
        System.out.println("coord: " + selection.Coords.get(i));
        wordPlay.setLetter(Letter.getLetter(word.charAt(i)), selection.Coords.get(i));
      }
    }

    System.out.println("word: " + word + " query: " + selection.Query + " wordplay: " + wordPlay);

    return wordPlay;
  }
}
