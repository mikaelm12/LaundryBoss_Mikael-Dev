package edu.mit.mitmobile2.laundry.machines;

import android.text.format.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import edu.mit.mitmobile2.laundry.R;
import edu.mit.mitmobile2.laundry.constants.ValuesObj;

public class LaundryMachineObject {

    //label is the machine number in person. i.e. dryer #02, washer #012
    private String Label;

    //id of the machine in the api calls
    private String MachineID;

    //usually a 0 or 1, rarely used
    private String OutOfService;

    private String ApplianceType;
    private String RoomStatus;
    private String TimeRemaining;
    private String MachineStatus;
    private String RoomName;

    //res id for each machine
    private int icon;

    //values used when setting alarms and the like
    private boolean NOTIFICATION_willAlarmSound;
    private boolean NOTIFICATION_useVibration;
    private long NOTIFICATION_reminderLeadupTime;

    //aaand their json key strings
    private final static String JSONKey_Alarm = "alarm_reminder";
    private final static String JSONKey_Vibration = "vibration_reminder";
    private final static String JSONKey_LeadupTime = "leaduptime_reminder";


    //time of machine completion
    private long CompletionTimeMillis;
    private final static String JSONKey_CompletionTime = "world unix completion time";

    //pure json representation of this machine's values above
    private String ThisObjectInJson;

    /**
     * constuctor used ONLY as standin while API data is loading
     */
    public LaundryMachineObject() {
        Label = "01";
        ApplianceType = "WASHER";
        TimeRemaining = "available";
    }

    /**
     * constructor used in the '.GetLaundryData_ROOM' class, the room name isn't
     * included in the json, so it must be provided
     */
    public LaundryMachineObject(JSONObject data, String _RoomName) {
        boolean JsonGetterSucessful = true;

        try {
            Label = data.getString("label");
            MachineID = data.getString("appliance_desc_key");
            OutOfService = data.getString("out_of_service");
            ApplianceType = data.getString("appliance_type");
            RoomStatus = data.getString("lrm_status");
            TimeRemaining = data.getString("time_remaining");
            RoomName = _RoomName;
            MachineStatus = "null";

            data.put("room_name", _RoomName);

            //save this object in json form!
            ThisObjectInJson = data.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            JsonGetterSucessful = false;
        }

        if (JsonGetterSucessful) {
            prepare();
            setTimeOfCompletion();
        }


    }

    /**
     * Used during in the '.Landing' class where we're merely displaying the saved machines
     * that already have the room name included!
     */
    public LaundryMachineObject(JSONObject data) {
        boolean JsonGetterSucessful = true;

        try {
            Label = data.getString("label");
            MachineID = data.getString("appliance_desc_key");
            OutOfService = data.getString("out_of_service");
            ApplianceType = data.getString("appliance_type");
            RoomStatus = data.getString("lrm_status");
            TimeRemaining = data.getString("time_remaining");
            RoomName = data.getString("room_name");

            NOTIFICATION_reminderLeadupTime = data.getLong(JSONKey_LeadupTime);
            NOTIFICATION_useVibration = data.getBoolean(JSONKey_Vibration);
            NOTIFICATION_willAlarmSound = data.getBoolean(JSONKey_Alarm);
            CompletionTimeMillis = data.getLong(JSONKey_CompletionTime);
            MachineStatus = "null";

            //save this object in json form!
            ThisObjectInJson = data.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            JsonGetterSucessful = false;
        }

        if (JsonGetterSucessful)
            prepare();


    }

    /**
     * Used to recreate a laundry machine from the saved json-string in the prefs. Use this constructor in the update service.
     * The new_data JSONObject is retrieved via api call and clobbers values in the old json object.
     */
    public LaundryMachineObject(String old_data_string, JSONObject new_data) {
        JSONObject old_data;
        try {
            old_data = new JSONObject(old_data_string);
        } catch (JSONException e) {
            e.printStackTrace();
            llog("saved json object from prefs is null or malformed, quitting");
            return;
            //if the saved JSONObject is null or malformed, quit
        }

        boolean JsonGetterSucessful = true;
        //from the api ->>
        // {"status":"Available","out_of_service":"0","time_remaining":"cycle ended 22 minutes ago"}

        LaundryMachineObject OldMachine = new LaundryMachineObject(old_data);
        //copy the old values into this object!

        try {
            //get all the old values
            Label = old_data.getString("label");
            MachineID = old_data.getString("appliance_desc_key");
            ApplianceType = old_data.getString("appliance_type");
            RoomStatus = old_data.getString("lrm_status");
            RoomName = old_data.getString("room_name");

            NOTIFICATION_reminderLeadupTime = old_data.getLong(JSONKey_LeadupTime);
            NOTIFICATION_useVibration = old_data.getBoolean(JSONKey_Vibration);
            NOTIFICATION_willAlarmSound = old_data.getBoolean(JSONKey_Alarm);
            CompletionTimeMillis = old_data.getLong(JSONKey_CompletionTime);

            //set the updated values
            MachineStatus = new_data.getString("status");
            OutOfService = new_data.getString("out_of_service");
            TimeRemaining = new_data.getString("time_remaining");

            //create a new json object with the new values!
            old_data.put("status", MachineStatus);
            old_data.put("out_of_service", OutOfService);
            old_data.put("time_remaining", TimeRemaining);

            //save the new json!
            ThisObjectInJson = old_data.toString();

        } catch (JSONException e) {
            e.printStackTrace();
            JsonGetterSucessful = false;
        }

        if (JsonGetterSucessful) {
            //load all the old values from the old machine!
            prepare();
        }


    }

/**
    prepare this machine to be shown to a user. This means setting the proper machine icon and fixing the all caps issue from the api, e.g. "MASEEH"
*/
    public void prepare() {
        //determine the drawable for this machine
        if (ApplianceType.toLowerCase().trim().contentEquals("washer")) {
            // washers
            if (isInUse()) {
                icon = R.drawable.laundry_in_use_washer;
            } else {
                icon = R.drawable.laundry_available_washer;
            }

            if (!OutOfService.contentEquals("0"))
                icon = R.drawable.laundry_out_of_order_washer;

        } else {
            // dryers
            if (isInUse()) {
                icon = R.drawable.laundry_in_use_dryer;
            } else {
                icon = R.drawable.laundry_available_dryer;
            }

            if (!OutOfService.contentEquals("0"))
                icon = R.drawable.laundry_out_of_order_dryer;
        }

        //fix capitalization
        ApplianceType = ValuesObj.capitalizeString(ApplianceType);

    }

    public boolean isInUse() {
        return TimeRemaining.contains("remaining");
    }

    /**
     * call this function once this machine's TimeRemaining field is set. the
     * completion field completes itself.
     */

    public void setTimeOfCompletion() {
        if (!isInUse())
            return;

        long completionTime = 0;
        long timeNow = System.currentTimeMillis();

        //get how long the machine is running for

        //strip everything but the digits "15 min" -> "15"
        String numeralString = TimeRemaining.replaceAll("[^0-9]", "").trim();
        llog("numeral string = "+numeralString);

        if (numeralString.contentEquals(""))
            return;

        //get the long value of the duration in millis
        long millisTillCompletion = Long.parseLong(numeralString)* DateUtils.MINUTE_IN_MILLIS;

        //add them to get the final completion time!
        completionTime = millisTillCompletion + timeNow;

        CompletionTimeMillis = completionTime;
        addCompletionTimeToThisMachineInJson();
    }

   /** use this method to get the actual calendar date of the machine completion time. e.g. "Thu Aug 01 12:56:10 EDT 2013"*/
    public String getCompletionDate(){
        Calendar d = Calendar.getInstance();
        d.setTimeInMillis(getCompletionTimeMillis());
        return d.getTime()+"";
    }

    /**
     * In terms of object creation, it's better to set these all at once
     */
    public void setReminderValues(boolean useAlarm, boolean useVibration, long reminderLeadupTime) {
        //create the json object
        try {

            JSONObject existingMachineJSONrepresentation = new JSONObject(getThisObjectInJson());
            existingMachineJSONrepresentation.put(JSONKey_Alarm, useAlarm);
            existingMachineJSONrepresentation.put(JSONKey_Vibration, useVibration);
            existingMachineJSONrepresentation.put(JSONKey_LeadupTime, reminderLeadupTime);

            ThisObjectInJson = existingMachineJSONrepresentation.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addCompletionTimeToThisMachineInJson() {
        //add the completion time to the json repr_
        try {

            JSONObject existingMachineJSONrepresentation = new JSONObject(getThisObjectInJson());
            existingMachineJSONrepresentation.put(JSONKey_CompletionTime, CompletionTimeMillis);

            ThisObjectInJson = existingMachineJSONrepresentation.toString();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getLabel() {
        return Label;
    }

    public String getMachineStatus() {
        return MachineStatus;
    }

    public String getTimeRemaining() {
        return TimeRemaining;
    }

    public String getApplianceType() {
        return ApplianceType;
    }

    public String getRoomStatus() {
        return RoomStatus;
    }

    public String getOutOfService() {
        return OutOfService;
    }

    public String getMachineID() {
        return MachineID;
    }

    public String getRoomName() {
        return RoomName;
    }

    public String getThisObjectInJson() {
        return ThisObjectInJson;
    }

    public int getIcon() {
        return icon;
    }

    public long getCompletionTimeMillis() {
        return CompletionTimeMillis;
    }

    //machine reminder getters
    public boolean getNOTIFICATION_useVibration() {
        return NOTIFICATION_useVibration;
    }

    public long getNOTIFICATION_reminderLeadupTime() {
        return NOTIFICATION_reminderLeadupTime;
    }

    public boolean willAlarmSound() {
        return NOTIFICATION_willAlarmSound;
    }



    private void llog(String msg) {
        ValuesObj.logme("<<" + getClass().getName() + ">> " + msg);
    }
}
