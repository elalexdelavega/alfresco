
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
 * Wed May 20 15:06:32 EEST 2009
 * Generated source version: 2.1.2
 * 
 */


@WebServiceClient(name = "DiscoveryService", 
                  wsdlLocation = "file:/D:/java/eclipse/work/WS-Binding-06c/source/wsdl/CMISWS-Service.wsdl",
                  targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200901") 
public class DiscoveryService extends Service {

    public final static URL WSDL_LOCATION;
    public final static QName SERVICE = new QName("http://docs.oasis-open.org/ns/cmis/ws/200901", "DiscoveryService");
    public final static QName DiscoveryServicePort = new QName("http://docs.oasis-open.org/ns/cmis/ws/200901", "DiscoveryServicePort");
    static {
        URL url = null;
        try {
            url = new URL("file:/D:/java/eclipse/work/WS-Binding-06c/source/wsdl/CMISWS-Service.wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from file:/D:/java/eclipse/work/WS-Binding-06c/source/wsdl/CMISWS-Service.wsdl");
            // e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public DiscoveryService(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public DiscoveryService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public DiscoveryService() {
        super(WSDL_LOCATION, SERVICE);
    }

    /**
     * 
     * @return
     *     returns DiscoveryServicePort
     */
    @WebEndpoint(name = "DiscoveryServicePort")
    public DiscoveryServicePort getDiscoveryServicePort() {
        return super.getPort(DiscoveryServicePort, DiscoveryServicePort.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns DiscoveryServicePort
     */
    @WebEndpoint(name = "DiscoveryServicePort")
    public DiscoveryServicePort getDiscoveryServicePort(WebServiceFeature... features) {
        return super.getPort(DiscoveryServicePort, DiscoveryServicePort.class, features);
    }

}
