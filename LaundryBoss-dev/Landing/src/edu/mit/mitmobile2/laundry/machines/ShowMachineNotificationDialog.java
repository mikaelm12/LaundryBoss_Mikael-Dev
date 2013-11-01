package edu.mit.mitmobile2.laundry.machines;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import edu.mit.mitmobile2.laundry.R;
import edu.mit.mitmobile2.laundry.constants.ValuesObj;

/**
 * Setup the notifications options popup window in it's own class
 */
public class ShowMachineNotificationDialog {
    private Context context;
    private SharedPreferences prefs_holder;
    private SharedPreferences.Editor prefs_editor;
    private LaundryMachineObject machine;
    private LayoutInflater inflater;

    private CheckBox cbNotifyWithVibrate, cbNotifyWithAlarm;
    private Spinner spReminderOptions;

    // dialog options
    private boolean bnotifywithvibrate, bnotifywithalarm;

    // more options
    private long ReminderTime;

    // when clicking the actual "set" button
    public static SetNotificationListener mListener;

    public ShowMachineNotificationDialog(Context c, LayoutInflater inf, LaundryMachineObject lmo) {
        context = c;

        inflater = inf;
        machine = lmo;
        // setup the prefs
        prefs_holder = context.getSharedPreferences(ValuesObj.P_FILENAME_NOTIFICATIONS, Context.MODE_PRIVATE);
        prefs_editor = prefs_holder.edit();
        createAlertBuilder();

    }

    public void done() {
        // get rid of the pointers to prevent memory leaks
//        context = null;
//        inflater = null;
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"})
    private void createAlertBuilder() {
        AlertDialog.Builder bld = new AlertDialog.Builder(context);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.laundry_set_machine_notification, null);
        bld.setView(v);

        //create all the view objects for the notification popup window
        cbNotifyWithVibrate = (CheckBox) v.findViewById(R.id.cbNotifyVibrate);
        cbNotifyWithAlarm = (CheckBox) v.findViewById(R.id.cbNotifyAlarm);
        bnotifywithalarm = true;
        cbNotifyWithAlarm.setChecked(bnotifywithalarm);

        //create the spinner used for the reminder spinner box
        spReminderOptions = (Spinner) v.findViewById(R.id.spNotifyWhen);

        ArrayAdapter ab = new ArrayAdapter(context, android.R.layout.simple_spinner_item, ValuesObj.NotifyMeSpinnerOptions);

        ab.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spReminderOptions.setAdapter(ab);

        //listen for when the spinner is selected with a new time
        spReminderOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(
                    AdapterView<?> arg0,
                    View arg1,
                    int pos,
                    long arg3) {
                String option = ValuesObj.NotifyMeSpinnerOptions[pos];
                ReminderTime = ValuesObj.NotifyMeSpinnerLookupTbl.get(option);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        // button listeners

        cbNotifyWithVibrate.setOnClickListener(new CheckBox.OnClickListener() {

            @Override
            public void onClick(View v) {
                bnotifywithvibrate = !cbNotifyWithVibrate.isChecked();
            }
        });

        cbNotifyWithAlarm.setOnClickListener(new CheckBox.OnClickListener() {

            @Override
            public void onClick(View v) {
                bnotifywithalarm = !cbNotifyWithAlarm.isChecked();
            }
        });

        // create the bottom buttons for the dialog
        bld.setPositiveButton("Cancel", null);

        bld.setNeutralButton("Set Notification", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // save the notification thing!
                machine.setReminderValues(bnotifywithalarm, bnotifywithvibrate, ReminderTime);

                if (machine.isInUse())
                    SaveNotification();
                mListener.onUserSavedMachine();
            }

        });

        bld.setTitle("notify me when finished...");
        bld.create().show();

    }

    private void SaveNotification() {

        // simply save the machine and these options
        // into the json to be handledlater

        // get the existing json from prefs
        machine.setTimeOfCompletion();
        String existing_saved_machines_json = prefs_holder.getString(ValuesObj.P_SAVED_MACHINES_JSON_STRING, "");
        JSONObject all_saved_machines = null, new_machine;

        try {
            if (!existing_saved_machines_json.equals("")) {
                all_saved_machines = new JSONObject(existing_saved_machines_json);
            } else {
                all_saved_machines = new JSONObject();
            }

            new_machine = new JSONObject(machine.getThisObjectInJson());
            llog("obj in json looks like "+machine.getThisObjectInJson());
            llog("new_machine looks like "+new_machine);

            //save the machine by it's unique id
            all_saved_machines.put(machine.getMachineID(), new_machine);

            llog("all saved machines looks like "+all_saved_machines.toString());

        } catch (JSONException e) {
            e.printStackTrace();
            llog("e has error " + e.getMessage());

        }

        if (all_saved_machines != null) {
//            save this json into the prefs
            prefs_editor.putString(ValuesObj.P_SAVED_MACHINES_JSON_STRING, all_saved_machines.toString());
            prefs_editor.commit();
            llog("machines saved!");
        }


        // startService();

    }

    public interface SetNotificationListener {

        public void onUserSavedMachine();

    }

    public void SetNotificationListener(SetNotificationListener eventListener) {
        mListener = eventListener;
    }

    private void llog(String msg) {
        ValuesObj.logme("<<" + this.getClass().getName() + ">> " + msg);
    }

}