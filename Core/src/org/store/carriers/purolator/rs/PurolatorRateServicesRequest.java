package org.store.carriers.purolator.rs;

import org.store.carriers.purolator.common.PurolatorPackage;
import org.store.core.utils.carriers.RateService;
import org.store.core.utils.carriers.RateServiceResponse;
import org.store.core.utils.carriers.ShipTo;
import org.store.core.utils.carriers.Shipper;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.xml.sax.SAXException;
import pws.client.EstimatingService.EstimatingServiceContract_GetFullEstimate_ValidationFaultFault_FaultMessage;
import pws.client.EstimatingService.EstimatingServiceContract_GetQuickEstimate_ValidationFaultFault_FaultMessage;
import pws.client.EstimatingService.EstimatingServiceStub;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Rogelio Caballero Justo
 * Date: 20-nov-2006
 */
public class PurolatorRateServicesRequest {

    private String status;
    private String language;

    private String username;
    private String password;
    private String accountKey;
    private String packageType;

    private Shipper shipper;
    private ShipTo shipTo;
    private ArrayList<PurolatorPackage> packages;

    public static Logger log = Logger.getLogger(PurolatorRateServicesRequest.class);
    private static final String DEFAULT_PACKAGE_TYPE = "CustomerPackaging";


    public PurolatorRateServicesRequest(String runStatus, Properties prop) {
        status = runStatus;
        if (prop != null) loadProperties(prop);
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public String getPackageType() {
        return (StringUtils.isNotEmpty(packageType)) ? packageType : DEFAULT_PACKAGE_TYPE;
    }

    public Shipper getShipper() {
        return shipper;
    }

    public void setShipper(Shipper shipper) {
        this.shipper = shipper;
    }

    public ShipTo getShipTo() {
        return shipTo;
    }

    public void setShipTo(ShipTo shipTo) {
        this.shipTo = shipTo;
    }

    public ArrayList<PurolatorPackage> getPackages() {
        return packages;
    }

    public void setPackages(ArrayList<PurolatorPackage> packages) {
        this.packages = packages;
    }

    private EstimatingServiceStub.Address getSenderAddress() {
        EstimatingServiceStub.Address addr = new EstimatingServiceStub.Address();
        addr.setName(shipper.getAttentionName());
        addr.setCompany(shipper.getName());
        addr.setCity(shipper.getAddress().getCity());
        addr.setProvince(shipper.getAddress().getStateProvinceCode());
        addr.setCountry(shipper.getAddress().getCountryCode());
        addr.setPostalCode(shipper.getAddress().getPostalCode());
        return addr;
    }

    private EstimatingServiceStub.Address getReceiverAddress() {
        EstimatingServiceStub.Address addr = new EstimatingServiceStub.Address();
        addr.setName(shipTo.getAttentionName());
        addr.setCompany(shipTo.getCompanyName());
        addr.setCity(shipTo.getAddress().getCity());
        addr.setProvince(shipTo.getAddress().getStateProvinceCode());
        addr.setCountry(shipTo.getAddress().getCountryCode());
        addr.setPostalCode(shipTo.getAddress().getPostalCode());
        return addr;
    }

    private EstimatingServiceStub.ShortAddress getReceiverShortAddress() {
        EstimatingServiceStub.ShortAddress addr = new EstimatingServiceStub.ShortAddress();
        addr.setCity(shipTo.getAddress().getCity());
        addr.setProvince(shipTo.getAddress().getStateProvinceCode());
        addr.setCountry(shipTo.getAddress().getCountryCode());
        addr.setPostalCode(shipTo.getAddress().getPostalCode());
        return addr;
    }

    public RateServiceResponse execute() throws IOException, SAXException {
        return getQuickEstimate();

    }

    private RateServiceResponse getFullEstimate() throws AxisFault {
       DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", dfs);

        EstimatingServiceStub stub = new EstimatingServiceStub ("https://devwebservices.purolator.com/PWS/V1/Estimating/EstimatingService.asmx");

		Options options = stub._getServiceClient().getOptions();
		HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
		auth.setUsername(getUsername());
		auth.setPassword(getPassword());
		options.setProperty(HTTPConstants.AUTHENTICATE,auth);

		//Define the SOAP Envelope Headers
		EstimatingServiceStub.RequestContextE requestContext = new EstimatingServiceStub.RequestContextE();
		EstimatingServiceStub.RequestContext requestContextBody = new EstimatingServiceStub.RequestContext();
		requestContextBody.setVersion("1.0");
		requestContextBody.setLanguage(EstimatingServiceStub.Language.en);
		requestContextBody.setGroupID("");
		requestContextBody.setRequestReference("RequestReference");
		requestContext.setRequestContext(requestContextBody);

		//Test the Full Estimate Method
        EstimatingServiceStub.GetFullEstimateRequest request = new EstimatingServiceStub.GetFullEstimateRequest();
		EstimatingServiceStub.GetFullEstimateRequestContainer reqContainer = new EstimatingServiceStub.GetFullEstimateRequestContainer();

		// Setup the request to perform a full estimate
		EstimatingServiceStub.Shipment shipment = getShipment("PurolatorExpress");
		reqContainer.setShipment(shipment);
		request.setGetFullEstimateRequest(reqContainer);

		// Call the service
        EstimatingServiceStub.GetFullEstimateResponse response = null;
        try {
            response = stub.GetFullEstimate(request,requestContext);
        } catch (EstimatingServiceContract_GetFullEstimate_ValidationFaultFault_FaultMessage e) {
            log.error(e.getMessage(), e);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
        }
        if (response!=null) {
            EstimatingServiceStub.GetFullEstimateResponseContainer respContainer = response.getGetFullEstimateResponse();
            if (respContainer!=null) {
                respContainer.getResponseInformation();
                RateServiceResponse res = new RateServiceResponse();

            // Verificar errores
            if (respContainer.getResponseInformation() != null && respContainer.getResponseInformation().getErrors() != null && respContainer.getResponseInformation().getErrors().getError().length> 0) {
                StringBuffer buff = new StringBuffer();
                for (EstimatingServiceStub.Error e : respContainer.getResponseInformation().getErrors().getError()) {
                    buff.append(e.getCode()).append(": ").append(e.getDescription()).append("<br/>");
                }
                res.setErrors(buff.toString());
            }

            if (respContainer.getShipmentEstimates() != null) {
                for(EstimatingServiceStub.ShipmentEstimate se : respContainer.getShipmentEstimates().getShipmentEstimate()) {
                    String daysToDelivery = (se.getEstimatedTransitDays()>0) ? String.valueOf( se.getEstimatedTransitDays()) : "";
                    res.addRateService(new RateService(se.getServiceID(),"CAD",df.format(se.getTotalPrice()),daysToDelivery));
                }
            }

            return res;
            }
        }
        return null;
    }

    private RateServiceResponse getQuickEstimate() throws AxisFault {
       DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("en"));
        dfs.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("0.00", dfs);

        EstimatingServiceStub stub = new EstimatingServiceStub ("https://devwebservices.purolator.com/PWS/V1/Estimating/EstimatingService.asmx");

		Options options = stub._getServiceClient().getOptions();
		HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
		auth.setUsername(getUsername());
		auth.setPassword(getPassword());
		options.setProperty(HTTPConstants.AUTHENTICATE,auth);

		//Define the SOAP Envelope Headers
		EstimatingServiceStub.RequestContextE requestContext = new EstimatingServiceStub.RequestContextE();
		EstimatingServiceStub.RequestContext requestContextBody = new EstimatingServiceStub.RequestContext();
		requestContextBody.setVersion("1.0");
		requestContextBody.setLanguage(EstimatingServiceStub.Language.en);
		requestContextBody.setGroupID("");
		requestContextBody.setRequestReference("RequestReference");
		requestContext.setRequestContext(requestContextBody);

		//Test the Full Estimate Method
        EstimatingServiceStub.GetQuickEstimateRequest request = new EstimatingServiceStub.GetQuickEstimateRequest();
		EstimatingServiceStub.GetQuickEstimateRequestContainer reqContainer = new EstimatingServiceStub.GetQuickEstimateRequestContainer();

		// Setup the request to perform a full estimate
		reqContainer.setSenderPostalCode(getSenderAddress().getPostalCode());
        reqContainer.setBillingAccountNumber(getAccountKey());
        reqContainer.setPackageType(getPackageType());
        reqContainer.setReceiverAddress(getReceiverShortAddress());
        reqContainer.setTotalWeight(getTotalWeight());
		request.setGetQuickEstimateRequest(reqContainer);

		// Call the service
        EstimatingServiceStub.GetQuickEstimateResponse response = null;
        try {
            response = stub.GetQuickEstimate(request,requestContext);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
        } catch (EstimatingServiceContract_GetQuickEstimate_ValidationFaultFault_FaultMessage e) {
            log.error(e.getMessage(), e);
        }
        if (response!=null) {
            EstimatingServiceStub.GetQuickEstimateResponseContainer respContainer = response.getGetQuickEstimateResponse();
            if (respContainer!=null) {
                respContainer.getResponseInformation();
                RateServiceResponse res = new RateServiceResponse();

            // Verificar errores
            if (respContainer.getResponseInformation() != null && respContainer.getResponseInformation().getErrors() != null && respContainer.getResponseInformation().getErrors().getError()!=null && respContainer.getResponseInformation().getErrors().getError().length> 0) {
                StringBuffer buff = new StringBuffer();
                for (EstimatingServiceStub.Error e : respContainer.getResponseInformation().getErrors().getError()) {
                    buff.append(e.getCode()).append(": ").append(e.getDescription()).append("<br/>");
                }
                res.setErrors(buff.toString());
            }

            if (respContainer.getShipmentEstimates() != null && respContainer.getShipmentEstimates().getShipmentEstimate()!=null) {
                for(EstimatingServiceStub.ShipmentEstimate se : respContainer.getShipmentEstimates().getShipmentEstimate()) {
                    String daysToDelivery = (se.getEstimatedTransitDays()>0) ? String.valueOf( se.getEstimatedTransitDays()) : "";
                    res.addRateService(new RateService(se.getServiceID(),"CAD",df.format(se.getTotalPrice()),daysToDelivery));
                }
            }

            return res;
            }
        }
        return null;
    }

    private EstimatingServiceStub.TotalWeight getTotalWeight() {
        double totalWeight = 0d;
        for (PurolatorPackage aPackage : packages) {
            EstimatingServiceStub.Piece p = aPackage.getPiece();
            totalWeight += p.getWeight().getValue().floatValue();
        }
        EstimatingServiceStub.TotalWeight weight = new EstimatingServiceStub.TotalWeight();
        weight.setValue((int)Math.round(totalWeight));
        weight.setWeightUnit(EstimatingServiceStub.WeightUnit.lb);
        return weight;
    }

    private EstimatingServiceStub.Shipment getShipment(String serviceId) {
        EstimatingServiceStub.Shipment shipment = new EstimatingServiceStub.Shipment();

		EstimatingServiceStub.SenderInformation sender = new EstimatingServiceStub.SenderInformation();
		sender.setAddress(getSenderAddress());
//		sender.setTaxNumber("123456");
		shipment.setSenderInformation(sender);

		EstimatingServiceStub.ReceiverInformation receiver = new EstimatingServiceStub.ReceiverInformation();
		receiver.setAddress(getReceiverAddress());
//		receiver.setTaxNumber("654321");
		shipment.setReceiverInformation(receiver);

		EstimatingServiceStub.PackageInformation pack = new EstimatingServiceStub.PackageInformation();
		pack.setServiceID(serviceId);
		pack.setDescription("Description");

        double totalWeight = 0d;
		EstimatingServiceStub.Piece[] pieces = new EstimatingServiceStub.Piece[packages.size()];
        for (int i = 0; i < packages.size(); i++) {
            pieces[i] = packages.get(i).getPiece();
            totalWeight += pieces[i].getWeight().getValue().floatValue();
        }

        EstimatingServiceStub.TotalWeight weight = new EstimatingServiceStub.TotalWeight();
        weight.setValue((int)Math.round(totalWeight));
        weight.setWeightUnit(EstimatingServiceStub.WeightUnit.lb);
        pack.setTotalWeight(weight);
        pack.setTotalPieces(packages.size());

		EstimatingServiceStub.ArrayOfPiece arr = new EstimatingServiceStub.ArrayOfPiece();
		arr.setPiece(pieces);
		pack.setPiecesInformation(arr);

		pack.setDangerousGoodsDeclarationDocumentIndicator(false);

		//Define OptionsInformation
		//ResidentialSignatureDomestic
		//pack.setOptionsInformation(SetSignatureOption());

		//Origin Signature Not Required Option
		// pack.setOptionsInformation(SetOriginSignatureNotRequiredOption());

		shipment.setPackageInformation(pack);

		EstimatingServiceStub.PaymentInformation pay = new EstimatingServiceStub.PaymentInformation();
		pay.setPaymentType(EstimatingServiceStub.PaymentType.Sender);
		pay.setRegisteredAccountNumber(getAccountKey());
		pay.setBillingAccountNumber(getAccountKey());
		shipment.setPaymentInformation(pay);

		EstimatingServiceStub.PickupInformation pickUp = new EstimatingServiceStub.PickupInformation();
		pickUp.setPickupType(EstimatingServiceStub.PickupType.DropOff);
		shipment.setPickupInformation(pickUp);

		shipment.setOtherInformation(null);

		return shipment;
    }

    public String getContentXml() {
        VelocityContext context = new VelocityContext();
        context.put("bean", this);
        context.put("tool", new org.store.core.utils.carriers.velocity.CarrierVelocityTool());
        String templateName = "capost/capost_rs.vm";
        return org.store.core.utils.carriers.velocity.VelocityGenerator.getGeneratedXml(context, templateName);
    }

    public void loadProperties(Properties prop) {
        username = prop.getProperty("username");
        password = prop.getProperty("password");
        accountKey = prop.getProperty("account.key");
        if (prop.containsKey("package.type") && StringUtils.isNotEmpty(prop.getProperty("package.type"))) packageType = prop.getProperty("package.type");
        if (status == null || "".equals(status)) status = prop.getProperty("status");
        shipper = new Shipper(prop);
    }


    public void addPackage(PurolatorPackage pack) {
        if (packages == null) setPackages(new ArrayList<PurolatorPackage>());
        packages.add(pack);
    }

}