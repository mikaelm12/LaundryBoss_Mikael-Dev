package edu.mit.mitmobile2.laundry;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import edu.mit.mitmobile2.laundry.constants.ValuesObj;
import edu.mit.mitmobile2.laundry.machines.LaundryMachineObject;

public class Landing extends Activity implements OnClickListener {
    private Button goToAllDormView;
    private TextView standin;
    private SharedPreferences prefs_holder;
    private SharedPreferences.Editor prefs_editor;
    private Handler handler;
    private String timeRemainTemp;
    private Double timeRemain;
    private boolean timeAvailable = false;  //Checking if any machines are being watched
    private String TimeTest;
    private String[] Times;     //Both washer and dryer times
    private String finalTimeWasher ="";
    private String finalTimeDryer = "";
    private String finalTime;               //String with the washers times
    private String finalDryTime;            //Dryer Times

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.laundry_activity_main);
        llog("   ");
        llog("   ");
        llog("app started");

        LoadPrefs();
        standin = (TextView) findViewById(R.id.tvStandInForCardview);

        goToAllDormView = (Button) findViewById(R.id.bGotoAllDormView);
        goToAllDormView.setOnClickListener(this);

        handler = new Handler();
        handler.post(updateMachineCards);
        startUpdatingService();
        GridView gridview = (GridView) findViewById(R.id.gridView);
        gridview.setAdapter(new ImageAdapter(this));
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                if(timeAvailable == false){
                    if(position== 0){
                    String tempFinalTimeWasher = "No washer cycles running";
                    Toast.makeText(Landing.this, tempFinalTimeWasher, Toast.LENGTH_SHORT).show();
                    }
                    if(position== 1){
                        String tempFinalTimeDryer = "No dryer cycles running";
                    Toast.makeText(Landing.this, tempFinalTimeDryer, Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    machineSplitter();

                    if(position == 0){
                        for(String placement: Times){

                            if(placement.contains("Washer")){
                                finalTimeWasher = finalTimeWasher + placement + "\n";

                            }

                        }
                        Toast.makeText(Landing.this, finalTimeWasher, Toast.LENGTH_SHORT).show();
                        finalTimeWasher = "";
                    }
                    if(position == 1){
                        for(String placement: Times){
                            if(placement.contains("Dryer")){
                                finalTimeDryer = finalTimeDryer + placement + "\n";




                            }

                        }
                        Toast.makeText(Landing.this, finalTimeDryer, Toast.LENGTH_SHORT).show();
                        finalTimeDryer = "";
                    }
                    }

            }
        });









    }

   /* public String secondSplitter(String timeList) {
        String updatedTime;
        String[] temp;
        temp = timeList.split("\n");
        updatedTime = temp[0];
        return timeList;
        }
*/


    public void machineSplitter() {

        Times = timeRemainTemp.split("\n");
        //TimeTest = Times[0];


    }

    public void startUpdatingService() {
        llog("update service started from main menu");
        Intent SuperDuperService = new Intent(this, MachineMonitorService.class);
        startService(SuperDuperService);
    }

    private void LoadPrefs(){
        prefs_holder = getSharedPreferences(ValuesObj.P_FILENAME_NOTIFICATIONS, MODE_PRIVATE);
        prefs_editor = prefs_holder.edit();
    }

    @Override
    protected void onResume() {
        // update the list of cards
        super.onResume();

        llog("landing resumed");

        llog("printing prefs");
        ValuesObj.PrintPrefsFileToLog(prefs_holder);

        LoadPrefs();
        //remove the last updates so the app isn't updating too much
        handler.removeCallbacks(updateMachineCards);
        handler.post(updateMachineCards);

    }

    private Runnable updateMachineCards = new Runnable() {
        // refresh all the machines!
        public void run() {
            setupMachineDataView();

            handler.postDelayed(updateMachineCards, DateUtils.SECOND_IN_MILLIS*30);
        }

    };

    @Override
    protected void onPause() {
        // DESCRIBE ME!!
        super.onPause();
        handler.removeCallbacks(updateMachineCards);
        prefs_editor.commit();

    }

    private void setupMachineDataView() {

        // load the watched machines!
        String saved_machine_json = prefs_holder.getString(ValuesObj.P_SAVED_MACHINES_JSON_STRING, "");
        llog("list of all machines = " + saved_machine_json);

        if (saved_machine_json.length() > 5) {
            // load the machines into the front screen
            displayMachineData(saved_machine_json);
        } else {
            llog("no saved machines to display!");
        }
    }

    private void displayMachineData(String saved_machine_json) {
        // The machines are stored in a holder JSON object and are keyed by their unique machine ID
        JSONObject listOfAllMachines = null, singleMachineJson;

        //String builder is used since a quite large string is generated via appending.
        StringBuilder text = new StringBuilder(512);


        try {
            //recreate the larger json object via it's saved string format
            listOfAllMachines = new JSONObject(saved_machine_json);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // load data into the textview
        Iterator<String> it = listOfAllMachines.keys(); // iterate over json to load the textview!

        llog("  ");
        //begin loop
        while (it.hasNext()) {
            try {
                singleMachineJson = listOfAllMachines.getJSONObject(it.next());
                llog("single machine = " + singleMachineJson.toString());

                LaundryMachineObject LaundryMachine = new LaundryMachineObject(singleMachineJson);
                //to access the stored machines, do any work here

                //TODO: ERIC OR MIKE, this is an example of how the access the values of each machine. Build the new "cards" using a block like this one!


                //chained append calls are faster, or so google says
                //text.append(LaundryMachine.getMachineID())
                        //.append(" ")
                        text.append(LaundryMachine.getTimeRemaining())
                        .append(" ")
                        //.append(LaundryMachine.getLabel())
                        .append(" ")
                        .append(LaundryMachine.getRoomName())
                        .append(" ")
                        .append(LaundryMachine.getApplianceType())
                        .append("\n");
                //standin.setText(text);
                timeRemainTemp = text.toString();
                //timeRemain = Double.parseDouble(timeRemainTemp);
                timeAvailable = true;
                


            } catch (JSONException e) {
                llog("e has error "+e.getMessage());
            }

        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bGotoAllDormView:
                // clicked back, now go home
                Intent intent = new Intent(this, AllRooms.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }

    }

    private void llog(String msg) {
        ValuesObj.logme("<<" + this.getClass().getName() + ">> " + msg);
    }

}

