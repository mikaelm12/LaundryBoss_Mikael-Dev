package edu.mit.mitmobile2.laundry.httpgetters;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import edu.mit.mitmobile2.laundry.constants.ValuesObj;
import edu.mit.mitmobile2.laundry.machines.LaundryMachineObject;

public class GetLaundryData_MACHINE {

	public GetLaundryData_MACHINE() {
		// constructor to create instance
	}

	/**
     * Takes the old machine lmo and updates it!
	 * This API call only retrieves the time remaining, oos, machineID, and room status!
	 */
	public LaundryMachineObject getData(LaundryMachineObject old_machine) {
		// get this machine's data!

        //new machine
		LaundryMachineObject updated_machine;

        //json result of http get
        JSONObject this_machine_json = null;

        // get the json
		String hopefully_json = GetHttpData(old_machine.getMachineID());

		if (hopefully_json.length() < 10) {
			// definitely a problem!!!
			return null;
		}

		// parse it!
		try {
			this_machine_json = new JSONObject(hopefully_json);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// {"status":"Available","out_of_service":"0","time_remaining":"cycle ended 22 minutes ago"}
        updated_machine = new LaundryMachineObject(old_machine.getThisObjectInJson(),this_machine_json);
		return updated_machine;
	}

	private String GetHttpData(String machineID) {
		// get the json from the MIT LV API
		RetrieveSiteData task = new RetrieveSiteData();

		task.execute("http://mobile-dev.mit.edu/api/?module=laundry&call=getStatus&appliance_desc_key="
                + machineID);
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

		if (json_string.length() < 10) {
			llog("specific machine json string is less than 10 in len.");
			return null;

		}

		return json_string;
	}

	private void llog(String msg) {
		ValuesObj.logme("<<" + this.getClass().getName() + ">> " + msg);
	}

}
