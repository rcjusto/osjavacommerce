/**
 * TrackServiceSoapBindingStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.fedex.track.stub;

public class TrackServiceSoapBindingStub extends org.apache.axis.client.Stub implements com.fedex.track.stub.TrackPortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[4];
        _initOperationDesc1();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("getTrackNotification");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackNotificationRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackNotificationRequest"), com.fedex.track.stub.TrackNotificationRequest.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackNotificationReply"));
        oper.setReturnClass(com.fedex.track.stub.TrackNotificationReply.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackNotificationReply"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("retrieveSignatureProofOfDeliveryLetter");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "SignatureProofOfDeliveryLetterRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "SignatureProofOfDeliveryLetterRequest"), com.fedex.track.stub.SignatureProofOfDeliveryLetterRequest.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "SignatureProofOfDeliveryLetterReply"));
        oper.setReturnClass(com.fedex.track.stub.SignatureProofOfDeliveryLetterReply.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "SignatureProofOfDeliveryLetterReply"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("track");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackRequest"), com.fedex.track.stub.TrackRequest.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackReply"));
        oper.setReturnClass(com.fedex.track.stub.TrackReply.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackReply"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("sendSignatureProofOfDeliveryFax");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "SignatureProofOfDeliveryFaxRequest"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "SignatureProofOfDeliveryFaxRequest"), com.fedex.track.stub.SignatureProofOfDeliveryFaxRequest.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "SignatureProofOfDeliveryFaxReply"));
        oper.setReturnClass(com.fedex.track.stub.SignatureProofOfDeliveryFaxReply.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "SignatureProofOfDeliveryFaxReply"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        _operations[3] = oper;

    }

    public TrackServiceSoapBindingStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public TrackServiceSoapBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public TrackServiceSoapBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "Address");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.Address.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "ArrivalLocationType");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.ArrivalLocationType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "CarrierCodeType");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.CarrierCodeType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "ClientDetail");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.ClientDetail.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "Contact");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.Contact.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "ContactAndAddress");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.ContactAndAddress.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "Dimensions");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.Dimensions.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "Distance");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.Distance.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "DistanceUnits");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.DistanceUnits.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "EMailNotificationDetail");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.EMailNotificationDetail.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "EMailNotificationEventType");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.EMailNotificationEventType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "EMailNotificationFormatType");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.EMailNotificationFormatType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "EMailNotificationRecipient");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.EMailNotificationRecipient.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "EMailNotificationRecipientType");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.EMailNotificationRecipientType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "LinearUnits");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.LinearUnits.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "Localization");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.Localization.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "Notification");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.Notification.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "NotificationParameter");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.NotificationParameter.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "NotificationSeverityType");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.NotificationSeverityType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "OfficeOrderDeliveryMethodType");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.OfficeOrderDeliveryMethodType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "OperatingCompanyType");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.OperatingCompanyType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "PackagingType");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.PackagingType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "QualifiedTrackingNumber");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.QualifiedTrackingNumber.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "RedirectToHoldEligibilityType");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.RedirectToHoldEligibilityType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "ServiceType");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.ServiceType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "SignatureProofOfDeliveryFaxReply");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.SignatureProofOfDeliveryFaxReply.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "SignatureProofOfDeliveryFaxRequest");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.SignatureProofOfDeliveryFaxRequest.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "SignatureProofOfDeliveryImageType");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.SignatureProofOfDeliveryImageType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "SignatureProofOfDeliveryLetterReply");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.SignatureProofOfDeliveryLetterReply.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "SignatureProofOfDeliveryLetterRequest");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.SignatureProofOfDeliveryLetterRequest.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "StringBarcode");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.StringBarcode.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "StringBarcodeType");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.StringBarcodeType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackDeliveryLocationType");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.TrackDeliveryLocationType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackDetail");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.TrackDetail.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackEvent");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.TrackEvent.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackIdentifierType");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.TrackIdentifierType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackNotificationPackage");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.TrackNotificationPackage.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackNotificationRecipientDetail");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.TrackNotificationRecipientDetail.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackNotificationReply");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.TrackNotificationReply.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackNotificationRequest");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.TrackNotificationRequest.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackPackageIdentifier");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.TrackPackageIdentifier.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackReconciliation");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.TrackReconciliation.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackReply");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.TrackReply.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackRequest");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.TrackRequest.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackReturnLabelType");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.TrackReturnLabelType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TrackSplitShipmentPart");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.TrackSplitShipmentPart.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "TransactionDetail");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.TransactionDetail.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "VersionId");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.VersionId.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "WebAuthenticationCredential");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.WebAuthenticationCredential.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "WebAuthenticationDetail");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.WebAuthenticationDetail.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "Weight");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.Weight.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://fedex.com/ws/track/v5", "WeightUnits");
            cachedSerQNames.add(qName);
            cls = com.fedex.track.stub.WeightUnits.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public com.fedex.track.stub.TrackNotificationReply getTrackNotification(com.fedex.track.stub.TrackNotificationRequest trackNotificationRequest) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("getTrackNotification");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "getTrackNotification"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {trackNotificationRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.fedex.track.stub.TrackNotificationReply) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.fedex.track.stub.TrackNotificationReply) org.apache.axis.utils.JavaUtils.convert(_resp, com.fedex.track.stub.TrackNotificationReply.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.fedex.track.stub.SignatureProofOfDeliveryLetterReply retrieveSignatureProofOfDeliveryLetter(com.fedex.track.stub.SignatureProofOfDeliveryLetterRequest signatureProofOfDeliveryLetterRequest) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("retrieveSignatureProofOfDeliveryLetter");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "retrieveSignatureProofOfDeliveryLetter"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {signatureProofOfDeliveryLetterRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.fedex.track.stub.SignatureProofOfDeliveryLetterReply) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.fedex.track.stub.SignatureProofOfDeliveryLetterReply) org.apache.axis.utils.JavaUtils.convert(_resp, com.fedex.track.stub.SignatureProofOfDeliveryLetterReply.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.fedex.track.stub.TrackReply track(com.fedex.track.stub.TrackRequest trackRequest) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("track");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "track"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {trackRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.fedex.track.stub.TrackReply) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.fedex.track.stub.TrackReply) org.apache.axis.utils.JavaUtils.convert(_resp, com.fedex.track.stub.TrackReply.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

    public com.fedex.track.stub.SignatureProofOfDeliveryFaxReply sendSignatureProofOfDeliveryFax(com.fedex.track.stub.SignatureProofOfDeliveryFaxRequest signatureProofOfDeliveryFaxRequest) throws java.rmi.RemoteException {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("sendSignatureProofOfDeliveryFax");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "sendSignatureProofOfDeliveryFax"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {signatureProofOfDeliveryFaxRequest});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.fedex.track.stub.SignatureProofOfDeliveryFaxReply) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.fedex.track.stub.SignatureProofOfDeliveryFaxReply) org.apache.axis.utils.JavaUtils.convert(_resp, com.fedex.track.stub.SignatureProofOfDeliveryFaxReply.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
  throw axisFaultException;
}
    }

}
