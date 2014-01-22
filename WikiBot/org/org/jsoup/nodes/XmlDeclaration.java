package org.jsoup.nodes;

/**
 An XML Declaration.

 @author Jonathan Hedley, jonathan@hedley.net */
public class XmlDeclaration extends Node {
    
    /** The Constant DECL_KEY. */
    private static final String DECL_KEY = "declaration";
    
    /** The is processing instruction. */
    private final boolean isProcessingInstruction; // <! if true, <? if false, declaration (and last data char should be ?)

    /**
     * Create a new XML declaration.
     *
     * @param data data
     * @param baseUri base uri
     * @param isProcessingInstruction is processing instruction
     */
    public XmlDeclaration(String data, String baseUri, boolean isProcessingInstruction) {
        super(baseUri);
        attributes.put(DECL_KEY, data);
        this.isProcessingInstruction = isProcessingInstruction;
    }

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#nodeName()
     */
    public String nodeName() {
        return "#declaration";
    }

    /**
     Get the unencoded XML declaration.
     @return XML declaration
     */
    public String getWholeDeclaration() {
        return attributes.get(DECL_KEY);
    }

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#outerHtmlHead(java.lang.StringBuilder, int, org.jsoup.nodes.Document.OutputSettings)
     */
    void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
        accum
                .append("<")
                .append(isProcessingInstruction ? "!" : "?")
                .append(getWholeDeclaration())
                .append(">");
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
