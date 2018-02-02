package org.store.core.globals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class CountryFactory {

    public static List<Country> getCountries(Locale locale, boolean sort){
        String[] codes = Locale.getISOCountries();
        List<Country> countries = new ArrayList<Country>();
        for (String cCode : codes) {
            String cName = getCountryName(cCode, locale);
            if (cCode.equals(cName)) continue;
            countries.add(new Country(cCode, cName));
        }
        if (!sort) return countries;
        Collections.sort(countries, new CountryComparator());
        return countries;
    }

    public static List<Country> getCountries(Locale locale){
        return getCountries(locale,true);
    }

    public static String getCountryName(String code, Locale locale) {
        Locale tmpLocale = new Locale(locale.getLanguage(), code);
        return tmpLocale.getDisplayCountry(locale);
    }

    public static class Country {
        public Country(String code, String name) {
            this.code = code;
            this.name = name;
        }

        private String code;
        private String name;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class CountryComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            Country c1 = (Country) o1;
            Country c2 = (Country) o2;
            return c1.getName().compareTo(c2.getName());
        }
    }

}
