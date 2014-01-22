package org.jsoup;

import java.io.IOException;

/**
 * Signals that a HTTP request resulted in a not OK HTTP response.
 */
public class HttpStatusException extends IOException {
    
    /** The status code. */
    private int statusCode;
    
    /** The url. */
    private String url;

    /**
     * Instantiates a new http status exception.
     *
     * @param message the message
     * @param statusCode the status code
     * @param url the url
     */
    public HttpStatusException(String message, int statusCode, String url) {
        super(message);
        this.statusCode = statusCode;
        this.url = url;
    }

    /**
     * Gets the status code.
     *
     * @return the status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Gets the url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /* (non-Javadoc)
     * @see java.lang.Throwable#toString()
     */
    @Override
    public String toString() {
        return super.toString() + ". Status=" + statusCode + ", URL=" + url;
    }
}
