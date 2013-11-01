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
import edu.mit.mitmobile2.laundry.machines.LaundryMachineObject;

public class GetLaundryData_ROOM {
	
	private String roomID, status, roomName;
	
	public GetLaundryData_ROOM(String whichroomID, String roomName) {
		// which room data to retrieve
		this.roomID = whichroomID;
		this.roomName = roomName;
	}
	
	public LaundryMachineObject[] getData() {
		//return the states of ALL the laundry machines in an array of laundry machine objects
		
		//get the json
		String hopefully_json = GetHttpData();
		
		if (hopefully_json.length()<10){
			//definitely a problem!!!
			return null;
		}
		
		//parse it!
		JSONObject all_machines=null, onemachine_json=null; //rename these!
		try {
			all_machines = new JSONObject(hopefully_json);
			all_machines = all_machines.getJSONObject("appliances");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		
		Iterator it = all_machines.keys();		
		
		int len = all_machines.length();

		LaundryMachineObject[] results = new LaundryMachineObject[len];//return variable		
		int count=0;//incrementer
		
		while (it.hasNext()){
			String machine_id = (String) it.next();
			//retrieve the room info from the json
			try {
				onemachine_json = all_machines.getJSONObject(machine_id);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//
//
//			//get values!
//			String room_name = null,status =null, label=null,
//					time_rem=null, appliance_type=null;
//
//			try {
////				room_name = onemachine_json.getString("laundry_room_name");
//
//				status = onemachine_json.getString("lrm_status");
//				label = onemachine_json.getString("label");
//				time_rem = onemachine_json.getString("time_remaining");//not needed here?
//				appliance_type = onemachine_json.getString("appliance_type");
//
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				llog("couldn't retrieve machine data"+e.getMessage());
//			}
			
/*			"appliance_desc_key":"15713",
			"lrm_status":"Online",
			"appliance_type":"WASHER",
			"out_of_service":"0",
			"label":"01",
			"time_remaining":"available"*/
			
			//package and return
			
			LaundryMachineObject lmo = new LaundryMachineObject(onemachine_json,roomName);
//			lmo.machine_id = machine_id;
//			lmo.laundry_room_status = status; //Status means online/offline!
//			lmo.label = label;
//			lmo.time_remaining = time_rem;
//			lmo.appliance_type = appliance_type;
//			lmo.room_name = this.roomName;
//			lmo.fixAllCaps();
//			lmo.setmyicon();
//			lmo.setTimeOfCompletion();
			
			results[count] = lmo;
			count++;//I'm so stupid
			
			
			
		}
		
/*		("room data is thus. count= "+count);
		for (LaundryMachineObject lmo: results){
			(lmo.report());
		}*/
		
		return SortResultsPostParse(results);
	}
	

	private String GetHttpData() {
		// get the json from the MIT LV API
		RetrieveSiteData task = new RetrieveSiteData();

		task.execute("http://mobile-dev.mit.edu/api/?module=laundry&call=getRoomDetails&location="+roomID);
		String json_string = "null";
		
		try {
			json_string = task.get(RetrieveSiteData.RETRIEVAL_TIMEOUT_TIME_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return json_string;
	}
	
	////	////	////	ROOM SORTING	////	////	////	////
	
	
	private LaundryMachineObject[] SortResultsPostParse(LaundryMachineObject[] results) {
		// takes the LRO array and sorts the rooms alphabetically
		TreeSet ts = new TreeSet(new MyComp());
        
        for (LaundryMachineObject lro: results){
        	ts.add(lro);
        }
        
        LaundryMachineObject[] new_results = new LaundryMachineObject[results.length];
	
		Iterator it = ts.iterator();
		int count=0;
		while (it.hasNext()){
			new_results[count] = (LaundryMachineObject) it.next();
			count++;
		}
		
		
		return new_results;
	}
	
	class MyComp implements Comparator {
		public int compare(Object a, Object b) {
			// two json objects

			LaundryMachineObject lmo_a, lmo_b;
			lmo_a = (LaundryMachineObject) a;
			lmo_b = (LaundryMachineObject) b;

			return (lmo_a.getLabel().compareTo(lmo_b.getLabel()));
		}
	}
	
	private void llog(String msg) {
		ValuesObj.logme("<<" + this.getClass().getName() + ">> " + msg);
	}
}
