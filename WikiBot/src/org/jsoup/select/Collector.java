package org.jsoup.select;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

/**
 * Collects a list of elements that match the supplied criteria.
 *
 * @author Jonathan Hedley
 */
public class Collector {

    /**
     * Instantiates a new collector.
     */
    private Collector() {
    }

    /**
     Build a list of elements, by visiting root and every descendant of root, and testing it against the evaluator.
     @param eval Evaluator to test elements against
     @param root root of tree to descend
     @return list of matches; empty if none
     */
    public static Elements collect (Evaluator eval, Element root) {
        Elements elements = new Elements();
        new NodeTraversor(new Accumulator(root, elements, eval)).traverse(root);
        return elements;
    }

    /**
     * The Class Accumulator.
     */
    private static class Accumulator implements NodeVisitor {
        
        /** The root. */
        private final Element root;
        
        /** The elements. */
        private final Elements elements;
        
        /** The eval. */
        private final Evaluator eval;

        /**
         * Instantiates a new accumulator.
         *
         * @param root the root
         * @param elements the elements
         * @param eval the eval
         */
        Accumulator(Element root, Elements elements, Evaluator eval) {
            this.root = root;
            this.elements = elements;
            this.eval = eval;
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.NodeVisitor#head(org.jsoup.nodes.Node, int)
         */
        public void head(Node node, int depth) {
            if (node instanceof Element) {
                Element el = (Element) node;
                if (eval.matches(root, el))
                    elements.add(el);
            }
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.NodeVisitor#tail(org.jsoup.nodes.Node, int)
         */
        public void tail(Node node, int depth) {
            // void
        }
    }
}
