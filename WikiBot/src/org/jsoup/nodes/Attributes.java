package org.jsoup.nodes;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.helper.Validate;

/**
 * The attributes of an Element.
 * <p/>
 * Attributes are treated as a map: there can be only one value associated with an attribute key.
 * <p/>
 * Attribute key and value comparisons are done case insensitively, and keys are normalised to
 * lower-case.
 * 
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class Attributes implements Iterable<Attribute>, Cloneable {
    
    /** The Constant dataPrefix. */
    protected static final String dataPrefix = "data-";
    
    /** The attributes. */
    private LinkedHashMap<String, Attribute> attributes = null;
    // linked hash map to preserve insertion order.
    // null be default as so many elements have no attributes -- saves a good chunk of memory

    /**
     Get an attribute value by key.
     @param key the attribute key
     @return the attribute value if set; or empty string if not set.
     @see #hasKey(String)
     */
    public String get(String key) {
        Validate.notEmpty(key);

        if (attributes == null)
            return "";

        Attribute attr = attributes.get(key.toLowerCase());
        return attr != null ? attr.getValue() : "";
    }

    /**
     Set a new attribute, or replace an existing one by key.
     @param key attribute key
     @param value attribute value
     */
    public void put(String key, String value) {
        Attribute attr = new Attribute(key, value);
        put(attr);
    }

    /**
     Set a new attribute, or replace an existing one by key.
     @param attribute attribute
     */
    public void put(Attribute attribute) {
        Validate.notNull(attribute);
        if (attributes == null)
             attributes = new LinkedHashMap<String, Attribute>(2);
        attributes.put(attribute.getKey(), attribute);
    }

    /**
     Remove an attribute by key.
     @param key attribute key to remove
     */
    public void remove(String key) {
        Validate.notEmpty(key);
        if (attributes == null)
            return;
        attributes.remove(key.toLowerCase());
    }

    /**
     Tests if these attributes contain an attribute with this key.
     @param key key to check for
     @return true if key exists, false otherwise
     */
    public boolean hasKey(String key) {
        return attributes != null && attributes.containsKey(key.toLowerCase());
    }

    /**
     Get the number of attributes in this set.
     @return size
     */
    public int size() {
        if (attributes == null)
            return 0;
        return attributes.size();
    }

    /**
     Add all the attributes from the incoming set to this set.
     @param incoming attributes to add to these attributes.
     */
    public void addAll(Attributes incoming) {
        if (incoming.size() == 0)
            return;
        if (attributes == null)
            attributes = new LinkedHashMap<String, Attribute>(incoming.size());
        attributes.putAll(incoming.attributes);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<Attribute> iterator() {
        return asList().iterator();
    }

    /**
     Get the attributes as a List, for iteration. Do not modify the keys of the attributes via this view, as changes
     to keys will not be recognised in the containing set.
     @return an view of the attributes as a List.
     */
    public List<Attribute> asList() {
        if (attributes == null)
            return Collections.emptyList();

        List<Attribute> list = new ArrayList<Attribute>(attributes.size());
        for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
            list.add(entry.getValue());
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Retrieves a filtered view of attributes that are HTML5 custom data attributes; that is, attributes with keys
     * starting with {@code data-}.
     * @return map of custom data attributes.
     */
    public Map<String, String> dataset() {
        return new Dataset();
    }

    /**
     Get the HTML representation of these attributes.
     @return HTML
     */
    public String html() {
        StringBuilder accum = new StringBuilder();
        html(accum, (new Document("")).outputSettings()); // output settings a bit funky, but this html() seldom used
        return accum.toString();
    }
    
    /**
     * Html.
     *
     * @param accum the accum
     * @param out the out
     */
    void html(StringBuilder accum, Document.OutputSettings out) {
        if (attributes == null)
            return;
        
        for (Map.Entry<String, Attribute> entry : attributes.entrySet()) {
            Attribute attribute = entry.getValue();
            accum.append(" ");
            attribute.html(accum, out);
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return html();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attributes)) return false;
        
        Attributes that = (Attributes) o;
        
        if (attributes != null ? !attributes.equals(that.attributes) : that.attributes != null) return false;
        
        return true;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return attributes != null ? attributes.hashCode() : 0;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Attributes clone() {
        if (attributes == null)
            return new Attributes();

        Attributes clone;
        try {
            clone = (Attributes) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        clone.attributes = new LinkedHashMap<String, Attribute>(attributes.size());
        for (Attribute attribute: this)
            clone.attributes.put(attribute.getKey(), attribute.clone());
        return clone;
    }

    /**
     * The Class Dataset.
     */
    private class Dataset extends AbstractMap<String, String> {

        /**
         * Instantiates a new dataset.
         */
        private Dataset() {
            if (attributes == null)
                attributes = new LinkedHashMap<String, Attribute>(2);
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#entrySet()
         */
        public Set<Entry<String, String>> entrySet() {
            return new EntrySet();
        }

        /* (non-Javadoc)
         * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
         */
        @Override
        public String put(String key, String value) {
            String dataKey = dataKey(key);
            String oldValue = hasKey(dataKey) ? attributes.get(dataKey).getValue() : null;
            Attribute attr = new Attribute(dataKey, value);
            attributes.put(dataKey, attr);
            return oldValue;
        }

        /**
         * The Class EntrySet.
         */
        private class EntrySet extends AbstractSet<Map.Entry<String, String>> {
            
            /* (non-Javadoc)
             * @see java.util.AbstractCollection#iterator()
             */
            public Iterator<Map.Entry<String, String>> iterator() {
                return new DatasetIterator();
            }

            /* (non-Javadoc)
             * @see java.util.AbstractCollection#size()
             */
            public int size() {
                int count = 0;
                Iterator iter = new DatasetIterator();
                while (iter.hasNext())
                    count++;
                return count;
            }
        }

        /**
         * The Class DatasetIterator.
         */
        private class DatasetIterator implements Iterator<Map.Entry<String, String>> {
            
            /** The attr iter. */
            private Iterator<Attribute> attrIter = attributes.values().iterator();
            
            /** The attr. */
            private Attribute attr;
            
            /* (non-Javadoc)
             * @see java.util.Iterator#hasNext()
             */
            public boolean hasNext() {
                while (attrIter.hasNext()) {
                    attr = attrIter.next();
                    if (attr.isDataAttribute()) return true;
                }
                return false;
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#next()
             */
            public Entry<String, String> next() {
                return new Attribute(attr.getKey().substring(dataPrefix.length()), attr.getValue());
            }

            /* (non-Javadoc)
             * @see java.util.Iterator#remove()
             */
            public void remove() {
                attributes.remove(attr.getKey());
            }
        }
    }

    /**
     * Data key.
     *
     * @param key the key
     * @return the string
     */
    private static String dataKey(String key) {
        return dataPrefix + key;
    }
}
