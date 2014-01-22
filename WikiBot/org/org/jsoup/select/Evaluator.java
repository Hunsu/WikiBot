package org.jsoup.select;

import org.jsoup.helper.Validate;
import org.jsoup.nodes.Comment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.XmlDeclaration;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Evaluates that an element matches the selector.
 */
public abstract class Evaluator {
    
    /**
     * Instantiates a new evaluator.
     */
    protected Evaluator() {
    }

    /**
     * Test if the element meets the evaluator's requirements.
     *
     * @param root    Root of the matching subtree
     * @param element tested element
     * @return true, if successful
     */
    public abstract boolean matches(Element root, Element element);

    /**
     * Evaluator for tag name.
     */
    public static final class Tag extends Evaluator {
        
        /** The tag name. */
        private String tagName;

        /**
         * Instantiates a new tag.
         *
         * @param tagName the tag name
         */
        public Tag(String tagName) {
            this.tagName = tagName;
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            return (element.tagName().equals(tagName));
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("%s", tagName);
        }
    }

    /**
     * Evaluator for element id.
     */
    public static final class Id extends Evaluator {
        
        /** The id. */
        private String id;

        /**
         * Instantiates a new id.
         *
         * @param id the id
         */
        public Id(String id) {
            this.id = id;
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            return (id.equals(element.id()));
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("#%s", id);
        }

    }

    /**
     * Evaluator for element class.
     */
    public static final class Class extends Evaluator {
        
        /** The class name. */
        private String className;

        /**
         * Instantiates a new class.
         *
         * @param className the class name
         */
        public Class(String className) {
            this.className = className;
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            return (element.hasClass(className));
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format(".%s", className);
        }

    }

    /**
     * Evaluator for attribute name matching.
     */
    public static final class Attribute extends Evaluator {
        
        /** The key. */
        private String key;

        /**
         * Instantiates a new attribute.
         *
         * @param key the key
         */
        public Attribute(String key) {
            this.key = key;
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            return element.hasAttr(key);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("[%s]", key);
        }

    }

    /**
     * Evaluator for attribute name prefix matching.
     */
    public static final class AttributeStarting extends Evaluator {
        
        /** The key prefix. */
        private String keyPrefix;

        /**
         * Instantiates a new attribute starting.
         *
         * @param keyPrefix the key prefix
         */
        public AttributeStarting(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            List<org.jsoup.nodes.Attribute> values = element.attributes().asList();
            for (org.jsoup.nodes.Attribute attribute : values) {
                if (attribute.getKey().startsWith(keyPrefix))
                    return true;
            }
            return false;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("[^%s]", keyPrefix);
        }

    }

    /**
     * Evaluator for attribute name/value matching.
     */
    public static final class AttributeWithValue extends AttributeKeyPair {
        
        /**
         * Instantiates a new attribute with value.
         *
         * @param key the key
         * @param value the value
         */
        public AttributeWithValue(String key, String value) {
            super(key, value);
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            return element.hasAttr(key) && value.equalsIgnoreCase(element.attr(key));
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("[%s=%s]", key, value);
        }

    }

    /**
     * Evaluator for attribute name != value matching.
     */
    public static final class AttributeWithValueNot extends AttributeKeyPair {
        
        /**
         * Instantiates a new attribute with value not.
         *
         * @param key the key
         * @param value the value
         */
        public AttributeWithValueNot(String key, String value) {
            super(key, value);
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            return !value.equalsIgnoreCase(element.attr(key));
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("[%s!=%s]", key, value);
        }

    }

    /**
     * Evaluator for attribute name/value matching (value prefix).
     */
    public static final class AttributeWithValueStarting extends AttributeKeyPair {
        
        /**
         * Instantiates a new attribute with value starting.
         *
         * @param key the key
         * @param value the value
         */
        public AttributeWithValueStarting(String key, String value) {
            super(key, value);
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            return element.hasAttr(key) && element.attr(key).toLowerCase().startsWith(value); // value is lower case already
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("[%s^=%s]", key, value);
        }

    }

    /**
     * Evaluator for attribute name/value matching (value ending).
     */
    public static final class AttributeWithValueEnding extends AttributeKeyPair {
        
        /**
         * Instantiates a new attribute with value ending.
         *
         * @param key the key
         * @param value the value
         */
        public AttributeWithValueEnding(String key, String value) {
            super(key, value);
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            return element.hasAttr(key) && element.attr(key).toLowerCase().endsWith(value); // value is lower case
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("[%s$=%s]", key, value);
        }

    }

    /**
     * Evaluator for attribute name/value matching (value containing).
     */
    public static final class AttributeWithValueContaining extends AttributeKeyPair {
        
        /**
         * Instantiates a new attribute with value containing.
         *
         * @param key the key
         * @param value the value
         */
        public AttributeWithValueContaining(String key, String value) {
            super(key, value);
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            return element.hasAttr(key) && element.attr(key).toLowerCase().contains(value); // value is lower case
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("[%s*=%s]", key, value);
        }

    }

    /**
     * Evaluator for attribute name/value matching (value regex matching).
     */
    public static final class AttributeWithValueMatching extends Evaluator {
        
        /** The key. */
        String key;
        
        /** The pattern. */
        Pattern pattern;

        /**
         * Instantiates a new attribute with value matching.
         *
         * @param key the key
         * @param pattern the pattern
         */
        public AttributeWithValueMatching(String key, Pattern pattern) {
            this.key = key.trim().toLowerCase();
            this.pattern = pattern;
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            return element.hasAttr(key) && pattern.matcher(element.attr(key)).find();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("[%s~=%s]", key, pattern.toString());
        }

    }

    /**
     * Abstract evaluator for attribute name/value matching.
     */
    public abstract static class AttributeKeyPair extends Evaluator {
        
        /** The key. */
        String key;
        
        /** The value. */
        String value;

        /**
         * Instantiates a new attribute key pair.
         *
         * @param key the key
         * @param value the value
         */
        public AttributeKeyPair(String key, String value) {
            Validate.notEmpty(key);
            Validate.notEmpty(value);

            this.key = key.trim().toLowerCase();
            this.value = value.trim().toLowerCase();
        }
    }

    /**
     * Evaluator for any / all element matching.
     */
    public static final class AllElements extends Evaluator {

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            return true;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "*";
        }
    }

    /**
     * Evaluator for matching by sibling index number (e < idx).
     */
    public static final class IndexLessThan extends IndexEvaluator {
        
        /**
         * Instantiates a new index less than.
         *
         * @param index the index
         */
        public IndexLessThan(int index) {
            super(index);
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            return element.elementSiblingIndex() < index;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format(":lt(%d)", index);
        }

    }

    /**
     * Evaluator for matching by sibling index number (e > idx).
     */
    public static final class IndexGreaterThan extends IndexEvaluator {
        
        /**
         * Instantiates a new index greater than.
         *
         * @param index the index
         */
        public IndexGreaterThan(int index) {
            super(index);
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            return element.elementSiblingIndex() > index;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format(":gt(%d)", index);
        }

    }

    /**
     * Evaluator for matching by sibling index number (e = idx).
     */
    public static final class IndexEquals extends IndexEvaluator {
        
        /**
         * Instantiates a new index equals.
         *
         * @param index the index
         */
        public IndexEquals(int index) {
            super(index);
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            return element.elementSiblingIndex() == index;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format(":eq(%d)", index);
        }

    }
    
    /**
     * Evaluator for matching the last sibling (css :last-child).
     */
    public static final class IsLastChild extends Evaluator {
		
		/* (non-Javadoc)
		 * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
		 */
		@Override
		public boolean matches(Element root, Element element) {
			final Element p = element.parent();
			return p != null && !(p instanceof Document) && element.elementSiblingIndex() == p.children().size()-1;
		}
    	
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return ":last-child";
		}
    }
    
    /**
     * The Class IsFirstOfType.
     */
    public static final class IsFirstOfType extends IsNthOfType {
		
		/**
		 * Instantiates a new checks if is first of type.
		 */
		public IsFirstOfType() {
			super(0,1);
		}
		
		/* (non-Javadoc)
		 * @see org.jsoup.select.Evaluator.CssNthEvaluator#toString()
		 */
		@Override
		public String toString() {
			return ":first-of-type";
		}
    }
    
    /**
     * The Class IsLastOfType.
     */
    public static final class IsLastOfType extends IsNthLastOfType {
		
		/**
		 * Instantiates a new checks if is last of type.
		 */
		public IsLastOfType() {
			super(0,1);
		}
		
		/* (non-Javadoc)
		 * @see org.jsoup.select.Evaluator.CssNthEvaluator#toString()
		 */
		@Override
		public String toString() {
			return ":last-of-type";
		}
    }

    
    /**
     * The Class CssNthEvaluator.
     */
    public static abstract class CssNthEvaluator extends Evaluator {
    	
	    /** The b. */
	    protected final int a, b;
    	
    	/**
	     * Instantiates a new css nth evaluator.
	     *
	     * @param a the a
	     * @param b the b
	     */
	    public CssNthEvaluator(int a, int b) {
    		this.a = a;
    		this.b = b;
    	}
    	
	    /**
	     * Instantiates a new css nth evaluator.
	     *
	     * @param b the b
	     */
	    public CssNthEvaluator(int b) {
    		this(0,b);
    	}
    	
    	/* (non-Javadoc)
	     * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
	     */
	    @Override
    	public boolean matches(Element root, Element element) {
    		final Element p = element.parent();
    		if (p == null || (p instanceof Document)) return false;
    		
    		final int pos = calculatePosition(root, element);
    		if (a == 0) return pos == b;
    		
    		return (pos-b)*a >= 0 && (pos-b)%a==0;
    	}
    	
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			if (a == 0)
				return String.format(":%s(%d)",getPseudoClass(), b);
			if (b == 0)
				return String.format(":%s(%dn)",getPseudoClass(), a);
			return String.format(":%s(%dn%+d)", getPseudoClass(),a, b);
		}
    	
		/**
		 * Gets the pseudo class.
		 *
		 * @return the pseudo class
		 */
		protected abstract String getPseudoClass();
		
		/**
		 * Calculate position.
		 *
		 * @param root the root
		 * @param element the element
		 * @return the int
		 */
		protected abstract int calculatePosition(Element root, Element element);
    }
    
    
    /**
     * css-compatible Evaluator for :eq (css :nth-child).
     *
     * @see IndexEquals
     */
    public static final class IsNthChild extends CssNthEvaluator {

    	/**
	     * Instantiates a new checks if is nth child.
	     *
	     * @param a the a
	     * @param b the b
	     */
	    public IsNthChild(int a, int b) {
    		super(a,b);
		}

		/* (non-Javadoc)
		 * @see org.jsoup.select.Evaluator.CssNthEvaluator#calculatePosition(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
		 */
		protected int calculatePosition(Element root, Element element) {
			return element.elementSiblingIndex()+1;
		}

		
		/* (non-Javadoc)
		 * @see org.jsoup.select.Evaluator.CssNthEvaluator#getPseudoClass()
		 */
		protected String getPseudoClass() {
			return "nth-child";
		}
    }
    
    /**
     * css pseudo class :nth-last-child).
     *
     * @see IndexEquals
     */
    public static final class IsNthLastChild extends CssNthEvaluator {
    	
	    /**
	     * Instantiates a new checks if is nth last child.
	     *
	     * @param a the a
	     * @param b the b
	     */
	    public IsNthLastChild(int a, int b) {
    		super(a,b);
    	}

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator.CssNthEvaluator#calculatePosition(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        protected int calculatePosition(Element root, Element element) {
        	return element.parent().children().size() - element.elementSiblingIndex();
        }
        
		/* (non-Javadoc)
		 * @see org.jsoup.select.Evaluator.CssNthEvaluator#getPseudoClass()
		 */
		@Override
		protected String getPseudoClass() {
			return "nth-last-child";
		}
    }
    
    /**
     * css pseudo class nth-of-type.
     */
    public static class IsNthOfType extends CssNthEvaluator {
    	
	    /**
	     * Instantiates a new checks if is nth of type.
	     *
	     * @param a the a
	     * @param b the b
	     */
	    public IsNthOfType(int a, int b) {
    		super(a,b);
    	}

		/* (non-Javadoc)
		 * @see org.jsoup.select.Evaluator.CssNthEvaluator#calculatePosition(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
		 */
		protected int calculatePosition(Element root, Element element) {
			int pos = 0;
        	Elements family = element.parent().children();
        	for (int i = 0; i < family.size(); i++) {
        		if (family.get(i).tag() == element.tag()) pos++;
        		if (family.get(i) == element) break;
        	}
			return pos;
		}

		/* (non-Javadoc)
		 * @see org.jsoup.select.Evaluator.CssNthEvaluator#getPseudoClass()
		 */
		@Override
		protected String getPseudoClass() {
			return "nth-of-type";
		}
    }
    
    /**
     * The Class IsNthLastOfType.
     */
    public static class IsNthLastOfType extends CssNthEvaluator {

		/**
		 * Instantiates a new checks if is nth last of type.
		 *
		 * @param a the a
		 * @param b the b
		 */
		public IsNthLastOfType(int a, int b) {
			super(a, b);
		}
		
		/* (non-Javadoc)
		 * @see org.jsoup.select.Evaluator.CssNthEvaluator#calculatePosition(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
		 */
		@Override
		protected int calculatePosition(Element root, Element element) {
			int pos = 0;
        	Elements family = element.parent().children();
        	for (int i = element.elementSiblingIndex(); i < family.size(); i++) {
        		if (family.get(i).tag() == element.tag()) pos++;
        	}
			return pos;
		}

		/* (non-Javadoc)
		 * @see org.jsoup.select.Evaluator.CssNthEvaluator#getPseudoClass()
		 */
		@Override
		protected String getPseudoClass() {
			return "nth-last-of-type";
		}
    }

    /**
     * Evaluator for matching the first sibling (css :first-child).
     */
    public static final class IsFirstChild extends Evaluator {
    	
	    /* (non-Javadoc)
	     * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
	     */
	    @Override
    	public boolean matches(Element root, Element element) {
    		final Element p = element.parent();
    		return p != null && !(p instanceof Document) && element.elementSiblingIndex() == 0;
    	}
    	
    	/* (non-Javadoc)
	     * @see java.lang.Object#toString()
	     */
	    @Override
    	public String toString() {
    		return ":first-child";
    	}
    }
    
    /**
     * css3 pseudo-class :root.
     *
     * @see <a href="http://www.w3.org/TR/selectors/#root-pseudo">:root selector</a>
     */
    public static final class IsRoot extends Evaluator {
    	
	    /* (non-Javadoc)
	     * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
	     */
	    @Override
    	public boolean matches(Element root, Element element) {
    		final Element r = root instanceof Document?root.child(0):root;
    		return element == r;
    	}
    	
	    /* (non-Javadoc)
	     * @see java.lang.Object#toString()
	     */
	    @Override
    	public String toString() {
    		return ":root";
    	}
    }

    /**
     * The Class IsOnlyChild.
     */
    public static final class IsOnlyChild extends Evaluator {
		
		/* (non-Javadoc)
		 * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
		 */
		@Override
		public boolean matches(Element root, Element element) {
			final Element p = element.parent();
			return p!=null && !(p instanceof Document) && element.siblingElements().size() == 0;
		}
    	
	    /* (non-Javadoc)
	     * @see java.lang.Object#toString()
	     */
	    @Override
    	public String toString() {
    		return ":only-child";
    	}
    }

    /**
     * The Class IsOnlyOfType.
     */
    public static final class IsOnlyOfType extends Evaluator {
		
		/* (non-Javadoc)
		 * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
		 */
		@Override
		public boolean matches(Element root, Element element) {
			final Element p = element.parent();
			if (p==null || p instanceof Document) return false;
			
			int pos = 0;
        	Elements family = p.children();
        	for (int i = 0; i < family.size(); i++) {
        		if (family.get(i).tag().equals(element.tag())) pos++;
        	}
        	return pos == 1;
		}
    	
	    /* (non-Javadoc)
	     * @see java.lang.Object#toString()
	     */
	    @Override
    	public String toString() {
    		return ":only-of-type";
    	}
    }

    /**
     * The Class IsEmpty.
     */
    public static final class IsEmpty extends Evaluator {
		
		/* (non-Javadoc)
		 * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
		 */
		@Override
		public boolean matches(Element root, Element element) {
        	List<Node> family = element.childNodes();
        	for (int i = 0; i < family.size(); i++) {
        		Node n = family.get(i);
        		if (!(n instanceof Comment || n instanceof XmlDeclaration || n instanceof DocumentType)) return false; 
        	}
        	return true;
		}
    	
	    /* (non-Javadoc)
	     * @see java.lang.Object#toString()
	     */
	    @Override
    	public String toString() {
    		return ":empty";
    	}
    }

    /**
     * Abstract evaluator for sibling index matching.
     *
     * @author ant
     */
    public abstract static class IndexEvaluator extends Evaluator {
        
        /** The index. */
        int index;

        /**
         * Instantiates a new index evaluator.
         *
         * @param index the index
         */
        public IndexEvaluator(int index) {
            this.index = index;
        }
    }

    /**
     * Evaluator for matching Element (and its descendants) text.
     */
    public static final class ContainsText extends Evaluator {
        
        /** The search text. */
        private String searchText;

        /**
         * Instantiates a new contains text.
         *
         * @param searchText the search text
         */
        public ContainsText(String searchText) {
            this.searchText = searchText.toLowerCase();
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            return (element.text().toLowerCase().contains(searchText));
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format(":contains(%s", searchText);
        }
    }

    /**
     * Evaluator for matching Element's own text.
     */
    public static final class ContainsOwnText extends Evaluator {
        
        /** The search text. */
        private String searchText;

        /**
         * Instantiates a new contains own text.
         *
         * @param searchText the search text
         */
        public ContainsOwnText(String searchText) {
            this.searchText = searchText.toLowerCase();
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            return (element.ownText().toLowerCase().contains(searchText));
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format(":containsOwn(%s", searchText);
        }
    }

    /**
     * Evaluator for matching Element (and its descendants) text with regex.
     */
    public static final class Matches extends Evaluator {
        
        /** The pattern. */
        private Pattern pattern;

        /**
         * Instantiates a new matches.
         *
         * @param pattern the pattern
         */
        public Matches(Pattern pattern) {
            this.pattern = pattern;
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            Matcher m = pattern.matcher(element.text());
            return m.find();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format(":matches(%s", pattern);
        }
    }

    /**
     * Evaluator for matching Element's own text with regex.
     */
    public static final class MatchesOwn extends Evaluator {
        
        /** The pattern. */
        private Pattern pattern;

        /**
         * Instantiates a new matches own.
         *
         * @param pattern the pattern
         */
        public MatchesOwn(Pattern pattern) {
            this.pattern = pattern;
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element element) {
            Matcher m = pattern.matcher(element.ownText());
            return m.find();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format(":matchesOwn(%s", pattern);
        }
    }
}
