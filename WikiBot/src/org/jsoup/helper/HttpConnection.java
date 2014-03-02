package org.jsoup.helper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.parser.TokenQueue;

/**
 * Implementation of {@link Connection}.
 * @see org.jsoup.Jsoup#connect(String) 
 */
public class HttpConnection implements Connection {
    
    /**
     * Connect.
     *
     * @param url the url
     * @return the connection
     */
    public static Connection connect(String url) {
        Connection con = new HttpConnection();
        con.url(url);
        return con;
    }

    /**
     * Connect.
     *
     * @param url the url
     * @return the connection
     */
    public static Connection connect(URL url) {
        Connection con = new HttpConnection();
        con.url(url);
        return con;
    }

	/**
	 * Encode url.
	 *
	 * @param url the url
	 * @return the string
	 */
	private static String encodeUrl(String url) {
		if(url == null)
			return null;
    	return url.replaceAll(" ", "%20");
	}

    /** The req. */
    private Connection.Request req;
    
    /** The res. */
    private Connection.Response res;

	/**
	 * Instantiates a new http connection.
	 */
	private HttpConnection() {
        req = new Request();
        res = new Response();
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#url(java.net.URL)
     */
    public Connection url(URL url) {
        req.url(url);
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#url(java.lang.String)
     */
    public Connection url(String url) {
        Validate.notEmpty(url, "Must supply a valid URL");
        try {
            req.url(new URL(encodeUrl(url)));
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Malformed URL: " + url, e);
        }
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#userAgent(java.lang.String)
     */
    public Connection userAgent(String userAgent) {
        Validate.notNull(userAgent, "User agent must not be null");
        req.header("User-Agent", userAgent);
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#timeout(int)
     */
    public Connection timeout(int millis) {
        req.timeout(millis);
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#maxBodySize(int)
     */
    public Connection maxBodySize(int bytes) {
        req.maxBodySize(bytes);
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#followRedirects(boolean)
     */
    public Connection followRedirects(boolean followRedirects) {
        req.followRedirects(followRedirects);
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#referrer(java.lang.String)
     */
    public Connection referrer(String referrer) {
        Validate.notNull(referrer, "Referrer must not be null");
        req.header("Referer", referrer);
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#method(org.jsoup.Connection.Method)
     */
    public Connection method(Method method) {
        req.method(method);
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#ignoreHttpErrors(boolean)
     */
    public Connection ignoreHttpErrors(boolean ignoreHttpErrors) {
		req.ignoreHttpErrors(ignoreHttpErrors);
		return this;
	}

    /* (non-Javadoc)
     * @see org.jsoup.Connection#ignoreContentType(boolean)
     */
    public Connection ignoreContentType(boolean ignoreContentType) {
        req.ignoreContentType(ignoreContentType);
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#data(java.lang.String, java.lang.String)
     */
    public Connection data(String key, String value) {
        req.data(KeyVal.create(key, value));
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#data(java.util.Map)
     */
    public Connection data(Map<String, String> data) {
        Validate.notNull(data, "Data map must not be null");
        for (Map.Entry<String, String> entry : data.entrySet()) {
            req.data(KeyVal.create(entry.getKey(), entry.getValue()));
        }
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#data(java.lang.String[])
     */
    public Connection data(String... keyvals) {
        Validate.notNull(keyvals, "Data key value pairs must not be null");
        Validate.isTrue(keyvals.length %2 == 0, "Must supply an even number of key value pairs");
        for (int i = 0; i < keyvals.length; i += 2) {
            String key = keyvals[i];
            String value = keyvals[i+1];
            Validate.notEmpty(key, "Data key must not be empty");
            Validate.notNull(value, "Data value must not be null");
            req.data(KeyVal.create(key, value));
        }
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#data(java.util.Collection)
     */
    public Connection data(Collection<Connection.KeyVal> data) {
        Validate.notNull(data, "Data collection must not be null");
        for (Connection.KeyVal entry: data) {
            req.data(entry);
        }
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#header(java.lang.String, java.lang.String)
     */
    public Connection header(String name, String value) {
        req.header(name, value);
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#cookie(java.lang.String, java.lang.String)
     */
    public Connection cookie(String name, String value) {
        req.cookie(name, value);
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#cookies(java.util.Map)
     */
    public Connection cookies(Map<String, String> cookies) {
        Validate.notNull(cookies, "Cookie map must not be null");
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            req.cookie(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#parser(org.jsoup.parser.Parser)
     */
    public Connection parser(Parser parser) {
        req.parser(parser);
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#get()
     */
    public Document get() throws IOException {
        req.method(Method.GET);
        execute();
        return res.parse();
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#post()
     */
    public Document post() throws IOException {
        req.method(Method.POST);
        execute();
        return res.parse();
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#execute()
     */
    public Connection.Response execute() throws IOException {
        res = Response.execute(req);
        return res;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#request()
     */
    public Connection.Request request() {
        return req;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#request(org.jsoup.Connection.Request)
     */
    public Connection request(Connection.Request request) {
        req = request;
        return this;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#response()
     */
    public Connection.Response response() {
        return res;
    }

    /* (non-Javadoc)
     * @see org.jsoup.Connection#response(org.jsoup.Connection.Response)
     */
    public Connection response(Connection.Response response) {
        res = response;
        return this;
    }

    /**
     * The Class Base.
     *
     * @param <T> the generic type
     */
    @SuppressWarnings({"unchecked"})
    private static abstract class Base<T extends Connection.Base> implements Connection.Base<T> {
        
        /** The url. */
        URL url;
        
        /** The method. */
        Method method;
        
        /** The headers. */
        Map<String, String> headers;
        
        /** The cookies. */
        Map<String, String> cookies;

        /**
         * Instantiates a new base.
         */
        private Base() {
            headers = new LinkedHashMap<String, String>();
            cookies = new LinkedHashMap<String, String>();
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Base#url()
         */
        public URL url() {
            return url;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Base#url(java.net.URL)
         */
        public T url(URL url) {
            Validate.notNull(url, "URL must not be null");
            this.url = url;
            return (T) this;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Base#method()
         */
        public Method method() {
            return method;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Base#method(org.jsoup.Connection.Method)
         */
        public T method(Method method) {
            Validate.notNull(method, "Method must not be null");
            this.method = method;
            return (T) this;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Base#header(java.lang.String)
         */
        public String header(String name) {
            Validate.notNull(name, "Header name must not be null");
            return getHeaderCaseInsensitive(name);
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Base#header(java.lang.String, java.lang.String)
         */
        public T header(String name, String value) {
            Validate.notEmpty(name, "Header name must not be empty");
            Validate.notNull(value, "Header value must not be null");
            removeHeader(name); // ensures we don't get an "accept-encoding" and a "Accept-Encoding"
            headers.put(name, value);
            return (T) this;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Base#hasHeader(java.lang.String)
         */
        public boolean hasHeader(String name) {
            Validate.notEmpty(name, "Header name must not be empty");
            return getHeaderCaseInsensitive(name) != null;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Base#removeHeader(java.lang.String)
         */
        public T removeHeader(String name) {
            Validate.notEmpty(name, "Header name must not be empty");
            Map.Entry<String, String> entry = scanHeaders(name); // remove is case insensitive too
            if (entry != null)
                headers.remove(entry.getKey()); // ensures correct case
            return (T) this;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Base#headers()
         */
        public Map<String, String> headers() {
            return headers;
        }

        /**
         * Gets the header case insensitive.
         *
         * @param name the name
         * @return the header case insensitive
         */
        private String getHeaderCaseInsensitive(String name) {
            Validate.notNull(name, "Header name must not be null");
            // quick evals for common case of title case, lower case, then scan for mixed
            String value = headers.get(name);
            if (value == null)
                value = headers.get(name.toLowerCase());
            if (value == null) {
                Map.Entry<String, String> entry = scanHeaders(name);
                if (entry != null)
                    value = entry.getValue();
            }
            return value;
        }

        /**
         * Scan headers.
         *
         * @param name the name
         * @return the map. entry
         */
        private Map.Entry<String, String> scanHeaders(String name) {
            String lc = name.toLowerCase();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().toLowerCase().equals(lc))
                    return entry;
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Base#cookie(java.lang.String)
         */
        public String cookie(String name) {
            Validate.notNull(name, "Cookie name must not be null");
            return cookies.get(name);
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Base#cookie(java.lang.String, java.lang.String)
         */
        public T cookie(String name, String value) {
            Validate.notEmpty(name, "Cookie name must not be empty");
            Validate.notNull(value, "Cookie value must not be null");
            cookies.put(name, value);
            return (T) this;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Base#hasCookie(java.lang.String)
         */
        public boolean hasCookie(String name) {
            Validate.notEmpty("Cookie name must not be empty");
            return cookies.containsKey(name);
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Base#removeCookie(java.lang.String)
         */
        public T removeCookie(String name) {
            Validate.notEmpty("Cookie name must not be empty");
            cookies.remove(name);
            return (T) this;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Base#cookies()
         */
        public Map<String, String> cookies() {
            return cookies;
        }
    }

    /**
     * The Class Request.
     */
    public static class Request extends Base<Connection.Request> implements Connection.Request {
        
        /** The timeout milliseconds. */
        private int timeoutMilliseconds;
        
        /** The max body size bytes. */
        private int maxBodySizeBytes;
        
        /** The follow redirects. */
        private boolean followRedirects;
        
        /** The data. */
        private Collection<Connection.KeyVal> data;
        
        /** The ignore http errors. */
        private boolean ignoreHttpErrors = false;
        
        /** The ignore content type. */
        private boolean ignoreContentType = false;
        
        /** The parser. */
        private Parser parser;

      	/**
	       * Instantiates a new request.
	       */
	      private Request() {
            timeoutMilliseconds = 3000;
            maxBodySizeBytes = 1024 * 1024; // 1MB
            followRedirects = true;
            data = new ArrayList<Connection.KeyVal>();
            method = Connection.Method.GET;
            headers.put("Accept-Encoding", "gzip");
            parser = Parser.htmlParser();
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Request#timeout()
         */
        public int timeout() {
            return timeoutMilliseconds;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Request#timeout(int)
         */
        public Request timeout(int millis) {
            Validate.isTrue(millis >= 0, "Timeout milliseconds must be 0 (infinite) or greater");
            timeoutMilliseconds = millis;
            return this;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Request#maxBodySize()
         */
        public int maxBodySize() {
            return maxBodySizeBytes;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Request#maxBodySize(int)
         */
        public Connection.Request maxBodySize(int bytes) {
            Validate.isTrue(bytes >= 0, "maxSize must be 0 (unlimited) or larger");
            maxBodySizeBytes = bytes;
            return this;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Request#followRedirects()
         */
        public boolean followRedirects() {
            return followRedirects;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Request#followRedirects(boolean)
         */
        public Connection.Request followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Request#ignoreHttpErrors()
         */
        public boolean ignoreHttpErrors() {
            return ignoreHttpErrors;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Request#ignoreHttpErrors(boolean)
         */
        public Connection.Request ignoreHttpErrors(boolean ignoreHttpErrors) {
            this.ignoreHttpErrors = ignoreHttpErrors;
            return this;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Request#ignoreContentType()
         */
        public boolean ignoreContentType() {
            return ignoreContentType;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Request#ignoreContentType(boolean)
         */
        public Connection.Request ignoreContentType(boolean ignoreContentType) {
            this.ignoreContentType = ignoreContentType;
            return this;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Request#data(org.jsoup.Connection.KeyVal)
         */
        public Request data(Connection.KeyVal keyval) {
            Validate.notNull(keyval, "Key val must not be null");
            data.add(keyval);
            return this;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Request#data()
         */
        public Collection<Connection.KeyVal> data() {
            return data;
        }
        
        /* (non-Javadoc)
         * @see org.jsoup.Connection.Request#parser(org.jsoup.parser.Parser)
         */
        public Request parser(Parser parser) {
            this.parser = parser;
            return this;
        }
        
        /* (non-Javadoc)
         * @see org.jsoup.Connection.Request#parser()
         */
        public Parser parser() {
            return parser;
        }
    }

    /**
     * The Class Response.
     */
    public static class Response extends Base<Connection.Response> implements Connection.Response {
        
        /** The Constant MAX_REDIRECTS. */
        private static final int MAX_REDIRECTS = 20;
        
        /** The status code. */
        private int statusCode;
        
        /** The status message. */
        private String statusMessage;
        
        /** The byte data. */
        private ByteBuffer byteData;
        
        /** The charset. */
        private String charset;
        
        /** The content type. */
        private String contentType;
        
        /** The executed. */
        private boolean executed = false;
        
        /** The num redirects. */
        private int numRedirects = 0;
        
        /** The req. */
        private Connection.Request req;

        /**
         * Instantiates a new response.
         */
        Response() {
            super();
        }

        /**
         * Instantiates a new response.
         *
         * @param previousResponse the previous response
         * @throws IOException Signals that an I/O exception has occurred.
         */
        private Response(Response previousResponse) throws IOException {
            super();
            if (previousResponse != null) {
                numRedirects = previousResponse.numRedirects + 1;
                if (numRedirects >= MAX_REDIRECTS)
                    throw new IOException(String.format("Too many redirects occurred trying to load URL %s", previousResponse.url()));
            }
        }
        
        /**
         * Execute.
         *
         * @param req the req
         * @return the response
         * @throws IOException Signals that an I/O exception has occurred.
         */
        static Response execute(Connection.Request req) throws IOException {
            return execute(req, null);
        }

        /**
         * Execute.
         *
         * @param req the req
         * @param previousResponse the previous response
         * @return the response
         * @throws IOException Signals that an I/O exception has occurred.
         */
        static Response execute(Connection.Request req, Response previousResponse) throws IOException {
            Validate.notNull(req, "Request must not be null");
            String protocol = req.url().getProtocol();
            if (!protocol.equals("http") && !protocol.equals("https"))
                throw new MalformedURLException("Only http & https protocols supported");

            // set up the request for execution
            if (req.method() == Connection.Method.GET && req.data().size() > 0)
                serialiseRequestUrl(req); // appends query string
            HttpURLConnection conn = createConnection(req);
            Response res;
            try {
                conn.connect();
                if (req.method() == Connection.Method.POST)
                    writePost(req.data(), conn.getOutputStream());

                int status = conn.getResponseCode();
                boolean needsRedirect = false;
                if (status != HttpURLConnection.HTTP_OK) {
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER)
                        needsRedirect = true;
                    else if (!req.ignoreHttpErrors())
                        throw new HttpStatusException("HTTP error fetching URL", status, req.url().toString());
                }
                res = new Response(previousResponse);
                res.setupFromConnection(conn, previousResponse);
                if (needsRedirect && req.followRedirects()) {
                    req.method(Method.GET); // always redirect with a get. any data param from original req are dropped.
                    req.data().clear();

                    String location = res.header("Location");
                    if (location != null && location.startsWith("http:/") && location.charAt(6) != '/') // fix broken Location: http:/temp/AAG_New/en/index.php
                        location = location.substring(6);
                    req.url(new URL(req.url(), encodeUrl(location)));

                    for (Map.Entry<String, String> cookie : res.cookies.entrySet()) { // add response cookies to request (for e.g. login posts)
                        req.cookie(cookie.getKey(), cookie.getValue());
                    }
                    return execute(req, res);
                }
                res.req = req;

                // check that we can handle the returned content type; if not, abort before fetching it
                String contentType = res.contentType();
                if (contentType != null && !req.ignoreContentType() && (!(contentType.startsWith("text/") || contentType.startsWith("application/xml") || contentType.startsWith("application/xhtml+xml"))))
                    throw new UnsupportedMimeTypeException("Unhandled content type. Must be text/*, application/xml, or application/xhtml+xml",
                            contentType, req.url().toString());

                InputStream bodyStream = null;
                InputStream dataStream = null;
                try {
                    dataStream = conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream();
                    bodyStream = res.hasHeader("Content-Encoding") && res.header("Content-Encoding").equalsIgnoreCase("gzip") ?
                            new BufferedInputStream(new GZIPInputStream(dataStream)) :
                            new BufferedInputStream(dataStream);

                    res.byteData = DataUtil.readToByteBuffer(bodyStream, req.maxBodySize());
                    res.charset = DataUtil.getCharsetFromContentType(res.contentType); // may be null, readInputStream deals with it
                } finally {
                    if (bodyStream != null) bodyStream.close();
                    if (dataStream != null) dataStream.close();
                }
            } finally {
                // per Java's documentation, this is not necessary, and precludes keepalives. However in practise,
                // connection errors will not be released quickly enough and can cause a too many open files error.
                conn.disconnect();
            }

            res.executed = true;
            return res;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Response#statusCode()
         */
        public int statusCode() {
            return statusCode;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Response#statusMessage()
         */
        public String statusMessage() {
            return statusMessage;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Response#charset()
         */
        public String charset() {
            return charset;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Response#contentType()
         */
        public String contentType() {
            return contentType;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Response#parse()
         */
        public Document parse() throws IOException {
            Validate.isTrue(executed, "Request must be executed (with .execute(), .get(), or .post() before parsing response");
            Document doc = DataUtil.parseByteData(byteData, charset, url.toExternalForm(), req.parser());
            byteData.rewind();
            charset = doc.outputSettings().charset().name(); // update charset from meta-equiv, possibly
            return doc;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Response#body()
         */
        public String body() {
            Validate.isTrue(executed, "Request must be executed (with .execute(), .get(), or .post() before getting response body");
            // charset gets set from header on execute, and from meta-equiv on parse. parse may not have happened yet
            String body;
            if (charset == null)
                body = Charset.forName(DataUtil.defaultCharset).decode(byteData).toString();
            else
                body = Charset.forName(charset).decode(byteData).toString();
            byteData.rewind();
            return body;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.Response#bodyAsBytes()
         */
        public byte[] bodyAsBytes() {
            Validate.isTrue(executed, "Request must be executed (with .execute(), .get(), or .post() before getting response body");
            return byteData.array();
        }

        // set up connection defaults, and details from request
        /**
         * Creates the connection.
         *
         * @param req the req
         * @return the http url connection
         * @throws IOException Signals that an I/O exception has occurred.
         */
        private static HttpURLConnection createConnection(Connection.Request req) throws IOException {
            HttpURLConnection conn = (HttpURLConnection) req.url().openConnection();
            conn.setRequestMethod(req.method().name());
            conn.setInstanceFollowRedirects(false); // don't rely on native redirection support
            conn.setConnectTimeout(req.timeout());
            conn.setReadTimeout(req.timeout());
            if (req.method() == Method.POST)
                conn.setDoOutput(true);
            if (req.cookies().size() > 0)
                conn.addRequestProperty("Cookie", getRequestCookieString(req));
            for (Map.Entry<String, String> header : req.headers().entrySet()) {
                conn.addRequestProperty(header.getKey(), header.getValue());
            }
            return conn;
        }

        // set up url, method, header, cookies
        /**
         * Setup from connection.
         *
         * @param conn the conn
         * @param previousResponse the previous response
         * @throws IOException Signals that an I/O exception has occurred.
         */
        private void setupFromConnection(HttpURLConnection conn, Connection.Response previousResponse) throws IOException {
            method = Connection.Method.valueOf(conn.getRequestMethod());
            url = conn.getURL();
            statusCode = conn.getResponseCode();
            statusMessage = conn.getResponseMessage();
            contentType = conn.getContentType();

            Map<String, List<String>> resHeaders = conn.getHeaderFields();
            processResponseHeaders(resHeaders);

            // if from a redirect, map previous response cookies into this response
            if (previousResponse != null) {
                for (Map.Entry<String, String> prevCookie : previousResponse.cookies().entrySet()) {
                    if (!hasCookie(prevCookie.getKey()))
                        cookie(prevCookie.getKey(), prevCookie.getValue());
                }
            }
        }

        /**
         * Process response headers.
         *
         * @param resHeaders the res headers
         */
        void processResponseHeaders(Map<String, List<String>> resHeaders) {
            for (Map.Entry<String, List<String>> entry : resHeaders.entrySet()) {
                String name = entry.getKey();
                if (name == null)
                    continue; // http/1.1 line

                List<String> values = entry.getValue();
                if (name.equalsIgnoreCase("Set-Cookie")) {
                    for (String value : values) {
                        if (value == null)
                            continue;
                        TokenQueue cd = new TokenQueue(value);
                        String cookieName = cd.chompTo("=").trim();
                        String cookieVal = cd.consumeTo(";").trim();
                        if (cookieVal == null)
                            cookieVal = "";
                        // ignores path, date, domain, secure et al. req'd?
                        // name not blank, value not null
                        if (cookieName != null && cookieName.length() > 0)
                            cookie(cookieName, cookieVal);
                    }
                } else { // only take the first instance of each header
                    if (!values.isEmpty())
                        header(name, values.get(0));
                }
            }
        }

        /**
         * Write post.
         *
         * @param data the data
         * @param outputStream the output stream
         * @throws IOException Signals that an I/O exception has occurred.
         */
        private static void writePost(Collection<Connection.KeyVal> data, OutputStream outputStream) throws IOException {
            OutputStreamWriter w = new OutputStreamWriter(outputStream, DataUtil.defaultCharset);
            boolean first = true;
            for (Connection.KeyVal keyVal : data) {
                if (!first) 
                    w.append('&');
                else
                    first = false;
                
                w.write(URLEncoder.encode(keyVal.key(), DataUtil.defaultCharset));
                w.write('=');
                w.write(URLEncoder.encode(keyVal.value(), DataUtil.defaultCharset));
            }
            w.close();
        }
        
        /**
         * Gets the request cookie string.
         *
         * @param req the req
         * @return the request cookie string
         */
        private static String getRequestCookieString(Connection.Request req) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> cookie : req.cookies().entrySet()) {
                if (!first)
                    sb.append("; ");
                else
                    first = false;
                sb.append(cookie.getKey()).append('=').append(cookie.getValue());
                // todo: spec says only ascii, no escaping / encoding defined. validate on set? or escape somehow here?
            }
            return sb.toString();
        }

        // for get url reqs, serialise the data map into the url
        /**
         * Serialise request url.
         *
         * @param req the req
         * @throws IOException Signals that an I/O exception has occurred.
         */
        private static void serialiseRequestUrl(Connection.Request req) throws IOException {
            URL in = req.url();
            StringBuilder url = new StringBuilder();
            boolean first = true;
            // reconstitute the query, ready for appends
            url
                .append(in.getProtocol())
                .append("://")
                .append(in.getAuthority()) // includes host, port
                .append(in.getPath())
                .append("?");
            if (in.getQuery() != null) {
                url.append(in.getQuery());
                first = false;
            }
            for (Connection.KeyVal keyVal : req.data()) {
                if (!first)
                    url.append('&');
                else
                    first = false;
                url
                    .append(URLEncoder.encode(keyVal.key(), DataUtil.defaultCharset))
                    .append('=')
                    .append(URLEncoder.encode(keyVal.value(), DataUtil.defaultCharset));
            }
            req.url(new URL(url.toString()));
            req.data().clear(); // moved into url as get params
        }
    }

    /**
     * The Class KeyVal.
     */
    public static class KeyVal implements Connection.KeyVal {
        
        /** The key. */
        private String key;
        
        /** The value. */
        private String value;

        /**
         * Creates the.
         *
         * @param key the key
         * @param value the value
         * @return the key val
         */
        public static KeyVal create(String key, String value) {
            Validate.notEmpty(key, "Data key must not be empty");
            Validate.notNull(value, "Data value must not be null");
            return new KeyVal(key, value);
        }

        /**
         * Instantiates a new key val.
         *
         * @param key the key
         * @param value the value
         */
        private KeyVal(String key, String value) {
            this.key = key;
            this.value = value;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.KeyVal#key(java.lang.String)
         */
        public KeyVal key(String key) {
            Validate.notEmpty(key, "Data key must not be empty");
            this.key = key;
            return this;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.KeyVal#key()
         */
        public String key() {
            return key;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.KeyVal#value(java.lang.String)
         */
        public KeyVal value(String value) {
            Validate.notNull(value, "Data value must not be null");
            this.value = value;
            return this;
        }

        /* (non-Javadoc)
         * @see org.jsoup.Connection.KeyVal#value()
         */
        public String value() {
            return value;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return key + "=" + value;
        }      
    }
}
