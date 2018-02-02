package org.store.core.beans;

import org.store.core.beans.utils.ExportedBean;
import org.store.core.beans.utils.StoreBean;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.criterion.Restrictions;
import org.hibernate.event.EventSource;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


@Entity
@Table(name = "t_user")
public class User extends BaseBean implements StoreBean, ExportedBean {

    public static final String ADMROL_SUPERADMIN = "superadmin";
    public static final String ADMROL_ACCOUNTING = "accounting";
    public static final String ADMROL_SALES = "sales";
    public static final String ADMROL_SHIPPING = "shipping";
    public static final String ADMROL_TECHNICAL = "technical";
    public static final String ADMROL_PROVIDER = "provider";
    public static final String ADMROL_AFFILIATE = "affiliate";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUser;

    @Column(length = 512)
    private String email;

    @Column(length = 50)
    private String password;

    // Tienda en la q esta configurado el producto
    @Column(length = 10)
    private String inventaryCode;

    @Column(length = 512)
    private String passwordHint;

    private Long visits;
    private Long rewardPoints;
    private Double credit;
    private Date birthday;
    private Date registerDate;
    private Boolean admin;

    @Column(length = 2)
    private String sex;

    @Column(length = 50)
    private String userId;

    @Column(length = 250)
    private String firstname;

    @Column(length = 50)
    private String title;

    @Column(length = 250)
    private String lastname;

    private String phone;
    @Column(length = 500)
    private String website;
    @Column(length = 250)
    private String companyName;

    @Column(length = 50)
    private String lastIP;
    @Column(length = 100)
    private String lastBrowser;

    @Column(length = 50)
    private String levelRequested;
    @Column(length = 250)
    private String levelStatus;

    @Column(length = 5)
    private String language;

    private Double dealerCreditLimit;
    private boolean dealerAppOnFile;
    private boolean dealerResellCardOnFile;
    private boolean dealerCreditCardOnFile;

    @Column(length = 50)
    private String dealerResellNumber;

    private Double affiliatePercent;

    @Column(length = 50)
    private String affiliateSkin;

    private Boolean anonymousCheckout;

    private Boolean blocked;

    // Categoria alternativa, para sincronizacion con sistemas externos
    @Column(length = 250)
    private String altCategory;

    @ManyToOne
    private UserLevel level;
    @ManyToOne
    private Payterms payterms;
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    @OrderBy(value = "created")
    private Set<UserWishList> wishList;
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Set<UserAddress> addressList;
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    private Set<UserPreference> preferences;
    @ManyToMany
    private Set<UserAdminRole> roles;

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String getEmail() {
        return this.email != null ? this.email : "";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordHint() {
        return passwordHint;
    }

    public void setPasswordHint(String passwordHint) {
        this.passwordHint = passwordHint;
    }

    public Long getVisits() {
        return visits;
    }

    public void setVisits(Long visits) {
        this.visits = visits;
    }

    public Long getRewardPoints() {
        return rewardPoints != null ? rewardPoints : 0;
    }

    public void setRewardPoints(Long rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public void addRewardPoints(Long p) {
        if (p != null) setRewardPoints(getRewardPoints() + p);
    }

    public Double getCredit() {
        return credit;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

    public String getInventaryCode() {
        return inventaryCode;
    }

    public void setInventaryCode(String inventaryCode) {
        this.inventaryCode = inventaryCode;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Double getDealerCreditLimit() {
        return dealerCreditLimit;
    }

    public void setDealerCreditLimit(Double dealerCreditLimit) {
        this.dealerCreditLimit = dealerCreditLimit;
    }

    public boolean isDealerAppOnFile() {
        return dealerAppOnFile;
    }

    public void setDealerAppOnFile(boolean dealerAppOnFile) {
        this.dealerAppOnFile = dealerAppOnFile;
    }

    public boolean isDealerResellCardOnFile() {
        return dealerResellCardOnFile;
    }

    public void setDealerResellCardOnFile(boolean dealerResellCardOnFile) {
        this.dealerResellCardOnFile = dealerResellCardOnFile;
    }

    public boolean isDealerCreditCardOnFile() {
        return dealerCreditCardOnFile;
    }

    public void setDealerCreditCardOnFile(boolean dealerCreditCardOnFile) {
        this.dealerCreditCardOnFile = dealerCreditCardOnFile;
    }

    public String getDealerResellNumber() {
        return dealerResellNumber;
    }

    public void setDealerResellNumber(String dealerResellNumber) {
        this.dealerResellNumber = dealerResellNumber;
    }

    public Double getAffiliatePercent() {
        return affiliatePercent;
    }

    public void setAffiliatePercent(Double affiliatePercent) {
        this.affiliatePercent = affiliatePercent;
    }

    public String getAffiliateSkin() {
        return affiliateSkin;
    }

    public void setAffiliateSkin(String affiliateSkin) {
        this.affiliateSkin = affiliateSkin;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLastIP() {
        return lastIP;
    }

    public void setLastIP(String lastIP) {
        this.lastIP = lastIP;
    }

    public String getLastBrowser() {
        return lastBrowser;
    }

    public void setLastBrowser(String lastBrowser) {
        this.lastBrowser = lastBrowser;
    }

    public String getLevelRequested() {
        return levelRequested;
    }

    public void setLevelRequested(String levelRequested) {
        this.levelRequested = levelRequested;
    }

    public String getLevelStatus() {
        return levelStatus;
    }

    public void setLevelStatus(String levelStatus) {
        this.levelStatus = levelStatus;
    }

    public String getLanguage() {
        return StringUtils.isNotEmpty(language) ? language : "en";
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean getAnonymousCheckout() {
        return anonymousCheckout != null && anonymousCheckout;
    }

    public void setAnonymousCheckout(Boolean anonymousCheckout) {
        this.anonymousCheckout = anonymousCheckout;
    }

    public Boolean getBlocked() {
        return blocked != null && blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

    public String getAltCategory() {
        return altCategory;
    }

    public void setAltCategory(String altCategory) {
        this.altCategory = altCategory;
    }

    public UserLevel getLevel() {
        return level;
    }

    public void setLevel(UserLevel level) {
        this.level = level;
    }

    public Payterms getPayterms() {
        return payterms;
    }

    public void setPayterms(Payterms payterms) {
        this.payterms = payterms;
    }

    public Set<UserWishList> getWishList() {
        return wishList;
    }

    public void setWishList(Set<UserWishList> wishList) {
        this.wishList = wishList;
    }

    public Set<UserAddress> getAddressList() {
        return addressList;
    }

    public void setAddressList(Set<UserAddress> addressList) {
        this.addressList = addressList;
    }

    public Set<UserPreference> getPreferences() {
        return preferences;
    }

    public void setPreferences(Set<UserPreference> preferences) {
        this.preferences = preferences;
    }

    public Set<UserAdminRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<UserAdminRole> roles) {
        this.roles = roles;
    }

    public String getPreference(String code) {
        if (preferences != null && StringUtils.isNotEmpty(code)) {
            for (UserPreference p : preferences) {
                if (code.equalsIgnoreCase(p.getPreferenceCode())) return p.getPreferenceValue();
            }
        }
        return null;
    }

    public boolean hasPreference(String code, String value) {
        if (preferences != null && StringUtils.isNotEmpty(code)) {
            for (UserPreference p : preferences) {
                if (code.equalsIgnoreCase(p.getPreferenceCode()) && value.equalsIgnoreCase(p.getPreferenceValue())) return true;
            }
        }
        return false;
    }

    public void addPreference(String code, String value) {
        UserPreference p = new UserPreference();
        p.setPreferenceCode(code);
        p.setPreferenceValue(value);
        p.setUser(this);
        if (getAction() != null) getAction().getDao().save(p);
        if (preferences == null) setPreferences(new HashSet<UserPreference>());
        preferences.add(p);
    }

    public void delPreference(String code, String value) {
        if (preferences != null && StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(value)) {
            UserPreference toDelete = null;
            for (UserPreference p : preferences) {
                if (code.equalsIgnoreCase(p.getPreferenceCode()) && value.equalsIgnoreCase(p.getPreferenceValue()))
                    toDelete = p;
            }
            if (toDelete != null) {
                preferences.remove(toDelete);
                if (getAction() != null) getAction().getDao().delete(toDelete);
            }
        }
    }

    public boolean requestLevelPending() {
        String actualLevel = (level != null) ? level.getCode() : "";
        return StringUtils.isNotEmpty(levelRequested) &&
                !levelRequested.equalsIgnoreCase(UserLevel.DEFAULT_LEVEL) &&
                !levelRequested.equalsIgnoreCase(actualLevel);
    }

    public String getFullName() {
        StringBuilder buffer = new StringBuilder();
        if (StringUtils.isNotEmpty(title)) buffer.append(title);
        if (StringUtils.isNotEmpty(firstname)) {
            if (StringUtils.isNotEmpty(buffer.toString())) buffer.append(" ");
            buffer.append(firstname);
        }
        if (StringUtils.isNotBlank(lastname)) {
            if (StringUtils.isNotEmpty(buffer.toString())) buffer.append(" ");
            buffer.append(lastname);
        }
        return buffer.toString();
    }

    public boolean hasRole(Long roleId) {
        if (roleId != null && CollectionUtils.isNotEmpty(roles)) {
            for (UserAdminRole r : roles)
                if (roleId.equals(r.getId())) return true;
        }
        return false;
    }

    public boolean hasRoleCode(String roleCode) {
        if (StringUtils.isNotEmpty(roleCode) && CollectionUtils.isNotEmpty(roles)) {
            for (UserAdminRole r : roles)
                if (roleCode.equalsIgnoreCase(r.getRoleCode())) return true;
        }
        return false;
    }

    public boolean isAffiliate() {
        return level != null && UserLevel.AFFILIATE_LEVEL.equalsIgnoreCase(level.getCode());
    }

    public long addVisit() {
        return visits = (visits == null) ? 1 : visits + 1;
    }

    public UserAddress getBillingAddress() {
        if (addressList != null && addressList.size() > 0) {
            for (UserAddress add : addressList)
                if (add.getBilling()) return add;
        }
        return null;
    }

    public UserAddress getShippingAddress() {
        if (addressList != null && addressList.size() > 0) {
            for (UserAddress add : addressList)
                if (add.getShipping()) return add;
        }
        return null;
    }

    public List<UserPreference> getPreferencesByCode(String code) {
        if (StringUtils.isNotEmpty(code) && preferences != null) {
            List<UserPreference> res = new ArrayList<UserPreference>();
            for (UserPreference pref : preferences)
                if (code.equalsIgnoreCase(pref.getPreferenceCode()) && StringUtils.isNotEmpty(pref.getPreferenceValue()))
                    res.add(pref);
            return res;
        }
        return null;
    }

    public UserPreference getRegisteredSubscription() {
        List<UserPreference> l = getPreferencesByCode("register.subscriptions");
        return l.size()>0 ? l.get(0) : null;
    }

    public boolean isSubscribed() {
        UserPreference up = getRegisteredSubscription();
        return up!=null && "yes".equalsIgnoreCase(up.getPreferenceValue());
    }

    public List<UserPreference> getStockAlerts() {
        return getPreferencesByCode(UserPreference.STOCK_ALERT);
    }

    public List<UserPreference> getFeeExemption() {
        return getPreferencesByCode(UserPreference.FEE_EXEMPTION);
    }

    public List<UserPreference> getTaxExemption() {
        return getPreferencesByCode(UserPreference.TAX_EXEMPTION);
    }

    public boolean hasFeeExemption(Long idFee) {
        List<UserPreference> l = getFeeExemption();
        if (idFee != null && l != null && l.size() > 0) {
            for (UserPreference p : l) {
                if (idFee.toString().equalsIgnoreCase(p.getPreferenceValue())) return true;
            }
        }
        return false;
    }

    public boolean hasTaxExemption(Long idTax) {
        List<UserPreference> l = getTaxExemption();
        if (idTax != null && l != null && l.size() > 0) {
            for (UserPreference p : l) {
                if (idTax.toString().equalsIgnoreCase(p.getPreferenceValue())) return true;
            }
        }
        return false;
    }

    public String getAccessCode() {
        if (StringUtils.isNotEmpty(userId) && StringUtils.isNotEmpty(password)) {
            String cad = userId + ":" + password;
            return new String(Hex.encodeHex(cad.getBytes()));
        }
        return null;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("id", getIdUser())
                .toString();
    }

    public boolean equals(Object other) {
        if (other == null) return false;
        if ((this == other)) return true;
        if (!(other instanceof User)) return false;
        User castOther = (User) other;
        return new EqualsBuilder()
                .append(this.getIdUser(), castOther.getIdUser())
                .isEquals();
    }

    public UserAddress findAddress(String street1, String cityName, String postalCode) {
        if (addressList != null && !addressList.isEmpty()) {
            for (UserAddress add : addressList) {
                if (new EqualsBuilder()
                        .append(street1, add.getAddress())
                        .append(cityName, add.getCity())
                        .append(postalCode, add.getZipCode())
                        .isEquals()) return add;
            }
        }
        return null;
    }

    public Map<String, Object> toMap(String lang) {
        Map res = new HashMap();
        try {
            Map m = describe();
            res.putAll(m);

            if (level != null) {
                Map ml = level.toMap(lang);
                for (Object key : ml.keySet()) res.put("level." + key, ml.get(key));
            }

        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e); 
        } catch (InvocationTargetException e) {
            log.error(e.getMessage(), e); 
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e); 
        }

        return res;
    }

    public void fromMap(Map<String, String> m) {
        try {
            BeanUtils.populate(this, m);
        } catch (IllegalAccessException e) {
            log.error(e.getMessage(), e); 
        } catch (InvocationTargetException e) {
            log.error(e.getMessage(), e); 
        }
    }

    public Map<String, Object> equalMap(Map<String, String> map, String lang) {
        Map<String, Object> res = new HashMap<String, Object>();
        Map userMap = toMap(lang);
        for (String key : map.keySet()) {
            if (StringUtils.isEmpty(map.get(key)) && (!userMap.containsKey(key) || userMap.get(key) == null || StringUtils.isEmpty(userMap.get(key).toString()))) {
            } else if (!new EqualsBuilder().append(normalizeZero(map.get(key)), normalizeZero(userMap.get(key))).isEquals()) {
                res.put(key, new Object[]{userMap.get(key), map.get(key)});
            }
        }
        return res;
    }

    private String normalizeZero(Object cad) {
        return (cad == null || "0".equals(cad) || "0.0".equals(cad) || "0.00".equals(cad)) ? "" : cad.toString().trim();
    }

    public static String generatePassword(int length) {
        String validChars = "abcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder s = new StringBuilder();
        Random generator = new Random();
        for (int i = 0; i < length; i++) s.append(validChars.charAt(generator.nextInt(validChars.length())));
        return s.toString();
    }

    public UserAddress getAddressById(Long idAddress) {
        if (idAddress!=null && addressList!=null && !addressList.isEmpty()) {
            for(UserAddress a : addressList)
                if (idAddress.equals(a.getIdAddress()))
                    return a;
        }
        return  null;
    }

    @Override
    public boolean handlePreDelete(EventSource session) {
        List l = session.createCriteria(ShopCart.class).add(Restrictions.eq("user", this)).list();
        for(Object o : l) session.delete(o);
        return super.handlePreDelete(session);
    }
}