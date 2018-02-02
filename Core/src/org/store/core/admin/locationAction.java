package org.store.core.admin;

import org.store.core.beans.LocationStore;
import org.store.core.beans.State;
import org.store.core.beans.utils.BreadCrumb;
import org.store.core.globals.CountryFactory;
import org.store.core.globals.StoreMessages;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class locationAction extends AdminModuleAction implements StoreMessages {

    @Override
    public void prepare() throws Exception {
        locationStore = (LocationStore) dao.get(LocationStore.class, idStore);
    }

    public String list() throws Exception {
        if (ids != null && ids.length > 0) {
            for (int i = 0; i < ids.length; i++) {
                LocationStore bean = (LocationStore) dao.get(LocationStore.class, ids[i]);
                if (bean != null) {
                    bean.setActive(active != null && active.length > i && "Y".equalsIgnoreCase(active[i]));
                    bean.setMain(main != null && main.length > i && "Y".equalsIgnoreCase(main[i]));
                    dao.save(bean);
                }
            }
        }

        if (selecteds != null && selecteds.length > 0) {
            for (Long id : selecteds) {
                LocationStore bean = (LocationStore) dao.get(LocationStore.class, id);
                if (bean != null) {
                    String res = dao.isUsedLocationStore(bean);
                    if (StringUtils.isNotEmpty(res)) {
                        addActionError(getText(CNT_ERROR_CANNOT_DELETE_LOCATION, CNT_DEFAULT_ERROR_CANNOT_DELETE_LOCATION, new String[]{bean.getStoreName(), res}));
                    } else {
                        dao.delete(bean);
                    }
                }
            }
            dao.flushSession();
        }

        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.location.list"), null, null));
        addToStack("locations", dao.getLocationStores(false));
        return SUCCESS;
    }

    public String edit() throws Exception {
        setCountries(CountryFactory.getCountries(getLocale()));
        getBreadCrumbs().add(new BreadCrumb(null, getText("admin.location.list"), url("listlocation", "/admin"), null));
        getBreadCrumbs().add(new BreadCrumb(null, getText(locationStore != null ? "admin.location.modify" : "admin.location.new"), null, null));
        return SUCCESS;
    }

    public String save() throws Exception {
        if (locationStore != null) {
            State state = (idState != null) ? (State) dao.get(State.class, idState) : null;
            if (state == null && !StringUtils.isEmpty(newState)) {
                state = new State();
                state.setCountryCode(locationStore.getIdCountry());
                state.setStateCode(StringUtils.left(newState, 5));
                state.setStateName(newState);
                state.setInventaryCode(getStoreCode());
                dao.save(state);
            }
            locationStore.setState(state);
            locationStore.setInventaryCode(getStoreCode());
            dao.save(locationStore);
        }
        return SUCCESS;
    }

    private Long idState;
    private String newState;
    private LocationStore locationStore;
    private Long idStore;
    private Long storeState;
    private List countries;

    private Long[] ids;
    private String[] active;
    private String[] main;


    public Long[] getIds() {
        return ids;
    }

    public void setIds(Long[] ids) {
        this.ids = ids;
    }

    public String[] getActive() {
        return active;
    }

    public void setActive(String[] active) {
        this.active = active;
    }

    public String[] getMain() {
        return main;
    }

    public void setMain(String[] main) {
        this.main = main;
    }

    public Long getIdState() {
        return idState;
    }

    public void setIdState(Long idState) {
        this.idState = idState;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public LocationStore getLocationStore() {
        return locationStore;
    }

    public void setLocationStore(LocationStore locationStore) {
        this.locationStore = locationStore;
    }

    public Long getIdStore() {
        return idStore;
    }

    public void setIdStore(Long idStore) {
        this.idStore = idStore;
    }

    public Long getStoreState() {
        return storeState;
    }

    public void setStoreState(Long storeState) {
        this.storeState = storeState;
    }

    public List getCountries() {
        return countries;
    }

    public void setCountries(List countries) {
        this.countries = countries;
    }
}
