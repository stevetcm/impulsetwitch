package com.orangemuffin.impulse.utils;

/* Created by OrangeMuffin on 2019-04-07 */
public class ConvertStringUtil {
    public static String convertHtmlEntities(String entity) {
        if (entity.equals("1")) {
            return ":)";
        } else if (entity.equals("2")) {
            return ":(";
        } else if (entity.equals("3")) {
            return ":D";
        } else if (entity.equals("4")) {
            return ">(";
        } else if (entity.equals("5")) {
            return ":|";
        } else if (entity.equals("6")) {
            return "O_o";
        } else if (entity.equals("7")) {
            return "B)";
        } else if (entity.equals("8")) {
            return ":O";
        } else if (entity.equals("9")) {
            return "<3";
        } else if (entity.equals("10")) {
            return ":/";
        } else if (entity.equals("11")) {
            return ";)";
        } else if (entity.equals("12")) {
            return ":P";
        } else if (entity.equals("13")) {
            return ";P";
        } else if (entity.equals("14")) {
            return "R)";
        }
        return null;
    }
}
