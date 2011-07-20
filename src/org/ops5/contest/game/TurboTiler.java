/**
 * $Id:$
 */
package org.ops5.contest.game;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

import static org.linkedin.contest.game.api.Board.*;
import static org.linkedin.contest.game.api.Board.SquareType.*;


public class TurboTiler implements Player
{
  private enum Orientation
  {
    Vertical,
    Horizontal
  }

  private static final Character MATCH_CHAR = '.';

  private Random _random;
  private Dictionary _dictionary;
  private Map<Integer, Set<String>> _wordBuckets = null;
  private int _discardCount = 0;

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
    _dictionary = Dictionary.getInstance();
    _wordBuckets = createWordBuckets(_dictionary);
  }

  private Map<Integer, Set<String>> createWordBuckets(Dictionary dictionary)
  {
    Map<Integer, Set<String>> wordBuckets = new HashMap<Integer, Set<String>>();

    for (String word : dictionary.getWords())
    {
      if (!wordBuckets.containsKey(word.length()))
      {
        wordBuckets.put(word.length(), new HashSet<String>());
      }
      wordBuckets.get(word.length()).add(word);
    }

    return wordBuckets;
  }

  public Move move(Board board, List<Letter> letters, int myScore, int opponentScore)
  {
//    System.out.println("Letters: " + letters.toString());

    List<ScoredMove> moves = new ArrayList<ScoredMove>();

    List<WordPlay> played = board.getPlayedWords();

    moves.addAll(
        played.isEmpty()
            ? findAllMoves(board, letters, board.getStart(), Orientation.Horizontal)
            : findAllMoves(board, letters));
    moves.addAll(addDefaultMoves(letters));

    ScoredMove result = selectWinningMove(board, moves, myScore, opponentScore);

    if (result.Score < 10)
    {
      if (_discardCount > 2)
      {
        System.out.println("forced discard");
        result = new ScoredMove(discardTiles(letters), 1.0);
        _discardCount = 0;
      }
      else
      {
        _discardCount++;
      }
    } else {
      _discardCount = 0;
    }

    System.out.println("letters: " + letters.toString());
    System.out.println("winning Move: " + result);
    if (result.Move instanceof WordPlay)
    {
      System.out.println("board start: " + board.getStart());
      WordPlay wordPlay = (WordPlay)result.Move;
      for (int i = 0; i < wordPlay.getLetters().size(); i++)
      {
        System.out.println(wordPlay.getCoordinate(i));
      }
    }

    return result.Move;
  }

  private ScoredMove selectWinningMove(Board board, List<ScoredMove> moves, int myScore, int opponentScore)
  {
    if (moves.size() == 0)
    {
      System.out.println("no moves to choose winner from!");
      return new ScoredMove(Pass.INSTANCE, 1.0);
    }

//    System.out.println("have # moves: " + moves.size());

    Collections.sort(moves, new Comparator<Object>()
    {
      public int compare(Object o, Object o1)
      {
        // descending
        return ((ScoredMove) o1).Score.compareTo(((ScoredMove) o).Score);
      }
    });

/*
    for (ScoredMove move : moves)
    {
      System.out.println("final move: " + move.Move + " score: " + move.Score);
    }
*/
    List<ScoredMove> subset;

    System.out.println(myScore);

    subset = moves.subList(0, Math.min(10, moves.size()));

    for (ScoredMove move : subset)
    {
      System.out.println("final move: " + move.Move + " score: " + move.Score);
    }

    for (ScoredMove move : subset)
    {
      if (!(move.Move instanceof WordPlay)) continue;

      System.out.println("final move: " + move.Move + " score: " + move.Score);

      double adjust = 0.0;

      WordPlay play = (WordPlay)move.Move;

      for (int i = 0; i < play.getLetterCount(); i++)
      {
        if (play.getLetter(i).equals(Letter.getLetter('A'))) adjust -= 5;
        if (play.getLetter(i).equals(Letter.getLetter('E'))) adjust -= 5;
        if (play.getLetter(i).equals(Letter.getLetter('I'))) adjust -= 5;
        if (play.getLetter(i).equals(Letter.getLetter('N'))) adjust -= 5;
        if (play.getLetter(i).equals(Letter.getLetter('R'))) adjust -= 5;
        if (play.getLetter(i).equals(Letter.getLetter('S'))) adjust -= 5;

        if (play.getLetter(i).equals(Letter.getLetter('J'))
              || play.getLetter(i).equals(Letter.getLetter('X'))
              || play.getLetter(i).equals(Letter.getLetter('K'))
              || play.getLetter(i).equals(Letter.getLetter('Q'))
              || play.getLetter(i).equals(Letter.getLetter('Z'))
          )
        {
          adjust += 10;
        }

        Coordinate coord = play.getCoordinate(i);

        adjust -= getSquareValue(board, coord.getEast());
        adjust -= getSquareValue(board, coord.getEast(1));
        adjust -= getSquareValue(board, coord.getNorth());
        adjust -= getSquareValue(board, coord.getNorth(1));
        adjust -= getSquareValue(board, coord.getSouth());
        adjust -= getSquareValue(board, coord.getSouth(1));
        adjust -= getSquareValue(board, coord.getWest());
        adjust -= getSquareValue(board, coord.getWest(1));
      }

      adjust += (7 - play.getLetterCount()) * 10;

      System.out.println(move.Score);
      System.out.println(adjust);
      move.Score = (move.Score * 100) + (adjust);
      System.out.println(move.Score);
    }

    Collections.sort(subset, new Comparator<Object>()
    {
      public int compare(Object o, Object o1)
      {
        // descending
        return ((ScoredMove) o1).Score.compareTo(((ScoredMove) o).Score);
      }
    });

    for (ScoredMove move : subset)
    {
      System.out.println("final move: " + move.Move + " score: " + move.Score);
    }

    ScoredMove result = subset.get(0);

    return result;
  }

  private double getSquareValue(Board board, Coordinate coordinate)
  {
    if (coordinate == null) return 0.0;
    if (board.getLetter(coordinate) != null) return 0.0;
    return getSquareValue(board.getSquareType(coordinate));
  }

  private double getSquareValue(Board.SquareType type)
  {
    switch(type)
    {
      case START: return 0.0;
      case PLAIN: return 0.0;
      case DOUBLE_LETTER: return 2;
      case DOUBLE_WORD: return 4;
      case TRIPLE_LETTER: return 8;
      case TRIPLE_WORD: return 12;
    }
    return 1.0;
  }

  private List<ScoredMove> addDefaultMoves(List<Letter> letters)
  {
    Move result;

    List<ScoredMove> moves = new ArrayList<ScoredMove>();

    moves.add(new ScoredMove(discardTiles(letters), _random.nextDouble()));

    moves.add(new ScoredMove(Pass.INSTANCE, _random.nextDouble()));

    return moves;
  }

  private Move discardTiles(List<Letter> letters)
  {
    Set<Letter> goodChars = new HashSet<Letter>();

    goodChars.add(Letter.getLetter('A'));
    goodChars.add(Letter.getLetter('E'));
    goodChars.add(Letter.getLetter('I'));
    goodChars.add(Letter.getLetter('N'));
    goodChars.add(Letter.getLetter('R'));
    goodChars.add(Letter.getLetter('S'));

    List<Letter> discards = new ArrayList<Letter>();
    for (int i = 0; i < letters.size(); i++)
    {
      if (goodChars.contains(letters.get(i)))
      {
        continue;
      }
      discards.add(letters.get(i));
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

    List<String> words = executeQuery(_dictionary, selection, letters);

    for (String word : words)
    {
      WordPlay wordPlay = createWordPlayFromQuery(board, selection, orientation, word);
//      System.out.println("WordPlay: " + wordPlay.toString());
      if (board.getPlayedWords().size() == 0 ||
            (board.getPlayedWords().size() > 0 && board.checkWordPlay(wordPlay)))
      {
//        System.out.println("valid play: " + wordPlay);
        int playScore = board.computeScore(wordPlay);
        moves.add(new ScoredMove((Move)wordPlay, (double)playScore));
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

//    System.out.println("orientation: " + orientation + " windowSize: " + windowSize + " side: " + side);

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
          if (side > 0)
          {
            break;
          }
          if (windowSize == 0)
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
          if (side > 0)
          {
            break;
          }
          if (windowSize == 0)
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

    Pattern query = Pattern.compile(String.format("^%s$", selection.Query));
    int queryLength = selection.Query.length();
    Set<String> dict = _wordBuckets.get(queryLength);

    if (dict == null) return words;

//    System.out.println("query: " + query);

    for (String candidate : dict)
    {
      Matcher matcher = query.matcher(candidate);
      if (matcher.matches())
      {
//        System.out.println("match 1: "+candidate);

        List<Letter> letterLIst = new ArrayList<Letter>(letters);
        boolean valid = true;

        for (int i = 0; i < selection.Letters.size(); i++)
        {
          if (selection.Letters.get(i) == MATCH_CHAR)
          {
            Letter letter = Letter.getLetter(candidate.charAt(i));
            int idx = letterLIst.indexOf(letter);
            if (idx < 0)
            {
              valid = false;
              break;
            }
            letterLIst.remove(letter);
          }
        }

        if (valid)
        {
//          System.out.println("match 2: "+candidate);
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
//        System.out.println("coord: " + selection.Coords.get(i));
        wordPlay.setLetter(Letter.getLetter(word.charAt(i)), selection.Coords.get(i));
      }
    }

//    System.out.println("word: " + word + " query: " + selection.Query + " wordplay: " + wordPlay);

    return wordPlay;
  }
}
