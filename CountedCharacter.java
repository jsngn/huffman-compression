/**
 * Class declaration for objects that hold a character and its frequency
 *
 * @author Mien Nguyen, Dartmouth CS 10, CountedCharacter class as submission for PS-3
 */

public class CountedCharacter {
    // Just the character and its frequency
    private Character thisChar;
    private Integer thisFreq;

    /**
     * Constructor assigns values to instance variables holding character and frequency
     * @param c the character we want to store
     * @param f its frequency in file
     */
    public CountedCharacter(Character c, Integer f) {
        thisChar = c;
        thisFreq = f;
    }

    /**
     * Returns the character stored to caller
     */
    public Character getThisChar() {
        return thisChar;
    }

    /**
     * Returns the frequency stored to caller
     */
    public Integer getThisFreq() {
        return thisFreq;
    }

    /**
     * For easy viewing of output in debugging
     */
    @Override
    public String toString() {
        return thisChar + ": " + thisFreq;
    }
}
