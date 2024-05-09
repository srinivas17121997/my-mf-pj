package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PortfolioItem {

    public String schemeName;
    public String code;
    public BigDecimal nav;
    public  BigDecimal yesterdayNAV;
    public BigDecimal percentChange;
    public BigDecimal units;
    public BigDecimal DayProfitOrLoss;

    public BigDecimal currentValue;
    private BigDecimal dividePercent= new BigDecimal(100);

    public PortfolioItem(String schemeName, String code, BigDecimal nav, BigDecimal yesterdayNAV,BigDecimal units) {
        this.schemeName = schemeName;
        this.code = code;
        this.nav = nav;
        this.yesterdayNAV = yesterdayNAV;
        this.units=units;
        this.DayProfitOrLoss=nav.multiply(units).subtract(yesterdayNAV.multiply(units));
        if(yesterdayNAV.intValue()!=0){
            this.percentChange=nav.subtract(yesterdayNAV).divide(yesterdayNAV, 3,RoundingMode.FLOOR).multiply(dividePercent);

        }
        this.currentValue=nav.multiply(units);
        //System.out.println(toString());
    }

    @Override
    public String toString() {
        return "PortfolioItem{" +
                "schemeName='" + schemeName + '\'' +
                ", nav=" + nav.setScale(2,RoundingMode.FLOOR) +
                ", percentage=" + percentChange +
                ", DayProfitOrLoss=" + DayProfitOrLoss +
                ", currentValue=" + currentValue +
                '}';
    }

    public String getSchemeName() {
        return schemeName;
    }

    public BigDecimal getUnits() {
        return units;
    }

    public BigDecimal getCurrentValue() {
        return currentValue;
    }
}
