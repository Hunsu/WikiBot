package org.jsoup.nodes;

/**
 A comment node.

 @author Jonathan Hedley, jonathan@hedley.net */
public class Comment extends Node {
    
    /** The Constant COMMENT_KEY. */
    private static final String COMMENT_KEY = "comment";

    /**
     Create a new comment node.
     @param data The contents of the comment
     @param baseUri base URI
     */
    public Comment(String data, String baseUri) {
        super(baseUri);
        attributes.put(COMMENT_KEY, data);
    }

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#nodeName()
     */
    public String nodeName() {
        return "#comment";
    }

    /**
     Get the contents of the comment.
     @return comment content
     */
    public String getData() {
        return attributes.get(COMMENT_KEY);
    }

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#outerHtmlHead(java.lang.StringBuilder, int, org.jsoup.nodes.Document.OutputSettings)
     */
    void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
        if (out.prettyPrint())
            indent(accum, depth, out);
        accum
                .append("<!--")
                .append(getData())
                .append("-->");
    }

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#outerHtmlTail(java.lang.StringBuilder, int, org.jsoup.nodes.Document.OutputSettings)
     */
    void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {}

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#toString()
     */
    public String toString() {
        return outerHtml();
    }
}
