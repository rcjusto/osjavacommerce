package org.store.carriers.fedex;


import org.store.carriers.fedex.common.FedexPackage;
import org.store.core.utils.carriers.CarrierMethod;
import org.store.core.utils.carriers.CarrierPropertyGroup;
import org.store.core.utils.carriers.RateService;
import org.store.core.utils.carriers.RateServiceResponse;
import org.store.core.utils.carriers.ShipTo;
import org.store.core.utils.carriers.ShippingResult;
import org.store.core.utils.carriers.TrackResponse;
import org.store.core.utils.carriers.VoidResponse;
import org.store.core.utils.carriers.velocity.VelocityGenerator;
import org.store.core.utils.carriers.Shipper;
import org.store.carriers.fedex.cl.FedexCloseRequest;
import org.store.carriers.fedex.cl.FedexCloseResponse;
import org.store.carriers.fedex.rs.FedexRateServicesRequest;
import org.store.carriers.fedex.rs.FedexRateServicesResponse;
import org.store.carriers.fedex.sb.FedexSubscriptionRequest;
import org.store.carriers.fedex.sb.FedexSubscriptionResponse;
import org.store.carriers.fedex.sr.FedexShipRequest;
import org.store.carriers.fedex.sr.FedexShipResponse;
import org.store.carriers.fedex.tr.FedexTrackRequest;
import org.store.carriers.fedex.tr.FedexTrackResponse;
import org.store.carriers.fedex.vo.FedexVoidRequest;
import org.store.carriers.fedex.vo.FedexVoidResponse;
import org.store.core.globals.SomeUtils;
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
public class CarrierServiceFedexImpl implements org.store.core.utils.carriers.CarrierService {
    private String status;

    public static Logger log = Logger.getLogger(CarrierServiceFedexImpl.class);
    public static final String URL = "https://gateway.fedex.com:443/GatewayDC";


    private Properties properties;

    public CarrierServiceFedexImpl() {
    }

    public CarrierServiceFedexImpl(Properties props) {
        this.properties = props;
    }

    public RateServiceResponse getRateServices(ShipTo shipTo, List<org.store.core.utils.carriers.BasePackage> packages) {
        return getRateServices(shipTo, null, packages);
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public RateServiceResponse getRateServices(org.store.core.utils.carriers.ShipTo shipTo, Shipper shipFrom, List<org.store.core.utils.carriers.BasePackage> packages) {
        RateServiceResponse resultado = new RateServiceResponse();

        // Buscar las opciones de shipping
        FedexRateServicesRequest rs = new FedexRateServicesRequest(status, properties);

        rs.setDestinationAddress(shipTo.getAddress());
        if (shipFrom != null) rs.setOriginAddress(shipFrom.getAddress());

        if (packages.size() > 0) {
            FedexPackage pack = new FedexPackage(packages.get(0), properties);
            rs.setPack(pack);
        }

        try {
            rs.setCarrierCode("FDXG");
            FedexRateServicesResponse resRS_FDXG = rs.execute();
            if (!resRS_FDXG.hasError() && resRS_FDXG.getRateServices() != null) {
                for (RateService rateService : resRS_FDXG.getRateServices()) {
                    resultado.addRateService(rateService);
                }
            }
            rs.setCarrierCode("FDXE");
            FedexRateServicesResponse resRS_FDXE = rs.execute();
            if (!resRS_FDXE.hasError() && resRS_FDXE.getRateServices() != null) {
                for (RateService rateService : resRS_FDXE.getRateServices()) {
                    resultado.addRateService(rateService);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e); 
            resultado.setErrors("Can not connect with FEDEX service.");
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
            resultado.setErrors("Error connecting with FEDEX service.");
        }
        return resultado;
    }

    public org.store.core.utils.carriers.ShippingResult generateShipping(org.store.core.utils.carriers.ShipTo shipTo, List<org.store.core.utils.carriers.BasePackage> packages, String shippingMethod, String imgPath) {
        return generateShipping(shipTo, null, packages, shippingMethod, imgPath, Calendar.getInstance().getTime());
    }

    public org.store.core.utils.carriers.ShippingResult generateShipping(ShipTo shipTo, Shipper shipFrom, List<org.store.core.utils.carriers.BasePackage> packages, String shippingMethod, String imgPath, Date shipDate) {
        log.info("-> Genarating shipping 1");
        ShippingResult result = new org.store.core.utils.carriers.ShippingResult();
        FedexShipRequest req = new FedexShipRequest( status, properties);
        log.info("-> Genarating shipping 2");

        req.setDestinationAddress(shipTo);
        if (shipFrom != null) {
            req.setOriginAddress(shipFrom);
        }
        req.setShipDate(shipDate);
        if (SomeUtils.dayDiff( shipDate, Calendar.getInstance().getTime())!=0) req.setFutureDayShipment("true");
        req.setService(shippingMethod);
        req.setPack(new FedexPackage(packages.get(0), properties));
        log.info("-> Genarating shipping 3");

        try {
            FedexShipResponse res = req.execute();
            log.info("-> Genarating shipping 4");

            if (!res.hasError()) {
                result.setShippingNumber(res.getTrackingNumber());
                result.setShipmentCharge(res.getNetCharge());
                org.store.core.utils.carriers.BasePackage pack = packages.get(0);
                result.getTrackingNumbers().put(pack.getProductId(), res.getTrackingNumber());
                res.saveGraphicImage(imgPath + res.getTrackingNumber() + ".png");
            } else {
                result.addError("FEDEX", res.getErrorCode(), res.getErrorDescription());
                log.error(res.getErrorCode() + " - " + res.getErrorDescription());
            }
        } catch (IOException e) {
            result.addError("FEDEX", "Exception", e.toString());
            log.error(e.toString());
        } catch (Exception e) {
            result.addError("FEDEX", "Exception", e.toString());
            log.error(e.toString());
        }

        return result;
    }

    public org.store.core.utils.carriers.VoidResponse makeVoid(String trackingNumber, String shippingMethod) {
        VoidResponse res = new VoidResponse(false);
        FedexVoidResponse response = null;
        FedexVoidRequest req = new FedexVoidRequest( status, properties);
        req.setCarrierCode(getCarrierCodeByService(shippingMethod));
        req.setTrackingNumber(trackingNumber);
        try {
            response = req.execute();
        } catch (IOException e) {
            log.error(e.getMessage(), e); 
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }
        if (response != null && !response.hasError()) {
            res.setAcepted(true);
        } else if (response != null) {
            res.setErrorCode(response.getErrorCode());
            res.setErrorDesc(response.getErrorDescription());
        }
        return res;
    }

    public String getHTMLTracking(String trackingNumber, String shipMethod) {
        org.store.core.utils.carriers.TrackResponse tr = getTracking(trackingNumber, shipMethod);
        VelocityContext context = new VelocityContext();
        context.put("bean", tr);
        context.put("tool", new org.store.core.utils.carriers.velocity.CarrierVelocityTool());
        String templateName = "fedex/fedex_trackresponse.vm";
        return VelocityGenerator.getGeneratedXml(context, templateName);
    }

    public TrackResponse getTracking(String trackingNumber, String shipMethod) {
        FedexTrackResponse res = null;
        FedexTrackRequest req = new FedexTrackRequest( status, properties);
        try {
            res = req.execute();
        } catch (IOException e) {
            log.error(e.getMessage(), e); 
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }
        return res;
    }

    public boolean available(org.store.core.utils.carriers.ShipTo shipTo, List<org.store.core.utils.carriers.BasePackage> packages) {
        return packages != null && packages.size() == 1;
    }

    public List<CarrierPropertyGroup> getPropertyNames() {
        List<CarrierPropertyGroup> res = new ArrayList<CarrierPropertyGroup>();
        res.add( new CarrierPropertyGroup("shipper")
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
                .addProperty("AccountNumber")
                .addProperty("MeterNumber")
        );
        res.add(new CarrierPropertyGroup("options")
                .addProperty("WeightUnits","LBS,KGS")
                .addProperty("DimentionsUnits","IN,CM")
                .addProperty("HoldAtLocation","true,false")
                .addProperty("DryIce","true,false")
                .addProperty("ResidentialDelivery","true,false")
                .addProperty("InsidePickup","true,false")
                .addProperty("InsideDelivery","true,false")
                .addProperty("SaturdayPickup","true,false")
                .addProperty("SaturdayDelivery","true,false")
        );
        return res;
    }

    public List<CarrierMethod> getShippingMethods() {
        List<CarrierMethod> res = new ArrayList<CarrierMethod>();
        res.add(new CarrierMethod("FEDEX2DAY","FedEx 2Day"));
        res.add(new CarrierMethod("FEDEXEXPRESSSAVER","FedEx Express Saver"));
        res.add(new CarrierMethod("FEDEXGROUND","FedEx Ground"));
        res.add(new CarrierMethod("FIRSTOVERNIGHT","FedEx First Overnight"));
        res.add(new CarrierMethod("GROUNDHOMEDELIVERY","FedEx Home Delivery"));
        res.add(new CarrierMethod("GROUNDHOMEDELIVERY","FedEx Priority Overnight"));
        res.add(new CarrierMethod("STANDARDOVERNIGHT","FedEx Standard Overnight"));
        return res;
    }

    public void setProperties(Properties p) {
        properties = p;
    }

    public String getName() {
        return "OLD-FEDEX";
    }

    public String makeSubscription(Shipper shipFrom) {
        String res;
        FedexSubscriptionRequest request = new FedexSubscriptionRequest( status, properties);
        if (shipFrom != null) request.setShipper(shipFrom);

        try {
            FedexSubscriptionResponse resp = request.execute();
            res = resp.getXml();
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
            res = e.toString();
        }
        return res;
    }

    public String makeClose(String carrier, Date shipDate, String manifestPath) {
        String filename;
        FedexCloseRequest request = new FedexCloseRequest( status, properties);
        if (carrier != null) request.setCarrierCode(carrier);
        if (shipDate != null) request.setDate(shipDate);

        try {
            FedexCloseResponse resp = request.execute();
            if (!resp.hasError()) {
                filename = manifestPath + request.getDateFN() + "_" + resp.getFilename() + ".txt";
                resp.saveManifest(filename);
            }
            else filename = resp.getErrorDescription();
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
            filename = e.toString();
        }
        return filename;
    }

    private String getCarrierCodeByService(String service) {
        String carrierCode;
        if ("FEDEXGROUND".equalsIgnoreCase(service)) carrierCode = "FDXG";
        else if ("GROUNDHOMEDELIVERY".equalsIgnoreCase(service)) carrierCode = "FDXG";
        else carrierCode = "FDXE";
        return carrierCode;
    }


}
