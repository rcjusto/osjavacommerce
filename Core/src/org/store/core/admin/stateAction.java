package org.store.core.admin;

import org.store.core.beans.State;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.CountryFactory;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.geonames.FeatureClass;
import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;

import java.util.List;
import java.util.Locale;

@Namespace(value = "/admin")
@ParentPackage(value = "store-admin")
public class stateAction extends AdminModuleAction implements StoreMessages {

    @Action(value = "addallcountries", results = @Result(type = "redirectAction", location = "liststate"))
    public String addAllCountries() throws Exception {
        List<CountryFactory.Country> countries = CountryFactory.getCountries(Locale.getDefault());
        for(CountryFactory.Country c : countries) {
            List<State> states = dao.getStatesByCountry(countryCode);
            if (states==null || states.isEmpty()) {
                State state = new State();
                state.setStateName("*");
                state.setStateCode("*");
                state.setCountryCode(c.getCode());
                dao.save(state);
            }
        }
        return SUCCESS;
    }

    @Action(value = "stategenerator", results = @Result(type = "velocity", location = "/WEB-INF/views/admin/statelist.vm"))
    public String generate() throws Exception {
        if (StringUtils.isNotEmpty(countryCode)) {
            try {
                ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
                searchCriteria.setCountryCode(countryCode);
                searchCriteria.setFeatureClass(FeatureClass.A);
                searchCriteria.setFeatureCode("ADM1");
                searchCriteria.setStyle(Style.FULL);
                searchCriteria.setLanguage(getDefaultLanguage());
                ToponymSearchResult searchResult = WebService.search(searchCriteria);
                for (Toponym toponym : searchResult.getToponyms()) {
                    if (StringUtils.isNotEmpty(toponym.getAdminCode1())) {
                        State st = dao.getStateByCode(countryCode, toponym.getAdminCode1());
                        if (st == null) {
                            st = new State();
                            st.setCountryCode(countryCode);
                            st.setInventaryCode(getStoreCode());
                            st.setStateCode(toponym.getAdminCode1());
                            st.setStateName(toponym.getAdminName1());
                            dao.save(st);
                        }

                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                addActionError(getText(CNT_ERROR_GEONAME_CONEXION, CNT_DEFAULT_ERROR_GEONAME_CONEXION));
            }
            addToStack("states", dao.getStatesByCountry(countryCode));
        }
        return SUCCESS;
    }

    public String list() throws Exception {

        if (StringUtils.isNotEmpty(deleteCountry)) {
            List<State> states = dao.getStatesByCountry(deleteCountry);
            for(State s : states) {
                String error = dao.isUsedState(s);
                if (StringUtils.isEmpty(error)) dao.delete(s);
                else addActionError(getText(CNT_CANNOT_DELETE_STATE, CNT_DEFAULT_CANNOT_DELETE_STATE, new String[]{s.getStateCode(), error}));
            }
        }

        if (StringUtils.isNotEmpty(countryCode) && stateCode != null && stateName != null && ArrayUtils.isSameLength(stateCode, stateName)) {

            for (int i = 0; i < stateId.length; i++) {
                State s = (State) dao.get(State.class, stateId[i]);
                if (StringUtils.isNotEmpty(stateCode[i]) && StringUtils.isNotEmpty(stateName[i])) {
                    if (s == null) s = new State();
                    s.setCountryCode(countryCode);
                    s.setInventaryCode(getStoreCode());
                    s.setStateCode(stateCode[i]);
                    s.setStateName(stateName[i]);
                    dao.save(s);
                } else if (s != null) {
                    String error = dao.isUsedState(s);
                    if (StringUtils.isEmpty(error)) dao.delete(s);
                    else addActionError(getText(CNT_CANNOT_DELETE_STATE, CNT_DEFAULT_CANNOT_DELETE_STATE, new String[]{s.getStateCode(), error}));
                }

            }
        }
        if (StringUtils.isEmpty(countryCode)) countryCode = getDefaultCountry();
        addToStack("states", dao.getStatesByCountry(countryCode));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.state.list"), null, null));
        return SUCCESS;
    }

    private String deleteCountry;
    private String countryCode;
    private Long[] stateId;
    private String[] stateCode;
    private String[] stateName;

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Long[] getStateId() {
        return stateId;
    }

    public void setStateId(Long[] stateId) {
        this.stateId = stateId;
    }

    public String[] getStateCode() {
        return stateCode;
    }

    public void setStateCode(String[] stateCode) {
        this.stateCode = stateCode;
    }

    public String[] getStateName() {
        return stateName;
    }

    public void setStateName(String[] stateName) {
        this.stateName = stateName;
    }

    public String getDeleteCountry() {return deleteCountry;}

    public void setDeleteCountry(String deleteCountry) {this.deleteCountry = deleteCountry;}
}
