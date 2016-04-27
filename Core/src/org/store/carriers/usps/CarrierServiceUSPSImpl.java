package org.store.carriers.usps;


import org.store.carriers.usps.common.USPSPackage;
import org.store.core.utils.carriers.BasePackage;
import org.store.core.utils.carriers.CarrierMethod;
import org.store.core.utils.carriers.CarrierPropertyGroup;
import org.store.core.utils.carriers.CarrierService;
import org.store.core.utils.carriers.RateServiceResponse;
import org.store.core.utils.carriers.ShippingResult;
import org.store.core.utils.carriers.VoidResponse;
import org.store.core.utils.carriers.velocity.CarrierVelocityTool;
import org.store.core.utils.carriers.Shipper;
import org.store.carriers.usps.dc.USPSDeliveryConfirmRequest;
import org.store.carriers.usps.dc.USPSDeliveryConfirmResponse;
import org.store.carriers.usps.rs.UspsRateServicesRequestD;
import org.store.carriers.usps.rs.UspsRateServicesRequestI;
import org.store.carriers.usps.rs.UspsRateServicesResponseD;
import org.store.carriers.usps.rs.UspsRateServicesResponseI;
import org.store.carriers.usps.tr.USPSTrackRequest;
import org.store.carriers.usps.tr.USPSTrackResponse;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 30-nov-2006
 */
public class CarrierServiceUSPSImpl implements CarrierService {
    public static Logger log = Logger.getLogger(CarrierServiceUSPSImpl.class);
    private Properties properties;

    public CarrierServiceUSPSImpl() {
    }

    public CarrierServiceUSPSImpl(Properties props) {
        this.properties = props;
    }

    public RateServiceResponse getRateServices(org.store.core.utils.carriers.ShipTo shipTo, Shipper shipFrom, List<org.store.core.utils.carriers.BasePackage> packages) {
        RateServiceResponse resultado = new RateServiceResponse();

        if ("US".equalsIgnoreCase(shipTo.getAddress().getCountryCode())) {
            UspsRateServicesRequestD rs = new UspsRateServicesRequestD(properties);
            rs.setDestinationAddress(shipTo.getAddress());
            if (shipFrom != null) rs.setOriginAddress(shipFrom.getAddress());
            rs.setPackages(new ArrayList<USPSPackage>());
            for (org.store.core.utils.carriers.BasePackage packObj : packages)
                rs.getPackages().add(new USPSPackage(packObj));
            try {
                UspsRateServicesResponseD resRS = rs.execute();
                if (!resRS.hasError()) {
                    for (org.store.core.utils.carriers.RateService rateService : resRS.getRateServices()) {
                        resultado.addRateService(rateService);
                    }
                } else {
                    resultado.setErrors(resRS.getErrorDescription());
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e); 
                resultado.setErrors("Can not connect with USPS service.");
            } catch (Exception e) {
                log.error(e.getMessage(), e); 
                resultado.setErrors("Error connecting with USPS service.");
            }
        } else {
            UspsRateServicesRequestI rs = new UspsRateServicesRequestI(properties);
            rs.setDestinationAddress(shipTo.getAddress());
            if (shipFrom != null) rs.setOriginAddress(shipFrom.getAddress());
            rs.setPackages(new ArrayList<USPSPackage>());
            for (org.store.core.utils.carriers.BasePackage packObj : packages)
                rs.getPackages().add(new USPSPackage(packObj));
            try {
                UspsRateServicesResponseI resRS = rs.execute();
                if (!resRS.hasError()) {
                    for (org.store.core.utils.carriers.RateService rateService : resRS.getRateServices()) {
                        resultado.addRateService(rateService);
                    }
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e); 
                resultado.setErrors("Can not connect with USPS service.");
            } catch (Exception e) {
                log.error(e.getMessage(), e); 
                resultado.setErrors("Error connecting with UPS service.");
            }
        }
        return resultado;
    }

    public RateServiceResponse getRateServices(org.store.core.utils.carriers.ShipTo shipTo, List<org.store.core.utils.carriers.BasePackage> packages) {
        return getRateServices(shipTo, null, packages);
    }

    public ShippingResult generateShipping(org.store.core.utils.carriers.ShipTo shipTo, List<BasePackage> packages, String shippingMethod, String imgPath) {
        return generateShipping(shipTo, null, packages, shippingMethod, imgPath, Calendar.getInstance().getTime());
    }

    public org.store.core.utils.carriers.ShippingResult generateShipping(org.store.core.utils.carriers.ShipTo shipTo, org.store.core.utils.carriers.Shipper shipFrom, List<org.store.core.utils.carriers.BasePackage> packages, String shippingMethod, String imgPath, Date shipDate) {
        log.info("-> Genarating shipping 1");
        org.store.core.utils.carriers.ShippingResult result = new ShippingResult();
        USPSDeliveryConfirmRequest req = new USPSDeliveryConfirmRequest(properties);
        log.info("-> Genarating shipping 2");

        req.setDestinationAddress(shipTo);
        if (shipFrom != null) {
            req.setOriginAddress(shipFrom);
        }
        req.setServiceType(shippingMethod);
        req.setPackages(new USPSPackage(packages.get(0)));
        log.info("-> Genarating shipping 3");

        try {
            USPSDeliveryConfirmResponse res = req.execute();
            log.info("-> Genarating shipping 4");

            if (!res.hasError()) {
                result.setShippingNumber(res.getDeliveryConfirmationNumber());

                BasePackage pack = packages.get(0);
                result.getTrackingNumbers().put(pack.getProductId(), res.getDeliveryConfirmationNumber());
                res.saveGraphicImage(imgPath + res.getDeliveryConfirmationNumber() + ".pdf");

                // Buscar el costo
                if ("US".equalsIgnoreCase(shipTo.getAddress().getCountryCode())) {
                    UspsRateServicesRequestD rs = new UspsRateServicesRequestD(properties);
                    rs.setDestinationAddress(shipTo.getAddress());
                    if (shipFrom != null) rs.setOriginAddress(shipFrom.getAddress());
                    rs.setPackages(new ArrayList<USPSPackage>());
                    for (org.store.core.utils.carriers.BasePackage packObj : packages)
                        rs.getPackages().add(new USPSPackage(packObj));
                    rs.setServices(shippingMethod);
                    try {
                        UspsRateServicesResponseD resRS = rs.execute();
                        if (!resRS.hasError()) {
                            for (org.store.core.utils.carriers.RateService rateService : resRS.getRateServices()) {
                                    result.setShipmentCharge( rateService.getValue() );
                                    result.setShipmentChargeCurr( rateService.getCurrencyCode() );
                            }
                        }
                    } catch (IOException e) {
                        log.error(e.getMessage(), e); 
                    } catch (Exception e) {
                        log.error(e.getMessage(), e); 
                    }
                }


            } else {
                result.addError("USPS", res.getErrorCode(), res.getErrorDescription());
                log.error(res.getErrorCode() + " - " + res.getErrorDescription());
            }
        } catch (IOException e) {
            result.addError("USPS", "Exception", e.toString());
            log.error(e.toString());
        } catch (Exception e) {
            result.addError("USPS", "Exception", e.toString());
            log.error(e.toString());
        }

        return result;
    }

    public VoidResponse makeVoid(String trackingNumber, String shippingMethod) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public org.store.core.utils.carriers.TrackResponse getTracking(String trackingNumber) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getHTMLTracking(String trackingNumber, String shipMethod) {
        USPSTrackResponse res;
        USPSTrackRequest req = new USPSTrackRequest(properties);
        req.setShipmentIdentificationNumber(trackingNumber);
        try {
            res = req.execute();
            VelocityContext context = new VelocityContext();
            context.put("bean", res);
            context.put("tool", new CarrierVelocityTool());
            String templateName = "usps/usps_trackresponse.vm";
            return org.store.core.utils.carriers.velocity.VelocityGenerator.getGeneratedXml(context, templateName);

        } catch (IOException e) {
            log.error(e.getMessage(), e); 
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }
        return null;
    }

    public boolean available(org.store.core.utils.carriers.ShipTo shipTo, List<org.store.core.utils.carriers.BasePackage> packages) {
        for (org.store.core.utils.carriers.BasePackage aPackage : packages) {
            USPSPackage uspsPack = new USPSPackage(aPackage);
            if (!uspsPack.canApply()) return false;
        }
        return true;
    }

    public List<CarrierPropertyGroup> getPropertyNames() {
        List<CarrierPropertyGroup> res = new ArrayList<CarrierPropertyGroup>();
        res.add( new CarrierPropertyGroup("shipper")
                .addProperty("shipper.shipper.number")
                .addProperty("shipper.name")
                .addProperty("shipper.attention.name")
                .addProperty("shipper.address.line1")
                .addProperty("shipper.address.line2")
                .addProperty("shipper.address.city")
                .addProperty("shipper.address.state")
                .addProperty("shipper.address.postalcode")
                .addProperty("shipper.address.country")
                .addProperty("shipper.phone.number")
        );
        res.add(new CarrierPropertyGroup("credentials")
                .addProperty("user")
        );
        return res;
    }

    public List<CarrierMethod> getShippingMethods() {
        List<CarrierMethod> res = new ArrayList<CarrierMethod>();
        res.add(new CarrierMethod("Express Mail","Express Mail"));
        res.add(new CarrierMethod("Library Mail","Library Mail"));
        res.add(new CarrierMethod("Media Mail","Media Mail"));
        res.add(new CarrierMethod("Parcel Post","Parcel Post"));
        res.add(new CarrierMethod("Priority Mail","Priority Mail"));
        return res;
    }

    public void setProperties(Properties p) {
        properties = p;
    }

    public String getName() {
        return "USPS";
    }

}
