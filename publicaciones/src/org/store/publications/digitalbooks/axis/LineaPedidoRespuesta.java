/**
 * LineaPedidoRespuesta.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.store.publications.digitalbooks.axis;

public class LineaPedidoRespuesta  implements java.io.Serializable {
    private java.lang.String item_pedido;

    private java.lang.String link;

    private java.lang.String ean_13;

    public LineaPedidoRespuesta() {
    }

    public LineaPedidoRespuesta(
           java.lang.String item_pedido,
           java.lang.String link,
           java.lang.String ean_13) {
           this.item_pedido = item_pedido;
           this.link = link;
           this.ean_13 = ean_13;
    }


    /**
     * Gets the item_pedido value for this LineaPedidoRespuesta.
     * 
     * @return item_pedido
     */
    public java.lang.String getItem_pedido() {
        return item_pedido;
    }


    /**
     * Sets the item_pedido value for this LineaPedidoRespuesta.
     * 
     * @param item_pedido
     */
    public void setItem_pedido(java.lang.String item_pedido) {
        this.item_pedido = item_pedido;
    }


    /**
     * Gets the link value for this LineaPedidoRespuesta.
     * 
     * @return link
     */
    public java.lang.String getLink() {
        return link;
    }


    /**
     * Sets the link value for this LineaPedidoRespuesta.
     * 
     * @param link
     */
    public void setLink(java.lang.String link) {
        this.link = link;
    }


    /**
     * Gets the ean_13 value for this LineaPedidoRespuesta.
     * 
     * @return ean_13
     */
    public java.lang.String getEan_13() {
        return ean_13;
    }


    /**
     * Sets the ean_13 value for this LineaPedidoRespuesta.
     * 
     * @param ean_13
     */
    public void setEan_13(java.lang.String ean_13) {
        this.ean_13 = ean_13;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof LineaPedidoRespuesta)) return false;
        LineaPedidoRespuesta other = (LineaPedidoRespuesta) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.item_pedido==null && other.getItem_pedido()==null) || 
             (this.item_pedido!=null &&
              this.item_pedido.equals(other.getItem_pedido()))) &&
            ((this.link==null && other.getLink()==null) || 
             (this.link!=null &&
              this.link.equals(other.getLink()))) &&
            ((this.ean_13==null && other.getEan_13()==null) || 
             (this.ean_13!=null &&
              this.ean_13.equals(other.getEan_13())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getItem_pedido() != null) {
            _hashCode += getItem_pedido().hashCode();
        }
        if (getLink() != null) {
            _hashCode += getLink().hashCode();
        }
        if (getEan_13() != null) {
            _hashCode += getEan_13().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(LineaPedidoRespuesta.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("digitalbooks.theme.browser.webservice.soap.DBSOAPMethods", "LineaPedidoRespuesta"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("item_pedido");
        elemField.setXmlName(new javax.xml.namespace.QName("", "item_pedido"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("link");
        elemField.setXmlName(new javax.xml.namespace.QName("", "link"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ean_13");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ean_13"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
