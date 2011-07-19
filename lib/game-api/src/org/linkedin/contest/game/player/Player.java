/**
 * $Id:$
 */
package org.linkedin.contest.game.player;

import java.util.List;

import org.linkedin.contest.game.api.Letter;
import org.linkedin.contest.game.api.Move;
import org.linkedin.contest.game.api.Board;


/**
 * TBD: Add documentation for this interface.
 */
public interface Player
{
	/**
   * An init method the game program will call prior to the match beginning.
   * Can be used to init any needed state.
	 */
  public void init();

	/**
   * The API we will use to call you AI when a move needs to be made.
   */
  public Move move(Board playerBoard, List<Letter> letters, int myScore, int opponentScore);
}
