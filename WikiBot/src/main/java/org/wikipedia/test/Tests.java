package org.wikipedia.test;

import java.io.IOException;
import javax.security.auth.login.FailedLoginException;

import org.wikiutils.ParseUtils;


/**
 * The Class Tests.
 */
public class Tests {

    /**
     * Instantiates a new tests.
     */
    public Tests() {
    }

    /**
     * The main method.
     * 
     * @param args
     *            the arguments
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws FailedLoginException
     */
    public static void main(String[] args) throws IOException,
	    FailedLoginException {

	String template = "{{Date de naissance|16|8|1978|âge=oui}}";
	String param = ParseUtils.getTemplateParam(template, 1);
	System.out.println(param);

    }
}
