/**
 * TrackReply.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fedex.track.stub;


/**
 * The descriptive data returned from a FedEx package tracking request.
 */
public class TrackReply  implements java.io.Serializable {
    /* This contains the severity type of the most severe Notification
     * in the Notifications array. */
    private com.fedex.track.stub.NotificationSeverityType highestSeverity;

    /* Information about the request/reply such was the transaction
     * successful or not, and any additional information relevant to the
     * request and/or reply. There may be multiple Notifications in a reply. */
    private com.fedex.track.stub.Notification[] notifications;

    /* Contains the CustomerTransactionDetail that is echoed back
     * to the caller for matching requests and replies and a Localization
     * element for defining the language/translation used in the reply data. */
    private com.fedex.track.stub.TransactionDetail transactionDetail;

    /* Contains the version of the reply being used. */
    private com.fedex.track.stub.VersionId version;

    /* True if duplicate packages (more than one package with the
     * same tracking number) have been found, and only limited data will
     * be provided for each one. */
    private java.lang.Boolean duplicateWaybill;

    /* True if additional packages remain to be retrieved. */
    private java.lang.Boolean moreData;

    /* Value that must be passed in a TrackNotification request to
     * retrieve the next set of packages (when MoreDataAvailable = true). */
    private java.lang.String pagingToken;

    /* Contains detailed tracking information for the requested packages(s). */
    private com.fedex.track.stub.TrackDetail[] trackDetails;

    public TrackReply() {
    }

    public TrackReply(
           com.fedex.track.stub.NotificationSeverityType highestSeverity,
           com.fedex.track.stub.Notification[] notifications,
           com.fedex.track.stub.TransactionDetail transactionDetail,
           com.fedex.track.stub.VersionId version,
           java.lang.Boolean duplicateWaybill,
           java.lang.Boolean moreData,
           java.lang.String pagingToken,
           com.fedex.track.stub.TrackDetail[] trackDetails) {
           this.highestSeverity = highestSeverity;
           this.notifications = notifications;
           this.transactionDetail = transactionDetail;
           this.version = version;
           this.duplicateWaybill = duplicateWaybill;
           this.moreData = moreData;
           this.pagingToken = pagingToken;
           this.trackDetails = trackDetails;
    }


    /**
     * Gets the highestSeverity value for this TrackReply.
     * 
     * @return highestSeverity   * This contains the severity type of the most severe Notification
     * in the Notifications array.
     */
    public com.fedex.track.stub.NotificationSeverityType getHighestSeverity() {
        return highestSeverity;
    }


    /**
     * Sets the highestSeverity value for this TrackReply.
     * 
     * @param highestSeverity   * This contains the severity type of the most severe Notification
     * in the Notifications array.
     */
    public void setHighestSeverity(com.fedex.track.stub.NotificationSeverityType highestSeverity) {
        this.highestSeverity = highestSeverity;
    }


    /**
     * Gets the notifications value for this TrackReply.
     * 
     * @return notifications   * Information about the request/reply such was the transaction
     * successful or not, and any additional information relevant to the
     * request and/or reply. There may be multiple Notifications in a reply.
     */
    public com.fedex.track.stub.Notification[] getNotifications() {
        return notifications;
    }


    /**
     * Sets the notifications value for this TrackReply.
     * 
     * @param notifications   * Information about the request/reply such was the transaction
     * successful or not, and any additional information relevant to the
     * request and/or reply. There may be multiple Notifications in a reply.
     */
    public void setNotifications(com.fedex.track.stub.Notification[] notifications) {
        this.notifications = notifications;
    }

    public com.fedex.track.stub.Notification getNotifications(int i) {
        return this.notifications[i];
    }

    public void setNotifications(int i, com.fedex.track.stub.Notification _value) {
        this.notifications[i] = _value;
    }


    /**
     * Gets the transactionDetail value for this TrackReply.
     * 
     * @return transactionDetail   * Contains the CustomerTransactionDetail that is echoed back
     * to the caller for matching requests and replies and a Localization
     * element for defining the language/translation used in the reply data.
     */
    public com.fedex.track.stub.TransactionDetail getTransactionDetail() {
        return transactionDetail;
    }


    /**
     * Sets the transactionDetail value for this TrackReply.
     * 
     * @param transactionDetail   * Contains the CustomerTransactionDetail that is echoed back
     * to the caller for matching requests and replies and a Localization
     * element for defining the language/translation used in the reply data.
     */
    public void setTransactionDetail(com.fedex.track.stub.TransactionDetail transactionDetail) {
        this.transactionDetail = transactionDetail;
    }


    /**
     * Gets the version value for this TrackReply.
     * 
     * @return version   * Contains the version of the reply being used.
     */
    public com.fedex.track.stub.VersionId getVersion() {
        return version;
    }


    /**
     * Sets the version value for this TrackReply.
     * 
     * @param version   * Contains the version of the reply being used.
     */
    public void setVersion(com.fedex.track.stub.VersionId version) {
        this.version = version;
    }


    /**
     * Gets the duplicateWaybill value for this TrackReply.
     * 
     * @return duplicateWaybill   * True if duplicate packages (more than one package with the
     * same tracking number) have been found, and only limited data will
     * be provided for each one.
     */
    public java.lang.Boolean getDuplicateWaybill() {
        return duplicateWaybill;
    }


    /**
     * Sets the duplicateWaybill value for this TrackReply.
     * 
     * @param duplicateWaybill   * True if duplicate packages (more than one package with the
     * same tracking number) have been found, and only limited data will
     * be provided for each one.
     */
    public void setDuplicateWaybill(java.lang.Boolean duplicateWaybill) {
        this.duplicateWaybill = duplicateWaybill;
    }


    /**
     * Gets the moreData value for this TrackReply.
     * 
     * @return moreData   * True if additional packages remain to be retrieved.
     */
    public java.lang.Boolean getMoreData() {
        return moreData;
    }


    /**
     * Sets the moreData value for this TrackReply.
     * 
     * @param moreData   * True if additional packages remain to be retrieved.
     */
    public void setMoreData(java.lang.Boolean moreData) {
        this.moreData = moreData;
    }


    /**
     * Gets the pagingToken value for this TrackReply.
     * 
     * @return pagingToken   * Value that must be passed in a TrackNotification request to
     * retrieve the next set of packages (when MoreDataAvailable = true).
     */
    public java.lang.String getPagingToken() {
        return pagingToken;
    }


    /**
     * Sets the pagingToken value for this TrackReply.
     * 
     * @param pagingToken   * Value that must be passed in a TrackNotification request to
     * retrieve the next set of packages (when MoreDataAvailable = true).
     */
    public void setPagingToken(java.lang.String pagingToken) {
        this.pagingToken = pagingToken;
    }


    /**
     * Gets the trackDetails value for this TrackReply.
     * 
     * @return trackDetails   * Contains detailed tracking information for the requested packages(s).
     */
    public com.fedex.track.stub.TrackDetail[] getTrackDetails() {
        return trackDetails;
    }


    /**
     * Sets the trackDetails value for this TrackReply.
     * 
     * @param trackDetails   * Contains detailed tracking information for the requested packages(s).
     */
    public void setTrackDetails(com.fedex.track.stub.TrackDetail[] trackDetails) {
        this.trackDetails = trackDetails;
    }

    public com.fedex.track.stub.TrackDetail getTrackDetails(int i) {
        return this.trackDetails[i];
    }

    public void setTrackDetails(int i, com.fedex.track.stub.TrackDetail _value) {
        this.trackDetails[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof TrackReply)) return false;
        TrackReply other = (TrackReply) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.highestSeverity==null && other.getHighestSeverity()==null) || 
             (this.highestSeverity!=null &&
              this.highestSeverity.equals(other.getHighestSeverity()))) &&
            ((this.notifications==null && other.getNotifications()==null) || 
             (this.notifications!=null &&
              java.util.Arrays.equals(this.notifications, other.getNotifications()))) &&
            ((this.transactionDetail==null && other.getTransactionDetail()==null) || 
             (this.transactionDetail!=null &&
              this.transactionDetail.equals(other.getTransactionDetail()))) &&
            ((this.version==null && other.getVersion()==null) || 
             (this.version!=null &&
              this.version.equals(other.getVersion()))) &&
            ((this.duplicateWaybill==null && other.getDuplicateWaybill()==null) || 
             (this.duplicateWaybill!=null &&
              this.duplicateWaybill.equals(other.getDuplicateWaybill()))) &&
            ((this.moreData==null && other.getMoreData()==null) || 
             (this.moreData!=null &&
              this.moreData.equals(other.getMoreData()))) &&
            ((this.pagingToken==null && other.getPagingToken()==null) || 
             (this.pagingToken!=null &&
              this.pagingToken.equals(other.getPagingToken()))) &&
            ((this.trackDetails==null && other.getTrackDetails()==null) || 
             (this.trackDetails!=null &&
              java.util.Arrays.equals(this.trackDetails, other.getTrackDetails())));
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
        if (getHighestSeverity() != null) {
            _hashCode += getHighestSeverity().hashCode();
        }
        if (getNotifications() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getNotifications());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getNotifications(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getTransactionDetail() != null) {
            _hashCode += getTransactionDetail().hashCode();
        }
        if (getVersion() != null) {
            _hashCode += getVersion().hashCode();
        }
        if (getDuplicateWaybill() != null) {
            _hashCode += getDuplicateWaybill().hashCode();
        }
        if (getMoreData() != null) {
            _hashCode += getMoreData().hashCode();
        }
        if (getPagingToken() != null) {
            _hashCode += getPagingToken().hashCode();
        }
        if (getTrackDetails() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getTrackDetails());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getTrackDetails(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(TrackReply.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackReply"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("highestSeverity");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "HighestSeverity"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "NotificationSeverityType"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("notifications");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "Notifications"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "Notification"));
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
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
        elemField.setFieldName("duplicateWaybill");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "DuplicateWaybill"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("moreData");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "MoreData"));
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
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("trackDetails");
        elemField.setXmlName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackDetails"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackDetail"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
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
