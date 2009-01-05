/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.connector;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.Base64;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Remote client for for accessing data from remote URLs.
 * <p>
 * Can be used as a Script root object for simple HTTP requests.
 * <p>
 * Generally remote URLs will be "data" webscripts (i.e. returning XML/JSON) called from
 * web-tier script objects and will be housed within an Alfresco Repository server.
 * <p>
 * Supports HTTP methods of GET, DELETE, PUT and POST of body content data.
 * <p>
 * A 'Response' is returned containing the response data stream as a String and the Status
 * object representing the status code and error information if any. Methods supplying an
 * InputStream will force a POST and methods supplying an OutputStream will stream the result
 * directly to it and not generate a response in the 'Response' object. 
 * 
 * @author Kevin Roast
 */
public class RemoteClient extends AbstractClient
{
    private static Log logger = LogFactory.getLog(RemoteClient.class);
    
    private static final String CHARSETEQUALS = "charset=";
    private static final int BUFFERSIZE = 4096;
    
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 60000;
    
    private String defaultEncoding;
    private String ticket;
    private String ticketName = "alf_ticket";
    private String requestContentType = "application/octet-stream";
    private HttpMethod requestMethod = HttpMethod.GET;
    
    private String username;
    private String password;
    
    private Map<String, String> requestProperties;
    
    // special http error codes used internally to detect specific error
    public static final int SC_REMOTE_CONN_TIMEOUT = 499;
    public static final int SC_REMOTE_CONN_NOHOST  = 498;


    /**
     * Construction
     * 
     * @param endpoint         HTTP API endpoint of remote Alfresco server webapp
     *                         For example http://servername:8080/alfresco
     */
    public RemoteClient(String endpoint)
    {
        this(endpoint, null);
    }

    /**
     * Construction
     * 
     * @param endpoint         HTTP API endpoint of remote Alfresco server webapp
     *                         For example http://servername:8080/alfresco
     * @param defaultEncoding  Encoding to use when converting responses that do not specify one
     */
    public RemoteClient(String endpoint, String defaultEncoding)
    {
        super(endpoint);
        this.defaultEncoding = defaultEncoding;
    }

    /**
     * Sets the authentication ticket to use. Will be used for all future call() requests.
     * 
     * @param ticket
     */
    public void setTicket(String ticket)
    {
        this.ticket = ticket;
    }
    
    /**
     * Returns the authentication ticket
     * 
     * @return
     */
    public String getTicket()
    {
        return this.ticket;
    }

    /**
     * Sets the authentication ticket name to use.  Will be used for all future call() requests.
     * 
     * This allows the ticket mechanism to be repurposed for non-Alfresco
     * implementations that may require similar argument passing
     * 
     * @param ticket
     */
    public void setTicketName(String ticketName)
    {
        this.ticketName = ticketName;
    }
    
    /**
     * @return the authentication ticket name to use
     */
    public String getTicketName()
    {
        return this.ticketName;
    }
    
    /**
     * Basic HTTP auth. Will be used for all future call() requests.
     * 
     * @param user
     * @param pass
     */
    public void setUsernamePassword(String user, String pass)
    {
        this.username = user;
        this.password = pass;
    }

    /**
     * @param requestContentType     the POST request "Content-Type" header value to set
     *        NOTE: this value is reset to the default of GET after a call() is made. 
     */
    public void setRequestContentType(String contentType)
    {
        if (requestContentType != null && requestContentType.length() != 0)
        {
            this.requestContentType = contentType;
        }
    }

    /**
     * @param requestMethod  the request Method to set i.e. one of GET/POST/PUT/DELETE etc.
     *        if not set, GET will be assumed unless an InputStream is supplied during call()
     *        in which case POST will be used unless the request method overrides it with PUT.
     *        NOTE: this value is reset to the default of GET after a call() is made. 
     */
    public void setRequestMethod(HttpMethod method)
    {
        if (method != null)
        {
            this.requestMethod = method;
        }
    }
    
    /**
     * Allows for additional request properties to be set onto this object
     * These request properties are applied to the connection when
     * the connection is called. Will be used for all future call() requests.
     * 
     * @param requestProperties
     */
    public void setRequestProperties(Map<String, String> requestProperties)
    {
        this.requestProperties = requestProperties;
    }    

    /**
     * Call a remote WebScript uri. The endpoint as supplied in the constructor will be used
     * as the prefix for the full WebScript url.
     * 
     * This API is generally called from a script host.
     * 
     * @param uri     WebScript URI - for example /test/myscript?arg=value
     * 
     * @return Response object from the call {@link Response}
     */
    public Response call(String uri)
    {
        return call(uri, true, null);
    }
    
    /**
     * Call a remote WebScript uri, passing the supplied body as a POST request (unless the
     * request method is set to override as say PUT).
     * 
     * @param uri    Uri to call on the endpoint
     * @param body   Body of the POST request.
     * 
     * @return Response object from the call {@link Response}
     */
    public Response call(String uri, String body)
    {
        try
        {
            byte[] bytes = body.getBytes("UTF-8");
            return call(uri, true, new ByteArrayInputStream(bytes));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new AlfrescoRuntimeException("Encoding not supported.", e);
        }
    }

    /**
     * Call a remote WebScript uri. The endpoint as supplied in the constructor will be used
     * as the prefix for the full WebScript url.
     * 
     * @param uri    WebScript URI - for example /test/myscript?arg=value
     * @param in     The optional InputStream to the call - if supplied a POST will be performed
     * 
     * @return Response object from the call {@link Response}
     */
    public Response call(String uri, InputStream in)
    {
        return call(uri, true, in);
    }

    /**
     * Call a remote WebScript uri. The endpoint as supplied in the constructor will be used
     * as the prefix for the full WebScript url.
     * 
     * @param uri    WebScript URI - for example /test/myscript?arg=value
     * @param buildResponseString   True to build a String result automatically based on the response
     *                              encoding, false to instead return the InputStream in the Response.
     * @param in     The optional InputStream to the call - if supplied a POST will be performed
     * 
     * @return Response object from the call {@link Response}
     */
    public Response call(String uri, boolean buildResponseString, InputStream in)
    {
        if (in != null)
        {
            // we have been supplied an input for the request - either POST or PUT
            if (this.requestMethod != HttpMethod.POST && this.requestMethod != HttpMethod.PUT)
            {
                this.requestMethod = HttpMethod.POST;
            }
        }
        
        Response result;
        ResponseStatus status = new ResponseStatus();
        try
        {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream(BUFFERSIZE);
            String encoding = service(buildURL(uri), in, bOut, status);
            if (buildResponseString)
            {
                String data;
                if (encoding != null)
                {
                    data = bOut.toString(encoding);
                }
                else
                {
                    data = (defaultEncoding != null ? bOut.toString(defaultEncoding) : bOut.toString());
                }
                result = new Response(data, status);
            }
            else
            {
                result = new Response(new ByteArrayInputStream(bOut.toByteArray()), status);
            }
            result.setEncoding(encoding);
        }
        catch (IOException ioErr)
        {
            if (logger.isDebugEnabled())
                logger.debug("Error status " + status.getCode() + " " + status.getMessage());

            // error information already applied to Status object during service() call
            result = new Response(status);
        }

        return result;
    }

    /**
     * Call a remote WebScript uri. The endpoint as supplied in the constructor will be used
     * as the prefix for the full WebScript url.
     * 
     * @param uri    WebScript URI - for example /test/myscript?arg=value
     * @param out    OutputStream to stream successful response to - will be closed automatically.
     *               A response data string will not therefore be available in the Response object.
     *               If remote call fails the OutputStream will not be modified or closed.
     * 
     * @return Response object from the call {@link Response}
     */
    public Response call(String uri, OutputStream out)
    {
        return call(uri, null, out);
    }

    /**
     * Call a remote WebScript uri. The endpoint as supplied in the constructor will be used
     * as the prefix for the full WebScript url.
     * 
     * @param uri    WebScript URI - for example /test/myscript?arg=value
     * @param in     The optional InputStream to the call - if supplied a POST will be performed
     * @param out    OutputStream to stream response to - will be closed automatically.
     *               A response data string will not therefore be available in the Response object.
     *               If remote call returns a status code then any available error response will be
     *               streamed into the output.
     *               If remote call fails completely the OutputStream will not be modified or closed.
     * 
     * @return Response object from the call {@link Response}
     */
    public Response call(String uri, InputStream in, OutputStream out)
    {
        if (in != null)
        {
            // we have been supplied an input for the request - either POST or PUT
            if (this.requestMethod != HttpMethod.POST && this.requestMethod != HttpMethod.PUT)
            {
                this.requestMethod = HttpMethod.POST;
            }
        }
        
        Response result;
        ResponseStatus status = new ResponseStatus();
        try
        {
            String encoding = service(buildURL(uri), in, out, status);
            result = new Response(status);
            result.setEncoding(encoding);
        }
        catch (IOException ioErr)
        {
            if (logger.isDebugEnabled())
                logger.debug("Error status " + status.getCode() + " " + status.getMessage());

            // error information already applied to Status object during service() call
            result = new Response(status);
        }

        return result;
    }

    /**
     * Call a remote WebScript uri. The endpoint as supplied in the constructor will be used
     * as the prefix for the full WebScript url.
     * 
     * @param uri    WebScript URI - for example /test/myscript?arg=value
     * @param req    HttpServletRequest the request to retrieve input and headers etc. from
     * @param res    HttpServletResponse the response to stream response to - will be closed automatically.
     *               A response data string will not therefore be available in the Response object.
     *               The HTTP method to be used should be set via the setter otherwise GET will be assumed
     *               and the InputStream will not be retrieve from the request.
     *               If remote call returns a status code then any available error response will be
     *               streamed into the response object. 
     *               If remote call fails completely the OutputStream will not be modified or closed.
     * 
     * @return Response object from the call {@link Response}
     */
    public Response call(String uri, HttpServletRequest req, HttpServletResponse res)
    {
        Response result;
        ResponseStatus status = new ResponseStatus();
        try
        {
            boolean isPush = (requestMethod == HttpMethod.POST || requestMethod == HttpMethod.PUT);
            String encoding = service(
                    buildURL(uri),
                    isPush ? req.getInputStream() : null,
                    res != null ? res.getOutputStream() : null,
                    req, res, status);
            result = new Response(status);
            result.setEncoding(encoding);
        }
        catch (IOException ioErr)
        {
            if (logger.isDebugEnabled())
                logger.debug("Error status " + status.getCode() + " " + status.getMessage());

            // error information already applied to Status object during service() call
            result = new Response(status);
        }
        
        return result;
    }
    
    /**
     * Build the URL object based on the supplied uri and configured endpoint. Ticket
     * will be appiled as an argument if available.
     * 
     * @param uri     URI to build URL against
     * 
     * @return the URL object representing the call.
     * 
     * @throws MalformedURLException
     */
    private URL buildURL(String uri) throws MalformedURLException
    {
        URL url;
        // TODO: DC - check support for abs urls
        String resolvedUri = uri.startsWith(endpoint) ? uri : endpoint + uri;
        if (getTicket() == null)
        {
            url = new URL(resolvedUri);
        }
        else
        {
            url = new URL(resolvedUri +
                    (uri.lastIndexOf('?') == -1 ? ("?"+getTicketName()+"="+getTicket()) : ("&"+getTicketName()+"="+getTicket())));
        }
        return url;
    }

    /**
     * Service a remote URL and write the the result into an output stream.
     * If an InputStream is provided then a POST will be performed with the content
     * pushed to the url. Otherwise a standard GET will be performed.
     * 
     * @param url    The URL to open and retrieve data from
     * @param in     The optional InputStream - if set a POST will be performed
     * @param out    The OutputStream to write result to
     * @param status The status object to apply the response code too
     * 
     * @return encoding specified by the source URL - may be null
     * 
     * @throws IOException
     */
    private String service(URL url, InputStream in, OutputStream out, ResponseStatus status)
        throws IOException
    {
        return service(url, in, out, null, null, status);
    }

    /**
     * Service a remote URL and write the the result into an output stream.
     * If an InputStream is provided then a POST will be performed with the content
     * pushed to the url. Otherwise a standard GET will be performed.
     * 
     * @param url    The URL to open and retrieve data from
     * @param in     The optional InputStream - if set a POST or similar will be performed
     * @param out    The OutputStream to write result to
     * @param res    Optional HttpServletResponse - to which response headers will be copied - i.e. proxied
     * @param status The status object to apply the response code too
     * 
     * @return encoding specified by the source URL - may be null
     * 
     * @throws IOException
     */
    private String service(URL url, InputStream in, OutputStream out,
            HttpServletRequest req, HttpServletResponse res, ResponseStatus status)
        throws IOException
    {
        final boolean trace = logger.isTraceEnabled();
        final boolean debug = logger.isDebugEnabled();
        if (debug)
        {
            logger.debug("Executing " + "(" + requestMethod + ") " + url.toString());
            if (in != null)  logger.debug(" - InputStream supplied - will push...");
            if (out != null) logger.debug(" - OutputStream supplied - will stream response...");
            if (req != null && res != null) logger.debug(" - Full Proxy mode between servlet request and response...");
        }
        
        HttpClient client = new HttpClient();
        
        final HttpClientParams params = client.getParams();
        params.setBooleanParameter("http.connection.stalecheck", false);
        params.setBooleanParameter("http.tcp.nodelay", true);
        if (!debug)
        {
            params.setIntParameter("http.connection.timeout", CONNECT_TIMEOUT);
            params.setIntParameter("http.socket.timeout", READ_TIMEOUT);
        }
        
        final org.apache.commons.httpclient.HttpMethod method;
        switch (this.requestMethod)
        {
            default:
            case GET:
                method = new GetMethod(url.toString());
                break;
            case PUT:
                method = new PutMethod(url.toString());
                break;
            case POST:
                method = new PostMethod(url.toString());
                break;
            case DELETE:
                method = new DeleteMethod(url.toString());
                break;
            case HEAD:
                method = new HeadMethod(url.toString());
                break;
        }
        
        try
        {
            // proxy over any headers from the request stream to proxied request
            if (req != null)
            {
                Enumeration<String> headers = req.getHeaderNames();
                while (headers.hasMoreElements())
                {
                    String key = headers.nextElement();
                    if (key != null)
                    {
                        method.setRequestHeader(key, req.getHeader(key));
                        if (trace) logger.trace("Proxy request header: " + key + "=" + req.getHeader(key));
                    }
                }
            }
            
            // apply request properties, allows for the assignment of specific header properties
            if (this.requestProperties != null && this.requestProperties.size() != 0)
            {
                for (Map.Entry<String, String> entry : requestProperties.entrySet())
                {
                    String headerName = entry.getKey();
                    String headerValue = this.requestProperties.get(headerName);
                    method.addRequestHeader(headerName, headerValue);
                    if (trace) logger.trace("Added request header: " + headerName + "=" + headerValue);
                }
            }
            
            // HTTP basic auth support
            if (this.username != null && this.password != null)
            {
                String auth = this.username + ':' + this.password;
                method.addRequestHeader("Authorization", "Basic " + Base64.encodeBytes(auth.getBytes()));
                if (debug) logger.debug("Applied HTTP Basic Authorization");
            }
            
            // prepare the POST/PUT entity data if input supplied
            if (in != null)
            {
                method.setRequestHeader("Content-Type", this.requestContentType);
                
                // apply content-length here if known (i.e. from proxied req)
                // if this is not set, then the content will be buffered in memory
                int contentLength = InputStreamRequestEntity.CONTENT_LENGTH_AUTO;
                if (req != null)
                {
                    contentLength = req.getContentLength();
                }
                
                if (debug) logger.debug("Setting content-type=" + this.requestContentType + " content-length=" + contentLength);
                
                ((EntityEnclosingMethod)method).setRequestEntity(new InputStreamRequestEntity(in, contentLength));
            }
            
            // execute the method to get the response code and proxy over if required
            final int responseCode = client.executeMethod(method);
            if (res != null)
            {
                res.setStatus(responseCode);
            }
            status.setCode(responseCode);
            if (debug) logger.debug("Response status code: " + responseCode);
            
            // walk over headers that are returned from the connection
            // if we have a servlet response, proxy the headers back to the response
            // otherwise, store headers on status
            Header contentType = null;
            Header contentLength = null;
            for (Header header : method.getResponseHeaders())
            {
                // NOTE: Tomcat does not appear to be obeying the servlet spec here.
                //       If you call setHeader() the spec says it will "clear existing values" - i.e. not
                //       add additional values to existing headers - but for Server and Transfer-Encoding
                //       if we set them, then two values are received in the response...
                // In addition handle the fact that the key can be null.
                final String key = header.getName();
                if (key != null)
                {
                    if (!key.equalsIgnoreCase("Server") && !key.equalsIgnoreCase("Transfer-Encoding"))
                    {
                        if (res != null)
                        {
                            res.setHeader(key, header.getValue());
                        }
                        
                        // store headers back onto status
                        status.setHeader(key, header.getValue());
                        
                        if (trace) logger.trace("Response header: " + key + "=" + header.getValue()); 
                    }
                    
                    // grab a reference to the the content-type header here if we find it
                    if (contentType == null && key.equalsIgnoreCase("Content-Type"))
                    {
                        contentType = header;
                    }
                    // grab a reference to the content-length header here if we find it
                    else if (contentLength == null && key.equalsIgnoreCase("Content-Length"))
                    {
                        contentLength = header;
                    }
                }
            }
            
            // locate response encoding from the headers
            String encoding = null;
            String ct = null;
            if (contentType != null)
            {
                ct = contentType.getValue();
                int csi = ct.indexOf(CHARSETEQUALS);
                if (csi != -1)
                {
                    encoding = ct.substring(csi + CHARSETEQUALS.length());
                }
            }
            if (debug) logger.debug("Response encoding: " + contentType);
            
            // perform the stream write from the response to the output
            int bufferSize = BUFFERSIZE;
            if (contentLength != null)
            {
                int length = Integer.parseInt(contentLength.getValue());
                if (length < bufferSize)
                {
                    bufferSize = length;
                }
            }
            StringBuilder traceBuf = null;
            if (trace)
            {
                traceBuf = new StringBuilder(bufferSize);
            }
            boolean responseCommit = false;
            if (responseCode != HttpServletResponse.SC_NOT_MODIFIED)
            {
                InputStream input = method.getResponseBodyAsStream();
                if (input != null)
                {
                    try
                    {
                        byte[] buffer = new byte[bufferSize];
                        int read = input.read(buffer);
                        if (read != -1) responseCommit = true;
                        while (read != -1)
                        {
                            if (out != null)
                            {
                                out.write(buffer, 0, read);
                            }
                            
                            if (trace)
                            {
                                if (ct != null && (ct.startsWith("text/") || ct.startsWith("application/json")))
                                {
                                    traceBuf.append(new String(buffer, 0, read));
                                }
                            }
                            
                            read = input.read(buffer);
                        }
                    }
        		    finally
        		    {
                        if (trace && traceBuf.length() != 0)
                        {
                            logger.trace("Output (" + (traceBuf.length()) + " bytes) from: " + url.toString());
                            logger.trace(traceBuf.toString());
                        }
                        try
                        {
                            try
                            {
                                input.close();
                            }
                            finally
                            {
                                if (responseCommit)
                                {
                                    if (out != null)
                                    {
                                        out.close();
                                    }
                                }
                            }
                        }
                        catch (IOException e)
                        {
                            if (logger.isWarnEnabled())
                                logger.warn("Exception during close() of HTTP API connection", e);
                        }
                    }
                }
            }
            
            // apply error response message if required
            if (res != null && responseCode != HttpServletResponse.SC_OK && !responseCommit)
            {
                res.sendError(responseCode, method.getStatusText());
            }
            
            // if we get here call was successful
            return encoding;
        }
        catch (ConnectTimeoutException timeErr)
        {
            // caught a socket timeout IO exception - apply internal error code
            status.setCode(SC_REMOTE_CONN_TIMEOUT);
            status.setException(timeErr);
            status.setMessage(timeErr.getMessage());
            if (res != null)
            {
                // externally just return a generic 500 server error
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, timeErr.getMessage());
            }            
            
            throw timeErr;
        }
        catch (UnknownHostException hostErr)
        {
            // caught an unknown host IO exception - apply internal error code
            status.setCode(SC_REMOTE_CONN_NOHOST);
            status.setException(hostErr);
            status.setMessage(hostErr.getMessage());
            if (res != null)
            {
                // externally just return a generic 500 server error
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, hostErr.getMessage());
            }            
            
            throw hostErr;
        }
        catch (ConnectException connErr)
        {
            // caught a no host IO exception - apply internal error code
            status.setCode(SC_REMOTE_CONN_NOHOST);
            status.setException(connErr);
            status.setMessage(connErr.getMessage());
            if (res != null)
            {
                // externally just return a generic 500 server error
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, connErr.getMessage());
            }            
            
            throw connErr;
        }
        catch (IOException ioErr)
        {
            // caught a general IO exception - apply generic error code so one gets returned
            status.setCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            status.setException(ioErr);
            status.setMessage(ioErr.getMessage());
            if (res != null)
            {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ioErr.getMessage());
            }            
            
            throw ioErr;
        }
        finally
        {
            // reset state values
            method.releaseConnection();
            this.requestContentType = "application/octet-stream";
            this.requestMethod = HttpMethod.GET;
        }
    }    
}
