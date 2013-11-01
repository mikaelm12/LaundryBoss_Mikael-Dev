package edu.mit.mitmobile2.laundry;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import edu.mit.mitmobile2.laundry.constants.ValuesObj;
import edu.mit.mitmobile2.laundry.httpgetters.GetLaundryData_MACHINE;
import edu.mit.mitmobile2.laundry.machines.LaundryMachineObject;

/**
 * This service is started to monitor all the tracked machines in the background
 * via internet usage and prefs updates
 */

public class MachineMonitorService extends IntentService {
    private SharedPreferences prefs_holder;
    private SharedPreferences.Editor prefs_editor;
    private JSONObject All_Saved_Machines;

    //if false, stops all updates and suicides this service on the next scheduled update tick
    private boolean continueUpdates = true;

    //update tick for all the machines
    public static final long MachineUpdateTimeMillis = 30 * DateUtils.SECOND_IN_MILLIS;

    @Override
    public void onCreate() {
        super.onCreate();
        // setup the prefs
        prefs_holder = this.getSharedPreferences(ValuesObj.P_FILENAME_NOTIFICATIONS, MODE_PRIVATE);
        prefs_editor = prefs_holder.edit();
        llog("service init procedure done");
    }

    public MachineMonitorService() {
        super(MachineMonitorService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        llog("doing useful things");

        // check for internet!
        boolean isOnline = ValuesObj.isDeviceOnline(this);

        if (isOnline) {
            // get all the machines from the prefs
            boolean WasJsonSucessfullyRetrieved = getSavedMachinesFromPrefs();

            // cycle through the machines
            if (WasJsonSucessfullyRetrieved)
                CycleThroughAllMachines();

        } else {
            llog("saved machines not updated. no internet ;) ");
        }

        // After doing useful things...
        scheduleNextUpdate();

    }

    /**
     * loop over all the machines and update their properties as needed
     */
    private void CycleThroughAllMachines() {
        if (All_Saved_Machines.length() == 0) {
            llog("no more machines! ending bck service.");
            continueUpdates = false;
        }

        //used to loop over each machine by its unique ID
        Iterator<?> it = All_Saved_Machines.keys();

        //used to update each machine
        GetLaundryData_MACHINE machineUpdater_http = new GetLaundryData_MACHINE();
        LaundryMachineObject OldMachine, NewMachine;

        JSONObject All_Saved_Machines_clone = new JSONObject();

        while (it.hasNext()) {
            String ID = (String) it.next();

            try {

                //get the old machine out of the body and create the updated one
                OldMachine = new LaundryMachineObject(All_Saved_Machines.getJSONObject(ID));
                NewMachine = machineUpdater_http.getData(OldMachine);//issue!
                NewMachine.setTimeOfCompletion();

                //see if everything checks out
                boolean keepThisMachine = true;
                llog("old machine completes at " + OldMachine.getCompletionDate());
                llog("new machine completes at " + NewMachine.getCompletionDate());

                //1st check: if a time discrepancy exists with the completion dates, something is borked
                long difference = NewMachine.getCompletionTimeMillis() - OldMachine.getCompletionTimeMillis();
                if (Math.abs(difference)>DateUtils.MINUTE_IN_MILLIS*2)
                    MachineCompletionTimeChanged(NewMachine);

//                //2nd check: if the machine ended too long ago, delete it from updates
//                long timeNow = System.currentTimeMillis();
//                if (timeNow - NewMachine.getCompletionTimeMillis() > DateUtils.MINUTE_IN_MILLIS * 5)
//                    keepThisMachine=false;

                //3rd check: if the machine isn't in use, delete it
                keepThisMachine = NewMachine.isInUse();

                //put the new one in the old one's place!
                if (keepThisMachine) {
//                    All_Saved_Machines.put(ID, new JSONObject(NewMachine.getThisObjectInJson()));
                    All_Saved_Machines_clone.put(ID, new JSONObject(NewMachine.getThisObjectInJson()));
                } else {
//                    All_Saved_Machines.remove(ID);
                    //can't remove an id during a loop! concurrent exception
                }

            } catch (JSONException e) {
                llog(e.getMessage());
            }

        }

        All_Saved_Machines = All_Saved_Machines_clone;

        SaveMachinesToPrefs();
    }

    private void SaveMachinesToPrefs() {
        llog("all saved machines "+All_Saved_Machines.toString());

        prefs_editor.putString(ValuesObj.P_SAVED_MACHINES_JSON_STRING, All_Saved_Machines.toString());
        prefs_editor.commit();
        llog("machines saved!");

    }

    /**
    The machine's completion date jumped by 2 minutes, something is off!
*/
    private void MachineCompletionTimeChanged(LaundryMachineObject newMachine) {

    }

    /**
     * loads the json saved machines from the prefs
     */
    private boolean getSavedMachinesFromPrefs() {
        // return true if the json came back good
        // false if not good
        String machines_json = prefs_holder.getString(ValuesObj.P_SAVED_MACHINES_JSON_STRING, "");

        llog("machines_json = "+machines_json);
        if (machines_json.length() == 0)
            return false;

        // json is good! now convert
        try {
            All_Saved_Machines = new JSONObject(machines_json);
        } catch (JSONException e) {
            llog("json error e= " + e.getMessage());
            return false;
        }

        return true;

    }

    private void scheduleNextUpdate() {
        if (continueUpdates) {
            Intent intent = new Intent(this, this.getClass());
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            long nextUpdateTimeMillis = System.currentTimeMillis()
                    + MachineUpdateTimeMillis;

            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC, nextUpdateTimeMillis, pendingIntent);

            llog("next update scheduled in " + MachineUpdateTimeMillis + " millis");
        } else {
            llog("updates not continued, stopself ran");
            stopSelf();
        }
    }

    private void llog(String msg) {
        ValuesObj.logme("<<" + this.getClass().getName() + ">> " + msg);
    }
}
