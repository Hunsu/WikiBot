package org.jsoup.parser;

import org.jsoup.helper.DescendableLinkedList;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * The Class TreeBuilder.
 *
 * @author Jonathan Hedley
 */
abstract class TreeBuilder {
    
    /** The reader. */
    CharacterReader reader;
    
    /** The tokeniser. */
    Tokeniser tokeniser;
    
    /** The doc. */
    protected Document doc; // current doc we are building into
    
    /** The stack. */
    protected DescendableLinkedList<Element> stack; // the stack of open elements
    
    /** The base uri. */
    protected String baseUri; // current base uri, for creating new elements
    
    /** The current token. */
    protected Token currentToken; // currentToken is used only for error tracking.
    
    /** The errors. */
    protected ParseErrorList errors; // null when not tracking errors

    /**
     * Initialise parse.
     *
     * @param input the input
     * @param baseUri the base uri
     * @param errors the errors
     */
    protected void initialiseParse(String input, String baseUri, ParseErrorList errors) {
        Validate.notNull(input, "String input must not be null");
        Validate.notNull(baseUri, "BaseURI must not be null");

        doc = new Document(baseUri);
        reader = new CharacterReader(input);
        this.errors = errors;
        tokeniser = new Tokeniser(reader, errors);
        stack = new DescendableLinkedList<Element>();
        this.baseUri = baseUri;
    }

    /**
     * Parses the.
     *
     * @param input the input
     * @param baseUri the base uri
     * @return the document
     */
    Document parse(String input, String baseUri) {
        return parse(input, baseUri, ParseErrorList.noTracking());
    }

    /**
     * Parses the.
     *
     * @param input the input
     * @param baseUri the base uri
     * @param errors the errors
     * @return the document
     */
    Document parse(String input, String baseUri, ParseErrorList errors) {
        initialiseParse(input, baseUri, errors);
        runParser();
        return doc;
    }

    /**
     * Run parser.
     */
    protected void runParser() {
        while (true) {
            Token token = tokeniser.read();
            process(token);

            if (token.type == Token.TokenType.EOF)
                break;
        }
    }

    /**
     * Process.
     *
     * @param token the token
     * @return true, if successful
     */
    protected abstract boolean process(Token token);

    /**
     * Current element.
     *
     * @return the element
     */
    protected Element currentElement() {
        return stack.getLast();
    }
}
