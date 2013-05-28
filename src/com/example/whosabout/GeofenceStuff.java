package com.example.whosabout;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.android.gms.location.Geofence;

public class GeofenceStuff {
	
    /**
     * A single Geofence object, defined by its center and radius.
     */
	public static class SimpleGeofence {
        // Instance variables
        private final String mId;
        private final double mLatitude;
        private final double mLongitude;
        private final float mRadius;
        private long mExpirationDuration;
        private int mTransitionType;

    /**
     * @param geofenceId The Geofence's request ID
     * @param latitude Latitude of the Geofence's center.
     * @param longitude Longitude of the Geofence's center.
     * @param radius Radius of the geofence circle.
     * @param expiration Geofence expiration duration
     * @param transition Type of Geofence transition.
     */
    public SimpleGeofence(
            String geofenceId,
            double latitude,
            double longitude,
            float radius,
            long expiration,
            int transition) {
        // Set the instance fields from the constructor
        this.mId = geofenceId;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mRadius = radius;
        this.mExpirationDuration = expiration;
        this.mTransitionType = transition;
    }
    // Instance field getters
    public String getId() {
        return mId;
    }
    public double getLatitude() {
        return mLatitude;
    }
    public double getLongitude() {
        return mLongitude;
    }
    public float getRadius() {
        return mRadius;
    }
    public long getExpirationDuration() {
        return mExpirationDuration;
    }
    public int getTransitionType() {
        return mTransitionType;
    }
    /**
     * Creates a Location Services Geofence object from a
     * SimpleGeofence.
     *
     * @return A Geofence object
     */
    public Geofence toGeofence() {
        // Build a new Geofence object
        return new Geofence.Builder()
                .setRequestId(getId())
                .setTransitionTypes(mTransitionType)
                .setCircularRegion(
                        getLatitude(), getLongitude(), getRadius())
                .setExpirationDuration(mExpirationDuration)
                .build();
    }
}
/**
 * Storage for geofence values, implemented in SharedPreferences.
 */
public static class SimpleGeofenceStore {
    // Keys for flattened geofences stored in SharedPreferences
    public static final String KEY_LATITUDE =
            "com.example.android.geofence.KEY_LATITUDE";
    public static final String KEY_LONGITUDE =
            "com.example.android.geofence.KEY_LONGITUDE";
    public static final String KEY_RADIUS =
            "com.example.android.geofence.KEY_RADIUS";
    public static final String KEY_EXPIRATION_DURATION =
            "com.example.android.geofence.KEY_EXPIRATION_DURATION";
    public static final String KEY_TRANSITION_TYPE =
            "com.example.android.geofence.KEY_TRANSITION_TYPE";
    // The prefix for flattened geofence keys
    public static final String KEY_PREFIX =
            "com.example.android.geofence.KEY";
    /*
     * Invalid values, used to test geofence storage when
     * retrieving geofences
     */
    public static final long INVALID_LONG_VALUE = -999l;
    public static final float INVALID_FLOAT_VALUE = -999.0f;
    public static final int INVALID_INT_VALUE = -999;
    // The SharedPreferences object in which geofences are stored
    private final SharedPreferences mPrefs;
    // The name of the SharedPreferences
    private static final String SHARED_PREFERENCES =
            "SharedPreferences";
    // Create the SharedPreferences storage with private access only
    public SimpleGeofenceStore(Context context) {
        mPrefs =
                context.getSharedPreferences(
                        SHARED_PREFERENCES,
                        Context.MODE_PRIVATE);
    }
    /**
     * Returns a stored geofence by its id, or returns null
     * if it's not found.
     *
     * @param id The ID of a stored geofence
     * @return A geofence defined by its center and radius. See
     */
    public SimpleGeofence getGeofence(String id) {
        /*
         * Get the latitude for the geofence identified by id, or
         * INVALID_FLOAT_VALUE if it doesn't exist
         */
        double lat = mPrefs.getFloat(
                getGeofenceFieldKey(id, KEY_LATITUDE),
                INVALID_FLOAT_VALUE);
        /*
         * Get the longitude for the geofence identified by id, or
         * INVALID_FLOAT_VALUE if it doesn't exist
         */
        double lng = mPrefs.getFloat(
                getGeofenceFieldKey(id, KEY_LONGITUDE),
                INVALID_FLOAT_VALUE);
        /*
         * Get the radius for the geofence identified by id, or
         * INVALID_FLOAT_VALUE if it doesn't exist
         */
        float radius = mPrefs.getFloat(
                getGeofenceFieldKey(id, KEY_RADIUS),
                INVALID_FLOAT_VALUE);
        /*
         * Get the expiration duration for the geofence identified
         * by id, or INVALID_LONG_VALUE if it doesn't exist
         */
        long expirationDuration = mPrefs.getLong(
                getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION),
                INVALID_LONG_VALUE);
        /*
         * Get the transition type for the geofence identified by
         * id, or INVALID_INT_VALUE if it doesn't exist
         */
        int transitionType = mPrefs.getInt(
                getGeofenceFieldKey(id, KEY_TRANSITION_TYPE),
                INVALID_INT_VALUE);
        // If none of the values are incorrect, return the object
        if (
            lat != INVALID_FLOAT_VALUE &&
            lng != INVALID_FLOAT_VALUE &&
            radius != INVALID_FLOAT_VALUE &&
            expirationDuration !=
                    INVALID_LONG_VALUE &&
            transitionType != INVALID_INT_VALUE) {

            // Return a true Geofence object
            return new SimpleGeofence(
                    id, lat, lng, radius, expirationDuration,
                    transitionType);
        // Otherwise, return null.
        } else {
            return null;
        }
    }
    /**
     * Save a geofence.
     * @param geofence The SimpleGeofence containing the
     * values you want to save in SharedPreferences
     */
    public void setGeofence(String id, SimpleGeofence geofence) {
        /*
         * Get a SharedPreferences editor instance. Among other
         * things, SharedPreferences ensures that updates are atomic
         * and non-concurrent
         */
        Editor editor = mPrefs.edit();
        // Write the Geofence values to SharedPreferences
        editor.putFloat(
                getGeofenceFieldKey(id, KEY_LATITUDE),
                (float) geofence.getLatitude());
        editor.putFloat(
                getGeofenceFieldKey(id, KEY_LONGITUDE),
                (float) geofence.getLongitude());
        editor.putFloat(
                getGeofenceFieldKey(id, KEY_RADIUS),
                geofence.getRadius());
        editor.putLong(
                getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION),
                geofence.getExpirationDuration());
        editor.putInt(
                getGeofenceFieldKey(id, KEY_TRANSITION_TYPE),
                geofence.getTransitionType());
        // Commit the changes
        editor.commit();
    }
    public void clearGeofence(String id) {
        /*
         * Remove a flattened geofence object from storage by
         * removing all of its keys
         */
        Editor editor = mPrefs.edit();
        editor.remove(getGeofenceFieldKey(id, KEY_LATITUDE));
        editor.remove(getGeofenceFieldKey(id, KEY_LONGITUDE));
        editor.remove(getGeofenceFieldKey(id, KEY_RADIUS));
        editor.remove(getGeofenceFieldKey(id,
                KEY_EXPIRATION_DURATION));
        editor.remove(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE));
        editor.commit();
    }
    /**
     * Given a Geofence object's ID and the name of a field
     * (for example, KEY_LATITUDE), return the key name of the
     * object's values in SharedPreferences.
     *
     * @param id The ID of a Geofence object
     * @param fieldName The field represented by the key
     * @return The full key name of a value in SharedPreferences
     */
    private String getGeofenceFieldKey(String id,
            String fieldName) {
        return KEY_PREFIX + "_" + id + "_" + fieldName;
    }
}
}
