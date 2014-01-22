package org.jsoup.nodes;

import org.jsoup.helper.StringUtil;
import org.jsoup.helper.Validate;

/**
 A text node.

 @author Jonathan Hedley, jonathan@hedley.net */
public class TextNode extends Node {
    /*
    TextNode is a node, and so by default comes with attributes and children. The attributes are seldom used, but use
    memory, and the child nodes are never used. So we don't have them, and override accessors to attributes to create
    them as needed on the fly.
     */
    /** The Constant TEXT_KEY. */
    private static final String TEXT_KEY = "text";
    
    /** The text. */
    String text;

    /**
     Create a new TextNode representing the supplied (unencoded) text).

     @param text raw text
     @param baseUri base uri
     @see #createFromEncoded(String, String)
     */
    public TextNode(String text, String baseUri) {
        this.baseUri = baseUri;
        this.text = text;
    }

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#nodeName()
     */
    public String nodeName() {
        return "#text";
    }
    
    /**
     * Get the text content of this text node.
     * @return Unencoded, normalised text.
     * @see TextNode#getWholeText()
     */
    public String text() {
        return normaliseWhitespace(getWholeText());
    }
    
    /**
     * Set the text content of this text node.
     * @param text unencoded text
     * @return this, for chaining
     */
    public TextNode text(String text) {
        this.text = text;
        if (attributes != null)
            attributes.put(TEXT_KEY, text);
        return this;
    }

    /**
     Get the (unencoded) text of this text node, including any newlines and spaces present in the original.
     @return text
     */
    public String getWholeText() {
        return attributes == null ? text : attributes.get(TEXT_KEY);
    }

    /**
     Test if this text node is blank -- that is, empty or only whitespace (including newlines).
     @return true if this document is empty or only whitespace, false if it contains any text content.
     */
    public boolean isBlank() {
        return StringUtil.isBlank(getWholeText());
    }

    /**
     * Split this text node into two nodes at the specified string offset. After splitting, this node will contain the
     * original text up to the offset, and will have a new text node sibling containing the text after the offset.
     * @param offset string offset point to split node at.
     * @return the newly created text node containing the text after the offset.
     */
    public TextNode splitText(int offset) {
        Validate.isTrue(offset >= 0, "Split offset must be not be negative");
        Validate.isTrue(offset < text.length(), "Split offset must not be greater than current text length");

        String head = getWholeText().substring(0, offset);
        String tail = getWholeText().substring(offset);
        text(head);
        TextNode tailNode = new TextNode(tail, this.baseUri());
        if (parent() != null)
            parent().addChildren(siblingIndex()+1, tailNode);

        return tailNode;
    }

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#outerHtmlHead(java.lang.StringBuilder, int, org.jsoup.nodes.Document.OutputSettings)
     */
    void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
        String html = Entities.escape(getWholeText(), out);
        if (out.prettyPrint() && parent() instanceof Element && !Element.preserveWhitespace((Element) parent())) {
            html = normaliseWhitespace(html);
        }

        if (out.prettyPrint() && ((siblingIndex() == 0 && parentNode instanceof Element && ((Element) parentNode).tag().formatAsBlock() && !isBlank()) || (out.outline() && siblingNodes().size()>0 && !isBlank()) ))
            indent(accum, depth, out);
        accum.append(html);
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

    /**
     * Create a new TextNode from HTML encoded (aka escaped) data.
     *
     * @param encodedText Text containing encoded HTML (e.g. &amp;lt;)
     * @param baseUri the base uri
     * @return TextNode containing unencoded data (e.g. &lt;)
     */
    public static TextNode createFromEncoded(String encodedText, String baseUri) {
        String text = Entities.unescape(encodedText);
        return new TextNode(text, baseUri);
    }

    /**
     * Normalise whitespace.
     *
     * @param text the text
     * @return the string
     */
    static String normaliseWhitespace(String text) {
        text = StringUtil.normaliseWhitespace(text);
        return text;
    }

    /**
     * Strip leading whitespace.
     *
     * @param text the text
     * @return the string
     */
    static String stripLeadingWhitespace(String text) {
        return text.replaceFirst("^\\s+", "");
    }

    /**
     * Last char is whitespace.
     *
     * @param sb the sb
     * @return true, if successful
     */
    static boolean lastCharIsWhitespace(StringBuilder sb) {
        return sb.length() != 0 && sb.charAt(sb.length() - 1) == ' ';
    }

    // attribute fiddling. create on first access.
    /**
     * Ensure attributes.
     */
    private void ensureAttributes() {
        if (attributes == null) {
            attributes = new Attributes();
            attributes.put(TEXT_KEY, text);
        }
    }

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#attr(java.lang.String)
     */
    @Override
    public String attr(String attributeKey) {
        ensureAttributes();
        return super.attr(attributeKey);
    }

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#attributes()
     */
    @Override
    public Attributes attributes() {
        ensureAttributes();
        return super.attributes();
    }

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#attr(java.lang.String, java.lang.String)
     */
    @Override
    public Node attr(String attributeKey, String attributeValue) {
        ensureAttributes();
        return super.attr(attributeKey, attributeValue);
    }

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#hasAttr(java.lang.String)
     */
    @Override
    public boolean hasAttr(String attributeKey) {
        ensureAttributes();
        return super.hasAttr(attributeKey);
    }

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#removeAttr(java.lang.String)
     */
    @Override
    public Node removeAttr(String attributeKey) {
        ensureAttributes();
        return super.removeAttr(attributeKey);
    }

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#absUrl(java.lang.String)
     */
    @Override
    public String absUrl(String attributeKey) {
        ensureAttributes();
        return super.absUrl(attributeKey);
    }
}
