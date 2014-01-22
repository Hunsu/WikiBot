package org.jsoup.parser;

import org.jsoup.helper.Validate;

import java.util.Locale;

/**
 CharacterReader consumes tokens off a string. To replace the old TokenQueue.
 */
class CharacterReader {
    
    /** The Constant EOF. */
    static final char EOF = (char) -1;

    /** The input. */
    private final char[] input;
    
    /** The length. */
    private final int length;
    
    /** The pos. */
    private int pos = 0;
    
    /** The mark. */
    private int mark = 0;

    /**
     * Instantiates a new character reader.
     *
     * @param input the input
     */
    CharacterReader(String input) {
        Validate.notNull(input);
        this.input = input.toCharArray();
        this.length = this.input.length;
    }

    /**
     * Pos.
     *
     * @return the int
     */
    int pos() {
        return pos;
    }

    /**
     * Checks if is empty.
     *
     * @return true, if is empty
     */
    boolean isEmpty() {
        return pos >= length;
    }

    /**
     * Current.
     *
     * @return the char
     */
    char current() {
        return isEmpty() ? EOF : input[pos];
    }

    /**
     * Consume.
     *
     * @return the char
     */
    char consume() {
        char val = isEmpty() ? EOF : input[pos];
        pos++;
        return val;
    }

    /**
     * Unconsume.
     */
    void unconsume() {
        pos--;
    }

    /**
     * Advance.
     */
    void advance() {
        pos++;
    }

    /**
     * Mark.
     */
    void mark() {
        mark = pos;
    }

    /**
     * Rewind to mark.
     */
    void rewindToMark() {
        pos = mark;
    }

    /**
     * Consume as string.
     *
     * @return the string
     */
    String consumeAsString() {
        return new String(input, pos++, 1);
    }

    /**
     * Returns the number of characters between the current position and the next instance of the input char.
     *
     * @param c scan target
     * @return offset between current position and next instance of target. -1 if not found.
     */
    int nextIndexOf(char c) {
        // doesn't handle scanning for surrogates
        for (int i = pos; i < length; i++) {
            if (c == input[i])
                return i - pos;
        }
        return -1;
    }

    /**
     * Returns the number of characters between the current position and the next instance of the input sequence.
     *
     * @param seq scan target
     * @return offset between current position and next instance of target. -1 if not found.
     */
    int nextIndexOf(CharSequence seq) {
        // doesn't handle scanning for surrogates
        char startChar = seq.charAt(0);
        for (int offset = pos; offset < length; offset++) {
            // scan to first instance of startchar:
            if (startChar != input[offset])
                while(++offset < length && startChar != input[offset]);
            int i = offset + 1;
            int last = i + seq.length()-1;
            if (offset < length && last <= length) {
                for (int j = 1; i < last && seq.charAt(j) == input[i]; i++, j++);
                if (i == last) // found full sequence
                    return offset - pos;
            }
        }
        return -1;
    }

    /**
     * Consume to.
     *
     * @param c the c
     * @return the string
     */
    String consumeTo(char c) {
        int offset = nextIndexOf(c);
        if (offset != -1) {
            String consumed = new String(input, pos, offset);
            pos += offset;
            return consumed;
        } else {
            return consumeToEnd();
        }
    }

    /**
     * Consume to.
     *
     * @param seq the seq
     * @return the string
     */
    String consumeTo(String seq) {
        int offset = nextIndexOf(seq);
        if (offset != -1) {
            String consumed = new String(input, pos, offset);
            pos += offset;
            return consumed;
        } else {
            return consumeToEnd();
        }
    }

    /**
     * Consume to any.
     *
     * @param chars the chars
     * @return the string
     */
    String consumeToAny(final char... chars) {
        int start = pos;

        OUTER: while (pos < length) {
            for (int i = 0; i < chars.length; i++) {
                if (input[pos] == chars[i])
                    break OUTER;
            }
            pos++;
        }

        return pos > start ? new String(input, start, pos-start) : "";
    }

    /**
     * Consume to end.
     *
     * @return the string
     */
    String consumeToEnd() {
        String data = new String(input, pos, length-pos);
        pos = length;
        return data;
    }

    /**
     * Consume letter sequence.
     *
     * @return the string
     */
    String consumeLetterSequence() {
        int start = pos;
        while (pos < length) {
            char c = input[pos];
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))
                pos++;
            else
                break;
        }

        return new String(input, start, pos - start);
    }

    /**
     * Consume letter then digit sequence.
     *
     * @return the string
     */
    String consumeLetterThenDigitSequence() {
        int start = pos;
        while (pos < length) {
            char c = input[pos];
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))
                pos++;
            else
                break;
        }
        while (!isEmpty()) {
            char c = input[pos];
            if (c >= '0' && c <= '9')
                pos++;
            else
                break;
        }

        return new String(input, start, pos - start);
    }

    /**
     * Consume hex sequence.
     *
     * @return the string
     */
    String consumeHexSequence() {
        int start = pos;
        while (pos < length) {
            char c = input[pos];
            if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f'))
                pos++;
            else
                break;
        }
        return new String(input, start, pos - start);
    }

    /**
     * Consume digit sequence.
     *
     * @return the string
     */
    String consumeDigitSequence() {
        int start = pos;
        while (pos < length) {
            char c = input[pos];
            if (c >= '0' && c <= '9')
                pos++;
            else
                break;
        }
        return new String(input, start, pos - start);
    }

    /**
     * Matches.
     *
     * @param c the c
     * @return true, if successful
     */
    boolean matches(char c) {
        return !isEmpty() && input[pos] == c;

    }

    /**
     * Matches.
     *
     * @param seq the seq
     * @return true, if successful
     */
    boolean matches(String seq) {
        int scanLength = seq.length();
        if (scanLength > length - pos)
            return false;

        for (int offset = 0; offset < scanLength; offset++)
            if (seq.charAt(offset) != input[pos+offset])
                return false;
        return true;
    }

    /**
     * Matches ignore case.
     *
     * @param seq the seq
     * @return true, if successful
     */
    boolean matchesIgnoreCase(String seq) {
        int scanLength = seq.length();
        if (scanLength > length - pos)
            return false;

        for (int offset = 0; offset < scanLength; offset++) {
            char upScan = Character.toUpperCase(seq.charAt(offset));
            char upTarget = Character.toUpperCase(input[pos + offset]);
            if (upScan != upTarget)
                return false;
        }
        return true;
    }

    /**
     * Matches any.
     *
     * @param seq the seq
     * @return true, if successful
     */
    boolean matchesAny(char... seq) {
        if (isEmpty())
            return false;

        char c = input[pos];
        for (char seek : seq) {
            if (seek == c)
                return true;
        }
        return false;
    }

    /**
     * Matches letter.
     *
     * @return true, if successful
     */
    boolean matchesLetter() {
        if (isEmpty())
            return false;
        char c = input[pos];
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }

    /**
     * Matches digit.
     *
     * @return true, if successful
     */
    boolean matchesDigit() {
        if (isEmpty())
            return false;
        char c = input[pos];
        return (c >= '0' && c <= '9');
    }

    /**
     * Match consume.
     *
     * @param seq the seq
     * @return true, if successful
     */
    boolean matchConsume(String seq) {
        if (matches(seq)) {
            pos += seq.length();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Match consume ignore case.
     *
     * @param seq the seq
     * @return true, if successful
     */
    boolean matchConsumeIgnoreCase(String seq) {
        if (matchesIgnoreCase(seq)) {
            pos += seq.length();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Contains ignore case.
     *
     * @param seq the seq
     * @return true, if successful
     */
    boolean containsIgnoreCase(String seq) {
        // used to check presence of </title>, </style>. only finds consistent case.
        String loScan = seq.toLowerCase(Locale.ENGLISH);
        String hiScan = seq.toUpperCase(Locale.ENGLISH);
        return (nextIndexOf(loScan) > -1) || (nextIndexOf(hiScan) > -1);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new String(input, pos, length - pos);
    }
}
