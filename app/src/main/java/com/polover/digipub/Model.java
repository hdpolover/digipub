package com.polover.digipub;

import java.util.Locale;

public class Model {
    public static class UserInfo {

        public static String getDefaultMeasuringUnit(){
            String currentCountry = Locale.getDefault().getCountry();
            String[] fahrenheitCountries = {"US", "BS", "BZ", "KY", "PW", "PR", "GU", "VI"};
            for (String countryCode : fahrenheitCountries) {
                if (currentCountry.equals(countryCode)) {
                    return "1";
                }
            }
            return "0";
        }

        public static String getDefaultWarningTemperature(){
            return getDefaultMeasuringUnit().equals("0")? "34" : "94";
        }
    }
}
