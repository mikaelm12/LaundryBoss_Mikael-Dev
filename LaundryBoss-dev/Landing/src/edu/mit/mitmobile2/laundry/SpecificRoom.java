package edu.mit.mitmobile2.laundry;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import edu.mit.mitmobile2.laundry.constants.ValuesObj;
import edu.mit.mitmobile2.laundry.httpgetters.GetLaundryData_ROOM;
import edu.mit.mitmobile2.laundry.machines.LaundryMachineObject;
import edu.mit.mitmobile2.laundry.machines.ShowMachineNotificationDialog;
import edu.mit.mitmobile2.laundry.machines.ShowMachineNotificationDialog.SetNotificationListener;

public class SpecificRoom extends ListActivity {

	LaundryMachineObject[] MachineStatuses;
	private SharedPreferences prefs_holder;
	private SharedPreferences.Editor prefs_editor;
	private String[] layoutkeys = new String[] {
	"machine_name", "machine_status", "machine_icon", "machine_number" };

	private int[] layoutvalues = new int[] {
	R.id.tvMachineName, R.id.tvMachineStatus, R.id.ivMachineIcon, R.id.tvMachineNumber };

	private Handler handler;
	private String whichroomID, roomName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		handler = new Handler();

		if (ValuesObj.isDeviceOnline(this) == true) {
            //set up list view UI
            getListView().setBackgroundResource(R.drawable.laundry_main_repeat);
            getListView().setCacheColorHint(Color.TRANSPARENT);
            getListView().setSelector(android.R.color.transparent);

			// recieve bundle info from the previous activity
			Bundle gotBasket = getIntent().getExtras();
			whichroomID = gotBasket.getString(ValuesObj.BUNDLER_ROOM_ID);
			roomName = gotBasket.getString(ValuesObj.BUNDLER_ROOM_NAME);

			MachineStatuses = new LaundryMachineObject[3];
			for (int i = 0; i < 3; i++) {
				MachineStatuses[i] = new LaundryMachineObject();

			}

			ArrayList<HashMap<String, ?>> data = SetupListViewData(MachineStatuses);

			SimpleAdapter adapter = new SimpleAdapter(SpecificRoom.this, data, R.layout.laundry_specific_room_view_adapter, layoutkeys, layoutvalues);
			setListAdapter(adapter);

//			ValuesObj.logme("starting handler loop");

            //setup the handler to allow for updates during user-use and not auto-scroll back to the top
			handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {

					super.handleMessage(msg);
					ArrayList<HashMap<String, ?>> data = (ArrayList<HashMap<String, ?>>) msg.obj;

					SimpleAdapter adapter = new SimpleAdapter(SpecificRoom.this, data, R.layout.laundry_specific_room_view_adapter, layoutkeys, layoutvalues);

					setListAdapter(adapter);

					getListView().setDivider(null);
					getListView().setSelectionFromTop(msg.arg1, msg.arg2);
				}
			};

			handler.postDelayed(updateMachines, 5);

		}

		// setup the prefs
		prefs_holder = getSharedPreferences(ValuesObj.P_FILENAME_NOTIFICATIONS, MODE_PRIVATE);
		prefs_editor = prefs_holder.edit();
	}

	private Runnable updateMachines = new Runnable() {
		// refresh all the machines!
		public void run() {
            // load in background

			GetLaundryData_ROOM room_data = new GetLaundryData_ROOM(whichroomID, roomName);

			MachineStatuses = room_data.getData();
            if (MachineStatuses != null){

                ArrayList<HashMap<String, ?>> data = SetupListViewData(MachineStatuses);
                Message msg = handler.obtainMessage();
                msg.obj = data;

                int savedPosition = getListView().getFirstVisiblePosition();
                View firstVisibleView = getListView().getChildAt(0);
                int savedListTop = (firstVisibleView == null) ? 0 : firstVisibleView.getTop();

                msg.arg1 = savedPosition;
                msg.arg2 = savedListTop;
                handler.sendMessage(msg);

                handler.postDelayed(updateMachines, 60000);
            } else {
                //we got a null case!
                safeTellUser("http request error!");
                llog("all room experienced error with the alldormstatuses. http error? quitting");
                finish();
                return;
            }
        }
	};

    private void safeTellUser(String msg){
        ValuesObj.AlertPlayer(this,true,msg);
    }

	@Override
	protected void onPause() {
		// DESCRIBE ME!!
		super.onPause();
		handler.removeCallbacks(updateMachines);
		prefs_editor.commit();

		finish();
	}

	/*
	 * http://goo.gl/NFVAS
	 */

	// ///////////////// Activity stuff below

	private ArrayList<HashMap<String, ?>> SetupListViewData(
			LaundryMachineObject[] machines) {
		// make the listview containing all the dorms!
		ArrayList<HashMap<String, ?>> data = new ArrayList<HashMap<String, ?>>();

		// now let's fill with data!
		int len = machines.length;

		HashMap<String, Object> row = new HashMap<String, Object>();
		// to be re-used in the loop below

		for (LaundryMachineObject lmo : machines) {
			// get the obj containing all the dorms!
			if (lmo != null) {
				row = new HashMap<String, Object>();
				row.put("machine_status", lmo.getTimeRemaining());
				row.put("machine_name", lmo.getApplianceType());
				row.put("machine_icon", lmo.getIcon());
				row.put("machine_number", lmo.getLabel());

				data.add(row);
			}

		}

		return data;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);

		LaundryMachineObject machine_selected = MachineStatuses[position];
		final ShowMachineNotificationDialog mShowMachineNotificationDialog = new ShowMachineNotificationDialog(this, this.getLayoutInflater(), machine_selected);

		mShowMachineNotificationDialog.SetNotificationListener(new SetNotificationListener() {

            @Override
            public void onUserSavedMachine() {
                startService();
                mShowMachineNotificationDialog.done();

            }
        });
	}


	// start the updating service
	
	public void startService() {
		/* Implement this. It should start your service */
		Intent SuperDuperService = new Intent(this, MachineMonitorService.class);
		startService(SuperDuperService);
	}

//	public void stopService() {
//		/* Implement this. It should stop your service */
//		stopService(SuperDuperService);
//	}

	private void llog(String msg) {
		ValuesObj.logme("<<" + this.getClass().getName() + ">> " + msg);
	}
}