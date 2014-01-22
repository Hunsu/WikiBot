package org.jsoup.parser;

import org.jsoup.helper.Validate;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;

/**
 * Parse tokens for the Tokeniser.
 */
abstract class Token {
    
    /** The type. */
    TokenType type;

    /**
     * Instantiates a new token.
     */
    private Token() {
    }
    
    /**
     * Token type.
     *
     * @return the string
     */
    String tokenType() {
        return this.getClass().getSimpleName();
    }

    /**
     * The Class Doctype.
     */
    static class Doctype extends Token {
        
        /** The name. */
        final StringBuilder name = new StringBuilder();
        
        /** The public identifier. */
        final StringBuilder publicIdentifier = new StringBuilder();
        
        /** The system identifier. */
        final StringBuilder systemIdentifier = new StringBuilder();
        
        /** The force quirks. */
        boolean forceQuirks = false;

        /**
         * Instantiates a new doctype.
         */
        Doctype() {
            type = TokenType.Doctype;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        String getName() {
            return name.toString();
        }

        /**
         * Gets the public identifier.
         *
         * @return the public identifier
         */
        String getPublicIdentifier() {
            return publicIdentifier.toString();
        }

        /**
         * Gets the system identifier.
         *
         * @return the system identifier
         */
        public String getSystemIdentifier() {
            return systemIdentifier.toString();
        }

        /**
         * Checks if is force quirks.
         *
         * @return true, if is force quirks
         */
        public boolean isForceQuirks() {
            return forceQuirks;
        }
    }

    /**
     * The Class Tag.
     */
    static abstract class Tag extends Token {
        
        /** The tag name. */
        protected String tagName;
        
        /** The pending attribute name. */
        private String pendingAttributeName; // attribute names are generally caught in one hop, not accumulated
        
        /** The pending attribute value. */
        private StringBuilder pendingAttributeValue; // but values are accumulated, from e.g. & in hrefs

        /** The self closing. */
        boolean selfClosing = false;
        
        /** The attributes. */
        Attributes attributes; // start tags get attributes on construction. End tags get attributes on first new attribute (but only for parser convenience, not used).

        /**
         * New attribute.
         */
        void newAttribute() {
            if (attributes == null)
                attributes = new Attributes();

            if (pendingAttributeName != null) {
                Attribute attribute;
                if (pendingAttributeValue == null)
                    attribute = new Attribute(pendingAttributeName, "");
                else
                    attribute = new Attribute(pendingAttributeName, pendingAttributeValue.toString());
                attributes.put(attribute);
            }
            pendingAttributeName = null;
            if (pendingAttributeValue != null)
                pendingAttributeValue.delete(0, pendingAttributeValue.length());
        }

        /**
         * Finalise tag.
         */
        void finaliseTag() {
            // finalises for emit
            if (pendingAttributeName != null) {
                // todo: check if attribute name exists; if so, drop and error
                newAttribute();
            }
        }

        /**
         * Name.
         *
         * @return the string
         */
        String name() {
            Validate.isFalse(tagName.length() == 0);
            return tagName;
        }

        /**
         * Name.
         *
         * @param name the name
         * @return the tag
         */
        Tag name(String name) {
            tagName = name;
            return this;
        }

        /**
         * Checks if is self closing.
         *
         * @return true, if is self closing
         */
        boolean isSelfClosing() {
            return selfClosing;
        }

        /**
         * Gets the attributes.
         *
         * @return the attributes
         */
        @SuppressWarnings({"TypeMayBeWeakened"})
        Attributes getAttributes() {
            return attributes;
        }

        // these appenders are rarely hit in not null state-- caused by null chars.
        /**
         * Append tag name.
         *
         * @param append the append
         */
        void appendTagName(String append) {
            tagName = tagName == null ? append : tagName.concat(append);
        }

        /**
         * Append tag name.
         *
         * @param append the append
         */
        void appendTagName(char append) {
            appendTagName(String.valueOf(append));
        }

        /**
         * Append attribute name.
         *
         * @param append the append
         */
        void appendAttributeName(String append) {
            pendingAttributeName = pendingAttributeName == null ? append : pendingAttributeName.concat(append);
        }

        /**
         * Append attribute name.
         *
         * @param append the append
         */
        void appendAttributeName(char append) {
            appendAttributeName(String.valueOf(append));
        }

        /**
         * Append attribute value.
         *
         * @param append the append
         */
        void appendAttributeValue(String append) {
            ensureAttributeValue();
            pendingAttributeValue.append(append);
        }

        /**
         * Append attribute value.
         *
         * @param append the append
         */
        void appendAttributeValue(char append) {
            ensureAttributeValue();
            pendingAttributeValue.append(append);
        }

        /**
         * Append attribute value.
         *
         * @param append the append
         */
        void appendAttributeValue(char[] append) {
            ensureAttributeValue();
            pendingAttributeValue.append(append);
        }

        /**
         * Ensure attribute value.
         */
        private final void ensureAttributeValue() {
            if (pendingAttributeValue == null)
                pendingAttributeValue = new StringBuilder();
        }
    }

    /**
     * The Class StartTag.
     */
    static class StartTag extends Tag {
        
        /**
         * Instantiates a new start tag.
         */
        StartTag() {
            super();
            attributes = new Attributes();
            type = TokenType.StartTag;
        }

        /**
         * Instantiates a new start tag.
         *
         * @param name the name
         */
        StartTag(String name) {
            this();
            this.tagName = name;
        }

        /**
         * Instantiates a new start tag.
         *
         * @param name the name
         * @param attributes the attributes
         */
        StartTag(String name, Attributes attributes) {
            this();
            this.tagName = name;
            this.attributes = attributes;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            if (attributes != null && attributes.size() > 0)
                return "<" + name() + " " + attributes.toString() + ">";
            else
                return "<" + name() + ">";
        }
    }

    /**
     * The Class EndTag.
     */
    static class EndTag extends Tag{
        
        /**
         * Instantiates a new end tag.
         */
        EndTag() {
            super();
            type = TokenType.EndTag;
        }

        /**
         * Instantiates a new end tag.
         *
         * @param name the name
         */
        EndTag(String name) {
            this();
            this.tagName = name;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "</" + name() + ">";
        }
    }

    /**
     * The Class Comment.
     */
    static class Comment extends Token {
        
        /** The data. */
        final StringBuilder data = new StringBuilder();
        
        /** The bogus. */
        boolean bogus = false;

        /**
         * Instantiates a new comment.
         */
        Comment() {
            type = TokenType.Comment;
        }

        /**
         * Gets the data.
         *
         * @return the data
         */
        String getData() {
            return data.toString();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "<!--" + getData() + "-->";
        }
    }

    /**
     * The Class Character.
     */
    static class Character extends Token {
        
        /** The data. */
        private final String data;

        /**
         * Instantiates a new character.
         *
         * @param data the data
         */
        Character(String data) {
            type = TokenType.Character;
            this.data = data;
        }

        /**
         * Gets the data.
         *
         * @return the data
         */
        String getData() {
            return data;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return getData();
        }
    }

    /**
     * The Class EOF.
     */
    static class EOF extends Token {
        
        /**
         * Instantiates a new eof.
         */
        EOF() {
            type = Token.TokenType.EOF;
        }
    }

    /**
     * Checks if is doctype.
     *
     * @return true, if is doctype
     */
    boolean isDoctype() {
        return type == TokenType.Doctype;
    }

    /**
     * As doctype.
     *
     * @return the doctype
     */
    Doctype asDoctype() {
        return (Doctype) this;
    }

    /**
     * Checks if is start tag.
     *
     * @return true, if is start tag
     */
    boolean isStartTag() {
        return type == TokenType.StartTag;
    }

    /**
     * As start tag.
     *
     * @return the start tag
     */
    StartTag asStartTag() {
        return (StartTag) this;
    }

    /**
     * Checks if is end tag.
     *
     * @return true, if is end tag
     */
    boolean isEndTag() {
        return type == TokenType.EndTag;
    }

    /**
     * As end tag.
     *
     * @return the end tag
     */
    EndTag asEndTag() {
        return (EndTag) this;
    }

    /**
     * Checks if is comment.
     *
     * @return true, if is comment
     */
    boolean isComment() {
        return type == TokenType.Comment;
    }

    /**
     * As comment.
     *
     * @return the comment
     */
    Comment asComment() {
        return (Comment) this;
    }

    /**
     * Checks if is character.
     *
     * @return true, if is character
     */
    boolean isCharacter() {
        return type == TokenType.Character;
    }

    /**
     * As character.
     *
     * @return the character
     */
    Character asCharacter() {
        return (Character) this;
    }

    /**
     * Checks if is eof.
     *
     * @return true, if is eof
     */
    boolean isEOF() {
        return type == TokenType.EOF;
    }

    /**
     * The Enum TokenType.
     */
    enum TokenType {
        
        /** The Doctype. */
        Doctype,
        
        /** The Start tag. */
        StartTag,
        
        /** The End tag. */
        EndTag,
        
        /** The Comment. */
        Comment,
        
        /** The Character. */
        Character,
        
        /** The eof. */
        EOF
    }
}
