package org.store.fedex;

import com.fedex.rate.stub.*;
import org.apache.axis.types.NonNegativeInteger;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.store.core.globals.SomeUtils;
import org.store.core.utils.carriers.BasePackage;
import org.store.core.utils.carriers.RateServiceResponse;
import org.store.core.utils.carriers.ShipTo;
import org.store.core.utils.carriers.Shipper;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 * Rogelio Caballero
 * 7/06/12 0:13
 */
public class RateService {
    public static Logger log = Logger.getLogger(FedexServiceImpl.class);

    private Properties properties;
    private static final String SERVICE_END_POINT_TEST = "https://wsbeta.fedex.com:443/web-services/rate";
    private static final String SERVICE_END_POINT_LIVE = "https://ws.fedex.com/web-services/rate";

    public RateService(Properties properties) {
        this.properties = properties;
    }

    public RateServiceResponse getRateServices(ShipTo shipTo, Shipper shipFrom, List<BasePackage> packages) {
        log.debug("FEDEX - Rate Service");
        RateServiceResponse result = new RateServiceResponse();
        RateRequest request = new RateRequest();
        request.setClientDetail(getClientDetail());
        request.setWebAuthenticationDetail(getWebAuthenticationDetail());
        request.setTransactionDetail(new TransactionDetail("Rate Available Service Request", null));
        request.setVersion(new VersionId("crs", 10, 0, 0));
        request.setReturnTransitAndCommit(true);

        RequestedShipment requestedShipment = new RequestedShipment();
        requestedShipment.setSpecialServicesRequested(getSpecialServicesRequested());
        requestedShipment.setShipTimestamp(Calendar.getInstance());
        requestedShipment.setDropoffType(DropoffType.REGULAR_PICKUP);
        if (shipFrom == null) shipFrom = new Shipper(properties);
        requestedShipment.setShipper(getParty(shipFrom));
        requestedShipment.setRecipient(getParty(shipTo));
        requestedShipment.setShippingChargesPayment(new Payment(PaymentType.SENDER, null));
        List<RequestedPackageLineItem> items = new ArrayList<RequestedPackageLineItem>();
        for (BasePackage bp : packages) {
            items.add(getRequestedPackageLineItem(bp));
        }
        requestedShipment.setRequestedPackageLineItems(items.toArray(new RequestedPackageLineItem[items.size()]));
        requestedShipment.setPackageCount(new NonNegativeInteger(String.valueOf(packages.size())));
        requestedShipment.setRateRequestTypes(new RateRequestType[]{RateRequestType.ACCOUNT});
        request.setRequestedShipment(requestedShipment);
        log.debug("FEDEX :" + items.size() + " items");
        try {
            RateServiceLocator service = new RateServiceLocator();
            service.setRateServicePortEndpointAddress(("live".equalsIgnoreCase(properties.getProperty("Mode"))) ? SERVICE_END_POINT_LIVE : SERVICE_END_POINT_TEST);
            if (log.isDebugEnabled()) log.debug("FEDEX : Using " + properties.getProperty("Mode") + " configuration");

            RatePortType port = service.getRateServicePort();
            RateReply reply = port.getRates(request);
            if (isResponseOk(reply.getHighestSeverity())) {
                log.debug("FEDEX: Response OK");
                if (reply.getRateReplyDetails() != null && reply.getRateReplyDetails().length > 0) {
                    log.debug("FEDEX: " + reply.getRateReplyDetails().length + " services");
                    for (RateReplyDetail rrd : reply.getRateReplyDetails()) {
                        String code = rrd.getServiceType().getValue();
                        String transit = "";
                        if (rrd.getDeliveryTimestamp() != null) {
                            transit = String.valueOf(SomeUtils.dayDiff(SomeUtils.dateIni(Calendar.getInstance().getTime()), rrd.getDeliveryTimestamp().getTime()));
                        } else if (rrd.getTransitTime() != null) {
                            transit = rrd.getTransitTime().getValue();
                            if ("ONE_DAY".equalsIgnoreCase(transit)) transit = "1";
                            else if ("TWO_DAYS".equalsIgnoreCase(transit)) transit = "2";
                            else if ("THREE_DAYS".equalsIgnoreCase(transit)) transit = "3";
                            else if ("FOUR_DAYS".equalsIgnoreCase(transit)) transit = "4";
                            else if ("FIVE_DAYS".equalsIgnoreCase(transit)) transit = "5";
                            else if ("SIX_DAYS".equalsIgnoreCase(transit)) transit = "6";
                            else if ("SEVEN_DAYS".equalsIgnoreCase(transit)) transit = "7";
                        }
                        String currencyCode = null;
                        double total = 0;
                        if (rrd.getRatedShipmentDetails() != null && rrd.getRatedShipmentDetails().length > 0) {
                            for (RatedShipmentDetail rsd : rrd.getRatedShipmentDetails()) {
                                if (rsd.getShipmentRateDetail() != null) {
                                    if (rsd.getShipmentRateDetail().getTotalSurcharges() != null && rsd.getShipmentRateDetail().getTotalSurcharges().getAmount() != null) {
                                        total += rsd.getShipmentRateDetail().getTotalSurcharges().getAmount().doubleValue();
                                        currencyCode = rsd.getShipmentRateDetail().getTotalSurcharges().getCurrency();
                                    }
                                    if (rsd.getShipmentRateDetail().getTotalNetCharge() != null && rsd.getShipmentRateDetail().getTotalNetCharge().getAmount() != null) {
                                        total += rsd.getShipmentRateDetail().getTotalNetCharge().getAmount().doubleValue();
                                        currencyCode = rsd.getShipmentRateDetail().getTotalNetCharge().getCurrency();
                                    }
                                }
                            }
                        }
                        if (total > 0) {
                            if (StringUtils.isEmpty(currencyCode)) currencyCode = "USD";
                            result.addRateService(new org.store.core.utils.carriers.RateService(code, currencyCode.toUpperCase(), formatMoney(total), transit));
                            log.debug("FEDEX: Service OK -> " + code);
                        } else {
                            log.error("FEDEX: Method " + code + " without cost.");
                            result.setErrors("Method " + code + " without cost.");
                        }
                    }
                }
            } else {
                if (reply.getNotifications() != null && reply.getNotifications().length > 0) {
                    StringBuilder b = new StringBuilder();
                    for (Notification n : reply.getNotifications()) {
                        if (n != null) {
                            log.error("FEDEX NOTIFICATION: " + n.getMessage());
                            b.append("(").append(n.getCode()).append(") ").append(n.getMessage()).append("\n");
                        }
                    }
                    if (StringUtils.isNotEmpty(b.toString())) result.setErrors(b.toString());
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            result.setErrors("Error connecting with FEDEX service.");
        }
        return result;
    }

    private ClientDetail getClientDetail() {
        ClientDetail cd = new ClientDetail();
        String mode = properties.getProperty("Mode");
        if (StringUtils.isEmpty(mode)) mode = "test";
        cd.setAccountNumber(properties.getProperty(mode + ".AccountNumber"));
        cd.setMeterNumber(properties.getProperty(mode + ".MeterNumber"));
        return cd;
    }

    private WebAuthenticationDetail getWebAuthenticationDetail() {
        WebAuthenticationCredential wac = new WebAuthenticationCredential();
        String mode = properties.getProperty("Mode");
        if (StringUtils.isEmpty(mode)) mode = "test";
        wac.setKey(properties.getProperty(mode + ".CustomerKey"));
        wac.setPassword(properties.getProperty(mode + ".CustomerPassword"));
        return new WebAuthenticationDetail(wac);
    }

    private Party getParty(Shipper shipper) {
        Party party = new Party();
        party.setAddress(getAddress(shipper.getAddress()));
        party.setAccountNumber(shipper.getShipperNumber());
        Contact contact = new Contact();
        contact.setEMailAddress(shipper.getEmail());
        contact.setPhoneNumber(shipper.getPhoneNumber());
        contact.setPersonName(shipper.getAttentionName());
        party.setContact(contact);
        return party;
    }

    private Party getParty(ShipTo shipTo) {
        Party party = new Party();
        party.setAddress(getAddress(shipTo.getAddress()));
        Contact contact = new Contact();
        contact.setCompanyName(shipTo.getCompanyName());
        if (shipTo.getPhoneNumber() != null) contact.setPhoneNumber(shipTo.getPhoneNumber().toString(""));
        contact.setPersonName(shipTo.getAttentionName());
        party.setContact(contact);
        return party;
    }

    private Address getAddress(org.store.core.utils.carriers.Address input) {
        Address address = new Address();
        address.setCity(input.getCity());
        address.setCountryCode(input.getCountryCode());
        address.setPostalCode(input.getPostalCode());
        //    address.setResidential(input.());
        address.setStateOrProvinceCode(input.getStateProvinceCode());
        List<String> lines = new ArrayList<String>();
        if (StringUtils.isNotEmpty(input.getAddressLine1())) lines.add(input.getAddressLine1());
        if (StringUtils.isNotEmpty(input.getAddressLine2())) lines.add(input.getAddressLine2());
        if (!lines.isEmpty()) address.setStreetLines(lines.toArray(new String[lines.size()]));
        return address;
    }

    private RequestedPackageLineItem getRequestedPackageLineItem(BasePackage bp) {
        RequestedPackageLineItem item = new RequestedPackageLineItem();
        item.setGroupPackageCount(new NonNegativeInteger("1"));
        item.setWeight(getWeight(bp));
        item.setInsuredValue(new Money(bp.getCurrencyCode(), new BigDecimal(bp.getInsuredValueMonetaryValue())));
        item.setDimensions(getDimensions(bp));
        return item;
    }

    private Weight getWeight(BasePackage bp)
    {
        if ("Pound".equalsIgnoreCase(bp.getWeightUnit())) {
            return new Weight(WeightUnits.KG, new BigDecimal(bp.getPackageWeight() * 0.453592D));
        }
        return new Weight(WeightUnits.KG, new BigDecimal(bp.getPackageWeight()));
    }

    private Dimensions getDimensions(BasePackage bp) {
        if ("Inch".equalsIgnoreCase(bp.getDimensionUnit())) {
            NonNegativeInteger l = new NonNegativeInteger(String.valueOf(Double.valueOf(Math.ceil(bp.getDimensionsLength() * 2.54D)).intValue()));
            NonNegativeInteger w = new NonNegativeInteger(String.valueOf(Double.valueOf(Math.ceil(bp.getDimensionsWidth() * 2.54D)).intValue()));
            NonNegativeInteger h = new NonNegativeInteger(String.valueOf(Double.valueOf(Math.ceil(bp.getDimensionsHeight() * 2.54D)).intValue()));
            return new Dimensions(l, w, h, LinearUnits.CM);
        } else if ("cm".equalsIgnoreCase(bp.getDimensionUnit())) {
            NonNegativeInteger l = new NonNegativeInteger(String.valueOf(((Double) Math.ceil(bp.getDimensionsLength())).intValue()));
            NonNegativeInteger w = new NonNegativeInteger(String.valueOf(((Double) Math.ceil(bp.getDimensionsWidth())).intValue()));
            NonNegativeInteger h = new NonNegativeInteger(String.valueOf(((Double) Math.ceil(bp.getDimensionsHeight())).intValue()));
            return new Dimensions(l, w, h, LinearUnits.CM);
        } else {
            NonNegativeInteger l = new NonNegativeInteger(String.valueOf(((Double) Math.ceil(bp.getDimensionsLength() / 100)).intValue()));
            NonNegativeInteger w = new NonNegativeInteger(String.valueOf(((Double) Math.ceil(bp.getDimensionsWidth() / 100)).intValue()));
            NonNegativeInteger h = new NonNegativeInteger(String.valueOf(((Double) Math.ceil(bp.getDimensionsHeight() / 100)).intValue()));
            return new Dimensions(l, w, h, LinearUnits.CM);
        }
    }

    private ShipmentSpecialServicesRequested getSpecialServicesRequested() {
        List<ShipmentSpecialServiceType> list = new ArrayList<ShipmentSpecialServiceType>();
        if ("true".equalsIgnoreCase(properties.getProperty("HoldAtLocation"))) list.add(ShipmentSpecialServiceType.HOLD_AT_LOCATION);
        if ("true".equalsIgnoreCase(properties.getProperty("InsidePickup"))) list.add(ShipmentSpecialServiceType.INSIDE_PICKUP);
        if ("true".equalsIgnoreCase(properties.getProperty("InsideDelivery"))) list.add(ShipmentSpecialServiceType.INSIDE_DELIVERY);
        if ("true".equalsIgnoreCase(properties.getProperty("SaturdayPickup"))) list.add(ShipmentSpecialServiceType.SATURDAY_PICKUP);
        if ("true".equalsIgnoreCase(properties.getProperty("SaturdayDelivery"))) list.add(ShipmentSpecialServiceType.SATURDAY_DELIVERY);
        if (!list.isEmpty()) {
            ShipmentSpecialServicesRequested result = new ShipmentSpecialServicesRequested();
            result.setSpecialServiceTypes(list.toArray(new ShipmentSpecialServiceType[list.size()]));
            return result;
        }
        return null;
    }

    private boolean isResponseOk(NotificationSeverityType notificationSeverityType) {
        return notificationSeverityType != null && (notificationSeverityType.equals(NotificationSeverityType.WARNING) || notificationSeverityType.equals(NotificationSeverityType.NOTE) || notificationSeverityType.equals(NotificationSeverityType.SUCCESS));
    }

    private String formatMoney(double total) {
        DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", dfs);
        return df.format(total);
    }

}
