package com.example.whosabout;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whosabout.GeofenceStuff.SimpleGeofence;
import com.example.whosabout.GeofenceStuff.SimpleGeofenceStore;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationClient.OnAddGeofencesResultListener;
import com.google.android.gms.location.LocationStatusCodes;

public class MainActivity extends FragmentActivity implements
		ConnectionCallbacks, OnConnectionFailedListener,
		OnAddGeofencesResultListener, CompoundButton.OnCheckedChangeListener {

	private Boolean TRACKING_STATUS;
	private Switch s;

	// geofence utilities
	private SimpleGeofence mGeoFence;
	private SimpleGeofenceStore mGeofenceStorage;
	private List<Geofence> mGeofenceList;

	// Holds the location client
	private LocationClient mLocationClient;
	// Stores the PendingIntent used to request geofence monitoring
	private PendingIntent mGeofenceRequestIntent;

	// Defines the allowable request types.
	public enum REQUEST_TYPE {
		ADD
	}

	private REQUEST_TYPE mRequestType;
	// Flag that indicates if a request is underway.
	private boolean mInProgress;

	// geofence instance
	private double mLatitude;
	private double mLongitude;
	private float mRadius = 1;

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
		filter.addCategory(Intent.CATEGORY_DEFAULT);
		ResponseReceiver receiver = new ResponseReceiver();
		registerReceiver(receiver, filter);

		// Load up Bundle data
		if (savedInstanceState != null) {
			TRACKING_STATUS = savedInstanceState.getBoolean("TRACKING_STATUS");
		} else {
			TRACKING_STATUS = false;
		}

		// Setup listener to monitor switch movements
		s = (Switch) findViewById(R.id.switch_tracking);
		if (s != null) {
			s.setOnCheckedChangeListener(this);
		}

		// Instantiate a new geofence storage area
		mGeofenceStorage = new SimpleGeofenceStore(this);

		// Instantiate the current List of geofences
		mGeofenceList = new ArrayList<Geofence>();

		// Start with the request flag set to false
		mInProgress = false;

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	private Boolean checkPlayServicesAvailability() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			return true;
		} else {
			return false;
		}
	}

	private void initializeGeofences() {
		// Get current Long and Latitude
		// Replace this for UI input next iteration
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location location = lm
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		mLongitude = location.getLongitude();
		mLatitude = location.getLatitude();

		Toast.makeText(this,
				"Longitude " + mLongitude + " - Latitude " + mLatitude,
				Toast.LENGTH_SHORT).show();
		mGeoFence = new SimpleGeofence("1", mLatitude, mLongitude, mRadius,
				Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER
						| Geofence.GEOFENCE_TRANSITION_EXIT);
		// Push GeoFence object to an array of geofences
		mGeofenceStorage.setGeofence("1", mGeoFence);
		mGeofenceList.add(mGeoFence.toGeofence());
		addGeofences();
		TextView blah = (TextView) findViewById(R.id.output_message);
		blah.setText("KOOKARACHAS");
	}

	/*
	 * Create a PendingIntent that triggers an IntentService in your app when a
	 * geofence transition occurs.
	 */
	private PendingIntent getTransitionPendingIntent() {
		// Create an explicit Intent
		Intent intent = new Intent(this, ReceiveTransitionsIntentService.class);
		/*
		 * Return the PendingIntent
		 */
		return PendingIntent.getService(this, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			TRACKING_STATUS = true;
			Boolean success = checkPlayServicesAvailability();
			if (success == true) {
				initializeGeofences();
			}
		} else {
			TextView blah = (TextView) findViewById(R.id.output_message);
			blah.setText("Switched Off");
			TRACKING_STATUS = false;
		}

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean("TRACKING_STATUS", TRACKING_STATUS);
		super.onSaveInstanceState(savedInstanceState);
	}

	/**
	 * Start a request for geofence monitoring by calling
	 * LocationClient.connect().
	 */
	public void addGeofences() {
		// Start a request to add geofences
		mRequestType = REQUEST_TYPE.ADD;

		/*
		 * Create a new location client object. Since the current activity class
		 * implements ConnectionCallbacks and OnConnectionFailedListener, pass
		 * the current activity object as the listener for both parameters
		 */
		mLocationClient = new LocationClient(this, this, this);
		// If a request is not already underway
		if (!mInProgress) {
			// Indicate that a request is underway
			mInProgress = true;
			// Request a connection from the client to Location Services
			mLocationClient.connect();
		} else {
			/*
			 * A request is already underway. You can handle this situation by
			 * disconnecting the client, re-setting the flag, and then re-trying
			 * the request.
			 */
			// "Could not send request to add Geofence at this time";
		}
	}

	@Override
	public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
		if (LocationStatusCodes.SUCCESS == statusCode) {
			// Do something AWESOME
			Toast.makeText(
					this,
					"Google says Geofence was added okay"
							+ geofenceRequestIds[0], Toast.LENGTH_SHORT).show();
		} else {
			// Don't do anything fun :(
			Toast.makeText(this, "Negatory - Geofence failed to add",
					Toast.LENGTH_SHORT).show();
		}
		mInProgress = false;
		// TODO - undo
		mLocationClient.disconnect();

	}

	@Override
	public void onConnected(Bundle dataBundle) {
		switch (mRequestType) {
		case ADD:
			// Get the PendingIntent for the request
			PendingIntent mTransitionPendingIntent = getTransitionPendingIntent();
			// Send a request to add the current geofences
			mLocationClient.addGeofences(mGeofenceList,
					mTransitionPendingIntent, this);
			
		}
	}

	@Override
	public void onDisconnected() {
		// Turn off the request flag
		mInProgress = false;
		// Destroy the current location client
		mLocationClient = null;
	}

	public class ResponseReceiver extends BroadcastReceiver {
		public static final String ACTION_RESP = "com.example.whosabout.intent.action.MESSAGE_PROCESSED";

		@Override
		public void onReceive(Context context, Intent intent) {
			Toast.makeText(context, "COCK SLAM - received response",
					Toast.LENGTH_SHORT).show();
			TextView result = (TextView) findViewById(R.id.output_message);
			String text = intent.getStringExtra("omg");
			result.setText(text);
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// Turn off the request flag
		mInProgress = false;
		/*
		 * If the error has a resolution, start a Google Play services activity
		 * to resolve it.
		 */
		/*
		 * if (connectionResult.hasResolution()) { try {
		 * connectionResult.startResolutionForResult( this,
		 * CONNECTION_FAILURE_RESOLUTION_REQUEST); } catch (SendIntentException
		 * e) { // Log the error e.printStackTrace(); } // If no resolution is
		 * available, display an error dialog } else { // Get the error code int
		 * errorCode = connectionResult.getErrorCode(); // Get the error dialog
		 * from Google Play services Dialog errorDialog =
		 * GooglePlayServicesUtil.getErrorDialog( errorCode, this,
		 * CONNECTION_FAILURE_RESOLUTION_REQUEST); // If Google Play services
		 * can provide an error dialog if (errorDialog != null) { // Create a
		 * new DialogFragment for the error dialog ErrorDialogFragment
		 * errorFragment = new ErrorDialogFragment(); // Set the dialog in the
		 * DialogFragment errorFragment.setDialog(errorDialog); // Show the
		 * error dialog in the DialogFragment errorFragment.show(
		 * getSupportFragmentManager(), "Geofence Detection"); } }
		 */
	}
	/*
	 * public static class ErrorDialogFragment extends DialogFragment { //
	 * Global field to contain the error dialog private Dialog mDialog;
	 * 
	 * // Default constructor. Sets the dialog field to null public
	 * ErrorDialogFragment() { super(); mDialog = null; }
	 * 
	 * // Set the dialog to display public void setDialog(Dialog dialog) {
	 * mDialog = dialog; }
	 * 
	 * // Return a Dialog to the DialogFragment.
	 * 
	 * @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
	 * return mDialog; }
	 * 
	 * }
	 * 
	 * 
	 * /* Handle results returned to the FragmentActivity by Google Play
	 * services
	 */
	// .*
	// @Override
	// protected void onActivityResult(
	// int requestCode, int resultCode, Intent data) {
	// // Decide what to do based on the original request code
	// switch (requestCode) {
	//
	// case CONNECTION_FAILURE_RESOLUTION_REQUEST :
	// /*
	// * If the result code is Activity.RESULT_OK, try
	// * to connect again
	// */
	// switch (resultCode) {
	//
	// case Activity.RESULT_OK :
	// /*
	// * Try the request again
	// */
	//
	// break;
	// }
	//
	// }
	//
	// }
	//
	//
	// private boolean servicesConnected() {
	// // Check that Google Play services is available
	// int resultCode =
	// GooglePlayServicesUtil.
	// isGooglePlayServicesAvailable(this);
	// // If Google Play services is available
	// if (ConnectionResult.SUCCESS == resultCode) {
	// // In debug mode, log the status
	// Log.d("Geofence Detection",
	// "Google Play services is available.");
	// // Continue
	// return true;
	// // Google Play services was not available for some reason
	// } else {
	// // Get the error code
	// int errorCode = resultCode;
	// // Get the error dialog from Google Play services
	// Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
	// errorCode,
	// this,
	// CONNECTION_FAILURE_RESOLUTION_REQUEST);
	//
	// // If Google Play services can provide an error dialog
	// if (errorDialog != null) {
	// // Create a new DialogFragment for the error dialog
	// ErrorDialogFragment errorFragment =
	// new ErrorDialogFragment();
	// // Set the dialog in the DialogFragment
	// errorFragment.setDialog(errorDialog);
	// // Show the error dialog in the DialogFragment
	// /*errorFragment.show(
	// getSupportFragmentManager(),
	// "Geofence Detection");*/
	// }
	// }
	// }*/
}