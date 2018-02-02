package ca.canadapost.cpcdp.rating;

import ca.canadapost.cpcdp.rating.generated.messages.Messages;
import ca.canadapost.cpcdp.rating.generated.rating.MailingScenario;
import ca.canadapost.cpcdp.rating.generated.rating.MailingScenario.Destination;
import ca.canadapost.cpcdp.rating.generated.rating.MailingScenario.Destination.Domestic;
import ca.canadapost.cpcdp.rating.generated.rating.PriceQuotes;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.apache.axis.types.NonNegativeInteger;
import org.apache.commons.lang.StringUtils;
import org.store.core.utils.carriers.*;

import javax.xml.bind.JAXBContext;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Properties;


public class GetRatesClient {

    private static final String LINK_TEST = "https://ct.soa-gw.canadapost.ca/rs/ship/price";
    private static final String LINK_LIFE = "https://soa-gw.canadapost.ca/rs/ship/price";

    private Properties properties;
    private String status;

    public GetRatesClient(String status, Properties properties) {
        this.properties = properties;
        this.status = status;
    }

    public RateServiceResponse getRateServices(ShipTo shipTo, Shipper shipFrom, List<BasePackage> packages) {
        if (packages.size() == 1) return getRateServices(shipTo, shipFrom, packages.get(0));
        else {
            RateServiceResponse result = new RateServiceResponse();
            for (BasePackage pack : packages) {
                RateServiceResponse res = getRateServices(shipTo, shipFrom, pack);
                if (StringUtils.isNotEmpty(res.getErrors())) result.setErrors(res.getErrors());
                if (!res.getRateServices().isEmpty()) {
                    for (RateService tmpRS : res.getRateServices()) {
                        boolean found = false;
                        for (RateService rs : result.getRateServices()) {
                            if (rs.getCode().equals(tmpRS.getCode())) {
                                rs.setValue(rs.getValue() + tmpRS.getValue());
                                found = true;
                            }
                        }
                        if (!found) {
                            result.getRateServices().add(tmpRS);
                        }
                    }
                }
            }
            return result;
        }
    }

    public RateServiceResponse getRateServices(ShipTo shipTo, Shipper shipFrom, BasePackage pack) {
        RateServiceResponse result = new RateServiceResponse();

        String username = properties.getProperty("user.id");
        String password = properties.getProperty("password");
        String mailedBy = properties.getProperty("customer.number");
        String currency = properties.getProperty("currency", "CAD");

        // Create GetRates XML Request Object
        MailingScenario mailingScenario = new MailingScenario();
        mailingScenario.setCustomerNumber(mailedBy);

        MailingScenario.ParcelCharacteristics parcelCharacteristics = new MailingScenario.ParcelCharacteristics();
        parcelCharacteristics.setWeight(getWeight(pack));
        parcelCharacteristics.setDimensions(getDimensions(pack));
        mailingScenario.setParcelCharacteristics(parcelCharacteristics);

        mailingScenario.setOriginPostalCode(sanitizeZipCode(shipFrom.getAddress().getPostalCode()));

        Destination destination = new Destination();
        if ("CA".equalsIgnoreCase(shipTo.getAddress().getCountryCode())) {
            Domestic domestic = new Domestic();
            domestic.setPostalCode(sanitizeZipCode(shipTo.getAddress().getPostalCode()));
            destination.setDomestic(domestic);
        } else if ("US".equalsIgnoreCase(shipTo.getAddress().getCountryCode())) {
            Destination.UnitedStates us = new Destination.UnitedStates();
            us.setZipCode(sanitizeZipCode(shipTo.getAddress().getPostalCode()));
            destination.setUnitedStates(us);
        } else {
            Destination.International international = new Destination.International();
            international.setCountryCode(shipTo.getAddress().getCountryCode());
            destination.setInternational(international);
        }
        mailingScenario.setDestination(destination);

        // Execute GetRates Request
        ClientConfig config = new DefaultClientConfig();
        Client aClient = Client.create(config);
        aClient.addFilter(new com.sun.jersey.api.client.filter.HTTPBasicAuthFilter(username, password));

        WebResource aWebResource = aClient.resource("LIVE".equalsIgnoreCase(this.status) ? "https://soa-gw.canadapost.ca/rs/ship/price" : "https://ct.soa-gw.canadapost.ca/rs/ship/price");
        ClientResponse resp = aWebResource
                .accept(new String[] { "application/vnd.cpc.ship.rate-v2+xml" })
                .header("Content-Type", "application/vnd.cpc.ship.rate-v2+xml")
                .acceptLanguage(new String[] { "en-CA" })
                .post(ClientResponse.class, mailingScenario);
        InputStream respIS = resp.getEntityInputStream();
        System.out.println("HTTP Response Status: " + resp.getStatus() + " " + resp.getClientResponseStatus());
        JAXBContext jc;
        try {
            jc = JAXBContext.newInstance(PriceQuotes.class, Messages.class);
            Object entity = jc.createUnmarshaller().unmarshal(respIS);
            // Determine whether response data matches GetRatesInfo schema.
            if (entity instanceof PriceQuotes) {
                PriceQuotes priceQuotes = (PriceQuotes) entity;
                for (PriceQuotes.PriceQuote aPriceQuote : priceQuotes.getPriceQuotes()) {
                    Float value = (aPriceQuote.getPriceDetails() != null && aPriceQuote.getPriceDetails().getDue() != null) ? aPriceQuote.getPriceDetails().getDue().floatValue() : null;
                    if (value != null && value > 0) {
                        Integer days = (aPriceQuote.getServiceStandard() != null && aPriceQuote.getServiceStandard().getExpectedTransitTime() != null) ? aPriceQuote.getServiceStandard().getExpectedTransitTime().intValue() : null;
                        result.addRateService(new org.store.core.utils.carriers.RateService(aPriceQuote.getServiceCode(), currency, value.toString(), (days != null) ? days.toString() : ""));
                    }
                }
            } else {
                // Assume Error Schema
                Messages messageData = (Messages) entity;
                StringBuilder errors = new StringBuilder();
                for (Messages.Message aMessage : messageData.getMessage()) {
                    errors.append(aMessage.getDescription()).append(" (").append(aMessage.getCode()).append(")\n");
                }
                result.setErrors(errors.toString());
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        aClient.destroy();
        return result;
    }


    private String sanitizeZipCode(String zc) {
        return (zc!=null) ? zc.toUpperCase().replaceAll(" ","") : "";
    }

    private BigDecimal getWeight(BasePackage bp) {
        double weight = ("Pound".equalsIgnoreCase(bp.getWeightUnit())) ? 0.453592 * bp.getPackageWeight() : bp.getPackageWeight();
        return new BigDecimal(weight, new MathContext(2, RoundingMode.CEILING));
    }

    private MailingScenario.ParcelCharacteristics.Dimensions getDimensions(BasePackage bp) {
        MathContext mathContent = new MathContext(1, RoundingMode.CEILING);

        float rate = 1.0f;
        if ("Inch".equalsIgnoreCase(bp.getDimensionUnit())) rate = 2.64f;
        else if ("m".equalsIgnoreCase(bp.getDimensionUnit())) rate =100f;

        MailingScenario.ParcelCharacteristics.Dimensions dimensions = new MailingScenario.ParcelCharacteristics.Dimensions();
        dimensions.setLength(new BigDecimal(rate * bp.getDimensionsLength(),mathContent));
        dimensions.setWidth(new BigDecimal(rate * bp.getDimensionsWidth(),mathContent));
        dimensions.setHeight(new BigDecimal(rate * bp.getDimensionsHeight(),mathContent));
        return dimensions;
    }


}
