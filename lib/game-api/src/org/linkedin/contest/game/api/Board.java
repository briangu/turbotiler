package org.linkedin.contest.game.api;

import java.util.List;

/**
 * Interface to the board. 
 * 
 * Allows querying of all the words played so far, the letter on a given square on the board
 * and whether a given word can be played legally or not
 */
public interface Board
{
  public enum SquareType
  {
      START,
      PLAIN,
      DOUBLE_LETTER,
      TRIPLE_LETTER,
      DOUBLE_WORD,
      TRIPLE_WORD;
  }

  /**
   * Returns the list of words played on the board so far
   * 
   * Note that each {@link WordPlay} instance only contains the series of
   * letters that are played and the location of each of the letters on the board. 
   * It does not represent an entire word played.
   * The list of plays will be in the ordered that they were played (It does not include skips).
   * 
   * @return list of words played on the board
   */
  List<WordPlay> getPlayedWords();

  /**
   * Returns the coordinate of the start position.
   *
   * @return The coordinate of the start position.
   */
  public Coordinate getStart();

  /**
   * Returns the list of coordinates which have a double letter score.
   *
   * @return The list of coordinates which have a double letter score.
   */
  public List<Coordinate> getDoubleLetterList();

  /**
   * Returns the list of coordinates which have a triple letter score.
   *
   * @return The list of coordinates which have a triple letter score.
   */
  public List<Coordinate> getTripleLetterList();

  /**
   * Returns the list of coordinates which have a double word score.
   *
   * @return The list of coordinates which have a double word score.
   */
  public List<Coordinate> getDoubleWordList();

  /**
   * Returns the list of coordinates which have a triple word score.
   *
   * @return The list of coordinates which have a triple word score.
   */
  public List<Coordinate> getTripleWordList();

  /**
   * Returns the letter at a given location on the board.
   * 
   * @param coord The co-ordinate of a square on the board
   * 
   * @return The letter at the location specified by <code>coord</code> or null if no letter is present there
   */
  public Letter getLetter(Coordinate coord);
  
  /**
   * Returns the type of square at a given location on the board.
   *
   * @param coord The co-ordinate of a square on the board
   * 
   * @return The type of square at the location specified by <code>coord</code>
   */
  public SquareType getSquareType(Coordinate coord);

    /**
     * Returns the score you will be awarded if you were to play the relevant move.
     * Will return a negative # if it detects a problem.
     * Please note checkWordPlay(attempt) should be used to comprehensively detect errors.
     *
     * @param word The move under consideration.
     * @return The relevant integer score for such a move on this board or -1 if a problem is detected.
     */
    public int computeScore (WordPlay word);

  /**
   * Checks if a given play of letters is a valid one
   * 
   * @param attempt the sequence of letters to be placed on the board
   * 
   * @return true if the play is valid, false otherwise
   */
  public boolean checkWordPlay(WordPlay attempt);
  
  /**
   * Returns a copy of the board in its current state
   * 
   * @return a copy of the board in its current state
   */
  public Board createCopy();


    /**
     * Makes the word play on this board.
     * It will return the score for that move if successful.
     * It will throw a runtime exception if the move is invalid.
     *
     * @param word The word play to make on this board.
     * @return the score for this move.
     */
  public int playWord (WordPlay word);
}
