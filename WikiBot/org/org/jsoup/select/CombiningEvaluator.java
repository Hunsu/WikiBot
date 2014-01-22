package org.jsoup.select;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Base combining (and, or) evaluator.
 */
abstract class CombiningEvaluator extends Evaluator {
    
    /** The evaluators. */
    final ArrayList<Evaluator> evaluators;
    
    /** The num. */
    int num = 0;

    /**
     * Instantiates a new combining evaluator.
     */
    CombiningEvaluator() {
        super();
        evaluators = new ArrayList<Evaluator>();
    }

    /**
     * Instantiates a new combining evaluator.
     *
     * @param evaluators the evaluators
     */
    CombiningEvaluator(Collection<Evaluator> evaluators) {
        this();
        this.evaluators.addAll(evaluators);
        updateNumEvaluators();
    }

    /**
     * Right most evaluator.
     *
     * @return the evaluator
     */
    Evaluator rightMostEvaluator() {
        return num > 0 ? evaluators.get(num - 1) : null;
    }
    
    /**
     * Replace right most evaluator.
     *
     * @param replacement the replacement
     */
    void replaceRightMostEvaluator(Evaluator replacement) {
        evaluators.set(num - 1, replacement);
    }

    /**
     * Update num evaluators.
     */
    void updateNumEvaluators() {
        // used so we don't need to bash on size() for every match test
        num = evaluators.size();
    }

    /**
     * The Class And.
     */
    static final class And extends CombiningEvaluator {
        
        /**
         * Instantiates a new and.
         *
         * @param evaluators the evaluators
         */
        And(Collection<Evaluator> evaluators) {
            super(evaluators);
        }

        /**
         * Instantiates a new and.
         *
         * @param evaluators the evaluators
         */
        And(Evaluator... evaluators) {
            this(Arrays.asList(evaluators));
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element node) {
            for (int i = 0; i < num; i++) {
                Evaluator s = evaluators.get(i);
                if (!s.matches(root, node))
                    return false;
            }
            return true;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return StringUtil.join(evaluators, " ");
        }
    }

    /**
     * The Class Or.
     */
    static final class Or extends CombiningEvaluator {
        /**
         * Create a new Or evaluator. The initial evaluators are ANDed together and used as the first clause of the OR.
         * @param evaluators initial OR clause (these are wrapped into an AND evaluator).
         */
        Or(Collection<Evaluator> evaluators) {
            super();
            if (num > 1)
                this.evaluators.add(new And(evaluators));
            else // 0 or 1
                this.evaluators.addAll(evaluators);
            updateNumEvaluators();
        }

        /**
         * Instantiates a new or.
         */
        Or() {
            super();
        }

        /**
         * Adds the.
         *
         * @param e the e
         */
        public void add(Evaluator e) {
            evaluators.add(e);
            updateNumEvaluators();
        }

        /* (non-Javadoc)
         * @see org.jsoup.select.Evaluator#matches(org.jsoup.nodes.Element, org.jsoup.nodes.Element)
         */
        @Override
        public boolean matches(Element root, Element node) {
            for (int i = 0; i < num; i++) {
                Evaluator s = evaluators.get(i);
                if (s.matches(root, node))
                    return true;
            }
            return false;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format(":or%s", evaluators);
        }
    }
}
