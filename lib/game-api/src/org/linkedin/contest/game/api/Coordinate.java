package org.linkedin.contest.game.api;

/**
 * Represents a co-ordinate on the board.
 * 
 * The X-coordinate runs horizontally between 0 and 14 (inclusive).
 * The Y-coordinate runs vertically between 0 and 14 (inclusive).
 *
 */
public final class Coordinate
{
  public static final int BOARD_SIZE = 15;
  
  private static final Coordinate[][] COORDINATES;
  static
  {
    COORDINATES = new Coordinate[BOARD_SIZE][BOARD_SIZE];
    for(int i=0;i<BOARD_SIZE;i++)
    {
      for(int j=0;j<BOARD_SIZE;j++)
      {
        COORDINATES[i][j] = new Coordinate(i, j);
      }
    }
  }
  
  public static final Coordinate CENTER = COORDINATES[7][7];
 
  private final int xCoord;
  private final int yCoord;
  
  private Coordinate(int xCoord, int yCoord)
  {
    this.xCoord = xCoord;
    this.yCoord = yCoord;
  }
  
  /**
   * Returns the coordinate representing <code>(xCoord, yCoord)</code> on the board
   * 
   * @param xCoord
   * @param yCoord
   * 
   * @return the coordinate representing <code>(xCoord, yCoord)</code> on the board
   * 
   * @throws ArrayIndexOutOfBoundsException if xCoord or yCoord falls out of the board
   */
  public static Coordinate get(int xCoord, int yCoord)
  {
    return COORDINATES[xCoord][yCoord];
  }
  
  /**
   * Returns the coordinate immediately to the north of this coordinate or null the current 
   * coordinate is on the top edge of the board
   * 
   * @return the coordinate immediately to the north of this coordinate or null the current 
   * coordinate is on the top edge of the board
   */
  public Coordinate getNorth()
  {
    return getNorth(1);
  }
  
  /**
   * Returns the coordinate <code>offset</code> squares to the north of this coordinate or null if
   * no such square exists on the board
   * 
   * @param offset the number of scores to the north to return
   * 
   * @return the coordinate <code>offset</code> squares to the north of this coordinate or null if
   * no such square exists on the board
   */
  public Coordinate getNorth(int offset)
  {
    if ((yCoord - offset) < 0)
    {
      // Nothing north of here
      return null;
    }
    return applyOffset(0, -offset);
  }

  /**
   * Returns the coordinate immediately to the north-east of this coordinate or null the current 
   * coordinate is on the top or right edge of the board
   * 
   * @return the coordinate immediately to the north-east of this coordinate or null the current 
   * coordinate is on the top or right edge of the board
   */
  public Coordinate getNorthEast()
  {
    Coordinate north = getNorth();
    return north == null ? null : north.getEast();
  }

  /**
   * Returns the coordinate immediately to the east of this coordinate or null the current 
   * coordinate is on the right edge of the board
   * 
   * @return the coordinate immediately to the east of this coordinate or null the current 
   * coordinate is on the right edge of the board
   */
  public Coordinate getEast()
  {
    return getEast(1);
  }
  
  /**
   * Returns the coordinate <code>offset</code> squares to the east of this coordinate or null if
   * no such square exists on the board
   * 
   * @param offset the number of scores to the north to return
   * 
   * @return the coordinate <code>offset</code> squares to the east of this coordinate or null if
   * no such square exists on the board
   */
  public Coordinate getEast(int offset)
  {
    if ((xCoord + offset) >= BOARD_SIZE)
    {
      // Nothing east of here
      return null;
    }
    return applyOffset(offset, 0);
  }

  /**
   * Returns the coordinate immediately to the south-east of this coordinate or null the current 
   * coordinate is on the right or bottom edge of the board
   * 
   * @return the coordinate immediately to the south-east of this coordinate or null the current 
   * coordinate is on the right or bottom edge of the board
   */
  public Coordinate getSouthEast()
  {
    Coordinate south = getSouth();
    return south == null ? null : south.getEast();
  }

  /**
   * Returns the coordinate immediately to the south of this coordinate or null the current 
   * coordinate is on the bottom edge of the board
   * 
   * @return the coordinate immediately to the south of this coordinate or null the current 
   * coordinate is on the bottom edge of the board
   */
  public Coordinate getSouth()
  {
    return getSouth(1);
  }

  /**
   * Returns the coordinate <code>offset</code> squares to the south of this coordinate or null if
   * no such square exists on the board
   * 
   * @param offset the number of scores to the north to return
   * 
   * @return the coordinate <code>offset</code> squares to the south of this coordinate or null if
   * no such square exists on the board
   */
  public Coordinate getSouth(int offset)
  {
    if ((yCoord + offset) >= BOARD_SIZE)
    {
      // Nothing south of here
      return null;
    }
    return applyOffset(0, offset);
  }

  /**
   * Returns the coordinate immediately to the south-west of this coordinate or null the current 
   * coordinate is on the left or bottom edge of the board
   * 
   * @return the coordinate immediately to the south-west of this coordinate or null the current 
   * coordinate is on the left or bottom edge of the board
   */
  public Coordinate getSouthWest()
  {
    Coordinate south = getSouth();
    return south == null ? null : south.getWest();
  }

  /**
   * Returns the coordinate immediately to the west of this coordinate or null the current 
   * coordinate is on the left edge of the board
   * 
   * @return the coordinate immediately to the west of this coordinate or null the current 
   * coordinate is on the left edge of the board
   */
  public Coordinate getWest()
  {
    return getWest(1);
  }

  /**
   * Returns the coordinate <code>offset</code> squares to the west of this coordinate or null if
   * no such square exists on the board
   * 
   * @param offset the number of scores to the north to return
   * 
   * @return the coordinate <code>offset</code> squares to the west of this coordinate or null if
   * no such square exists on the board
   */
  public Coordinate getWest(int offset)
  {
    if ((xCoord - offset) < 0)
    {
      // Nothing west of here
      return null;
    }
    return applyOffset(-offset, 0);
  }

  /**
   * Returns the coordinate immediately to the north-west of this coordinate or null the current 
   * coordinate is on the left or top edge of the board
   * 
   * @return the coordinate immediately to the north-west of this coordinate or null the current 
   * coordinate is on the left or top edge of the board
   */
  public Coordinate getNorthWest()
  {
    Coordinate north = getNorth();
    return north == null ? null : north.getWest();
  }

  /**
   * Returns the coordinate <code>xOffset</code> squares to the east (or west if <code>xOffset</code> is negative)
   * and <code>yOffset</code> squares to the south (or north if <code>yOffset</code> is negative)
   * of this coordinate
   * 
   * @param xOffset the number of squares to offset to the east (west if negative)
   * @param yOffset the number of squares to offset to the south (north if negative)
   * 
   * @return the coordinate <code>xOffset</code> squares to the east and <code>yOffset</code> squares 
   * to the south of this coordinate
   * 
   * @throws ArrayIndexOutOfBoundsException if xCoord or yCoord falls out of the board
   */
  public Coordinate applyOffset(int xOffset, int yOffset)
  {
    return Coordinate.get(xCoord + xOffset, yCoord + yOffset);
  }
  
  public int getXCoord()
  {
    return xCoord;
  }
  
  public int getYCoord()
  {
    return yCoord;
  }

  @Override
  public String toString()
  {
    return "(" + xCoord + ", " + yCoord + ")";
  }
}
