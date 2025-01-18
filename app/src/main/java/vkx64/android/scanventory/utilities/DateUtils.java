package vkx64.android.scanventory.utilities;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    // Date Format
    private static final String DATE_FORMAT = "MM/dd/yyyy-HH:mm";

    // Create a shared method to get the formatted date string
    public static String getCurrentDateTime() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        return dateFormat.format(new Date());
    }

}
