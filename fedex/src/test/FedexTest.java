package test;

import org.apache.commons.lang.StringUtils;
import org.store.core.utils.carriers.*;
import org.store.fedex.FedexServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Rogelio Caballero
 * 6/06/12 13:38
 */
public class FedexTest {

    public static void main(String[] args) {
        FedexServiceImpl impl = new FedexServiceImpl();
        impl.setProperties(getProperties());
        RateServiceResponse response = impl.getRateServices(getShipTo(), getShipFrom(), getPackages());
        if (StringUtils.isNotEmpty(response.getErrors())) {
            System.out.println("ERRORS: " + response.getErrors());
        }
        if (response.getRateServices()!=null && !response.getRateServices().isEmpty()) {
            for(RateService rs : response.getRateServices()) {
                System.out.println("------------------------------------------------");
                System.out.println("Carrier: " + rs.getCarrier());
                System.out.println("Code: "+ rs.getCode());
                System.out.println("Cost: "+ String.valueOf(rs.getValue()) + " " + rs.getCurrencyCode());
                System.out.println("Days: " + rs.getDaysToDelivery());
                System.out.println("------------------------------------------------");
            }
        }
    }

    private static List<BasePackage> getPackages() {
        List<BasePackage> result = new ArrayList<BasePackage>();
        BasePackage p = new BasePackage();
        p.setCurrencyCode("CAD");
        p.setDescription("Una prueba");
        p.setDimensionsHeight(12);
        p.setDimensionsLength(4);
        p.setDimensionsWidth(8);
        p.setDimensionUnit("Inch");
        p.setInsuredValueMonetaryValue(23.50f);
        p.setPackageWeight(12);
        p.setWeightUnit("Pound");
        result.add(p);
        p = new BasePackage();
        p.setCurrencyCode("CAD");
        p.setDescription("Una prueba");
        p.setDimensionsHeight(10);
        p.setDimensionsLength(4);
        p.setDimensionsWidth(8);
        p.setDimensionUnit("Inch");
        p.setInsuredValueMonetaryValue(12.50f);
        p.setPackageWeight(5);
        p.setWeightUnit("Pound");
        result.add(p);
        return result;
    }

    private static Shipper getShipFrom() {
        Properties p = new Properties();
        p.setProperty("shipper.name", "Pepe Gonzalez");
        p.setProperty("shipper.attention.name", "Pepe Gonzalez");
        //p.setProperty("shipper.shipper.number", "");
        p.setProperty("shipper.phone.number", "416-533-4000");
        //p.setProperty("shipper.email", "");
        p.setProperty("shipper.address.line1", "872 Bathurst Street");
        //p.setProperty("shipper.address.line2", "");
        p.setProperty("shipper.address.city", "Toronto");
        p.setProperty("shipper.address.country", "CA");
        p.setProperty("shipper.address.postalcode", "M5R3G3");
        p.setProperty("shipper.address.state", "ON");
        return new Shipper(p);
    }

    private static ShipTo getShipTo() {
        Address address = new Address();
        address.setAddressLine1("55B LongBranch Ave");
        //address.setAddressLine2("");
        address.setCity("Toronto");
        address.setCountryCode("CA");
        address.setPostalCode("M8W3J1");
        address.setStateProvinceCode("ON");
        StructuredPhoneNumber phone = new StructuredPhoneNumber("647-219-4088","-");
        return new ShipTo("","Rogelio Caballero", address, phone);
    }

    private static Properties getProperties() {
        Properties p = new Properties();
        p.setProperty("AccountNumber","510087526");
        p.setProperty("MeterNumber","118559344");
        p.setProperty("ClientKey","ovTElGNSV8B8gwdm");
        p.setProperty("ClientPassword","pyrgG63gHaVwAHPgNarVKVQJq");
        return p;
    }

}
