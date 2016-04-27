package org.store.sage.bean;


import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SAGEAppliedTax {

    public static Logger log = Logger.getLogger(SAGEAppliedTax.class);
    private final String[] taxFields = {"FCP_ComptaCPT_Taxe1","FCP_ComptaCPT_Taxe2","FCP_ComptaCPT_Taxe3"};

    private Long id;
    private String FA_CodeFamille;
    private Integer FCP_Champ;
    private List<SAGETax> taxes;


    public SAGEAppliedTax() {
    }

    public SAGEAppliedTax(Connection connection, Long id) {
        this.id = id;
        loadFromDb(connection);
    }

    private void loadFromDb(Connection connection) {
        try {
            PreparedStatement stmt1 = connection.prepareStatement("SELECT * FROM F_FAMCOMPTA WHERE cbMarq=?");
            stmt1.setLong(1, getId());
            ResultSet rs1 = stmt1.executeQuery();
            if (rs1.next()) {
                setFA_CodeFamille(rs1.getString("FA_CodeFamille"));
                setFCP_Champ(rs1.getInt("FCP_Champ"));
                for(String taxField : taxFields) {
                    String taxCode = rs1.getString(taxField);
                    if (StringUtils.isNotEmpty(taxCode)) {
                        SAGETax tax = new SAGETax(connection,taxCode);
                        if (tax.isNeeded())
                            getTaxes().add(tax);
                    }
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFA_CodeFamille() {
        return FA_CodeFamille;
    }

    public void setFA_CodeFamille(String FA_CodeFamille) {
        this.FA_CodeFamille = FA_CodeFamille;
    }

    public Integer getFCP_Champ() {
        return FCP_Champ;
    }

    public void setFCP_Champ(Integer FCP_Champ) {
        this.FCP_Champ = FCP_Champ;
    }

    public List<SAGETax> getTaxes() {
        if (taxes==null) taxes = new ArrayList<SAGETax>();
        return taxes;
    }

    public void setTaxes(List<SAGETax> taxes) {
        this.taxes = taxes;
    }

    public boolean hasTaxes() {
        return taxes!=null && !taxes.isEmpty();
    }
}
