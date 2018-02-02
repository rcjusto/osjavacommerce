package org.store.sage.bean;

import org.store.core.beans.Order;
import org.store.core.beans.OrderDetail;
import org.store.core.beans.OrderDetailProduct;
import org.store.core.beans.ProductUserTax;
import org.store.core.beans.TaxPerFamily;
import org.store.core.dao.HibernateDAO;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SAGEOrder {

    public static Logger log = Logger.getLogger(SAGEOrder.class);
    private Integer DO_Domaine = 0;
    private Integer DO_Type = 1;
    private String DO_Piece;
    private Date DO_Date;
    private String DO_Tiers;
    private Integer AB_No = 0;
    private Integer CA_No = 0;
    private String CA_Num;
    private String CG_Num;
    private String CT_NumPayeur;
    private Integer DE_No;
    private Integer DO_BlFact;
    private Integer DO_Condition;
    private Integer DO_Expedit;
    private Integer DO_Period;
    private Integer DO_Tarif;
    private Integer DO_TypeColis = 1;
    private Integer N_CatCompta;
    private Integer LI_No;
    private Integer DO_Statut = 2;
    private String shippingLineName;
    private Integer CO_No;
    private String DO_Ref;

    private List<SAGEOrderLine> lines;

    private String lastError;
    private static final String CNT_UNIDAD = "Unidad";

    public SAGEOrder() {
    }

    public boolean importOrder(HibernateDAO dao, Connection conn, Order order, String lang, Integer gDe) {
        setDO_Piece("WEB" + order.getIdOrder().toString());
        setDO_Date(new java.sql.Date(order.getCreatedDate().getTime()));
        setDO_Tiers(order.getUser().getUserId());
        setDO_Ref(StringUtils.left(order.getCustomReference(),17));

        return loadUserData(conn, order, gDe) && loadAddressData(dao, conn, order, lang) && loadLines(dao, conn, order, lang);
    }

    private boolean loadLines(HibernateDAO dao, Connection conn, Order order, String lang) {
        long index = 1;
        lines = new ArrayList<SAGEOrderLine>();
        for (OrderDetail od : order.getOrderDetails()) {
            for (OrderDetailProduct odp : od.getOrderDetailProducts()) {
                SAGEOrderLine line = new SAGEOrderLine();
                line.importLine(odp, lang);
                line.setDL_Ligne(10000 * index++);
                line.setDE_No(getDE_No());
                lines.add(line);
            }
        }
        if (order.getTotalShipping() != null && order.getTotalShipping() > 0) {
            SAGEOrderLine line = new SAGEOrderLine();
            if (line.importShipping(dao, conn, order, lang)) {
                line.setDL_Ligne(10000 * index);
                line.setDE_No(getDE_No());
                lines.add(line);
            }
        }

        return true;
    }

    private boolean loadAddressData(HibernateDAO dao, Connection conn, Order order, String lang) {
        if (order.getDeliveryAddress() != null) {
            SAGEAddress address = new SAGEAddress(order.getDeliveryAddress(), lang);
            if (address.existsInSage(conn)) address.saveAll(conn);
            else if (!address.insert(conn, order.getUser())) {
                lastError = "Error insertando la direccion de envio, a la orden " + order.getIdOrder().toString();
                return false;
            } else {
                order.getDeliveryAddress().setExternalCode(address.getId());
                dao.save(order.getDeliveryAddress());
            }
            setLI_No(address.getLineNumber(conn));
        }
        return true;
    }

    public boolean loadUserData(Connection conn, Order order, Integer gDe) {
        try {
            String query = "select CA_Num,CG_NumPrinc,CT_NumPayeur,DE_No,CT_BlFact,N_Condition,N_Expedition,N_Period,N_CatTarif,N_CatCompta,CO_No from F_COMPTET where CT_Num=?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, order.getUser().getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                setCA_Num(rs.getString("CA_Num"));
                setCG_Num(rs.getString("CG_NumPrinc"));
                setCT_NumPayeur(rs.getString("CT_NumPayeur"));
                setDE_No(rs.getInt("DE_No"));
                setDO_BlFact(rs.getInt("CT_BlFact"));
                setDO_Condition(rs.getInt("N_Condition"));
                setDO_Expedit(rs.getInt("N_Expedition"));
                setDO_Period(rs.getInt("N_Period"));
                setDO_Tarif(rs.getInt("N_CatTarif"));
                setN_CatCompta(rs.getInt("N_CatCompta"));
                setCO_No(rs.getInt("CO_No"));
                checkDeposit(gDe);
                return true;
            } else {
                lastError = "El cliente " + order.getUser().getIdUser().toString() + " de la orden " + order.getIdOrder().toString() + ", no esta configurado en SAGE";
            }
        } catch (SQLException e) {
            lastError = "Error obteniendo datos del cliente de la orden " + order.getIdOrder().toString();
        }
        return false;
    }

    public boolean insert(Connection connection) {
        try {
            connection.setAutoCommit(false);
            String query = "insert into F_DOCENTETE (DO_Domaine, DO_Type, DO_Piece, DO_Date, DO_Tiers, AB_No, CA_No, CA_Num, CG_Num, " +
                    "CT_NumPayeur, DE_No, DO_BlFact, DO_Condition, DO_Expedit, DO_Period, DO_Tarif, DO_TypeColis, N_CatCompta, LI_No, DO_Statut, CO_No, DO_Ref) Values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            setParamInt(stmt, 1, getDO_Domaine());
            setParamInt(stmt, 2, getDO_Type());
            setParamStr(stmt, 3, getDO_Piece());
            setParamDat(stmt, 4, getDO_Date());
            setParamStr(stmt, 5, getDO_Tiers());
            setParamInt(stmt, 6, getAB_No());
            setParamInt(stmt, 7, getCA_No());
            setParamStr(stmt, 8, getCA_Num());
            setParamStr(stmt, 9, getCG_Num());
            setParamStr(stmt, 10, getCT_NumPayeur());
            setParamInt(stmt, 11, getDE_No());
            setParamInt(stmt, 12, getDO_BlFact());
            setParamInt(stmt, 13, getDO_Condition());
            setParamInt(stmt, 14, getDO_Expedit());
            setParamInt(stmt, 15, getDO_Period());
            setParamInt(stmt, 16, getDO_Tarif());
            setParamInt(stmt, 17, getDO_TypeColis());
            setParamInt(stmt, 18, getN_CatCompta());
            setParamInt(stmt, 19, getLI_No());
            setParamInt(stmt, 20, getDO_Statut());
            setParamInt(stmt, 21, getCO_No());
            setParamStr(stmt, 22,getDO_Ref());
            if (stmt.executeUpdate() > 0) {
                if (lines != null && !lines.isEmpty()) {
                    for (SAGEOrderLine line : lines) {
                        line.setDO_Piece(getDO_Piece());
                        line.insert(connection);
                        line.updateStock(connection);
                    }
                }
            }
            connection.commit();
            return true;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ignored) {}
            log.error(e.getMessage(), e); 
            lastError = "Error salvando orden " + getDO_Piece() + " (" + e.getMessage() + ")";
        }

        try {
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            log.error(e.getMessage(), e); 
        }
        return false;
    }

    private void checkDeposit(Integer gDe) {
        if (getDE_No() == null || getDE_No() == 0) {
            setDE_No(gDe);
        }
    }

    public Integer getDO_Domaine() {
        return DO_Domaine;
    }

    public void setDO_Domaine(Integer DO_Domaine) {
        this.DO_Domaine = DO_Domaine;
    }

    public Integer getDO_Type() {
        return DO_Type;
    }

    public void setDO_Type(Integer DO_Type) {
        this.DO_Type = DO_Type;
    }

    public String getDO_Piece() {
        return DO_Piece;
    }

    public void setDO_Piece(String DO_Piece) {
        this.DO_Piece = DO_Piece;
    }

    public Date getDO_Date() {
        return DO_Date;
    }

    public void setDO_Date(Date DO_Date) {
        this.DO_Date = DO_Date;
    }

    public String getDO_Tiers() {
        return DO_Tiers;
    }

    public void setDO_Tiers(String DO_Tiers) {
        this.DO_Tiers = DO_Tiers;
    }

    public Integer getAB_No() {
        return AB_No;
    }

    public void setAB_No(Integer AB_No) {
        this.AB_No = AB_No;
    }

    public Integer getCA_No() {
        return CA_No;
    }

    public void setCA_No(Integer CA_No) {
        this.CA_No = CA_No;
    }

    public String getCA_Num() {
        return CA_Num;
    }

    public void setCA_Num(String CA_Num) {
        this.CA_Num = CA_Num;
    }

    public String getCG_Num() {
        return CG_Num;
    }

    public void setCG_Num(String CG_Num) {
        this.CG_Num = CG_Num;
    }

    public String getCT_NumPayeur() {
        return CT_NumPayeur;
    }

    public void setCT_NumPayeur(String CT_NumPayeur) {
        this.CT_NumPayeur = CT_NumPayeur;
    }

    public Integer getDE_No() {
        return DE_No;
    }

    public void setDE_No(Integer DE_No) {
        this.DE_No = DE_No;
    }

    public Integer getDO_BlFact() {
        return DO_BlFact;
    }

    public void setDO_BlFact(Integer DO_BlFact) {
        this.DO_BlFact = DO_BlFact;
    }

    public Integer getDO_Condition() {
        return DO_Condition;
    }

    public void setDO_Condition(Integer DO_Condition) {
        this.DO_Condition = DO_Condition;
    }

    public Integer getDO_Expedit() {
        return DO_Expedit;
    }

    public void setDO_Expedit(Integer DO_Expedit) {
        this.DO_Expedit = DO_Expedit;
    }

    public Integer getDO_Period() {
        return DO_Period;
    }

    public void setDO_Period(Integer DO_Period) {
        this.DO_Period = DO_Period;
    }

    public Integer getDO_Tarif() {
        return DO_Tarif;
    }

    public void setDO_Tarif(Integer DO_Tarif) {
        this.DO_Tarif = DO_Tarif;
    }

    public Integer getDO_TypeColis() {
        return DO_TypeColis;
    }

    public void setDO_TypeColis(Integer DO_TypeColis) {
        this.DO_TypeColis = DO_TypeColis;
    }

    public Integer getN_CatCompta() {
        return N_CatCompta;
    }

    public void setN_CatCompta(Integer n_CatCompta) {
        N_CatCompta = n_CatCompta;
    }

    public Integer getLI_No() {
        return LI_No;
    }

    public void setLI_No(Integer LI_No) {
        this.LI_No = LI_No;
    }

    public Integer getDO_Statut() {
        return DO_Statut;
    }

    public void setDO_Statut(Integer DO_Statut) {
        this.DO_Statut = DO_Statut;
    }

    public Integer getCO_No() {
        return CO_No;
    }

    public void setCO_No(Integer CO_No) {
        this.CO_No = CO_No;
    }

    public String getDO_Ref() {
        return DO_Ref;
    }

    public void setDO_Ref(String DO_Ref) {
        this.DO_Ref = DO_Ref;
    }

    public String getShippingLineName() {
        return shippingLineName;
    }

    public void setShippingLineName(String shippingLineName) {
        this.shippingLineName = shippingLineName;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public class SAGEOrderLine {

        private Integer DO_Domaine;
        private Integer DO_Type;
        private String CT_Num;
        private String DO_Piece;
        private Long DL_Ligne;
        private String AR_Ref;
        private Double EU_Qte;
        private Integer DL_Valorise;
        private Date DO_Date;
        private Double DL_Qte;
        private Integer DL_No;
        private String DL_Design;
        private Double DL_PUTTC;
        private Double DL_MontantTTC;
        private Double DL_PrixUnitaire;
        private Double DL_MontantHT;
        private Double DL_PrixRU;
        private Integer DL_TNomencl;
        private Integer DL_TRemPied;
        private Integer DL_TRemExep;
        private Integer DL_TypePL;
        private Integer DE_No;
        private Double DL_Taxe1;
        private Double DL_Taxe2;
        private Double DL_Taxe3;
        private String EU_Enumere;

        public void importLine(OrderDetailProduct odp, String lang) {
            double productPrice = odp.getOrderDetail().getPrice() * odp.getPercentOfPrice();
            setDO_Domaine(0);
            setDO_Type(1);
            setCT_Num(odp.getOrder().getUser().getUserId());
            setAR_Ref(odp.getProduct().getPartNumber());
            setEU_Qte(odp.getOrderDetail().getQuantity().doubleValue());
            setDL_Valorise(1);
            setDO_Date(new java.sql.Date(odp.getOrder().getCreatedDate().getTime()));
            setDL_Qte(odp.getOrderDetail().getQuantity().doubleValue());
            setDL_No(0);
            setDL_Design(odp.getProduct().getProductName(lang));
            setDL_PrixUnitaire(productPrice);
            setDL_MontantHT(productPrice * odp.getOrderDetail().getQuantity());
            setDL_TNomencl(0);
            setDL_TRemPied(0);
            setDL_TRemExep(0);
            setDL_TypePL(0); //Factura
            setDL_PUTTC(productPrice + odp.getTax());
            setDL_MontantTTC((productPrice + odp.getTax()) * odp.getOrderDetail().getQuantity());
            setDL_PrixRU(odp.getCostPrice());
            setEU_Enumere(CNT_UNIDAD);
            List<Map<String, Object>> taxList = odp.getTaxesList();
            if (taxList != null && !taxList.isEmpty()) {
                if (taxList.size() > 0) {
                    Map taxMap = taxList.get(0);
                    setDL_Taxe1(100 * (Double) taxMap.get("value"));
                }
                if (taxList.size() > 1) {
                    Map taxMap = taxList.get(1);
                    setDL_Taxe2(100 * (Double) taxMap.get("value"));
                }
                if (taxList.size() > 2) {
                    Map taxMap = taxList.get(2);
                    setDL_Taxe3(100 * (Double) taxMap.get("value"));
                }
            }
        }

        public boolean importShipping(HibernateDAO dao, Connection conn, Order order, String lang) {

            SAGEProduct sageProd = new SAGEProduct(conn, shippingLineName);
            if (StringUtils.isNotEmpty(sageProd.getAR_Design())) {
                double productPrice = order.getTotalShipping();
                setDO_Domaine(0);
                setDO_Type(1);
                setCT_Num(order.getUser().getUserId());
                setAR_Ref(shippingLineName);
                setEU_Qte(1d);
                setDL_Valorise(1);
                setDO_Date(new java.sql.Date(order.getCreatedDate().getTime()));
                setDL_Qte(1d);
                setDL_No(0);
                setDL_Design(sageProd.getAR_Design());
                setDL_PrixUnitaire(productPrice);
                setDL_MontantHT(productPrice);
                setDL_TNomencl(0);
                setDL_TRemPied(0);
                setDL_TRemExep(0);
                setDL_TypePL(0); //Factura
                setDL_PrixRU(productPrice);

                double taxes = 0;
                ProductUserTax puTax = dao.getProductUserTaxes(sageProd.getFA_CodeFamille(), order.getUser().getAltCategory());
                if (puTax != null && puTax.getTaxes() != null && !puTax.getTaxes().isEmpty()) {
                    for (TaxPerFamily tpf : puTax.getTaxes()) {
                        if (tpf.getValue() != null && tpf.getValue() > 0) taxes += productPrice * tpf.getValue();
                    }
                    if (puTax.getTaxes().size() > 0) {
                        TaxPerFamily taxMap = puTax.getTaxes().get(0);
                        setDL_Taxe1(100 * taxMap.getValue());
                    }
                    if (puTax.getTaxes().size() > 1) {
                        TaxPerFamily taxMap = puTax.getTaxes().get(1);
                        setDL_Taxe2(100 * taxMap.getValue());
                    }
                    if (puTax.getTaxes().size() > 2) {
                        TaxPerFamily taxMap = puTax.getTaxes().get(2);
                        setDL_Taxe3(100 * taxMap.getValue());
                    }
                }
                setDL_PUTTC(productPrice + taxes);
                setDL_MontantTTC(productPrice + taxes);

                return true;
            } else {
                setLastError("No se encontro en SAGE el producto con AR_REF="+shippingLineName);
                return false;
            }
        }

        public boolean insert(Connection connection) {
            try {
                String query = "insert into F_DOCLIGNE (DO_Domaine, DO_Type, CT_Num, DO_Piece, DL_Ligne, AR_Ref, EU_Qte, DL_Valorise, DO_Date, DL_Qte, DL_No, DL_Design, DL_PrixUnitaire, DL_MontantHT, DL_TNomencl, DL_TRemPied, DL_TRemExep, DE_No, DL_TypePL, DL_PUTTC, DL_MontantTTC, DL_PrixRU, DL_Taxe1, DL_Taxe2, DL_Taxe3, EU_Enumere) " +
                        "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = connection.prepareStatement(query);
                setParamInt(stmt, 1, getDO_Domaine());
                setParamInt(stmt, 2, getDO_Type());
                setParamStr(stmt, 3, getCT_Num());
                setParamStr(stmt, 4, getDO_Piece());
                setParamLng(stmt, 5, getDL_Ligne());
                setParamStr(stmt, 6, getAR_Ref());
                setParamDbl(stmt, 7, getEU_Qte());
                setParamInt(stmt, 8, getDL_Valorise());
                setParamDat(stmt, 9, getDO_Date());
                setParamDbl(stmt, 10, getDL_Qte());
                setParamInt(stmt, 11, getDL_No());
                setParamStr(stmt, 12, getDL_Design());
                setParamDbl(stmt, 13, getDL_PrixUnitaire());
                setParamDbl(stmt, 14, getDL_MontantHT());
                setParamInt(stmt, 15, getDL_TNomencl());
                setParamInt(stmt, 16, getDL_TRemPied());
                setParamInt(stmt, 17, getDL_TRemExep());
                setParamInt(stmt, 18, getDE_No());
                setParamInt(stmt, 19, getDL_TypePL());
                setParamDbl(stmt, 20, getDL_PUTTC());
                setParamDbl(stmt, 21, getDL_MontantTTC());
                setParamDbl(stmt, 22, getDL_PrixRU());
                setParamDbl(stmt, 23, getDL_Taxe1());
                setParamDbl(stmt, 24, getDL_Taxe2());
                setParamDbl(stmt, 25, getDL_Taxe3());
                setParamStr(stmt, 26, getEU_Enumere());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                log.error(e.getMessage(), e); 
                lastError = "Error salvando linea de la orden " + getDO_Piece() + " (" + e.getMessage() + ")";
            }
            return false;
        }

        public boolean updateStock(Connection connection) {
            boolean result = false;
            try {
                PreparedStatement stmt = connection.prepareStatement( "select AS_QteRes from F_ARTSTOCK where AR_Ref=? and DE_No=?");
                setParamStr(stmt, 1, getAR_Ref());
                setParamInt(stmt, 2, getDE_No());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    Double oldSt = rs.getDouble(1);
                    if (oldSt!=null) {
                        PreparedStatement stmtUp = connection.prepareStatement( "update F_ARTSTOCK set AS_QteRes=?  where AR_Ref=? and DE_No=?");
                        setParamDbl(stmtUp, 1, oldSt + getDL_Qte());
                        setParamStr(stmtUp, 2, getAR_Ref());
                        setParamInt(stmtUp, 3, getDE_No());
                        if (stmtUp.executeUpdate()>0) result = true;
                    }
                }
            } catch (SQLException e) {
                log.error(e.getMessage(), e); 
            }
            return result;
        }

        public Integer getDO_Domaine() {
            return DO_Domaine;
        }

        public void setDO_Domaine(Integer DO_Domaine) {
            this.DO_Domaine = DO_Domaine;
        }

        public Integer getDO_Type() {
            return DO_Type;
        }

        public void setDO_Type(Integer DO_Type) {
            this.DO_Type = DO_Type;
        }

        public String getCT_Num() {
            return CT_Num;
        }

        public void setCT_Num(String CT_Num) {
            this.CT_Num = CT_Num;
        }

        public String getDO_Piece() {
            return DO_Piece;
        }

        public void setDO_Piece(String DO_Piece) {
            this.DO_Piece = DO_Piece;
        }

        public Long getDL_Ligne() {
            return DL_Ligne;
        }

        public void setDL_Ligne(Long DL_Ligne) {
            this.DL_Ligne = DL_Ligne;
        }

        public String getAR_Ref() {
            return AR_Ref;
        }

        public void setAR_Ref(String AR_Ref) {
            this.AR_Ref = AR_Ref;
        }

        public Double getEU_Qte() {
            return EU_Qte;
        }

        public void setEU_Qte(Double EU_Qte) {
            this.EU_Qte = EU_Qte;
        }

        public Integer getDL_Valorise() {
            return DL_Valorise;
        }

        public void setDL_Valorise(Integer DL_Valorise) {
            this.DL_Valorise = DL_Valorise;
        }

        public Date getDO_Date() {
            return DO_Date;
        }

        public void setDO_Date(Date DO_Date) {
            this.DO_Date = DO_Date;
        }

        public Double getDL_Qte() {
            return DL_Qte;
        }

        public void setDL_Qte(Double DL_Qte) {
            this.DL_Qte = DL_Qte;
        }

        public Integer getDL_No() {
            return DL_No;
        }

        public void setDL_No(Integer DL_No) {
            this.DL_No = DL_No;
        }

        public String getDL_Design() {
            return DL_Design;
        }

        public void setDL_Design(String DL_Design) {
            this.DL_Design = DL_Design;
        }

        public Double getDL_PrixUnitaire() {
            return DL_PrixUnitaire;
        }

        public void setDL_PrixUnitaire(Double DL_PrixUnitaire) {
            this.DL_PrixUnitaire = DL_PrixUnitaire;
        }

        public Double getDL_MontantHT() {
            return DL_MontantHT;
        }

        public void setDL_MontantHT(Double DL_MontantHT) {
            this.DL_MontantHT = DL_MontantHT;
        }

        public Integer getDL_TRemPied() {
            return DL_TRemPied;
        }

        public void setDL_TRemPied(Integer DL_TRemPied) {
            this.DL_TRemPied = DL_TRemPied;
        }

        public Integer getDL_TNomencl() {
            return DL_TNomencl;
        }

        public void setDL_TNomencl(Integer DL_TNomencl) {
            this.DL_TNomencl = DL_TNomencl;
        }

        public Integer getDL_TRemExep() {
            return DL_TRemExep;
        }

        public void setDL_TRemExep(Integer DL_TRemExep) {
            this.DL_TRemExep = DL_TRemExep;
        }

        public Integer getDE_No() {
            return DE_No;
        }

        public void setDE_No(Integer DE_No) {
            this.DE_No = DE_No;
        }

        public Double getDL_PUTTC() {
            return DL_PUTTC;
        }

        public void setDL_PUTTC(Double DL_PUTTC) {
            this.DL_PUTTC = DL_PUTTC;
        }

        public Double getDL_MontantTTC() {
            return DL_MontantTTC;
        }

        public void setDL_MontantTTC(Double DL_MontantTTC) {
            this.DL_MontantTTC = DL_MontantTTC;
        }

        public Integer getDL_TypePL() {
            return DL_TypePL;
        }

        public void setDL_TypePL(Integer DL_TypePL) {
            this.DL_TypePL = DL_TypePL;
        }

        public Double getDL_PrixRU() {
            return DL_PrixRU;
        }

        public void setDL_PrixRU(Double DL_PrixRU) {
            this.DL_PrixRU = DL_PrixRU;
        }

        public Double getDL_Taxe1() {
            return DL_Taxe1;
        }

        public void setDL_Taxe1(Double DL_Taxe1) {
            this.DL_Taxe1 = DL_Taxe1;
        }

        public Double getDL_Taxe2() {
            return DL_Taxe2;
        }

        public void setDL_Taxe2(Double DL_Taxe2) {
            this.DL_Taxe2 = DL_Taxe2;
        }

        public Double getDL_Taxe3() {
            return DL_Taxe3;
        }

        public void setDL_Taxe3(Double DL_Taxe3) {
            this.DL_Taxe3 = DL_Taxe3;
        }

        public String getEU_Enumere() {
            return EU_Enumere;
        }

        public void setEU_Enumere(String EU_Enumere) {
            this.EU_Enumere = EU_Enumere;
        }
    }

    protected void setParamDbl(PreparedStatement stmt, int index, Double value) throws SQLException {
        if (value != null) stmt.setDouble(index, value);
        else stmt.setNull(index, java.sql.Types.DOUBLE);
    }

    protected void setParamInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value != null) stmt.setInt(index, value);
        else stmt.setNull(index, java.sql.Types.INTEGER);
    }

    protected void setParamLng(PreparedStatement stmt, int index, Long value) throws SQLException {
        if (value != null) stmt.setLong(index, value);
        else stmt.setNull(index, java.sql.Types.BIGINT);
    }

    protected void setParamStr(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value != null) stmt.setString(index, value);
        else stmt.setNull(index, java.sql.Types.VARCHAR);
    }

    protected void setParamDat(PreparedStatement stmt, int index, Date value) throws SQLException {
        if (value != null) stmt.setDate(index, value);
        else stmt.setNull(index, java.sql.Types.DATE);
    }

}
