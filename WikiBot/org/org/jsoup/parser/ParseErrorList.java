package org.jsoup.parser;

import java.util.ArrayList;

/**
 * A container for ParseErrors.
 * 
 * @author Jonathan Hedley
 */
class ParseErrorList extends ArrayList<ParseError>{
    
    /** The Constant INITIAL_CAPACITY. */
    private static final int INITIAL_CAPACITY = 16;
    
    /** The max size. */
    private final int maxSize;
    
    /**
     * Instantiates a new parses the error list.
     *
     * @param initialCapacity the initial capacity
     * @param maxSize the max size
     */
    ParseErrorList(int initialCapacity, int maxSize) {
        super(initialCapacity);
        this.maxSize = maxSize;
    }
    
    /**
     * Can add error.
     *
     * @return true, if successful
     */
    boolean canAddError() {
        return size() < maxSize;
    }

    /**
     * Gets the max size.
     *
     * @return the max size
     */
    int getMaxSize() {
        return maxSize;
    }

    /**
     * No tracking.
     *
     * @return the parses the error list
     */
    static ParseErrorList noTracking() {
        return new ParseErrorList(0, 0);
    }
    
    /**
     * Tracking.
     *
     * @param maxSize the max size
     * @return the parses the error list
     */
    static ParseErrorList tracking(int maxSize) {
        return new ParseErrorList(INITIAL_CAPACITY, maxSize);
    }
}
