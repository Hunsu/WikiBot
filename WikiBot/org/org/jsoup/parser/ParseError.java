package org.jsoup.parser;

/**
 * A Parse Error records an error in the input HTML that occurs in either the tokenisation or the tree building phase.
 */
public class ParseError {
    
    /** The pos. */
    private int pos;
    
    /** The error msg. */
    private String errorMsg;

    /**
     * Instantiates a new parses the error.
     *
     * @param pos the pos
     * @param errorMsg the error msg
     */
    ParseError(int pos, String errorMsg) {
        this.pos = pos;
        this.errorMsg = errorMsg;
    }

    /**
     * Instantiates a new parses the error.
     *
     * @param pos the pos
     * @param errorFormat the error format
     * @param args the args
     */
    ParseError(int pos, String errorFormat, Object... args) {
        this.errorMsg = String.format(errorFormat, args);
        this.pos = pos;
    }

    /**
     * Retrieve the error message.
     * @return the error message.
     */
    public String getErrorMessage() {
        return errorMsg;
    }

    /**
     * Retrieves the offset of the error.
     * @return error offset within input
     */
    public int getPosition() {
        return pos;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return pos + ": " + errorMsg;
    }
}
