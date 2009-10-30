
/*
 * 
 */

package org.alfresco.repo.cmis.ws;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;

/**
 * This class was generated by Apache CXF 2.1.2
 * Fri Oct 23 11:53:52 BST 2009
 * Generated source version: 2.1.2
 * 
 */


@WebServiceClient(name = "NavigationService", 
                  wsdlLocation = "file:CMISWS-Service.wsdl",
                  targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200908/") 
public class NavigationService extends Service {

    public final static URL WSDL_LOCATION;
    public final static QName SERVICE = new QName("http://docs.oasis-open.org/ns/cmis/ws/200908/", "NavigationService");
    public final static QName NavigationServicePort = new QName("http://docs.oasis-open.org/ns/cmis/ws/200908/", "NavigationServicePort");
    static {
        URL url = null;
        try {
            url = new URL("file:CMISWS-Service.wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from file:CMISWS-Service.wsdl");
            // e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public NavigationService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public NavigationService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public NavigationService() {
        super(WSDL_LOCATION, SERVICE);
    }

    /**
     * 
     * @return
     *     returns NavigationServicePort
     */
    @WebEndpoint(name = "NavigationServicePort")
    public NavigationServicePort getNavigationServicePort() {
        return super.getPort(NavigationServicePort, NavigationServicePort.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns NavigationServicePort
     */
    @WebEndpoint(name = "NavigationServicePort")
    public NavigationServicePort getNavigationServicePort(WebServiceFeature... features) {
        return super.getPort(NavigationServicePort, NavigationServicePort.class, features);
    }

}
