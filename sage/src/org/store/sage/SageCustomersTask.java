package org.store.sage;

import org.store.core.beans.Job;
import org.store.core.beans.StoreProperty;
import org.store.core.beans.User;
import org.store.core.beans.UserAddress;
import org.store.core.globals.CountryFactory;
import org.store.core.globals.SomeUtils;
import org.store.core.globals.config.Store20Database;
import org.store.sage.bean.SAGEAddress;
import org.store.sage.bean.SAGEUser;
import org.apache.commons.lang.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SageCustomersTask extends SageTask {

    private Map<String, String> countries;
    private List<org.store.core.beans.State> states;
    private Long onlyExportUser;

    public SageCustomersTask(String storeCode, Job job, Store20Database databaseConfig) throws Exception {
        super(storeCode, job, databaseConfig);
    }

    @Override
    protected void execute() {
        setExecutionPercent(0d);
        fillStates();

        if (onlyExportUser != null) {
            User user = (User) dao.get(User.class, onlyExportUser);
            if (user!=null) exportUser(user);
        } else {
            // Importar usuarios
            int newUsers = 0, updUsers = 0;
            setExecutionMessage("Buscando clientes para sincronizar...");
            List<SAGEUser> usersToImport = getUsersToImport();
            int index = 0, totalUsers = usersToImport.size();
            for (SAGEUser sageUser : usersToImport) {
                setExecutionPercent(100d * index / totalUsers);
                setExecutionMessage("Sincronizando cliente " + String.valueOf(index++) + "/" + String.valueOf(totalUsers));
                if (StringUtils.isNotEmpty(sageUser.getCT_Num())) {
                    Boolean res = importUser(sageUser);
                    if (res != null) {
                        if (res) newUsers++;
                        else updUsers++;
                    }
                    dao.flushSession();
                    dao.clearSession();
                }
            }
            addOutputMessage(newUsers + " clientes nuevos importados");
            addOutputMessage(updUsers + " clientes actualizados");

            if ("Y".equalsIgnoreCase(getProperty(SageCustomersJob.PROP_UPDATE_CLIENT_ADDRESSES, "Y"))
                    || "Y".equalsIgnoreCase(getProperty(SageCustomersJob.PROP_UPDATE_CLIENT_NAMES, "Y"))
                    || "Y".equalsIgnoreCase(getProperty(SageCustomersJob.PROP_UPDATE_CONTACT_INFO, "Y"))) {

                List<User> usersToExport = getUsersToExport();
                for (User user : usersToExport) {
                    exportUser(user);
                }
            }

        }


    }

    private void exportUser(User user) {
        //buscar direccion de billing importada de sage
        UserAddress billingAddress = dao.getAddressByExternalCode(user, "BILLING_" + user.getUserId());

        SAGEUser sageUser = new SAGEUser(sageConnection, user, billingAddress, defaultLanguage);
        sageUser.setLevel(getSageLevel(user.getLevel()));

        if ("Y".equalsIgnoreCase(getProperty(SageCustomersJob.PROP_UPDATE_CLIENT_ADDRESSES, "Y"))) {
            sageUser.saveFieldToDb(sageConnection, "CT_Adresse");
            sageUser.saveFieldToDb(sageConnection, "CT_CodePostal");
            sageUser.saveFieldToDb(sageConnection, "CT_CodeRegion");
            sageUser.saveFieldToDb(sageConnection, "CT_Ville");
            sageUser.saveFieldToDb(sageConnection, "CT_Pays");

            for (UserAddress userAddress : user.getAddressList()) {
                if (userAddress.getExternalCode() == null || !userAddress.getExternalCode().startsWith("BILLING_")) {
                    SAGEAddress sageAddress = new SAGEAddress(userAddress, defaultLanguage);
                    sageAddress.saveFieldToDb(sageConnection, "LI_Adresse");
                    sageAddress.saveFieldToDb(sageConnection, "LI_CodePostal");
                    sageAddress.saveFieldToDb(sageConnection, "LI_CodeRegion");
                    sageAddress.saveFieldToDb(sageConnection, "LI_Pays");
                    sageAddress.saveFieldToDb(sageConnection, "LI_Ville");
                    sageAddress.saveFieldToDb(sageConnection, "LI_Telecopie");
                    sageAddress.saveFieldToDb(sageConnection, "LI_Telephone");
                    sageAddress.saveFieldToDb(sageConnection, "LI_Intitule");
                    sageAddress.saveFieldToDb(sageConnection, "LI_Contact");
                }
            }
        }
        if ("Y".equalsIgnoreCase(getProperty(SageCustomersJob.PROP_UPDATE_CLIENT_NAMES, "Y"))) {
            sageUser.saveFieldToDb(sageConnection, "CT_Intitule");
            sageUser.saveFieldToDb(sageConnection, "CT_Contact");
        }
        if ("Y".equalsIgnoreCase(getProperty(SageCustomersJob.PROP_UPDATE_CONTACT_INFO, "Y"))) {
            sageUser.saveFieldToDb(sageConnection, "CT_EMail");
            sageUser.saveFieldToDb(sageConnection, "CT_Site");
            sageUser.saveFieldToDb(sageConnection, "CT_Telephone");
        }


    }


    private List<User> getUsersToExport() {
        return dao.getUsers();
    }


    private Boolean importUser(SAGEUser sageUser) {
        Boolean res = Boolean.FALSE;
        User user = dao.getUserByUserId(sageUser.getCT_Num());
        if (user == null) {
            if (!"Y".equalsIgnoreCase(getProperty(SageCustomersJob.PROP_IMPORT_NEW_CLIENTS, "Y"))) return null;
            res = Boolean.TRUE;
            user = new User();
            user.setInventaryCode(storeCode);
            user.setRegisterDate(Calendar.getInstance().getTime());
            user.setUserId(sageUser.getCT_Num());
            try {
                user.setPassword(SomeUtils.encrypt3Des(sageUser.getCT_Num(), getEncryptionKey()));
            } catch (Exception e) {
                log.error(e.getMessage(), e); 
            }
        } else {
            if (!"Y".equalsIgnoreCase(getProperty(SageCustomersJob.PROP_UPDATE_OLD_CLIENTS, "Y"))) return null;
        }
        sageUser.copyToUser(user);
        user.setLevel(importUserLevel(sageUser.getLevel()));
        dao.save(user);

        // direccion de billing
        importAddress(sageUser.getBillingAddress(), user, true, false);

        // direcciones de shipping
        if (sageUser.getAddresses() != null) {
            for (SAGEAddress sageAddress : sageUser.getAddresses())
                importAddress(sageAddress, user, false, true);
        }

        return res;
    }

    private void importAddress(SAGEAddress sageAddress, User user, boolean billing, boolean shipping) {
        org.store.core.beans.State state = findState(sageAddress.getLI_CodeRegion(), sageAddress.getLI_Pays());
        if (state != null) {
            UserAddress userAddress = dao.getAddressByExternalCode(user, sageAddress.getId());
            if (userAddress == null) {
                userAddress = new UserAddress();
                userAddress.setUser(user);
                userAddress.setExternalCode(sageAddress.getId());
                userAddress.setBilling(billing);
                userAddress.setShipping(shipping);
            }
            userAddress.setState(state);
            userAddress.setIdCountry(state.getCountryCode());
            sageAddress.copyToAddress(userAddress);
            dao.save(userAddress);
        } else {
            addOutputMessage("No se pudo importar la direccion '" + sageAddress.getId() + "' del cliente '"+user.getUserId()+"', porque no se encontró la provincia '" + sageAddress.getLI_CodeRegion() + "' del pais '" + sageAddress.getLI_Pays() + "'");
        }
    }

    private List<SAGEUser> getUsersToImport() {
        List<SAGEUser> result = new ArrayList<SAGEUser>();
        try {
            PreparedStatement stmt = sageConnection.prepareStatement("SELECT CT_Num FROM F_COMPTET WHERE CT_Type=0 and CT_Siret='web'");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) result.add(new SAGEUser(sageConnection, rs.getString(1)));
        } catch (SQLException e) {
            log.error(e.getMessage(), e); 
        }
        return result;
    }

    private String getEncryptionKey() {
        StoreProperty encBean = dao.getStoreProperty(StoreProperty.PROP_ENCRYPTION_KEY, StoreProperty.TYPE_GENERAL, true);
        if (StringUtils.isEmpty(encBean.getValue())) {
            encBean.setValue(User.generatePassword(16));
            dao.save(encBean);
        }
        return encBean.getValue();
    }


    private void fillStates() {
        countries = new HashMap<String, String>();
        for (CountryFactory.Country c : CountryFactory.getCountries(new Locale(defaultLanguage))) {
            countries.put(c.getCode(), c.getName());
        }
        states = dao.getAllStates();
    }

    private org.store.core.beans.State findState(String li_codeRegion, String li_pays) {
        if (StringUtils.isEmpty(li_codeRegion)) return null;

        // buscar provincias
        org.store.core.beans.State result = null;
        for (org.store.core.beans.State state : states) {
            if (li_codeRegion.equalsIgnoreCase(state.getStateName())) {
                // Verificar pais
                String pais = (countries.containsKey(state.getCountryCode())) ? countries.get(state.getCountryCode()) : "";
                if (pais.equalsIgnoreCase(li_pays)) return state;
                else if (result == null) result = state;
            }
        }
        return result;
    }

    public void setOnlyExportUser(Long onlyExportUser) {
        this.onlyExportUser = onlyExportUser;
    }
}
