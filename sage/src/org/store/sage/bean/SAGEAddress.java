package org.store.sage.bean;


import org.store.core.beans.User;
import org.store.core.beans.UserAddress;
import org.store.core.globals.CountryFactory;
import org.store.core.globals.SomeUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

public class SAGEAddress {

    public static Logger log = Logger.getLogger(SAGEAddress.class);
    private String id;
    private Integer LI_No;
    private String LI_Intitule;
    private String LI_Contact;
    private String LI_Adresse;
    private String LI_Complement;
    private String LI_Ville;
    private String LI_CodePostal;
    private String LI_Pays;
    private String LI_CodeRegion;
    private String LI_Telephone;
    private String LI_Telecopie;

    public SAGEAddress() {
    }

    public SAGEAddress(Connection connection, Integer id) {
        this.id = id.toString();
        loadFromDb(connection);
    }

    public SAGEAddress(UserAddress userAddress, String lang) {
        setId(userAddress.getExternalCode());
        setLI_Adresse(userAddress.getAddress());
        setLI_CodePostal(userAddress.getZipCode());
        setLI_CodeRegion(userAddress.getState().getStateName());
        setLI_Contact(userAddress.getFullName());
        if (StringUtils.isNotEmpty(userAddress.getCompany())) setLI_Intitule(userAddress.getCompany());
        else if (StringUtils.isNotEmpty(userAddress.getCode())) setLI_Intitule(userAddress.getCode());
        else setLI_Intitule("");
        setLI_Pays(CountryFactory.getCountryName(userAddress.getIdCountry(), new Locale(lang)));
        setLI_Telecopie(userAddress.getFax());
        setLI_Telephone(userAddress.getPhone());
        setLI_Ville(userAddress.getCity());
    }

    private void loadFromDb(Connection connection) {
        try {
            PreparedStatement stmt1 = connection.prepareStatement("SELECT * FROM F_LIVRAISON WHERE cbMarq=?");
            stmt1.setInt(1, SomeUtils.strToInteger(getId()));
            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.next()) {
                setLI_Adresse(rs1.getString("LI_Adresse"));
                setLI_CodePostal(rs1.getString("LI_CodePostal"));
                setLI_CodeRegion(rs1.getString("LI_CodeRegion"));
                setLI_Complement(rs1.getString("LI_Complement"));
                setLI_Contact(rs1.getString("LI_Contact"));
                setLI_Intitule(rs1.getString("LI_Intitule"));
                setLI_Pays(rs1.getString("LI_Pays"));
                setLI_Telecopie(rs1.getString("LI_Telecopie"));
                setLI_Telephone(rs1.getString("LI_Telephone"));
                setLI_Ville(rs1.getString("LI_Ville"));
                setLI_No(rs1.getInt("LI_No"));
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e); 
        }
    }

    public Integer getLineNumber(Connection connection) {
        if (LI_No!=null && LI_No>0) return LI_No;
        try {
            PreparedStatement stmt1 = connection.prepareStatement("SELECT LI_No FROM F_LIVRAISON WHERE cbMarq=?");
            stmt1.setInt(1, SomeUtils.strToInteger(getId()));
            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.next()) {setLI_No(rs1.getInt("LI_No")); }
            return getLI_No();
        } catch (SQLException e) {
            log.error(e.getMessage(), e); 
        }
        return null;
    }

    public void saveFieldToDb(Connection connection, String fieldName) {
        try {
            String fieldValue = getFieldValue(fieldName);
            if (StringUtils.isNotEmpty(fieldValue)) {
                PreparedStatement stmt1 = connection.prepareStatement("update F_LIVRAISON SET " + fieldName + "=? WHERE cbMarq=?");
                stmt1.setString(1, fieldValue);
                stmt1.setString(2, getId());
                stmt1.execute();
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e); 
        }
    }

    public boolean existsInSage(Connection connection) {
        Integer id = SomeUtils.strToInteger(getId());
        if (id==null) return false;
        else {
            try {
                PreparedStatement stmt1 = connection.prepareStatement("SELECT count(1) FROM F_LIVRAISON WHERE cbMarq=?");
                stmt1.setInt(1, id);
                ResultSet rs1 = stmt1.executeQuery();
                return (rs1.next() && rs1.getInt(1) > 0);
            } catch (SQLException e) {
                log.error(e.getMessage(), e); 
            }
        }
        return false;
    }

    public void saveAll(Connection connection) {
        saveFieldToDb(connection, "LI_Intitule");
        saveFieldToDb(connection, "LI_Contact");
        saveFieldToDb(connection, "LI_Adresse");
        saveFieldToDb(connection, "LI_Complement");
        saveFieldToDb(connection, "LI_Ville");
        saveFieldToDb(connection, "LI_CodePostal");
        saveFieldToDb(connection, "LI_Pays");
        saveFieldToDb(connection, "LI_CodeRegion");
        saveFieldToDb(connection, "LI_Telephone");
        saveFieldToDb(connection, "LI_Telecopie");
    }

    public boolean insert(Connection connection, User user) {
        try {
            String query = "insert into F_LIVRAISON (CT_Num, LI_Intitule, LI_Contact, LI_EMail, LI_Adresse, LI_Ville, LI_CodeRegion, LI_CodePostal, LI_Pays,LI_Telephone, LI_Telecopie, LI_No, N_Expedition, N_Condition, LI_Principal) Values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, user.getUserId());
            stmt.setString(2, StringUtils.left(getLI_Intitule(),35));
            stmt.setString(3, StringUtils.left(getLI_Contact(), 35));
            stmt.setString(4, StringUtils.left(user.getEmail(), 69));
            stmt.setString(5, StringUtils.left(getLI_Adresse(), 35));
            stmt.setString(6, StringUtils.left(getLI_Ville(), 35));
            stmt.setString(7, StringUtils.left(getLI_CodeRegion(), 25));
            stmt.setString(8, StringUtils.left(getLI_CodePostal(), 9));
            stmt.setString(9, StringUtils.left(getLI_Pays(), 35));
            stmt.setString(10, StringUtils.left(getLI_Telephone(), 21));
            stmt.setString(11, StringUtils.left(getLI_Telecopie(), 21));
            // LI_No, N_Expedition, N_Condition, LI_Principal
            stmt.setInt(12, 0);
            stmt.setInt(13, 1);
            stmt.setInt(14, 1);
            stmt.setInt(15, 0);
            if (stmt.executeUpdate()>0) {
                stmt.close();
                ResultSet rsIdentity = connection.prepareStatement("select IDENT_CURRENT('F_LIVRAISON') as ADD_ID").executeQuery();
                if (rsIdentity.next()) {
                    setId(String.valueOf(rsIdentity.getInt(1)));
                    return true;
                }
            } else {
                stmt.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e); 
        }
        return false;
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

    public void copyToAddress(UserAddress add) {
        add.setExternalCode(getId());
        add.setAddress(getLI_Adresse());
        add.setCity(getLI_Ville());
        add.setCode(getContactName());
        add.setCompany(getLI_Intitule());
        add.setFax(getLI_Telecopie());
        add.setFirstname(getContactName());
        add.setLastname("");
        add.setPhone(getLI_Telephone());
        add.setZipCode(getLI_CodePostal());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLI_Intitule() {
        return LI_Intitule;
    }

    public void setLI_Intitule(String LI_Intitule) {
        this.LI_Intitule = LI_Intitule;
    }

    public String getLI_Contact() {
        return LI_Contact;
    }

    public void setLI_Contact(String LI_Contact) {
        this.LI_Contact = LI_Contact;
    }

    public String getLI_Adresse() {
        return LI_Adresse;
    }

    public void setLI_Adresse(String LI_Adresse) {
        this.LI_Adresse = LI_Adresse;
    }

    public String getLI_Complement() {
        return LI_Complement;
    }

    public void setLI_Complement(String LI_Complement) {
        this.LI_Complement = LI_Complement;
    }

    public String getLI_Ville() {
        return LI_Ville;
    }

    public void setLI_Ville(String LI_Ville) {
        this.LI_Ville = LI_Ville;
    }

    public String getLI_CodePostal() {
        return LI_CodePostal;
    }

    public void setLI_CodePostal(String LI_CodePostal) {
        this.LI_CodePostal = LI_CodePostal;
    }

    public String getLI_Pays() {
        return LI_Pays != null ? LI_Pays : "";
    }

    public void setLI_Pays(String LI_Pays) {
        this.LI_Pays = LI_Pays;
    }

    public String getLI_CodeRegion() {
        return LI_CodeRegion != null ? LI_CodeRegion : "";
    }

    public void setLI_CodeRegion(String LI_CodeRegion) {
        this.LI_CodeRegion = LI_CodeRegion;
    }

    public String getLI_Telephone() {
        return LI_Telephone;
    }

    public void setLI_Telephone(String LI_Telephone) {
        this.LI_Telephone = LI_Telephone;
    }

    public String getLI_Telecopie() {
        return LI_Telecopie;
    }

    public void setLI_Telecopie(String LI_Telecopie) {
        this.LI_Telecopie = LI_Telecopie;
    }

    public Integer getLI_No() {
        return LI_No;
    }

    public void setLI_No(Integer LI_No) {
        this.LI_No = LI_No;
    }

    public String getContactName() {
        return StringUtils.isNotEmpty(getLI_Contact()) ? getLI_Contact() : getLI_Intitule();
    }
}
