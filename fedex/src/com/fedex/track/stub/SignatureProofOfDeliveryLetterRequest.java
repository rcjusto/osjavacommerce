/**
 * SignatureProofOfDeliveryLetterRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fedex.track.stub;


/**
 * FedEx Signature Proof Of Delivery Letter request.
 */
public class SignatureProofOfDeliveryLetterRequest  implements java.io.Serializable {
    /* Descriptive data to be used in authentication of the sender's
     * identity (and right to use FedEx web services). */
    private com.fedex.track.stub.WebAuthenticationDetail webAuthenticationDetail;

    /* Descriptive data identifying the client submitting the transaction. */
    private com.fedex.track.stub.ClientDetail clientDetail;

    /* Contains a free form field that is echoed back in the reply
     * to match requests with replies and data that governs the data payload
     * language/translations. */
    private com.fedex.track.stub.TransactionDetail transactionDetail;

    /* The version of the request being used. */
    private com.fedex.track.stub.VersionId version;

    /* Tracking number and additional shipment data used to identify
     * a unique shipment for proof of delivery. */
    private com.fedex.track.stub.QualifiedTrackingNumber qualifiedTrackingNumber;

    /* Additional customer-supplied text to be added to the body of
     * the letter. */
    private java.lang.String additionalComments;

    /* Identifies the set of SPOD image types. */
    private com.fedex.track.stub.SignatureProofOfDeliveryImageType letterFormat;

    /* If provided this information will be print on the letter. */
    private com.fedex.track.stub.ContactAndAddress consignee;

    public SignatureProofOfDeliveryLetterRequest() {
    }

    public SignatureProofOfDeliveryLetterRequest(
           com.fedex.track.stub.WebAuthenticationDetail webAuthenticationDetail,
           com.fedex.track.stub.ClientDetail clientDetail,
           com.fedex.track.stub.TransactionDetail transactionDetail,
           com.fedex.track.stub.VersionId version,
           com.fedex.track.stub.QualifiedTrackingNumber qualifiedTrackingNumber,
           java.lang.String additionalComments,
           com.fedex.track.stub.SignatureProofOfDeliveryImageType letterFormat,
           com.fedex.track.stub.ContactAndAddress consignee) {
           this.webAuthenticationDetail = webAuthenticationDetail;
           this.clientDetail = clientDetail;
           this.transactionDetail = transactionDetail;
           this.version = version;
           this.qualifiedTrackingNumber = qualifiedTrackingNumber;
           this.additionalComments = additionalComments;
           this.letterFormat = letterFormat;
           this.consignee = consignee;
    }


    /**
     * Gets the webAuthenticationDetail value for this SignatureProofOfDeliveryLetterRequest.
     * 
     * @return webAuthenticationDetail   * Descriptive data to be used in authentication of the sender's
     * identity (and right to use FedEx web services).
     */
    public com.fedex.track.stub.WebAuthenticationDetail getWebAuthenticationDetail() {
        return webAuthenticationDetail;
    }


    /**
     * Sets the webAuthenticationDetail value for this SignatureProofOfDeliveryLetterRequest.
     * 
     * @param webAuthenticationDetail   * Descriptive data to be used in authentication of the sender's
     * identity (and right to use FedEx web services).
     */
    public void setWebAuthenticationDetail(com.fedex.track.stub.WebAuthenticationDetail webAuthenticationDetail) {
        this.webAuthenticationDetail = webAuthenticationDetail;
    }


    /**
     * Gets the clientDetail value for this SignatureProofOfDeliveryLetterRequest.
     * 
     * @return clientDetail   * Descriptive data identifying the client submitting the transaction.
     */
    public com.fedex.track.stub.ClientDetail getClientDetail() {
        return clientDetail;
    }


    /**
     * Sets the clientDetail value for this SignatureProofOfDeliveryLetterRequest.
     * 
     * @param clientDetail   * Descriptive data identifying the client submitting the transaction.
     */
    public void setClientDetail(com.fedex.track.stub.ClientDetail clientDetail) {
        this.clientDetail = clientDetail;
    }


    /**
     * Gets the transactionDetail value for this SignatureProofOfDeliveryLetterRequest.
     * 
     * @return transactionDetail   * Contains a free form field that is echoed back in the reply
     * to match requests with replies and data that governs the data payload
     * language/translations.
     */
    public com.fedex.track.stub.TransactionDetail getTransactionDetail() {
        return transactionDetail;
    }


    /**
     * Sets the transactionDetail value for this SignatureProofOfDeliveryLetterRequest.
     * 
     * @param transactionDetail   * Contains a free form field that is echoed back in the reply
     * to match requests with replies and data that governs the data payload
     * language/translations.
     */
    public void setTransactionDetail(com.fedex.track.stub.TransactionDetail transactionDetail) {
        this.transactionDetail = transactionDetail;
    }


    /**
     * Gets the version value for this SignatureProofOfDeliveryLetterRequest.
     * 
     * @return version   * The version of the request being used.
     */
    public com.fedex.track.stub.VersionId getVersion() {
        return version;
    }


    /**
     * Sets the version value for this SignatureProofOfDeliveryLetterRequest.
     * 
     * @param version   * The version of the request being used.
     */
    public void setVersion(com.fedex.track.stub.VersionId version) {
        this.version = version;
    }


    /**
     * Gets the qualifiedTrackingNumber value for this SignatureProofOfDeliveryLetterRequest.
     * 
     * @return qualifiedTrackingNumber   * Tracking number and additional shipment data used to identify
     * a unique shipment for proof of delivery.
     */
    public com.fedex.track.stub.QualifiedTrackingNumber getQualifiedTrackingNumber() {
        return qualifiedTrackingNumber;
    }


    /**
     * Sets the qualifiedTrackingNumber value for this SignatureProofOfDeliveryLetterRequest.
     * 
     * @param qualifiedTrackingNumber   * Tracking number and additional shipment data used to identify
     * a unique shipment for proof of delivery.
     */
    public void setQualifiedTrackingNumber(com.fedex.track.stub.QualifiedTrackingNumber qualifiedTrackingNumber) {
        this.qualifiedTrackingNumber = qualifiedTrackingNumber;
    }


    /**
     * Gets the additionalComments value for this SignatureProofOfDeliveryLetterRequest.
     * 
     * @return additionalComments   * Additional customer-supplied text to be added to the body of
     * the letter.
     */
    public java.lang.String getAdditionalComments() {
        return additionalComments;
    }


    /**
     * Sets the additionalComments value for this SignatureProofOfDeliveryLetterRequest.
     * 
     * @param additionalComments   * Additional customer-supplied text to be added to the body of
     * the letter.
     */
    public void setAdditionalComments(java.lang.String additionalComments) {
        this.additionalComments = additionalComments;
    }


    /**
     * Gets the letterFormat value for this SignatureProofOfDeliveryLetterRequest.
     * 
     * @return letterFormat   * Identifies the set of SPOD image types.
     */
    public com.fedex.track.stub.SignatureProofOfDeliveryImageType getLetterFormat() {
        return letterFormat;
    }


    /**
     * Sets the letterFormat value for this SignatureProofOfDeliveryLetterRequest.
     * 
     * @param letterFormat   * Identifies the set of SPOD image types.
     */
    public void setLetterFormat(com.fedex.track.stub.SignatureProofOfDeliveryImageType letterFormat) {
        this.letterFormat = letterFormat;
    }


    /**
     * Gets the consignee value for this SignatureProofOfDeliveryLetterRequest.
     * 
     * @return consignee   * If provided this information will be print on the letter.
     */
    public com.fedex.track.stub.ContactAndAddress getConsignee() {
        return consignee;
    }


    /**
     * Sets the consignee value for this SignatureProofOfDeliveryLetterRequest.
     * 
     * @param consignee   * If provided this information will be print on the letter.
     */
    public void setConsignee(com.fedex.track.stub.ContactAndAddress consignee) {
        this.consignee = consignee;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof SignatureProofOfDeliveryLetterRequest)) return false;
        SignatureProofOfDeliveryLetterRequest other = (SignatureProofOfDeliveryLetterRequest) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.webAuthenticationDetail==null && other.getWebAuthenticationDetail()==null) || 
             (this.webAuthenticationDetail!=null &&
              this.webAuthenticationDetail.equals(other.getWebAuthenticationDetail()))) &&
            ((this.clientDetail==null && other.getClientDetail()==null) || 
             (this.clientDetail!=null &&
              this.clientDetail.equals(other.getClientDetail()))) &&
            ((this.transactionDetail==null && other.getTransactionDetail()==null) || 
             (this.transactionDetail!=null &&
              this.transactionDetail.equals(other.getTransactionDetail()))) &&
            ((this.version==null && other.getVersion()==null) || 
             (this.version!=null &&
              this.version.equals(other.getVersion()))) &&
            ((this.qualifiedTrackingNumber==null && other.getQualifiedTrackingNumber()==null) || 
             (this.qualifiedTrackingNumber!=null &&
              this.qualifiedTrackingNumber.equals(other.getQualifiedTrackingNumber()))) &&
            ((this.additionalComments==null && other.getAdditionalComments()==null) || 
             (this.additionalComments!=null &&
              this.additionalComments.equals(other.getAdditionalComments()))) &&
            ((this.letterFormat==null && other.getLetterFormat()==null) || 
             (this.letterFormat!=null &&
              this.letterFormat.equals(other.getLetterFormat()))) &&
            ((this.consignee==null && other.getConsignee()==null) || 
             (this.consignee!=null &&
              this.consignee.equals(other.getConsignee())));
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
        if (getWebAuthenticationDetail() != null) {
            _hashCode += getWebAuthenticationDetail().hashCode();
        }
        if (getClientDetail() != null) {
            _hashCode += getClientDetail().hashCode();
        }
        if (getTransactionDetail() != null) {
            _hashCode += getTransactionDetail().hashCode();
        }
        if (getVersion() != null) {
            _hashCode += getVersion().hashCode();
        }
        if (getQualifiedTrackingNumber() != null) {
            _hashCode += getQualifiedTrackingNumber().hashCode();
        }
        if (getAdditionalComments() != null) {
            _hashCode += getAdditionalComments().hashCode();
        }
        if (getLetterFormat() != null) {
            _hashCode += getLetterFormat().hashCode();
        }
        if (getConsignee() != null) {
            _hashCode += getConsignee().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(SignatureProofOfDeliveryLetterRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "SignatureProofOfDeliveryLetterRequest"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("webAuthenticationDetail");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "WebAuthenticationDetail"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "WebAuthenticationDetail"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("clientDetail");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "ClientDetail"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "ClientDetail"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("transactionDetail");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TransactionDetail"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TransactionDetail"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("version");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "Version"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "VersionId"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("qualifiedTrackingNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "QualifiedTrackingNumber"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "QualifiedTrackingNumber"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("additionalComments");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "AdditionalComments"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("letterFormat");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "LetterFormat"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "SignatureProofOfDeliveryImageType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("consignee");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "Consignee"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "ContactAndAddress"));
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
