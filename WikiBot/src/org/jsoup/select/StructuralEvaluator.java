package org.jsoup.select;

import org.jsoup.nodes.Element;

/**
 * Base structural evaluator.
 */
abstract class StructuralEvaluator extends Evaluator {
    
    /** The evaluator. */
    Evaluator evaluator;

    /**
     * The Class Root.
     */
    static class Root extends Evaluator {
        
        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        public boolean matches(Element root, Element element) {
            return root == element;
        }
    }

    /**
     * The Class Has.
     */
    static class Has extends StructuralEvaluator {
        
        /**
         * Instantiates a new checks for.
         *
         * @param evaluator the evaluator
         */
        public Has(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        public boolean matches(Element root, Element element) {
            for (Element e : element.getAllElements()) {
                if (e != element && evaluator.matches(root, e))
                    return true;
            }
            return false;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return String.format(":has(%s)", evaluator);
        }
    }

    /**
     * The Class Not.
     */
    static class Not extends StructuralEvaluator {
        
        /**
         * Instantiates a new not.
         *
         * @param evaluator the evaluator
         */
        public Not(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        public boolean matches(Element root, Element node) {
            return !evaluator.matches(root, node);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return String.format(":not%s", evaluator);
        }
    }

    /**
     * The Class Parent.
     */
    static class Parent extends StructuralEvaluator {
        
        /**
         * Instantiates a new parent.
         *
         * @param evaluator the evaluator
         */
        public Parent(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        public boolean matches(Element root, Element element) {
            if (root == element)
                return false;

            Element parent = element.parent();
            while (parent != root) {
                if (evaluator.matches(root, parent))
                    return true;
                parent = parent.parent();
            }
            return false;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return String.format(":parent%s", evaluator);
        }
    }

    /**
     * The Class ImmediateParent.
     */
    static class ImmediateParent extends StructuralEvaluator {
        
        /**
         * Instantiates a new immediate parent.
         *
         * @param evaluator the evaluator
         */
        public ImmediateParent(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        public boolean matches(Element root, Element element) {
            if (root == element)
                return false;

            Element parent = element.parent();
            return parent != null && evaluator.matches(root, parent);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return String.format(":ImmediateParent%s", evaluator);
        }
    }

    /**
     * The Class PreviousSibling.
     */
    static class PreviousSibling extends StructuralEvaluator {
        
        /**
         * Instantiates a new previous sibling.
         *
         * @param evaluator the evaluator
         */
        public PreviousSibling(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        public boolean matches(Element root, Element element) {
            if (root == element)
                return false;

            Element prev = element.previousElementSibling();

            while (prev != null) {
                if (evaluator.matches(root, prev))
                    return true;

                prev = prev.previousElementSibling();
            }
            return false;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return String.format(":prev*%s", evaluator);
        }
    }

    /**
     * The Class ImmediatePreviousSibling.
     */
    static class ImmediatePreviousSibling extends StructuralEvaluator {
        
        /**
         * Instantiates a new immediate previous sibling.
         *
         * @param evaluator the evaluator
         */
        public ImmediatePreviousSibling(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        public boolean matches(Element root, Element element) {
            if (root == element)
                return false;

            Element prev = element.previousElementSibling();
            return prev != null && evaluator.matches(root, prev);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return String.format(":prev%s", evaluator);
        }
    }
}
