package org.store.sage.bean;

import org.store.core.beans.Product;
import org.store.core.beans.ProductLang;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SAGEProduct {

    public static Logger log = Logger.getLogger(SAGEProduct.class);
    private String AR_Ref;
    private String AR_Design;
    private Double AR_PrixVen;
    private Double AR_PrixAch;
    private Double AR_PoidsNet;
    private Double AR_Coef;
    private Integer AR_Delai;
    private Integer AR_Escompte;
    private String AR_Photo;
    private String CL_No1;
    private String CL_No2;
    private String CL_No3;
    private String CL_No4;
    private String AR_Langue1;
    private String FA_CodeFamille;
    private Double stock;
    private SAGECategory sageCategory;
    private Map<SAGEUserLevel, Double> sageTarif;

    public SAGEProduct(Connection sageConnection, String code) {
        setAR_Ref(code);
        loadFromDb(sageConnection);
    }

    private void loadFromDb(Connection connection) {
        try {
            // leer datos de F_ARTICLE
            PreparedStatement stmt1 = connection.prepareStatement("select AR_Ref,AR_Design,AR_PrixVen,AR_PrixAch,AR_PoidsNet,AR_Delai,AR_Escompte,AR_Photo,CL_No1,CL_No2,CL_No3,CL_No4,AR_Langue1,FA_CodeFamille, AR_Coef from F_ARTICLE where AR_Ref=?");
            stmt1.setString(1, getAR_Ref());
            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.next()) {
                setAR_Delai(rs1.getInt("AR_Delai"));
                setAR_Design(rs1.getString("AR_Design"));
                setAR_Escompte(rs1.getInt("AR_Escompte"));
                setAR_Langue1(rs1.getString("AR_Langue1"));
                setAR_Photo(rs1.getString("AR_Photo"));
                setAR_PoidsNet(rs1.getDouble("AR_PoidsNet"));
                setAR_PrixAch(rs1.getDouble("AR_PrixAch"));
                setAR_PrixVen(rs1.getDouble("AR_PrixVen"));
                setAR_Coef(rs1.getDouble("AR_Coef"));
                setFA_CodeFamille(rs1.getString("FA_CodeFamille"));
                setSageCategory(new SAGECategory(connection, rs1.getInt("CL_No1"), rs1.getInt("CL_No2"), rs1.getInt("CL_No3"), rs1.getInt("CL_No4")));
            }

            // leer stock de F_ARTSTOCK
            PreparedStatement stmt2 = connection.prepareStatement("select sum(AS_QteSto) as totalStock from F_ARTSTOCK where AR_Ref=?");
            stmt2.setString(1, getAR_Ref());
            ResultSet rs2 = stmt2.executeQuery();
            if (rs2.next()) setStock(rs2.getDouble(1));

            // leer precios por categoria tarifaria
            sageTarif = new HashMap<SAGEUserLevel, Double>();
            PreparedStatement stmt3 = connection.prepareStatement("select AC_Categorie,AC_Coef,P_CATTARIF.CT_Intitule from F_ARTCLIENT left join P_CATTARIF on F_ARTCLIENT.AC_Categorie = P_CATTARIF.cbMarq where AC_Coef>0 and AR_Ref=?");
            stmt3.setString(1, getAR_Ref());
            ResultSet rs3 = stmt3.executeQuery();
            while (rs3.next()) {
                SAGEUserLevel userLevel = new SAGEUserLevel(rs3.getInt("AC_Categorie"), rs3.getString("CT_Intitule"));
                sageTarif.put(userLevel, rs3.getDouble("AC_Coef"));
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }


    public String getAR_Ref() {
        return AR_Ref;
    }

    public void setAR_Ref(String AR_Ref) {
        this.AR_Ref = AR_Ref;
    }

    public Double getAR_Coef() {
        return AR_Coef;
    }

    public void setAR_Coef(Double AR_Coef) {
        this.AR_Coef = AR_Coef;
    }

    public String getAR_Design() {
        return AR_Design;
    }

    public void setAR_Design(String AR_Design) {
        this.AR_Design = AR_Design;
    }

    public Double getAR_PrixVen() {
        return AR_PrixVen;
    }

    public void setAR_PrixVen(Double AR_PrixVen) {
        this.AR_PrixVen = AR_PrixVen;
    }

    public Double getAR_PrixAch() {
        return AR_PrixAch;
    }

    public void setAR_PrixAch(Double AR_PrixAch) {
        this.AR_PrixAch = AR_PrixAch;
    }

    public Double getAR_PoidsNet() {
        return AR_PoidsNet;
    }

    public void setAR_PoidsNet(Double AR_PoidsNet) {
        this.AR_PoidsNet = AR_PoidsNet;
    }

    public String getAR_Photo() {
        return AR_Photo;
    }

    public void setAR_Photo(String AR_Photo) {
        this.AR_Photo = AR_Photo;
    }

    public Integer getAR_Delai() {
        return AR_Delai;
    }

    public void setAR_Delai(Integer AR_Delai) {
        this.AR_Delai = AR_Delai;
    }

    public Integer getAR_Escompte() {
        return AR_Escompte;
    }

    public void setAR_Escompte(Integer AR_Escompte) {
        this.AR_Escompte = AR_Escompte;
    }

    public Double getStock() {
        return stock;
    }

    public void setStock(Double stock) {
        this.stock = stock;
    }

    public String getCL_No1() {
        return CL_No1;
    }

    public void setCL_No1(String CL_No1) {
        this.CL_No1 = CL_No1;
    }

    public String getCL_No2() {
        return CL_No2;
    }

    public void setCL_No2(String CL_No2) {
        this.CL_No2 = CL_No2;
    }

    public String getCL_No3() {
        return CL_No3;
    }

    public void setCL_No3(String CL_No3) {
        this.CL_No3 = CL_No3;
    }

    public String getCL_No4() {
        return CL_No4;
    }

    public void setCL_No4(String CL_No4) {
        this.CL_No4 = CL_No4;
    }

    public String getAR_Langue1() {
        return AR_Langue1;
    }

    public void setAR_Langue1(String AR_Langue1) {
        this.AR_Langue1 = AR_Langue1;
    }

    public SAGECategory getSageCategory() {
        return sageCategory;
    }

    public void setSageCategory(SAGECategory sageCategory) {
        this.sageCategory = sageCategory;
    }

    public Map<SAGEUserLevel, Double> getSageTarif() {
        return sageTarif;
    }

    public void setSageTarif(Map<SAGEUserLevel, Double> sageTarif) {
        this.sageTarif = sageTarif;
    }

    public String getFA_CodeFamille() {
        return FA_CodeFamille;
    }

    public void setFA_CodeFamille(String FA_CodeFamille) {
        this.FA_CodeFamille = FA_CodeFamille;
    }

    public void copyToProduct(Product product) {
        if (product != null) {
            product.setCostPrice(getAR_PrixAch());
            if (product.getMarkupFactor() == null) product.setMarkupFactor(1.0);
            if (product.getErMarkupFactor() == null) product.setErMarkupFactor(1.0);
            product.setNoTaxable(false);
            if (StringUtils.isEmpty(product.getNeedShipping())) product.setNeedShipping("Y");
            product.setDeliveryTime(getAR_Delai() != null ? getAR_Delai().toString() : null);
            product.setWeight(getAR_PoidsNet());
            product.setActive(product.getCostPrice() != null && product.getCostPrice() > 0.0);
            product.setStock(getStock().longValue());
            product.setFixedStock(true);
            product.setAltCategory(getFA_CodeFamille());
        }
    }

    public void copyToProductLang(ProductLang pl) {
        if (pl != null) {
            pl.setProductName(getAR_Design());
            if (StringUtils.isNotEmpty(getAR_Langue1()) && "en".equalsIgnoreCase(pl.getProductLang())) pl.setProductName(getAR_Langue1());
        }
    }
}
