package org.store.sage.bean;


import org.store.core.beans.Tax;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SAGETax {

    public static Logger log = Logger.getLogger(SAGETax.class);
    private String TA_Code;
    private String TA_Intitule;
    private Double TA_Taux;
    private Integer TA_Type;
    private Integer TA_NP;
    private Integer TA_Sens;


    public SAGETax() {
    }

    public SAGETax(Connection connection, String code) {
        this.TA_Code = code;
        loadFromDb(connection);
    }

    private void loadFromDb(Connection connection) {
        try {
            PreparedStatement stmt1 = connection.prepareStatement("SELECT * FROM F_TAXE WHERE TA_Code=?");
            stmt1.setString(1, getTA_Code());
            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.next()) {
                setTA_Intitule(rs1.getString("TA_Intitule"));
                setTA_Sens(rs1.getInt("TA_Sens"));
                setTA_Type(rs1.getInt("TA_Type"));
                setTA_NP(rs1.getInt("TA_NP"));
                setTA_Taux(rs1.getDouble("TA_Taux"));
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    public String getTA_Code() {
        return TA_Code;
    }

    public void setTA_Code(String TA_Code) {
        this.TA_Code = TA_Code;
    }

    public String getTA_Intitule() {
        return TA_Intitule;
    }

    public void setTA_Intitule(String TA_Intitule) {
        this.TA_Intitule = TA_Intitule;
    }

    public Double getTA_Taux() {
        return TA_Taux;
    }

    public void setTA_Taux(Double TA_Taux) {
        this.TA_Taux = TA_Taux;
    }

    public Integer getTA_Type() {
        return TA_Type;
    }

    public void setTA_Type(Integer TA_Type) {
        this.TA_Type = TA_Type;
    }

    public Integer getTA_NP() {
        return TA_NP;
    }

    public void setTA_NP(Integer TA_NP) {
        this.TA_NP = TA_NP;
    }

    public Integer getTA_Sens() {
        return TA_Sens;
    }

    public void setTA_Sens(Integer TA_Sens) {
        this.TA_Sens = TA_Sens;
    }

    public void copyToTax(Tax tax) {
        tax.setTaxName(getTA_Intitule());
        tax.setValue(getTA_Taux());
    }

    public  Double getTaxValue() {
        return getTA_Taux()!=null  ? getTA_Taux() / 100 : 0;
    }

    public boolean isNeeded() {
        return getTA_Taux()!=null && getTA_Taux()>0;
    }

}
