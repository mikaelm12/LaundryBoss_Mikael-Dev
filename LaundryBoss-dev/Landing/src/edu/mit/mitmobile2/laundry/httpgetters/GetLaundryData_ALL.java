package edu.mit.mitmobile2.laundry.httpgetters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import edu.mit.mitmobile2.laundry.constants.ValuesObj;
import edu.mit.mitmobile2.laundry.machines.LaundryRoomObject;

public class GetLaundryData_ALL {
//TODO: change code procedure to use the clay json parser

    private final static String ALL_DORM_DATA_URL = "http://mobile-dev.mit.edu/api/?module=laundry&call=getRoomData";

    public LaundryRoomObject[] getData() {
        // return the states of ALL the laundry machines in an array of laundry
        // machine objects

        // get the json
        String hopefully_json = GetHttpData();

        if (hopefully_json ==  null) {
            return null;
        }

        // parse it!
        JSONObject alldorm_json = null, oneroom_json = null;
        try {
            alldorm_json = new JSONObject(hopefully_json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Iterator it = alldorm_json.keys();

        int len = alldorm_json.length();

        LaundryRoomObject[] results = new LaundryRoomObject[len];// return
        // variable
        int count = 0;// incrementer

        while (it.hasNext()) {
            String roomID = (String) it.next();
            // retrieve the room info from the json
            try {
                oneroom_json = alldorm_json.getJSONObject(roomID);

            } catch (JSONException e) {
                e.printStackTrace();
            }

//            // get values!
//            String room_name = null, status = null, available_washers = null, available_dryers = null, total_washers = null, total_dryers = null;
//
//            try {
//                room_name = oneroom_json.getString("laundry_room_name");
//                status = oneroom_json.getString("status");
//                available_washers = oneroom_json.getString("available_washers");
//                available_dryers = oneroom_json.getString("available_dryers");
////				total_washers = oneroom_json.getString("total_washers");
////				total_dryers = oneroom_json.getString("total_dryers");
//
//            } catch (JSONException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }


//              "location":"1364831", "laundry_room_name":"405 MEMORIAL DRIVE",
//			  "status":"online", "available_washers":"2",
//			  "available_dryers":"2", "total_washers":"2", "total_dryers":"2"


            LaundryRoomObject lro = new LaundryRoomObject(oneroom_json);
//            lro.id = roomID;
//            lro.status = status; // Status means online/offline!
//            lro.name = room_name;
//            lro.av_dryers = available_dryers;
//            lro.av_washers = available_washers;

//            lro.prepare();
//			lro.fixAllCaps();

            results[count] = lro;
            count++;// I'm so stupid

        }

        return SortResultsPostParse(results);
    }

    private String GetHttpData() {
        // get the json from the MIT LV API
        RetrieveSiteData task = new RetrieveSiteData();

        task.execute(ALL_DORM_DATA_URL);
        String json_string = "null";

        try {
            json_string = task.get(RetrieveSiteData.RETRIEVAL_TIMEOUT_TIME_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        if (json_string.length() < 5) {
            llog("ALL DORM json string is less than 5 in len. ");
            return null;

        }

        return json_string;
    }

    // // //// //// ROOM SORTING //// //// //// ////

    /**
     * takes the LRO array and sorts the rooms alphabetically
     */
    private LaundryRoomObject[] SortResultsPostParse(LaundryRoomObject[] results) {

        TreeSet ts = new TreeSet(new MyComp());

        for (LaundryRoomObject lro : results) {
            ts.add(lro);
        }

        LaundryRoomObject[] new_results = new LaundryRoomObject[results.length];

        Iterator it = ts.iterator();
        int count = 0;
        while (it.hasNext()) {
            new_results[count] = (LaundryRoomObject) it.next();
            count++;
        }

        return new_results;
    }

    class MyComp implements Comparator {
        public int compare(Object a, Object b) {
            // two json objects

            LaundryRoomObject lro_a, lro_b;
            lro_a = (LaundryRoomObject) a;
            lro_b = (LaundryRoomObject) b;

            return (lro_a.getName().compareTo(lro_b.getName()));
        }
    }

    private void llog(String msg) {
        ValuesObj.logme("<<" + this.getClass().getName() + ">> " + msg);
    }

}
