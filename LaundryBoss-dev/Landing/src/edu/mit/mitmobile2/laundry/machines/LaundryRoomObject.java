package edu.mit.mitmobile2.laundry.machines;

import org.json.JSONException;
import org.json.JSONObject;

import edu.mit.mitmobile2.laundry.constants.ValuesObj;

public class LaundryRoomObject {

    private String Name;
    private String ID;
    private String NumAvailableWashers;
    private String NumAvailableDryers;
    //statuses are online/offline
    private String OnlineStatus;

    private String ThisObjectInJson;

	public LaundryRoomObject() {
		Name = "Loading!";//"405 Memorial Drive";
		ID = "1364831";
		NumAvailableDryers = "2";
		NumAvailableWashers = "2";
        OnlineStatus = "Online";
	}

    public LaundryRoomObject(JSONObject data){
        boolean JsonGetterSucessful = true;
        try {
            Name = data.getString("laundry_room_name");
            OnlineStatus = data.getString("status");
            NumAvailableWashers = data.getString("available_washers");
            NumAvailableDryers = data.getString("available_dryers");
            ID = data.getString("location");
//				total_washers = data.getString("total_washers");
//				total_dryers = data.getString("total_dryers");

            ThisObjectInJson=data.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            JsonGetterSucessful=false;
        }

        if (JsonGetterSucessful==false)
            return;

        prepare();
    }

    /**call after initial field setup*/
    public void prepare(){
        //make sure the "available" fields have a valid int value!
        if (NumAvailableWashers.contentEquals(""))
            NumAvailableWashers="0";

        if (NumAvailableWashers.contentEquals("null"))
            NumAvailableWashers="0";

        if (NumAvailableDryers.contentEquals(""))
            NumAvailableDryers="0";

        if (NumAvailableDryers.contentEquals("null"))
            NumAvailableDryers="0";

        //fix caps
        Name = ValuesObj.capitalizeString(Name);

    }

    public String getNumAvailableDryers() {
        return NumAvailableDryers;
    }

    public String getNumAvailableWashers() {
        return NumAvailableWashers;
    }

    public String getID() {
        return ID;
    }

    public String getName() {
        return Name;
    }

    public String getOnlineStatus() {
        return OnlineStatus;
    }

}
