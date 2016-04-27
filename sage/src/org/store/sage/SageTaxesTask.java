package org.store.sage;

import org.store.core.beans.Job;
import org.store.core.beans.ProductUserTax;
import org.store.core.beans.TaxPerFamily;
import org.store.core.globals.config.Store20Database;
import org.store.sage.bean.SAGEAppliedTax;
import org.store.sage.bean.SAGETax;
import org.apache.commons.lang.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SageTaxesTask extends SageTask {


    public SageTaxesTask(String storeCode, Job job, Store20Database databaseConfig) throws Exception {
        super(storeCode, job, databaseConfig);
    }

    @Override
    protected void execute() {
        setExecutionPercent(0d);

        // importar categorias
        setExecutionMessage("Sincronizando impuestos...");
        List<SAGEAppliedTax> taxesToImport = getTaxesToImport();
        if (taxesToImport != null && !taxesToImport.isEmpty()) {
            int numtax = 0;
            int index = 0, totalTaxes = taxesToImport.size();
            for (SAGEAppliedTax sageApplyTax : taxesToImport) {
                setExecutionPercent(100d * index / totalTaxes);
                setExecutionMessage("Sincronizando impuesto " + String.valueOf(index++) + "/" + String.valueOf(totalTaxes));
                if (importTax(sageApplyTax)) numtax++;
            }
            addOutputMessage(numtax + " impuestos por familia importados");
            dao.flushSession();
            dao.clearSession();
        } else {
            addOutputMessage("No se encontraron impuestos para importar");
        }


    }

    private boolean importTax(SAGEAppliedTax sageApplyTax) {
        if (StringUtils.isNotEmpty(sageApplyTax.getFA_CodeFamille()) && sageApplyTax.getFCP_Champ()!=null && sageApplyTax.hasTaxes()) {
            ProductUserTax puTax = dao.getProductUserTaxes(sageApplyTax.getFA_CodeFamille(), sageApplyTax.getFCP_Champ().toString());
            if (puTax==null) {
                puTax = new ProductUserTax();
                puTax.setFamilyProduct(sageApplyTax.getFA_CodeFamille());
                puTax.setCategoryUser(sageApplyTax.getFCP_Champ().toString());
            }
            puTax.setTaxes(new ArrayList<TaxPerFamily>());
            for(SAGETax sageTax : sageApplyTax.getTaxes()) {
                puTax.getTaxes().add(getTaxPerFamily(sageTax));
            }
            dao.save(puTax);
            return true;
        }
        return false;
    }

    public TaxPerFamily getTaxPerFamily(SAGETax sageTax) {
        TaxPerFamily tpf = dao.getTaxPerFamilyByExtCode(sageTax.getTA_Code());
        if (tpf==null) {
            tpf = new TaxPerFamily();
            tpf.setExternalCode(sageTax.getTA_Code());
        }
        tpf.setTaxName(sageTax.getTA_Intitule());
        tpf.setValue(sageTax.getTaxValue());
        dao.save(tpf);
        return tpf;
    }


    private List<SAGEAppliedTax> getTaxesToImport() {
        List<SAGEAppliedTax> result = new ArrayList<SAGEAppliedTax>();
        try {
            PreparedStatement stmt = sageConnection.prepareStatement("select cbMarq from F_FAMCOMPTA where FCP_TypeFacture=0 and FCP_Type=0");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) result.add(new SAGEAppliedTax(sageConnection, rs.getLong(1)));
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }



}
