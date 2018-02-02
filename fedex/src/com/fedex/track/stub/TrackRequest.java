/**
 * TrackRequest.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fedex.track.stub;


/**
 * The descriptive data sent by a client to track a FedEx package.
 */
public class TrackRequest  implements java.io.Serializable {
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

    /* The FedEx operating company (transportation) used for this
     * package's delivery. */
    private com.fedex.track.stub.CarrierCodeType carrierCode;

    /* Identifies operating transportation company that is the specific
     * to the carrier code. */
    private com.fedex.track.stub.OperatingCompanyType operatingCompany;

    /* The type and value of the package identifier that is to be
     * used to retrieve the tracking information for a package or group of
     * packages. */
    private com.fedex.track.stub.TrackPackageIdentifier packageIdentifier;

    /* Used to distinguish duplicate FedEx tracking numbers. */
    private java.lang.String trackingNumberUniqueIdentifier;

    /* To narrow the search to a period in time the ShipDateRangeBegin
     * and ShipDateRangeEnd can be used to help eliminate duplicates. */
    private java.util.Date shipDateRangeBegin;

    /* To narrow the search to a period in time the ShipDateRangeBegin
     * and ShipDateRangeEnd can be used to help eliminate duplicates. */
    private java.util.Date shipDateRangeEnd;

    /* For tracking by references information either the account number
     * or destination postal code and country must be provided. */
    private java.lang.String shipmentAccountNumber;

    /* For tracking by references information either the account number
     * or destination postal code and country must be provided. */
    private com.fedex.track.stub.Address destination;

    /* If false the reply will contain summary/profile data including
     * current status. If true the reply contains profile + detailed scan
     * activity for each package. */
    private java.lang.Boolean includeDetailedScans;

    /* When the MoreData field = true in a TrackReply the PagingToken
     * must be sent in the subsequent TrackRequest to retrieve the next page
     * of data. */
    private java.lang.String pagingToken;

    public TrackRequest() {
    }

    public TrackRequest(
           com.fedex.track.stub.WebAuthenticationDetail webAuthenticationDetail,
           com.fedex.track.stub.ClientDetail clientDetail,
           com.fedex.track.stub.TransactionDetail transactionDetail,
           com.fedex.track.stub.VersionId version,
           com.fedex.track.stub.CarrierCodeType carrierCode,
           com.fedex.track.stub.OperatingCompanyType operatingCompany,
           com.fedex.track.stub.TrackPackageIdentifier packageIdentifier,
           java.lang.String trackingNumberUniqueIdentifier,
           java.util.Date shipDateRangeBegin,
           java.util.Date shipDateRangeEnd,
           java.lang.String shipmentAccountNumber,
           com.fedex.track.stub.Address destination,
           java.lang.Boolean includeDetailedScans,
           java.lang.String pagingToken) {
           this.webAuthenticationDetail = webAuthenticationDetail;
           this.clientDetail = clientDetail;
           this.transactionDetail = transactionDetail;
           this.version = version;
           this.carrierCode = carrierCode;
           this.operatingCompany = operatingCompany;
           this.packageIdentifier = packageIdentifier;
           this.trackingNumberUniqueIdentifier = trackingNumberUniqueIdentifier;
           this.shipDateRangeBegin = shipDateRangeBegin;
           this.shipDateRangeEnd = shipDateRangeEnd;
           this.shipmentAccountNumber = shipmentAccountNumber;
           this.destination = destination;
           this.includeDetailedScans = includeDetailedScans;
           this.pagingToken = pagingToken;
    }


    /**
     * Gets the webAuthenticationDetail value for this TrackRequest.
     * 
     * @return webAuthenticationDetail   * Descriptive data to be used in authentication of the sender's
     * identity (and right to use FedEx web services).
     */
    public com.fedex.track.stub.WebAuthenticationDetail getWebAuthenticationDetail() {
        return webAuthenticationDetail;
    }


    /**
     * Sets the webAuthenticationDetail value for this TrackRequest.
     * 
     * @param webAuthenticationDetail   * Descriptive data to be used in authentication of the sender's
     * identity (and right to use FedEx web services).
     */
    public void setWebAuthenticationDetail(com.fedex.track.stub.WebAuthenticationDetail webAuthenticationDetail) {
        this.webAuthenticationDetail = webAuthenticationDetail;
    }


    /**
     * Gets the clientDetail value for this TrackRequest.
     * 
     * @return clientDetail   * Descriptive data identifying the client submitting the transaction.
     */
    public com.fedex.track.stub.ClientDetail getClientDetail() {
        return clientDetail;
    }


    /**
     * Sets the clientDetail value for this TrackRequest.
     * 
     * @param clientDetail   * Descriptive data identifying the client submitting the transaction.
     */
    public void setClientDetail(com.fedex.track.stub.ClientDetail clientDetail) {
        this.clientDetail = clientDetail;
    }


    /**
     * Gets the transactionDetail value for this TrackRequest.
     * 
     * @return transactionDetail   * Contains a free form field that is echoed back in the reply
     * to match requests with replies and data that governs the data payload
     * language/translations.
     */
    public com.fedex.track.stub.TransactionDetail getTransactionDetail() {
        return transactionDetail;
    }


    /**
     * Sets the transactionDetail value for this TrackRequest.
     * 
     * @param transactionDetail   * Contains a free form field that is echoed back in the reply
     * to match requests with replies and data that governs the data payload
     * language/translations.
     */
    public void setTransactionDetail(com.fedex.track.stub.TransactionDetail transactionDetail) {
        this.transactionDetail = transactionDetail;
    }


    /**
     * Gets the version value for this TrackRequest.
     * 
     * @return version   * The version of the request being used.
     */
    public com.fedex.track.stub.VersionId getVersion() {
        return version;
    }


    /**
     * Sets the version value for this TrackRequest.
     * 
     * @param version   * The version of the request being used.
     */
    public void setVersion(com.fedex.track.stub.VersionId version) {
        this.version = version;
    }


    /**
     * Gets the carrierCode value for this TrackRequest.
     * 
     * @return carrierCode   * The FedEx operating company (transportation) used for this
     * package's delivery.
     */
    public com.fedex.track.stub.CarrierCodeType getCarrierCode() {
        return carrierCode;
    }


    /**
     * Sets the carrierCode value for this TrackRequest.
     * 
     * @param carrierCode   * The FedEx operating company (transportation) used for this
     * package's delivery.
     */
    public void setCarrierCode(com.fedex.track.stub.CarrierCodeType carrierCode) {
        this.carrierCode = carrierCode;
    }


    /**
     * Gets the operatingCompany value for this TrackRequest.
     * 
     * @return operatingCompany   * Identifies operating transportation company that is the specific
     * to the carrier code.
     */
    public com.fedex.track.stub.OperatingCompanyType getOperatingCompany() {
        return operatingCompany;
    }


    /**
     * Sets the operatingCompany value for this TrackRequest.
     * 
     * @param operatingCompany   * Identifies operating transportation company that is the specific
     * to the carrier code.
     */
    public void setOperatingCompany(com.fedex.track.stub.OperatingCompanyType operatingCompany) {
        this.operatingCompany = operatingCompany;
    }


    /**
     * Gets the packageIdentifier value for this TrackRequest.
     * 
     * @return packageIdentifier   * The type and value of the package identifier that is to be
     * used to retrieve the tracking information for a package or group of
     * packages.
     */
    public com.fedex.track.stub.TrackPackageIdentifier getPackageIdentifier() {
        return packageIdentifier;
    }


    /**
     * Sets the packageIdentifier value for this TrackRequest.
     * 
     * @param packageIdentifier   * The type and value of the package identifier that is to be
     * used to retrieve the tracking information for a package or group of
     * packages.
     */
    public void setPackageIdentifier(com.fedex.track.stub.TrackPackageIdentifier packageIdentifier) {
        this.packageIdentifier = packageIdentifier;
    }


    /**
     * Gets the trackingNumberUniqueIdentifier value for this TrackRequest.
     * 
     * @return trackingNumberUniqueIdentifier   * Used to distinguish duplicate FedEx tracking numbers.
     */
    public java.lang.String getTrackingNumberUniqueIdentifier() {
        return trackingNumberUniqueIdentifier;
    }


    /**
     * Sets the trackingNumberUniqueIdentifier value for this TrackRequest.
     * 
     * @param trackingNumberUniqueIdentifier   * Used to distinguish duplicate FedEx tracking numbers.
     */
    public void setTrackingNumberUniqueIdentifier(java.lang.String trackingNumberUniqueIdentifier) {
        this.trackingNumberUniqueIdentifier = trackingNumberUniqueIdentifier;
    }


    /**
     * Gets the shipDateRangeBegin value for this TrackRequest.
     * 
     * @return shipDateRangeBegin   * To narrow the search to a period in time the ShipDateRangeBegin
     * and ShipDateRangeEnd can be used to help eliminate duplicates.
     */
    public java.util.Date getShipDateRangeBegin() {
        return shipDateRangeBegin;
    }


    /**
     * Sets the shipDateRangeBegin value for this TrackRequest.
     * 
     * @param shipDateRangeBegin   * To narrow the search to a period in time the ShipDateRangeBegin
     * and ShipDateRangeEnd can be used to help eliminate duplicates.
     */
    public void setShipDateRangeBegin(java.util.Date shipDateRangeBegin) {
        this.shipDateRangeBegin = shipDateRangeBegin;
    }


    /**
     * Gets the shipDateRangeEnd value for this TrackRequest.
     * 
     * @return shipDateRangeEnd   * To narrow the search to a period in time the ShipDateRangeBegin
     * and ShipDateRangeEnd can be used to help eliminate duplicates.
     */
    public java.util.Date getShipDateRangeEnd() {
        return shipDateRangeEnd;
    }


    /**
     * Sets the shipDateRangeEnd value for this TrackRequest.
     * 
     * @param shipDateRangeEnd   * To narrow the search to a period in time the ShipDateRangeBegin
     * and ShipDateRangeEnd can be used to help eliminate duplicates.
     */
    public void setShipDateRangeEnd(java.util.Date shipDateRangeEnd) {
        this.shipDateRangeEnd = shipDateRangeEnd;
    }


    /**
     * Gets the shipmentAccountNumber value for this TrackRequest.
     * 
     * @return shipmentAccountNumber   * For tracking by references information either the account number
     * or destination postal code and country must be provided.
     */
    public java.lang.String getShipmentAccountNumber() {
        return shipmentAccountNumber;
    }


    /**
     * Sets the shipmentAccountNumber value for this TrackRequest.
     * 
     * @param shipmentAccountNumber   * For tracking by references information either the account number
     * or destination postal code and country must be provided.
     */
    public void setShipmentAccountNumber(java.lang.String shipmentAccountNumber) {
        this.shipmentAccountNumber = shipmentAccountNumber;
    }


    /**
     * Gets the destination value for this TrackRequest.
     * 
     * @return destination   * For tracking by references information either the account number
     * or destination postal code and country must be provided.
     */
    public com.fedex.track.stub.Address getDestination() {
        return destination;
    }


    /**
     * Sets the destination value for this TrackRequest.
     * 
     * @param destination   * For tracking by references information either the account number
     * or destination postal code and country must be provided.
     */
    public void setDestination(com.fedex.track.stub.Address destination) {
        this.destination = destination;
    }


    /**
     * Gets the includeDetailedScans value for this TrackRequest.
     * 
     * @return includeDetailedScans   * If false the reply will contain summary/profile data including
     * current status. If true the reply contains profile + detailed scan
     * activity for each package.
     */
    public java.lang.Boolean getIncludeDetailedScans() {
        return includeDetailedScans;
    }


    /**
     * Sets the includeDetailedScans value for this TrackRequest.
     * 
     * @param includeDetailedScans   * If false the reply will contain summary/profile data including
     * current status. If true the reply contains profile + detailed scan
     * activity for each package.
     */
    public void setIncludeDetailedScans(java.lang.Boolean includeDetailedScans) {
        this.includeDetailedScans = includeDetailedScans;
    }


    /**
     * Gets the pagingToken value for this TrackRequest.
     * 
     * @return pagingToken   * When the MoreData field = true in a TrackReply the PagingToken
     * must be sent in the subsequent TrackRequest to retrieve the next page
     * of data.
     */
    public java.lang.String getPagingToken() {
        return pagingToken;
    }


    /**
     * Sets the pagingToken value for this TrackRequest.
     * 
     * @param pagingToken   * When the MoreData field = true in a TrackReply the PagingToken
     * must be sent in the subsequent TrackRequest to retrieve the next page
     * of data.
     */
    public void setPagingToken(java.lang.String pagingToken) {
        this.pagingToken = pagingToken;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TrackRequest)) return false;
        TrackRequest other = (TrackRequest) obj;
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
            ((this.carrierCode==null && other.getCarrierCode()==null) || 
             (this.carrierCode!=null &&
              this.carrierCode.equals(other.getCarrierCode()))) &&
            ((this.operatingCompany==null && other.getOperatingCompany()==null) || 
             (this.operatingCompany!=null &&
              this.operatingCompany.equals(other.getOperatingCompany()))) &&
            ((this.packageIdentifier==null && other.getPackageIdentifier()==null) || 
             (this.packageIdentifier!=null &&
              this.packageIdentifier.equals(other.getPackageIdentifier()))) &&
            ((this.trackingNumberUniqueIdentifier==null && other.getTrackingNumberUniqueIdentifier()==null) || 
             (this.trackingNumberUniqueIdentifier!=null &&
              this.trackingNumberUniqueIdentifier.equals(other.getTrackingNumberUniqueIdentifier()))) &&
            ((this.shipDateRangeBegin==null && other.getShipDateRangeBegin()==null) || 
             (this.shipDateRangeBegin!=null &&
              this.shipDateRangeBegin.equals(other.getShipDateRangeBegin()))) &&
            ((this.shipDateRangeEnd==null && other.getShipDateRangeEnd()==null) || 
             (this.shipDateRangeEnd!=null &&
              this.shipDateRangeEnd.equals(other.getShipDateRangeEnd()))) &&
            ((this.shipmentAccountNumber==null && other.getShipmentAccountNumber()==null) || 
             (this.shipmentAccountNumber!=null &&
              this.shipmentAccountNumber.equals(other.getShipmentAccountNumber()))) &&
            ((this.destination==null && other.getDestination()==null) || 
             (this.destination!=null &&
              this.destination.equals(other.getDestination()))) &&
            ((this.includeDetailedScans==null && other.getIncludeDetailedScans()==null) || 
             (this.includeDetailedScans!=null &&
              this.includeDetailedScans.equals(other.getIncludeDetailedScans()))) &&
            ((this.pagingToken==null && other.getPagingToken()==null) || 
             (this.pagingToken!=null &&
              this.pagingToken.equals(other.getPagingToken())));
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
        if (getCarrierCode() != null) {
            _hashCode += getCarrierCode().hashCode();
        }
        if (getOperatingCompany() != null) {
            _hashCode += getOperatingCompany().hashCode();
        }
        if (getPackageIdentifier() != null) {
            _hashCode += getPackageIdentifier().hashCode();
        }
        if (getTrackingNumberUniqueIdentifier() != null) {
            _hashCode += getTrackingNumberUniqueIdentifier().hashCode();
        }
        if (getShipDateRangeBegin() != null) {
            _hashCode += getShipDateRangeBegin().hashCode();
        }
        if (getShipDateRangeEnd() != null) {
            _hashCode += getShipDateRangeEnd().hashCode();
        }
        if (getShipmentAccountNumber() != null) {
            _hashCode += getShipmentAccountNumber().hashCode();
        }
        if (getDestination() != null) {
            _hashCode += getDestination().hashCode();
        }
        if (getIncludeDetailedScans() != null) {
            _hashCode += getIncludeDetailedScans().hashCode();
        }
        if (getPagingToken() != null) {
            _hashCode += getPagingToken().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(TrackRequest.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackRequest"));
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
        elemField.setFieldName("carrierCode");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "CarrierCode"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "CarrierCodeType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("operatingCompany");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "OperatingCompany"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "OperatingCompanyType"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("packageIdentifier");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "PackageIdentifier"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackPackageIdentifier"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("trackingNumberUniqueIdentifier");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackingNumberUniqueIdentifier"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("shipDateRangeBegin");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "ShipDateRangeBegin"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("shipDateRangeEnd");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "ShipDateRangeEnd"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "date"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("shipmentAccountNumber");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "ShipmentAccountNumber"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("destination");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "Destination"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "Address"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("includeDetailedScans");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "IncludeDetailedScans"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pagingToken");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "PagingToken"));
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
