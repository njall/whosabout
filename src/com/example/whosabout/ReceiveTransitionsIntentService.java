package com.example.whosabout;

import java.util.List;

import com.example.whosabout.MainActivity.ResponseReceiver;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ReceiveTransitionsIntentService extends IntentService {
    //...
    /**
     * Sets an identifier for the service
     */
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
        Toast.makeText(this, "RecTransIntServ Intiated",
				Toast.LENGTH_SHORT).show();
    }
    /**
     * Handles incoming intents
     *@param intent The Intent sent by Location Services. This
     * Intent is provided
     * to Location Services (inside a PendingIntent) when you call
     * addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {
    	
    	Intent broadcastIntent = new Intent();
    	broadcastIntent.setAction(ResponseReceiver.ACTION_RESP);
    	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
    	broadcastIntent.putExtra("omg", "Coming at chu from ReceiveTransitionIntentService! Big up, braapapap");
    	sendBroadcast(broadcastIntent);
    	
    	
    	
        // First check for errors
    	Context c = getApplicationContext();
    	
    	Toast.makeText(c, "intent handled",
				Toast.LENGTH_SHORT).show();
    	
    	Log.d("ReceiveTransitionsIntentService",
                "Outputting Stuff!: COCKWAFFLE");
    	
        if (LocationClient.hasError(intent)) {
            // Get the error code with a static method
            int errorCode = LocationClient.getErrorCode(intent);
            // Log the error
            Log.e("ReceiveTransitionsIntentService",
                    "Location Services error: " +
                    Integer.toString(errorCode));
            /*
             * You can also send the error code to an Activity or
             * Fragment with a broadcast Intent
             */
        /*
         * If there's no error, get the transition type and the IDs
         * of the geofence or geofences that triggered the transition
         */
        } else {
            // Get the type of transition (entry or exit)
            int transitionType =
                    LocationClient.getGeofenceTransition(intent);
            // Test that a valid transition was reported
            if (
                (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
                 ||
                (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT)
               ) {
                List <Geofence> triggerList =
                        LocationClient.getTriggeringGeofences(intent);

                String[] triggerIds = new String[triggerList.size()];

                for (int i = 0; i < triggerIds.length; i++) {
                    // Store the Id of each geofence
                    triggerIds[i] = triggerList.get(i).getRequestId();
                }
                /*
                 * At this point, you can store the IDs for further use
                 * display them, or display the details associated with
                 * them.
                 */
                if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER)
                	Toast.makeText(this, "You've entered Schuster :) !" + transitionType,
    					Toast.LENGTH_SHORT).show();
                else Toast.makeText(this, "You've left Schuster :( !" + transitionType,
    					Toast.LENGTH_SHORT).show();
            }
        // An invalid transition was reported
         else {
            Log.e("ReceiveTransitionsIntentService",
                    "Geofence transition error: " +
                    Integer.toString(transitionType));
        }
    }
    }
}
