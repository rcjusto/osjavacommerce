/**
 * DBSOAPMethods_ServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.store.publications.digitalbooks.axis;

public class DBSOAPMethods_ServiceLocator extends org.apache.axis.client.Service implements org.store.publications.digitalbooks.axis.DBSOAPMethods_Service {

    public DBSOAPMethods_ServiceLocator() {
    }


    public DBSOAPMethods_ServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public DBSOAPMethods_ServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for DBSOAPMethods
    private java.lang.String DBSOAPMethods_address = "http://www.aglutinaeditores.com";

    public java.lang.String getDBSOAPMethodsAddress() {
        return DBSOAPMethods_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String DBSOAPMethodsWSDDServiceName = "DBSOAPMethods";

    public java.lang.String getDBSOAPMethodsWSDDServiceName() {
        return DBSOAPMethodsWSDDServiceName;
    }

    public void setDBSOAPMethodsWSDDServiceName(java.lang.String name) {
        DBSOAPMethodsWSDDServiceName = name;
    }

    public org.store.publications.digitalbooks.axis.DBSOAPMethods_PortType getDBSOAPMethods() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(DBSOAPMethods_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getDBSOAPMethods(endpoint);
    }

    public org.store.publications.digitalbooks.axis.DBSOAPMethods_PortType getDBSOAPMethods(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.store.publications.digitalbooks.axis.DBSOAPMethods_BindingStub _stub = new org.store.publications.digitalbooks.axis.DBSOAPMethods_BindingStub(portAddress, this);
            _stub.setPortName(getDBSOAPMethodsWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setDBSOAPMethodsEndpointAddress(java.lang.String address) {
        DBSOAPMethods_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.store.publications.digitalbooks.axis.DBSOAPMethods_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                org.store.publications.digitalbooks.axis.DBSOAPMethods_BindingStub _stub = new org.store.publications.digitalbooks.axis.DBSOAPMethods_BindingStub(new java.net.URL(DBSOAPMethods_address), this);
                _stub.setPortName(getDBSOAPMethodsWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("DBSOAPMethods".equals(inputPortName)) {
            return getDBSOAPMethods();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("digitalbooks.theme.browser.webservice.soap.DBSOAPMethods", "DBSOAPMethods");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("digitalbooks.theme.browser.webservice.soap.DBSOAPMethods", "DBSOAPMethods"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("DBSOAPMethods".equals(portName)) {
            setDBSOAPMethodsEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
