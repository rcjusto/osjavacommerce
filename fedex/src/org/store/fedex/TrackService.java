package org.store.fedex;

import com.fedex.track.stub.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.*;

/**
 * Rogelio Caballero
 * 7/06/12 0:18
 */
public class TrackService {
    public static Logger log = Logger.getLogger(FedexServiceImpl.class);
    private static final String SERVICE_END_POINT_TEST = "https://wsbeta.fedex.com:443/web-services/track";
    private static final String SERVICE_END_POINT_LIVE = "https://ws.fedex.com/web-services/track";

    private Properties properties;
    private List<PackageTracking> result; 
    private String errors;
    
    public TrackService(Properties properties) {
        this.properties = properties;
    }
    
    public TrackService(Properties properties, String trackingNumber) {
        this.properties = properties;
        getTracking(trackingNumber);
    }

    public void getTracking(String trackingNumber) {
        TrackRequest request = new TrackRequest();
        request.setClientDetail(getClientDetail());
        request.setWebAuthenticationDetail(createWebAuthenticationDetail());
        TransactionDetail transactionDetail = new TransactionDetail();
        transactionDetail.setCustomerTransactionId("Tracking Request");
        request.setTransactionDetail(transactionDetail);
        request.setVersion(new VersionId("trck", 5, 0, 0));

        TrackPackageIdentifier packageIdentifier = new TrackPackageIdentifier();
        packageIdentifier.setValue(trackingNumber);
        packageIdentifier.setType(TrackIdentifierType.TRACKING_NUMBER_OR_DOORTAG);
        request.setPackageIdentifier(packageIdentifier);
        request.setIncludeDetailedScans(Boolean.TRUE);

        try {
            TrackServiceLocator service = new TrackServiceLocator();
            service.setTrackServicePortEndpointAddress( ("live".equalsIgnoreCase(properties.getProperty("Mode"))) ? SERVICE_END_POINT_LIVE : SERVICE_END_POINT_TEST);
            TrackPortType port = service.getTrackServicePort();
            TrackReply reply = port.track(request);
            if (isResponseOk(reply.getHighestSeverity())) {
                if (reply.getTrackDetails() != null && reply.getTrackDetails().length>0) {
                    result = new ArrayList<PackageTracking>();
                    for (TrackDetail aTd : reply.getTrackDetails()) {
                        PackageTracking pt = new PackageTracking();
                        if (aTd.getPackageSequenceNumber()!=null) pt.number = aTd.getPackageSequenceNumber().longValue();
                        pt.tracking = aTd.getTrackingNumber();
                        pt.statusCode = aTd.getStatusCode();
                        pt.statusDesc = aTd.getStatusDescription();
                        pt.statusDesc = aTd.getStatusDescription();
                        if (aTd.getShipTimestamp()!=null) pt.shipDate = aTd.getShipTimestamp().getTime();
                        
                        if (aTd.getEvents() != null) {
                            for (TrackEvent trackEvent : aTd.getEvents()) {
                                if (trackEvent != null) {
                                    Map<String, Serializable> map = new HashMap<String, Serializable>();
                                    map.put("Timestamp", trackEvent.getTimestamp().getTime());
                                    map.put("Description", trackEvent.getEventDescription());
                                    Address address = trackEvent.getAddress();
                                    if (address != null) {
                                        System.out.println(address.getCity());
                                        map.put("City", address.getCity());
                                        map.put("State", address.getStateOrProvinceCode());
                                    }
                                    pt.details.add(map);
                                }
                            }
                        }
                        result.add(pt);
                    }
                }
            } else {
                if (reply.getNotifications() != null && reply.getNotifications().length > 0) {
                    StringBuilder b = new StringBuilder();
                    for (Notification n : reply.getNotifications()) {
                        if (n != null) {
                            b.append("(").append(n.getCode()).append(") ").append(n.getMessage()).append("\n");
                        }
                    }
                    if (StringUtils.isNotEmpty(b.toString())) errors = b.toString();
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private WebAuthenticationDetail createWebAuthenticationDetail() {
        WebAuthenticationCredential wac = new WebAuthenticationCredential();
        String mode = properties.getProperty("Mode");
        if (StringUtils.isEmpty(mode)) mode = "test";
        wac.setKey(properties.getProperty(mode + ".CustomerKey"));
        wac.setPassword(properties.getProperty(mode + ".CustomerPassword"));
        return new WebAuthenticationDetail(wac);
    }

    private ClientDetail getClientDetail() {
        ClientDetail cd = new ClientDetail();
        String mode = properties.getProperty("Mode");
        if (StringUtils.isEmpty(mode)) mode = "test";
        cd.setAccountNumber(properties.getProperty(mode + ".AccountNumber"));
        cd.setMeterNumber(properties.getProperty(mode + ".MeterNumber"));
        return cd;
    }

    private boolean isResponseOk(NotificationSeverityType notificationSeverityType) {
        return notificationSeverityType != null && (notificationSeverityType.equals(NotificationSeverityType.WARNING) || notificationSeverityType.equals(NotificationSeverityType.NOTE) || notificationSeverityType.equals(NotificationSeverityType.SUCCESS));
    }

    public class PackageTracking {
        private Long number;
        private String tracking;
        private String statusCode;
        private String statusDesc;
        private Date shipDate;
        private List<Map<String, Serializable>> details = new ArrayList<Map<String, Serializable>>();

        public Long getNumber() {
            return number;
        }

        public void setNumber(Long number) {
            this.number = number;
        }

        public String getTracking() {
            return tracking;
        }

        public void setTracking(String tracking) {
            this.tracking = tracking;
        }

        public String getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(String statusCode) {
            this.statusCode = statusCode;
        }

        public String getStatusDesc() {
            return statusDesc;
        }

        public void setStatusDesc(String statusDesc) {
            this.statusDesc = statusDesc;
        }

        public Date getShipDate() {
            return shipDate;
        }

        public void setShipDate(Date shipDate) {
            this.shipDate = shipDate;
        }

        public List<Map<String, Serializable>> getDetails() {
            return details;
        }

        public void setDetails(List<Map<String, Serializable>> details) {
            this.details = details;
        }
    }

    public List<PackageTracking> getResult() {
        return result;
    }

    public String getErrors() {
        return errors;
    }
}
