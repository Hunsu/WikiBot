package org.jsoup.nodes;

import org.jsoup.helper.StringUtil;
import org.jsoup.helper.Validate;

/**
 * A {@code <!DOCTYPE>} node.
 */
public class DocumentType extends Node {
    // todo: quirk mode from publicId and systemId

    /**
     * Create a new doctype element.
     * @param name the doctype's name
     * @param publicId the doctype's public ID
     * @param systemId the doctype's system ID
     * @param baseUri the doctype's base URI
     */
    public DocumentType(String name, String publicId, String systemId, String baseUri) {
        super(baseUri);

        Validate.notEmpty(name);
        attr("name", name);
        attr("publicId", publicId);
        attr("systemId", systemId);
    }

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#nodeName()
     */
    @Override
    public String nodeName() {
        return "#doctype";
    }

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#outerHtmlHead(java.lang.StringBuilder, int, org.jsoup.nodes.Document.OutputSettings)
     */
    @Override
    void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out) {
        accum.append("<!DOCTYPE ").append(attr("name"));
        if (!StringUtil.isBlank(attr("publicId")))
            accum.append(" PUBLIC \"").append(attr("publicId")).append("\"");
        if (!StringUtil.isBlank(attr("systemId")))
            accum.append(" \"").append(attr("systemId")).append("\"");
        accum.append('>');
    }

    /* (non-Javadoc)
     * @see org.jsoup.nodes.Node#outerHtmlTail(java.lang.StringBuilder, int, org.jsoup.nodes.Document.OutputSettings)
     */
    @Override
    void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out) {
    }
}
