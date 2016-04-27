package org.store.core.beans.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriceFilter {

    private double minPrice;
    private double maxPrice;
    private int count;

    public PriceFilter( int count, double minPrice, double maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.count = count;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public double getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Map<String, Double>> getAlg1(int c) {
        setCount(c);
        return getAlg1();
    }

    public List<Map<String, Double>> getAlg2(int c) {
        setCount(c);
        return getAlg2();
    }

    public List<Map<String, Double>> getAlg2() {
        List<Map<String, Double>> res = new ArrayList<Map<String, Double>>();
        if (maxPrice>minPrice) {
            double range = (maxPrice-minPrice) / count;
            long newRange = Math.max(1,(new Double(range/10).intValue())) * 10;
            double mm = 0;
            while(mm<maxPrice) {
                if (mm+newRange>minPrice) {
                    Map<String, Double> map = new HashMap<String, Double>();
                    map.put("min", mm);
                    map.put("max", mm+newRange);
                    res.add(map);
                }
                mm += newRange;
            }
        }
        return res;
    }

    public List<Map<String, Double>> getAlg1() {
        List<Map<String, Double>> res = new ArrayList<Map<String, Double>>();
        if (maxPrice>minPrice) {
            long range = new Double((maxPrice-minPrice) / count).longValue();
            String cad = String.valueOf(range);
            double newRange = Math.max(1,(Long.parseLong(cad.substring(0, 1))+1) * Math.pow(10,cad.length()-1));
            double mm = 0;
            while(mm<maxPrice) {
                if (mm+newRange>minPrice) {
                    Map<String, Double> map = new HashMap<String, Double>();
                    map.put("min", mm);
                    map.put("max", mm+newRange);
                    res.add(map);
                }
                mm += newRange;
            }
        }
        return res;
    }

}
