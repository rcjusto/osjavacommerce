/**
 * DBSOAPMethods_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.store.publications.digitalbooks.axis;

public interface DBSOAPMethods_PortType extends java.rmi.Remote {
    public java.lang.String cancelaPedido(java.lang.String retailer_id, java.lang.String id_pedido) throws java.rmi.RemoteException;
    public java.lang.String devuelveEnlace(java.lang.String retailer_id, java.lang.String id_pedido, java.lang.String item_pedido, java.lang.String ean_13) throws java.rmi.RemoteException;
    public org.store.publications.digitalbooks.axis.LineaPedidoRespuesta[] devuelveEnlaces(java.lang.String retailer_id, java.lang.String id_pedido) throws java.rmi.RemoteException;
    public java.lang.String libroVendido(java.lang.String retailer_id, java.lang.String id_pedido, java.lang.String ean_13, float precio, java.lang.String divisa, java.lang.String id_pais_facturacion) throws java.rmi.RemoteException;
    public java.lang.String libroVendidoV2(java.lang.String retailer_id, java.lang.String id_pedido, java.lang.String ean_13, float precio, java.lang.String divisa, java.lang.String id_pais_facturacion, int comprobar_precio) throws java.rmi.RemoteException;
    public org.store.publications.digitalbooks.axis.LineaPedidoRespuesta[] librosVendidos(java.lang.String retailer_id, java.lang.String id_pedido, org.store.publications.digitalbooks.axis.LineaPedido[] array_pedidos, java.lang.String id_pais_facturacion) throws java.rmi.RemoteException;
    public org.store.publications.digitalbooks.axis.LineaPedidoRespuesta[] librosVendidosV2(java.lang.String retailer_id, java.lang.String id_pedido, org.store.publications.digitalbooks.axis.LineaPedido[] array_pedidos, java.lang.String id_pais_facturacion, int comprobar_precio) throws java.rmi.RemoteException;
    public void solicitudCatalogoCompleto(java.lang.String retailer_id) throws java.rmi.RemoteException;
}
