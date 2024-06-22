package kr.co.ync.project.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Util {

    public static LocalDate strToLocalDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, formatter);
    }
}
