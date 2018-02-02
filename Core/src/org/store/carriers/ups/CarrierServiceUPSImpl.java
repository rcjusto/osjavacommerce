package org.store.carriers.ups;


import org.store.carriers.ups.common.UPSPackage;
import org.store.core.utils.carriers.BasePackage;
import org.store.core.utils.carriers.CarrierMethod;
import org.store.core.utils.carriers.CarrierPropertyGroup;
import org.store.core.utils.carriers.CarrierService;
import org.store.core.utils.carriers.RateService;
import org.store.core.utils.carriers.RateServiceResponse;
import org.store.core.utils.carriers.ShipTo;
import org.store.core.utils.carriers.ShippingResult;
import org.store.core.utils.carriers.VoidResponse;
import org.store.core.utils.carriers.velocity.CarrierVelocityTool;
import org.store.carriers.ups.rs.UPSRateServicesRequest;
import org.store.carriers.ups.rs.UPSRateServicesResponse;
import org.store.carriers.ups.sa.PackageResult;
import org.store.carriers.ups.sa.UPSShipAcceptRequest;
import org.store.carriers.ups.sa.UPSShipAcceptResponse;
import org.store.carriers.ups.sc.UPSShipConfirmRequest;
import org.store.carriers.ups.sc.UPSShipConfirmResponse;
import org.store.carriers.ups.tr.UPSTrackRequest;
import org.store.carriers.ups.tr.UPSTrackResponse;
import org.store.carriers.ups.vo.UPSVoidRequest;
import org.store.carriers.ups.vo.UPSVoidResponse;
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
public class CarrierServiceUPSImpl implements CarrierService {
    private String status;
    public static Logger log = Logger.getLogger(CarrierServiceUPSImpl.class);
    private Properties properties;

    public CarrierServiceUPSImpl() {
    }

    public CarrierServiceUPSImpl(Properties props) {
        this.properties = props;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public org.store.core.utils.carriers.RateServiceResponse getRateServices(ShipTo shipTo, List<org.store.core.utils.carriers.BasePackage> packages) {
        return getRateServices(shipTo, null, packages);
    }

    public org.store.core.utils.carriers.RateServiceResponse getRateServices(ShipTo shipTo, org.store.core.utils.carriers.Shipper shipFrom, List<org.store.core.utils.carriers.BasePackage> packages) {
        org.store.core.utils.carriers.RateServiceResponse resultado = new RateServiceResponse();

        // Buscar las opciones de shipping
        UPSRateServicesRequest rs = new UPSRateServicesRequest(status, properties);

        rs.setShipTo(shipTo);
        if (shipFrom != null) rs.setShipper(shipFrom);

        for (org.store.core.utils.carriers.BasePackage aPackage : packages)
            rs.addPackage(new UPSPackage(aPackage, properties));

        try {
            UPSRateServicesResponse resRS = rs.execute();
            if (!resRS.hasError()) {
                for (RateService rateService : resRS.getRateServices()) {
                    resultado.addRateService(rateService);
                }
            } else {
                resultado.setErrors(resRS.getErrorDescription());
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            resultado.setErrors("Can not connect with UPS service.");
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
            resultado.setErrors("Error connecting with UPS service.");
        }
        return resultado;
    }

    public org.store.core.utils.carriers.ShippingResult generateShipping(ShipTo shipTo, List<org.store.core.utils.carriers.BasePackage> packages, String shippingMethod, String imgPath) {
        return generateShipping(shipTo, null, packages, shippingMethod, imgPath, Calendar.getInstance().getTime());
    }

    public ShippingResult generateShipping(ShipTo shipTo, org.store.core.utils.carriers.Shipper shipFrom, List<org.store.core.utils.carriers.BasePackage> packages, String shippingMethod, String imgPath, Date shipDate) {
        log.info("-> Genarating shipping 1");
        org.store.core.utils.carriers.ShippingResult result = new ShippingResult();
        UPSShipConfirmRequest req = new UPSShipConfirmRequest(status, properties);
        log.info("-> Genarating shipping 2");

        req.setShipTo(shipTo);
        if (shipFrom != null) req.setShipper(shipFrom);
        req.setServiceCode(shippingMethod);
        for (org.store.core.utils.carriers.BasePackage aPackage : packages) req.addPackage(new UPSPackage(aPackage, properties));
        log.info("-> Genarating shipping 3");

        try {
            UPSShipConfirmResponse res = req.execute();
            log.info("-> Genarating shipping 4");

            if (res.getShipmentDigest() != null) {
                result = ShipAcceptNoThread(res, packages, imgPath);
            } else {
                result.addError("UPS", res.getErrorCode(), res.getErrorDescription());
                log.error(res.getErrorCode() + "-" + res.getErrorDescription());
            }
        } catch (IOException e) {
            result.addError("UPS", "Exception", e.toString());
            log.error(e.toString());
        } catch (Exception e) {
            result.addError("UPS", "Exception", e.toString());
            log.error(e.toString());
        }

        return result;
    }

    public VoidResponse makeVoid(String trackingNumber, String shippingMethod) {
        VoidResponse res = new org.store.core.utils.carriers.VoidResponse(false);
        UPSVoidResponse response = null;
        UPSVoidRequest req = new UPSVoidRequest(status, properties);
        req.setShipmentIdentificationNumber(trackingNumber);
        try {
            response = req.execute();
        } catch (IOException e) {
            log.error(e.getMessage(), e); 
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }
        if (response != null && response.getResponseStatusCode() != null && "1".equals(response.getResponseStatusCode().trim())) {
            res.setAcepted(true);
        } else if (response != null) {
            res.setErrorCode(response.getErrorCode());
            res.setErrorDesc(response.getErrorDescription());
        }
        return res;
    }

    public org.store.core.utils.carriers.TrackResponse getTracking(String trackingNumber) {
        UPSTrackResponse res = null;
        UPSTrackRequest req = new UPSTrackRequest(status, properties);
        req.setShipmentIdentificationNumber(trackingNumber);
        try {
            res = req.execute();
        } catch (IOException e) {
            log.error(e.getMessage(), e); 
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }
        return res;
    }

    public String getHTMLTracking(String trackingNumber, String shipMethod) {
        org.store.core.utils.carriers.TrackResponse tr = getTracking(trackingNumber);
        VelocityContext context = new VelocityContext();
        context.put("bean", tr);
        context.put("tool", new CarrierVelocityTool());
        String templateName = "ups/ups_trackresponse.vm";
        return org.store.core.utils.carriers.velocity.VelocityGenerator.getGeneratedXml(context, templateName);
    }

    public boolean available(org.store.core.utils.carriers.ShipTo shipTo, List<org.store.core.utils.carriers.BasePackage> packages) {
        for (org.store.core.utils.carriers.BasePackage aPackage : packages) {
            UPSPackage upsPack = new UPSPackage(aPackage, properties);
            if (!upsPack.canApply()) return false;
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
        res.add( new CarrierPropertyGroup("credentials")
                .addProperty("access.license.number")
                .addProperty("user.id")
                .addProperty("password")
        );
        res.add( new CarrierPropertyGroup("package.options")
                .addProperty("package.props.unit.dimensions","IN")
                .addProperty("package.props.unit.weight","LBS,KGS")
                .addProperty("package.props.confirmation.type","1|No signature,2|Signature Required,3|Adult Signature Required")
        );
        res.add( new CarrierPropertyGroup("rate.service.options")
                .addProperty("rs.saturday.delivery")
        );

        return res;
    }

    public List<CarrierMethod> getShippingMethods() {
        List<CarrierMethod> res = new ArrayList<CarrierMethod>();
        res.add(new CarrierMethod("01", "Next Day Air"));
        res.add(new CarrierMethod("02", "2nd Day Air"));
        res.add(new CarrierMethod("03", "UPS Ground"));
        res.add(new CarrierMethod("07", "UPS Worldwide Express"));
        res.add(new CarrierMethod("08", "UPS Worldwide Expedited"));
        res.add(new CarrierMethod("11", "UPS Standard"));
        res.add(new CarrierMethod("12", "3 Day Select"));
        res.add(new CarrierMethod("13", "Next Day Air Saver"));
        res.add(new CarrierMethod("14", "Next Day Air Early A.M."));
        res.add(new CarrierMethod("54", "Worldwide Express Plus"));
        res.add(new CarrierMethod("59", "2nd Day Air A.M."));
        res.add(new CarrierMethod("65", "Express Saver"));
        return res;
    }

    public void setProperties(Properties p) {
        properties = p;
    }

    public String getName() {
        return "UPS";
    }

    private ShippingResult ShipAcceptNoThread(UPSShipConfirmResponse shipConfirmResponse, List<BasePackage> packages, String imgPath) {
        ShippingResult result = new ShippingResult();
        try {
            UPSShipAcceptRequest reqAc = new UPSShipAcceptRequest(status, properties);
            reqAc.setShipmentDigest(shipConfirmResponse.getShipmentDigest());
            log.debug("enviando accept");
            UPSShipAcceptResponse resAc = reqAc.execute();
            if (!resAc.hasError()) {
                log.debug("salvando imagenes");
                resAc.saveGraphicImages(imgPath);
                result.setShippingNumber(resAc.getShipmentIdentificationNumber());
                result.setShipmentCharge(resAc.getTotalChargesValue());
                result.setShipmentChargeCurr(resAc.getTotalChargesCurrency());
                for (int i = 0; i < resAc.getPackages().size(); i++) {
                    PackageResult pr = resAc.getPackages().get(i);
                    if (i < packages.size()) {
                        org.store.core.utils.carriers.BasePackage pack = packages.get(i);
                        result.getTrackingNumbers().put(pack.getProductId(), pr.getTrackingNumber());
                    }
                }

                log.debug("imagenes recibidas");
            }

        } catch (Exception e) {
            result.addError("UPS", "Exception", e.toString());
            log.error(e.getMessage() + e.getCause().getMessage());
        }
        return result;
    }


}
