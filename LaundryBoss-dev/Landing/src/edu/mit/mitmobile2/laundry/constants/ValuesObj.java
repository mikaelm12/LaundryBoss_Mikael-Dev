package edu.mit.mitmobile2.laundry.constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class ValuesObj {

    // ***************** DEBUG
    public static final String TAGBOSS = "dormboss";

    // ***************** DATA PASSING B/T ACTIVITIES VIA BUNDLE
    public static final String BUNDLER_ROOM_ID = "BUNDLER_ROOM_ID";
    public static final String BUNDLER_ROOM_STATUS = "BUNDLER_ROOM_STATUS";
    public static final String BUNDLER_ROOM_NAME = "BUNDLER_ROOM_NAME";

    // ***************** SAVING DATA IN PREFS
    public static final String P_FILENAME_NOTIFICATIONS = "notifyme";
    public static final String P_SAVED_MACHINES_JSON_STRING = "json_saved_machines";

    // alarm options
//	public static final String PSM_key_ALARM_YES_NO = "so alarming";
//	public static final String PSM_key_ALARM_VIBRATE = "so moving";
//	public static final String PSM_key_ALARM_REMINDER_TIME = "so remindful";

    // ***************** NOTIFY ME
    public static final String[] NotifyMeSpinnerOptions = {
            "none", "5 min", "10 min", "15 min"};

    // lookup table!
    public static final HashMap<String, Integer> NotifyMeSpinnerLookupTbl = new HashMap<String, Integer>() {
        {
            put("none", 0);
            put("5 min", 5);
            put("10 min", 10);
            put("15 min", 15);

        }
    };

    public static void logme(String msg) {
        msg = msg.replace("edu.mit.mitmobile2.laundry","");
        Log.d(TAGBOSS, msg);
    }

    public static void AlertPlayer(
            Context context,
            boolean showShortMessage,
            String string) {
        int duration = Toast.LENGTH_LONG;
        if (showShortMessage)
            duration = Toast.LENGTH_SHORT;

        Toast.makeText(context, string, duration).show();
    }

    public static boolean isDeviceOnline(Context global_this) {
        ConnectivityManager cm = (ConnectivityManager) global_this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }

        ValuesObj.AlertPlayer(global_this, true, "Internet Connection Required");
        return false;

    }

    /**
     * turn HELLO WORLD into Hello World
     */
    public static String capitalizeString(String ALLCAPS) {
        //if the string is one of the known ones, then load it
        //else, do the string parse code

        if (CapitalizedStringMap.containsKey(ALLCAPS))
            return CapitalizedStringMap.get(ALLCAPS.trim());

        ALLCAPS = ALLCAPS.replaceAll("\\s+", " ");

        // BURTON-CONNOR FIX
        // add a space after the '-'
        // remove space after '-' when done

        ALLCAPS = ALLCAPS.replace("-", "- ");

        // split on spaces
        String[] spaceExplode = ALLCAPS.split(" ");

        // loop over the array, adding the correctly capitalized word to a
        // Stringbuilder string
        StringBuilder sb = new StringBuilder(8);
        for (String CAP : spaceExplode) {
            String firstLetter, cap, result;
            // first letter is supposed to be uppercase!
            firstLetter = CAP.substring(0, 1).toUpperCase();

            // get the full lower of CAP
            cap = CAP.toLowerCase();

            // replace the first letter of CAP with the firstletter!
            result = firstLetter + cap.substring(1, cap.length());

            // now append to the builder and continue with the loop
            sb.append(result + " ");
        }

        String returnMe = sb.toString();

        returnMe = returnMe.replace("- ", "-");
        return returnMe;
    }

    // capitalizing strings properly
    private static HashMap<String, String> CapitalizedStringMap = new HashMap<String, String>() {
        {
            put("WASHER", "Washer");
            put("DRYER", "Dryer");

            put("EDGERTON HOUSE LEFT", "Edgerton House Left");
            put("SIDNEY PACIFIC", "Sidney Pacific");
            put("WAREHOUSE", "Warehouse");
            put("EAST CAMPUS", "East Campus");
            put("SENIOR HOUSE", "Senior House");
            put("SIMMONS HALL RM 346", "Simmons Hall Rm 346");
            put("BEXLEY  RIGHT", "Bexley Right");
            put("SIMMONS HALL RM 676", "Simmons Hall Rm 676");
            put("SIMMONS HALL RM 765", "Simmons Hall Rm 765");
            put("SIMMONS HALL RM 845", "Simmons Hall Rm 845");
            put("SIMMONS HALL RM 529", "Simmons Hall Rm 529");
            put("BEXLEY LEFT", "Bexley Left");
            put("BAKER HOUSE", "Baker House");
            put("MCCORMICK", "Mccormick");
            put("EASTGATE", "Eastgate");
            put("GREEN HALL", "Green Hall");
            put("NEW ASHDOWN", "New Ashdown");
            put("EDGERTON HOUSE RIGHT", "Edgerton House Right");
            put("405 MEMORIAL DRIVE", "405 Memorial Drive");
            put("TANG HALL", "Tang Hall");
            put("MASSEEH HALL", "Masseeh Hall");
            put("MACGREGOR", "Macgregor");
            put("MCCORMICK ANNEX", "Mccormick Annex");
            put("NEXT HOUSE", "Next House");
            put("BURTON-CONNER", "Burton-Conner");
            put("WESTGATE", "Westgate");
            put("NEW HOUSE", "New House");
        }
    };

    public static void PrintPrefsFileToLog(SharedPreferences sp) {
        logme("printing prefs to log, enjoy");
        try {
            Map<String, ?> allThings = sp.getAll();
            if (allThings != null) {
                for (Map.Entry<String, ?> entry : allThings.entrySet()) {
                    logme(entry.getKey() + ": " +
                            entry.getValue().toString());
                }
            }
        } catch (NullPointerException e) {

        }
    }

}
