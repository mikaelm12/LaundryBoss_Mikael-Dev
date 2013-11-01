package edu.mit.mitmobile2.laundry;

import android.app.ListActivity;
import android.content.Intent;
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
import edu.mit.mitmobile2.laundry.httpgetters.GetLaundryData_ALL;
import edu.mit.mitmobile2.laundry.machines.LaundryRoomObject;

public class AllRooms extends ListActivity {

	// private final static ArrayList<HashMap<String, ?>> data = new
	// ArrayList<HashMap<String, ?>>();

	private static LaundryRoomObject[] AllDormStatuses;
	private static Handler handler;

	private static final String[] layoutkeys = new String[] {
	"Dorm Name", "Washer Status", "Dryer Status" };

	private static final int[] layoutvalues = new int[] {
	R.id.tvDormName, R.id.tvDormStatus, R.id.tvNumDryers };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// overridePendingTransition(R.anim.fadein, R.anim.fadeout);

        //set up the listview UI
        getListView().setBackgroundResource(R.drawable.laundry_main_repeat);
        getListView().setCacheColorHint(Color.TRANSPARENT);
        getListView().setSelector(android.R.color.transparent);

		if (ValuesObj.isDeviceOnline(this)) {

			AllDormStatuses = new LaundryRoomObject[2];

			for (int k = 0; k < 2; k++) {
				AllDormStatuses[k] = new LaundryRoomObject();
			}

			ArrayList<HashMap<String, ?>> data = SetupListViewData(AllDormStatuses);

			SimpleAdapter adapter = new SimpleAdapter(AllRooms.this, data, R.layout.laundry_maindormview_adapter, layoutkeys, layoutvalues);

			setListAdapter(adapter);

			// starting the handler!

			handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {

					super.handleMessage(msg);
					ArrayList<HashMap<String, ?>> data = (ArrayList<HashMap<String, ?>>) msg.obj;

					SimpleAdapter adapter = new SimpleAdapter(AllRooms.this, data, R.layout.laundry_maindormview_adapter, layoutkeys, layoutvalues);

					setListAdapter(adapter);

					getListView().setDivider(null);

                    //line below is so that the list screen position isnt offset when an update occurs
					getListView().setSelectionFromTop(msg.arg1, msg.arg2);
				}
			};

			handler.postDelayed(updateMachines, 50);

		} else {
            //no internet, no reason to be here!
			finish();
		}

	}

    private void safeTellUser(String msg){
        ValuesObj.AlertPlayer(this,true,msg);
    }


	private Runnable updateMachines = new Runnable() {
		// refresh all the machines!
		public void run() {

			AllDormStatuses = (new GetLaundryData_ALL()).getData();

			if (AllDormStatuses == null) {
				// parsing/ http request error
                safeTellUser("http request error!");
                llog("all room experienced error with the alldormstatuses. http error? quitting");
				finish();
                return;

			} else {

                ArrayList<HashMap<String, ?>> data = SetupListViewData(AllDormStatuses);
                Message msg = handler.obtainMessage();
                msg.obj = data;
                // handler.sendMessage(msg);

                int savedPosition = getListView().getFirstVisiblePosition();
                View firstVisibleView = getListView().getChildAt(0);
                int savedListTop = (firstVisibleView == null) ? 0 : firstVisibleView.getTop();

                msg.arg1 = savedPosition;
                msg.arg2 = savedListTop;
                handler.sendMessage(msg);

                handler.postDelayed(updateMachines, 60000);
            }
		}
	};

	// ///////////////// Activity stuff below

	@Override
	protected void onPause() {
		super.onPause();
		handler.removeCallbacks(updateMachines);
	}

	private ArrayList<HashMap<String, ?>> SetupListViewData(
			LaundryRoomObject[] listdata) {
		// make the listview containing all the dorms!
		ArrayList<HashMap<String, ?>> data = new ArrayList<HashMap<String, ?>>();

		// now let's fill with data!
		int len = listdata.length;

		HashMap<String, Object> row;
		// to be re-used in the loop below

		for (int i = 0; i < len - 1; i++) {
			LaundryRoomObject lro = listdata[i];
			// get the obj containing all the dorms!

			String numwashers, numdryers;
			numwashers = lro.getNumAvailableWashers();
			numdryers = lro.getNumAvailableDryers();

			// get the num washers then dryers

			row = new HashMap<String, Object>();

			row.put("Dorm Name", lro.getName());
			row.put("Washer Status", numwashers + " Washers");
			row.put("Dryer Status", numdryers + " Dryers");
			data.add(row);

		}

		return data;
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);

		// get the object
		LaundryRoomObject lro = AllDormStatuses[position];

		// open the specific screen
		OpenSpecificRoom(lro.getID(), lro.getName(), lro.getOnlineStatus());
	}

	private void OpenSpecificRoom(String ID, String NAME, String STATUS) {
		// open a class

		try {
			// now make the msg to send to the specific screen!
			Bundle basket = new Bundle();
			basket.putString(ValuesObj.BUNDLER_ROOM_ID, ID);
			basket.putString(ValuesObj.BUNDLER_ROOM_NAME, NAME);
			basket.putString(ValuesObj.BUNDLER_ROOM_STATUS, STATUS);

			Class<?> ourClass = Class.forName("edu.mit.mitmobile2.laundry.SpecificRoom");
			Intent ourIntent = new Intent(this, ourClass);

			ourIntent.putExtras(basket);
			startActivity(ourIntent);
			overridePendingTransition(R.anim.laundry_slide_from_right_in, R.anim.laundry_slide_from_right_out);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	private void llog(String msg) {
		ValuesObj.logme("<<" + this.getClass().getName() + ">> " + msg);
	}
}