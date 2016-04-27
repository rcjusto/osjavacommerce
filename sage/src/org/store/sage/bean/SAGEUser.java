package org.store.sage.bean;


import org.store.core.beans.User;
import org.store.core.beans.UserAddress;
import org.store.core.globals.CountryFactory;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SAGEUser {

    public static Logger log = Logger.getLogger(SAGEUser.class);
    private String CT_Num;
    private String CT_Intitule;
    private String CT_Contact;
    private String CT_EMail;
    private String CT_Site;
    private String CT_Telephone;
    private String CT_Adresse;
    private String CT_Complement;
    private String CT_Ville;
    private String CT_CodePostal;
    private String CT_Pays;
    private String CT_CodeRegion;
    private Integer N_CatCompta;
    private Integer CO_No;

    private SAGEUserLevel level;
    private List<SAGEAddress> addresses;

    public SAGEUser(Connection connection, String id) {
        this.CT_Num = id;
        loadFromDb(connection);
    }

    public SAGEUser(Connection conn, User user, UserAddress billingAddress, String lang) {
        setCT_Num(user.getUserId());

        if (billingAddress != null) {
            setCT_Adresse(billingAddress.getAddress());
            setCT_CodePostal(billingAddress.getZipCode());
            setCT_CodeRegion(billingAddress.getState().getStateName());
            setCT_Pays(CountryFactory.getCountryName(billingAddress.getIdCountry(), new Locale(lang)));
            setCT_Ville(billingAddress.getCity());
        }

        setCT_Contact(user.getFullName());
        setCT_Intitule(user.getCompanyName());
        setCT_EMail(user.getEmail());
        setCT_Site(user.getWebsite());
        setCT_Telephone(user.getPhone());

    }

    private void loadFromDb(Connection connection) {
        try {
            // Leer datos del usuario
            PreparedStatement stmt1 = connection.prepareStatement("SELECT CT_Adresse,CT_CodePostal,CT_CodeRegion,CT_Contact,CT_EMail,CT_Intitule,CT_Pays,CT_Site,CT_Telephone,CT_Ville,N_CatTarif,N_CatCompta,CO_No FROM F_COMPTET WHERE CT_Num=?");
            stmt1.setString(1, getCT_Num());
            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.next()) {
                setCT_Adresse(rs1.getString("CT_Adresse"));
                setCT_CodePostal(rs1.getString("CT_CodePostal"));
                setCT_CodeRegion(rs1.getString("CT_CodeRegion"));
                setCT_Contact(rs1.getString("CT_Contact"));
                setCT_EMail(rs1.getString("CT_EMail"));
                setCT_Intitule(rs1.getString("CT_Intitule"));
                setCT_Pays(rs1.getString("CT_Pays"));
                setCT_Site(rs1.getString("CT_Site"));
                setCT_Telephone(rs1.getString("CT_Telephone"));
                setCT_Ville(rs1.getString("CT_Ville"));
                setN_CatCompta(rs1.getInt("N_CatCompta"));
                setCO_No(rs1.getInt("CO_No"));
                setLevel(new SAGEUserLevel(connection, rs1.getInt("N_CatTarif")));
            }

            // Leer direcciones
            addresses = new ArrayList<SAGEAddress>();
            PreparedStatement stmt2 = connection.prepareStatement("select cbMarq from F_LIVRAISON where CT_NUM=?");
            stmt2.setString(1, getCT_Num());
            ResultSet rs2 = stmt2.executeQuery();
            while (rs2.next()) addresses.add(new SAGEAddress(connection, rs2.getInt(1)));

        } catch (SQLException e) {
            log.error(e.getMessage(), e); 
        }
    }

    public void copyToUser(User user) {
        user.setEmail(getCT_EMail());
        user.setCompanyName(getCT_Intitule());
        user.setFirstname(StringUtils.isNotEmpty(CT_Contact) ? CT_Contact : CT_Intitule);
        user.setWebsite(getCT_Site());
        user.setPhone(getCT_Telephone());
        if (getN_CatCompta()!=null) user.setAltCategory(getN_CatCompta().toString());
        user.setAdmin(false);
    }

    public void saveCattarifToDb(Connection connection) {
        try {
            if (getLevel()!=null && getLevel().getId()!=null) {
                PreparedStatement stmt1 = connection.prepareStatement("update F_COMPTET SET N_CatTarif=? WHERE CT_Num=?");
                stmt1.setInt(1, getLevel().getId());
                stmt1.setString(2, getCT_Num());
                stmt1.execute();
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e); 
        }
    }

    public void saveFieldToDb(Connection connection, String fieldName) {
        try {
            String fieldValue = getFieldValue(fieldName);
            if (StringUtils.isNotEmpty(fieldValue)) {
                PreparedStatement stmt1 = connection.prepareStatement("update F_COMPTET SET "+fieldName+"=? WHERE CT_Num=?");
                stmt1.setString(1, fieldValue);
                stmt1.setString(2, getCT_Num());
                stmt1.execute();
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e); 
        }
    }

    private String getFieldValue(String fieldName) {
        String result = null;
        try {
            result = BeanUtils.getProperty(this, fieldName);
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }
        return result;
    }


    public String getCT_Num() {
        return CT_Num;
    }

    public void setCT_Num(String CT_Num) {
        this.CT_Num = CT_Num;
    }

    public String getCT_Intitule() {
        return CT_Intitule;
    }

    public void setCT_Intitule(String CT_Intitule) {
        this.CT_Intitule = CT_Intitule;
    }

    public String getCT_Contact() {
        return CT_Contact;
    }

    public void setCT_Contact(String CT_Contact) {
        this.CT_Contact = CT_Contact;
    }

    public String getCT_EMail() {
        return CT_EMail;
    }

    public void setCT_EMail(String CT_EMail) {
        this.CT_EMail = CT_EMail;
    }

    public String getCT_Site() {
        return CT_Site;
    }

    public void setCT_Site(String CT_Site) {
        this.CT_Site = CT_Site;
    }

    public String getCT_Telephone() {
        return CT_Telephone;
    }

    public void setCT_Telephone(String CT_Telephone) {
        this.CT_Telephone = CT_Telephone;
    }

    public String getCT_Adresse() {
        return CT_Adresse;
    }

    public void setCT_Adresse(String CT_Adresse) {
        this.CT_Adresse = CT_Adresse;
    }

    public String getCT_Complement() {
        return CT_Complement;
    }

    public void setCT_Complement(String CT_Complement) {
        this.CT_Complement = CT_Complement;
    }

    public String getCT_Ville() {
        return CT_Ville;
    }

    public void setCT_Ville(String CT_Ville) {
        this.CT_Ville = CT_Ville;
    }

    public String getCT_CodePostal() {
        return CT_CodePostal;
    }

    public void setCT_CodePostal(String CT_CodePostal) {
        this.CT_CodePostal = CT_CodePostal;
    }

    public String getCT_Pays() {
        return CT_Pays;
    }

    public void setCT_Pays(String CT_Pays) {
        this.CT_Pays = CT_Pays;
    }

    public String getCT_CodeRegion() {
        return CT_CodeRegion;
    }

    public void setCT_CodeRegion(String CT_CodeRegion) {
        this.CT_CodeRegion = CT_CodeRegion;
    }

    public Integer getN_CatCompta() {
        return N_CatCompta;
    }

    public void setN_CatCompta(Integer n_CatCompta) {
        N_CatCompta = n_CatCompta;
    }

    public Integer getCO_No() {
        return CO_No;
    }

    public void setCO_No(Integer CO_No) {
        this.CO_No = CO_No;
    }

    public SAGEUserLevel getLevel() {
        return level;
    }

    public void setLevel(SAGEUserLevel level) {
        this.level = level;
    }

    public List<SAGEAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<SAGEAddress> addresses) {
        this.addresses = addresses;
    }


    public SAGEAddress getBillingAddress() {
        SAGEAddress billing = new SAGEAddress();
        billing.setId("BILLING_" + getCT_Num());
        billing.setLI_Adresse(getCT_Adresse());
        billing.setLI_CodePostal(getCT_CodePostal());
        billing.setLI_CodeRegion(getCT_CodeRegion());
        billing.setLI_Complement(getCT_Complement());
        billing.setLI_Contact(getCT_Contact());
        billing.setLI_Intitule(getCT_Intitule());
        billing.setLI_Pays(getCT_Pays());
        billing.setLI_Telephone(getCT_Telephone());
        billing.setLI_Ville(getCT_Ville());
        return billing;
    }

}
