package org.jsoup;

import java.io.IOException;

/**
 * Signals that a HTTP response returned a mime type that is not supported.
 */
public class UnsupportedMimeTypeException extends IOException {
    
    /** The mime type. */
    private String mimeType;
    
    /** The url. */
    private String url;

    /**
     * Instantiates a new unsupported mime type exception.
     *
     * @param message the message
     * @param mimeType the mime type
     * @param url the url
     */
    public UnsupportedMimeTypeException(String message, String mimeType, String url) {
        super(message);
        this.mimeType = mimeType;
        this.url = url;
    }

    /**
     * Gets the mime type.
     *
     * @return the mime type
     */
    public String getMimeType() {
        return mimeType;
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
        return super.toString() + ". Mimetype=" + mimeType + ", URL="+url;
    }
}
