package org.jsoup.nodes;

import org.jsoup.helper.StringUtil;
import org.jsoup.helper.Validate;
import org.jsoup.parser.Parser;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 The base, abstract Node model. Elements, Documents, Comments etc are all Node instances.

 @author Jonathan Hedley, jonathan@hedley.net */
public abstract class Node implements Cloneable {
    
    /** The parent node. */
    Node parentNode;
    
    /** The child nodes. */
    List<Node> childNodes;
    
    /** The attributes. */
    Attributes attributes;
    
    /** The base uri. */
    String baseUri;
    
    /** The sibling index. */
    int siblingIndex;

    /**
     Create a new Node.
     @param baseUri base URI
     @param attributes attributes (not null, but may be empty)
     */
    protected Node(String baseUri, Attributes attributes) {
        Validate.notNull(baseUri);
        Validate.notNull(attributes);
        
        childNodes = new ArrayList<Node>(4);
        this.baseUri = baseUri.trim();
        this.attributes = attributes;
    }

    /**
     * Instantiates a new node.
     *
     * @param baseUri the base uri
     */
    protected Node(String baseUri) {
        this(baseUri, new Attributes());
    }

    /**
     * Default constructor. Doesn't setup base uri, children, or attributes; use with caution.
     */
    protected Node() {
        childNodes = Collections.emptyList();
        attributes = null;
    }

    /**
     Get the node name of this node. Use for debugging purposes and not logic switching (for that, use instanceof).
     @return node name
     */
    public abstract String nodeName();

    /**
     * Get an attribute's value by its key.
     * <p/>
     * To get an absolute URL from an attribute that may be a relative URL, prefix the key with <code><b>abs</b></code>,
     * which is a shortcut to the {@link #absUrl} method.
     * E.g.: <blockquote><code>String url = a.attr("abs:href");</code></blockquote>
     * @param attributeKey The attribute key.
     * @return The attribute, or empty string if not present (to avoid nulls).
     * @see #attributes()
     * @see #hasAttr(String)
     * @see #absUrl(String)
     */
    public String attr(String attributeKey) {
        Validate.notNull(attributeKey);

        if (attributes.hasKey(attributeKey))
            return attributes.get(attributeKey);
        else if (attributeKey.toLowerCase().startsWith("abs:"))
            return absUrl(attributeKey.substring("abs:".length()));
        else return "";
    }

    /**
     * Get all of the element's attributes.
     * @return attributes (which implements iterable, in same order as presented in original HTML).
     */
    public Attributes attributes() {
        return attributes;
    }

    /**
     * Set an attribute (key=value). If the attribute already exists, it is replaced.
     * @param attributeKey The attribute key.
     * @param attributeValue The attribute value.
     * @return this (for chaining)
     */
    public Node attr(String attributeKey, String attributeValue) {
        attributes.put(attributeKey, attributeValue);
        return this;
    }

    /**
     * Test if this element has an attribute.
     * @param attributeKey The attribute key to check.
     * @return true if the attribute exists, false if not.
     */
    public boolean hasAttr(String attributeKey) {
        Validate.notNull(attributeKey);

        if (attributeKey.startsWith("abs:")) {
            String key = attributeKey.substring("abs:".length());
            if (attributes.hasKey(key) && !absUrl(key).equals(""))
                return true;
        }
        return attributes.hasKey(attributeKey);
    }

    /**
     * Remove an attribute from this element.
     * @param attributeKey The attribute to remove.
     * @return this (for chaining)
     */
    public Node removeAttr(String attributeKey) {
        Validate.notNull(attributeKey);
        attributes.remove(attributeKey);
        return this;
    }

    /**
     Get the base URI of this node.
     @return base URI
     */
    public String baseUri() {
        return baseUri;
    }

    /**
     Update the base URI of this node and all of its descendants.
     @param baseUri base URI to set
     */
    public void setBaseUri(final String baseUri) {
        Validate.notNull(baseUri);

        traverse(new NodeVisitor() {
            public void head(Node node, int depth) {
                node.baseUri = baseUri;
            }

            public void tail(Node node, int depth) {
            }
        });
    }

    /**
     * Get an absolute URL from a URL attribute that may be relative (i.e. an <code>&lt;a href></code> or
     * <code>&lt;img src></code>).
     * <p/>
     * E.g.: <code>String absUrl = linkEl.absUrl("href");</code>
     * <p/>
     * If the attribute value is already absolute (i.e. it starts with a protocol, like
     * <code>http://</code> or <code>https://</code> etc), and it successfully parses as a URL, the attribute is
     * returned directly. Otherwise, it is treated as a URL relative to the element's {@link #baseUri}, and made
     * absolute using that.
     * <p/>
     * As an alternate, you can use the {@link #attr} method with the <code>abs:</code> prefix, e.g.:
     * <code>String absUrl = linkEl.attr("abs:href");</code>
     *
     * @param attributeKey The attribute key
     * @return An absolute URL if one could be made, or an empty string (not null) if the attribute was missing or
     * could not be made successfully into a URL.
     * @see #attr
     * @see java.net.URL#URL(java.net.URL, String)
     */
    public String absUrl(String attributeKey) {
        Validate.notEmpty(attributeKey);

        String relUrl = attr(attributeKey);
        if (!hasAttr(attributeKey)) {
            return ""; // nothing to make absolute with
        } else {
            URL base;
            try {
                try {
                    base = new URL(baseUri);
                } catch (MalformedURLException e) {
                    // the base is unsuitable, but the attribute may be abs on its own, so try that
                    URL abs = new URL(relUrl);
                    return abs.toExternalForm();
                }
                // workaround: java resolves '//path/file + ?foo' to '//path/?foo', not '//path/file?foo' as desired
                if (relUrl.startsWith("?"))
                    relUrl = base.getPath() + relUrl;
                URL abs = new URL(base, relUrl);
                return abs.toExternalForm();
            } catch (MalformedURLException e) {
                return "";
            }
        }
    }

    /**
     Get a child node by its 0-based index.
     @param index index of child node
     @return the child node at this index. Throws a {@code IndexOutOfBoundsException} if the index is out of bounds.
     */
    public Node childNode(int index) {
        return childNodes.get(index);
    }

    /**
     Get this node's children. Presented as an unmodifiable list: new children can not be added, but the child nodes
     themselves can be manipulated.
     @return list of children. If no children, returns an empty list.
     */
    public List<Node> childNodes() {
        return Collections.unmodifiableList(childNodes);
    }

    /**
     * Returns a deep copy of this node's children. Changes made to these nodes will not be reflected in the original
     * nodes
     * @return a deep copy of this node's children
     */
    public List<Node> childNodesCopy() {
        List<Node> children = new ArrayList<Node>(childNodes.size());
        for (Node node : childNodes) {
            children.add(node.clone());
        }
        return children;
    }

    /**
     * Get the number of child nodes that this node holds.
     * @return the number of child nodes that this node holds.
     */
    public final int childNodeSize() {
        return childNodes.size();
    }
    
    /**
     * Child nodes as array.
     *
     * @return the node[]
     */
    protected Node[] childNodesAsArray() {
        return childNodes.toArray(new Node[childNodeSize()]);
    }

    /**
     Gets this node's parent node.
     @return parent node; or null if no parent.
     */
    public Node parent() {
        return parentNode;
    }

    /**
     Gets this node's parent node. Node overridable by extending classes, so useful if you really just need the Node type.
     @return parent node; or null if no parent.
     */
    public final Node parentNode() {
        return parentNode;
    }
    
    /**
     * Gets the Document associated with this Node. 
     * @return the Document associated with this Node, or null if there is no such Document.
     */
    public Document ownerDocument() {
        if (this instanceof Document)
            return (Document) this;
        else if (parentNode == null)
            return null;
        else
            return parentNode.ownerDocument();
    }
    
    /**
     * Remove (delete) this node from the DOM tree. If this node has children, they are also removed.
     */
    public void remove() {
        Validate.notNull(parentNode);
        parentNode.removeChild(this);
    }

    /**
     * Insert the specified HTML into the DOM before this node (i.e. as a preceding sibling).
     * @param html HTML to add before this node
     * @return this node, for chaining
     * @see #after(String)
     */
    public Node before(String html) {
        addSiblingHtml(siblingIndex(), html);
        return this;
    }

    /**
     * Insert the specified node into the DOM before this node (i.e. as a preceding sibling).
     * @param node to add before this node
     * @return this node, for chaining
     * @see #after(Node)
     */
    public Node before(Node node) {
        Validate.notNull(node);
        Validate.notNull(parentNode);

        parentNode.addChildren(siblingIndex(), node);
        return this;
    }

    /**
     * Insert the specified HTML into the DOM after this node (i.e. as a following sibling).
     * @param html HTML to add after this node
     * @return this node, for chaining
     * @see #before(String)
     */
    public Node after(String html) {
        addSiblingHtml(siblingIndex()+1, html);
        return this;
    }

    /**
     * Insert the specified node into the DOM after this node (i.e. as a following sibling).
     * @param node to add after this node
     * @return this node, for chaining
     * @see #before(Node)
     */
    public Node after(Node node) {
        Validate.notNull(node);
        Validate.notNull(parentNode);

        parentNode.addChildren(siblingIndex()+1, node);
        return this;
    }

    /**
     * Adds the sibling html.
     *
     * @param index the index
     * @param html the html
     */
    private void addSiblingHtml(int index, String html) {
        Validate.notNull(html);
        Validate.notNull(parentNode);

        Element context = parent() instanceof Element ? (Element) parent() : null;        
        List<Node> nodes = Parser.parseFragment(html, context, baseUri());
        parentNode.addChildren(index, nodes.toArray(new Node[nodes.size()]));
    }

    /**
     Wrap the supplied HTML around this node.
     @param html HTML to wrap around this element, e.g. {@code <div class="head"></div>}. Can be arbitrarily deep.
     @return this node, for chaining.
     */
    public Node wrap(String html) {
        Validate.notEmpty(html);

        Element context = parent() instanceof Element ? (Element) parent() : null;
        List<Node> wrapChildren = Parser.parseFragment(html, context, baseUri());
        Node wrapNode = wrapChildren.get(0);
        if (wrapNode == null || !(wrapNode instanceof Element)) // nothing to wrap with; noop
            return null;

        Element wrap = (Element) wrapNode;
        Element deepest = getDeepChild(wrap);
        parentNode.replaceChild(this, wrap);
        deepest.addChildren(this);

        // remainder (unbalanced wrap, like <div></div><p></p> -- The <p> is remainder
        if (wrapChildren.size() > 0) {
            for (int i = 0; i < wrapChildren.size(); i++) {
                Node remainder = wrapChildren.get(i);
                remainder.parentNode.removeChild(remainder);
                wrap.appendChild(remainder);
            }
        }
        return this;
    }

    /**
     * Removes this node from the DOM, and moves its children up into the node's parent. This has the effect of dropping
     * the node but keeping its children.
     * <p/>
     * For example, with the input html:<br/>
     *
     * @return the first child of this node, after the node has been unwrapped. Null if the node had no children.
     * {@code <div>One <span>Two <b>Three</b></span></div>}<br/>
     * Calling {@code element.unwrap()} on the {@code span} element will result in the html:<br/>
     * {@code <div>One Two <b>Three</b></div>}<br/>
     * and the {@code "Two "} {@link TextNode} being returned.
     * @see #remove()
     * @see #wrap(String)
     */
    public Node unwrap() {
        Validate.notNull(parentNode);

        int index = siblingIndex;
        Node firstChild = childNodes.size() > 0 ? childNodes.get(0) : null;
        parentNode.addChildren(index, this.childNodesAsArray());
        this.remove();

        return firstChild;
    }

    /**
     * Gets the deep child.
     *
     * @param el the el
     * @return the deep child
     */
    private Element getDeepChild(Element el) {
        List<Element> children = el.children();
        if (children.size() > 0)
            return getDeepChild(children.get(0));
        else
            return el;
    }
    
    /**
     * Replace this node in the DOM with the supplied node.
     * @param in the node that will will replace the existing node.
     */
    public void replaceWith(Node in) {
        Validate.notNull(in);
        Validate.notNull(parentNode);
        parentNode.replaceChild(this, in);
    }

    /**
     * Sets the parent node.
     *
     * @param parentNode the new parent node
     */
    protected void setParentNode(Node parentNode) {
        if (this.parentNode != null)
            this.parentNode.removeChild(this);
        this.parentNode = parentNode;
    }

    /**
     * Replace child.
     *
     * @param out the out
     * @param in the in
     */
    protected void replaceChild(Node out, Node in) {
        Validate.isTrue(out.parentNode == this);
        Validate.notNull(in);
        if (in.parentNode != null)
            in.parentNode.removeChild(in);
        
        Integer index = out.siblingIndex();
        childNodes.set(index, in);
        in.parentNode = this;
        in.setSiblingIndex(index);
        out.parentNode = null;
    }

    /**
     * Removes the child.
     *
     * @param out the out
     */
    protected void removeChild(Node out) {
        Validate.isTrue(out.parentNode == this);
        int index = out.siblingIndex();
        childNodes.remove(index);
        reindexChildren();
        out.parentNode = null;
    }

    /**
     * Adds the children.
     *
     * @param children the children
     */
    protected void addChildren(Node... children) {
        //most used. short circuit addChildren(int), which hits reindex children and array copy
        for (Node child: children) {
            reparentChild(child);
            childNodes.add(child);
            child.setSiblingIndex(childNodes.size()-1);
        }
    }

    /**
     * Adds the children.
     *
     * @param index the index
     * @param children the children
     */
    protected void addChildren(int index, Node... children) {
        Validate.noNullElements(children);
        for (int i = children.length - 1; i >= 0; i--) {
            Node in = children[i];
            reparentChild(in);
            childNodes.add(index, in);
        }
        reindexChildren();
    }

    /**
     * Reparent child.
     *
     * @param child the child
     */
    private void reparentChild(Node child) {
        if (child.parentNode != null)
            child.parentNode.removeChild(child);
        child.setParentNode(this);
    }
    
    /**
     * Reindex children.
     */
    private void reindexChildren() {
        for (int i = 0; i < childNodes.size(); i++) {
            childNodes.get(i).setSiblingIndex(i);
        }
    }
    
    /**
     Retrieves this node's sibling nodes. Similar to {@link #childNodes()  node.parent.childNodes()}, but does not
     include this node (a node is not a sibling of itself).
     @return node siblings. If the node has no parent, returns an empty list.
     */
    public List<Node> siblingNodes() {
        if (parentNode == null)
            return Collections.emptyList();

        List<Node> nodes = parentNode.childNodes;
        List<Node> siblings = new ArrayList<Node>(nodes.size() - 1);
        for (Node node: nodes)
            if (node != this)
                siblings.add(node);
        return siblings;
    }

    /**
     Get this node's next sibling.
     @return next sibling, or null if this is the last sibling
     */
    public Node nextSibling() {
        if (parentNode == null)
            return null; // root
        
        List<Node> siblings = parentNode.childNodes;
        Integer index = siblingIndex();
        Validate.notNull(index);
        if (siblings.size() > index+1)
            return siblings.get(index+1);
        else
            return null;
    }

    /**
     Get this node's previous sibling.
     @return the previous sibling, or null if this is the first sibling
     */
    public Node previousSibling() {
        if (parentNode == null)
            return null; // root

        List<Node> siblings = parentNode.childNodes;
        Integer index = siblingIndex();
        Validate.notNull(index);
        if (index > 0)
            return siblings.get(index-1);
        else
            return null;
    }

    /**
     * Get the list index of this node in its node sibling list. I.e. if this is the first node
     * sibling, returns 0.
     * @return position in node sibling list
     * @see org.jsoup.nodes.Element#elementSiblingIndex()
     */
    public int siblingIndex() {
        return siblingIndex;
    }
    
    /**
     * Sets the sibling index.
     *
     * @param siblingIndex the new sibling index
     */
    protected void setSiblingIndex(int siblingIndex) {
        this.siblingIndex = siblingIndex;
    }

    /**
     * Perform a depth-first traversal through this node and its descendants.
     * @param nodeVisitor the visitor callbacks to perform on each node
     * @return this node, for chaining
     */
    public Node traverse(NodeVisitor nodeVisitor) {
        Validate.notNull(nodeVisitor);
        NodeTraversor traversor = new NodeTraversor(nodeVisitor);
        traversor.traverse(this);
        return this;
    }

    /**
     Get the outer HTML of this node.
     @return HTML
     */
    public String outerHtml() {
        StringBuilder accum = new StringBuilder(128);
        outerHtml(accum);
        return accum.toString();
    }

    /**
     * Outer html.
     *
     * @param accum the accum
     */
    protected void outerHtml(StringBuilder accum) {
        new NodeTraversor(new OuterHtmlVisitor(accum, getOutputSettings())).traverse(this);
    }

    // if this node has no document (or parent), retrieve the default output settings
    /**
     * Gets the output settings.
     *
     * @return the output settings
     */
    private Document.OutputSettings getOutputSettings() {
        return ownerDocument() != null ? ownerDocument().outputSettings() : (new Document("")).outputSettings();
    }

    /**
     * Get the outer HTML of this node.
     *
     * @param accum accumulator to place HTML into
     * @param depth the depth
     * @param out the out
     */
    abstract void outerHtmlHead(StringBuilder accum, int depth, Document.OutputSettings out);

    /**
     * Outer html tail.
     *
     * @param accum the accum
     * @param depth the depth
     * @param out the out
     */
    abstract void outerHtmlTail(StringBuilder accum, int depth, Document.OutputSettings out);

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return outerHtml();
    }

    /**
     * Indent.
     *
     * @param accum the accum
     * @param depth the depth
     * @param out the out
     */
    protected void indent(StringBuilder accum, int depth, Document.OutputSettings out) {
        accum.append("\n").append(StringUtil.padding(depth * out.indentAmount()));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // todo: have nodes hold a child index, compare against that and parent (not children)
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = parentNode != null ? parentNode.hashCode() : 0;
        // not children, or will block stack as they go back up to parent)
        result = 31 * result + (attributes != null ? attributes.hashCode() : 0);
        return result;
    }

    /**
     * Create a stand-alone, deep copy of this node, and all of its children. The cloned node will have no siblings or
     * parent node. As a stand-alone object, any changes made to the clone or any of its children will not impact the
     * original node.
     * <p>
     * The cloned node may be adopted into another Document or node structure using {@link Element#appendChild(Node)}.
     * @return stand-alone cloned node
     */
    @Override
    public Node clone() {
        Node thisClone = doClone(null); // splits for orphan

        // Queue up nodes that need their children cloned (BFS).
        LinkedList<Node> nodesToProcess = new LinkedList<Node>();
        nodesToProcess.add(thisClone);

        while (!nodesToProcess.isEmpty()) {
            Node currParent = nodesToProcess.remove();

            for (int i = 0; i < currParent.childNodes.size(); i++) {
                Node childClone = currParent.childNodes.get(i).doClone(currParent);
                currParent.childNodes.set(i, childClone);
                nodesToProcess.add(childClone);
            }
        }

        return thisClone;
    }

    /*
     * Return a clone of the node using the given parent (which can be null).
     * Not a deep copy of children.
     */
    /**
     * Do clone.
     *
     * @param parent the parent
     * @return the node
     */
    protected Node doClone(Node parent) {
        Node clone;

        try {
            clone = (Node) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

        clone.parentNode = parent; // can be null, to create an orphan split
        clone.siblingIndex = parent == null ? 0 : siblingIndex;
        clone.attributes = attributes != null ? attributes.clone() : null;
        clone.baseUri = baseUri;
        clone.childNodes = new ArrayList<Node>(childNodes.size());

        for (Node child: childNodes)
            clone.childNodes.add(child);

        return clone;
    }

    /**
     * The Class OuterHtmlVisitor.
     */
    private static class OuterHtmlVisitor implements NodeVisitor {
        
        /** The accum. */
        private StringBuilder accum;
        
        /** The out. */
        private Document.OutputSettings out;

        /**
         * Instantiates a new outer html visitor.
         *
         * @param accum the accum
         * @param out the out
         */
        OuterHtmlVisitor(StringBuilder accum, Document.OutputSettings out) {
            this.accum = accum;
            this.out = out;
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.NodeVisitor#head(org.jsoup.nodes.Node, int)
         */
        public void head(Node node, int depth) {
            node.outerHtmlHead(accum, depth, out);
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.NodeVisitor#tail(org.jsoup.nodes.Node, int)
         */
        public void tail(Node node, int depth) {
            if (!node.nodeName().equals("#text")) // saves a void hit.
                node.outerHtmlTail(accum, depth, out);
        }
    }
}
