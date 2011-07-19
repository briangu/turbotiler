/**
 * $Id:$
 */
package org.linkedin.contest.game.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
/**
 * TBD: Add documentation for this class.
 */
public class Dictionary
{
    private static final String SOWPODS_FILE = "sowpods.txt";

    private static final Dictionary _instance = new Dictionary(SOWPODS_FILE);

    private final Set<String> words;

    private Dictionary(String sowpods)
    {
        Set<String> tempWords = new HashSet<String>();
        InputStream is = getClass().getClassLoader().getResourceAsStream(sowpods);
        BufferedReader input = null;
        try
        {
            input = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = input.readLine()) != null)
            {
                if (line.length() > 1)
                {
                    tempWords.add(line);
                }
            }
        }
        catch (Exception e)
        {
            System.err.println(e);
        }
        finally
        {
            try
            {
                input.close();
            }
            catch (Exception e)
            {
                System.err.println(e);
            }
        }
        words = Collections.unmodifiableSet(tempWords);
    }

    /**
     * Singleton for getting the copy of the Dictionary.
     *
     * @return the dictionary singleton object
     */
    public static Dictionary getInstance()
    {
        return _instance;
    }

    /**
     * Get the total number of legal words in the dictionary;
     *
     * @return the total number of legal words
     */
    public int getWordCount()
    {
        return words.size();
    }

    /**
     * Helper function to determine if a provided word is "legal".
     *
     * @return true if the word is considered legal, false otherwise.
     */
	  public boolean checkWord(String word)
	  {
		    return words.contains(word);
	  }

    /**
     * Helper function to return an iterator which will allow you access to all legal words.
     *
     * @return iterator which will provide all legal words.
     */
    public Set<String> getWords()
    {
        return words;
    }
}
