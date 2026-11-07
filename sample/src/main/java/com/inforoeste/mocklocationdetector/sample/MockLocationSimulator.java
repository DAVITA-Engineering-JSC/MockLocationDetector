package com.inforoeste.mocklocationdetector.sample;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Pushes a fixed simulated location, once per second, to the GPS test provider and to the
 * fused location provider. Used to exercise {@link com.inforoeste.mocklocationdetector.MockLocationDetector}.
 *
 * The app must be chosen in Developer options &gt; "Select mock location app" (API 23+),
 * otherwise {@link #start} throws a {@link SecurityException}.
 */
public class MockLocationSimulator {

    private static final String TAG = MockLocationSimulator.class.getSimpleName();
    private static final long UPDATE_INTERVAL_MS = 1000;
    private static final float ACCURACY_METERS = 3f;

    private final LocationManager mLocationManager;
    private final GoogleApiClient mGoogleApiClient;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mRunning;
    private boolean mFusedMockModeOn;
    private double mLatitude;
    private double mLongitude;

    private final Runnable mPushRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mRunning) {
                return;
            }
            pushLocation();
            mHandler.postDelayed(this, UPDATE_INTERVAL_MS);
        }
    };

    public MockLocationSimulator(Context context, GoogleApiClient googleApiClient) {
        mLocationManager = (LocationManager) context.getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);
        mGoogleApiClient = googleApiClient;
    }

    public boolean isRunning() {
        return mRunning;
    }

    /**
     * @throws SecurityException if this app is not the selected mock location app.
     */
    public void start(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
        if (!mRunning) {
            mLocationManager.addTestProvider(LocationManager.GPS_PROVIDER,
                    false, false, false, false, true, true, true,
                    Criteria.POWER_LOW, Criteria.ACCURACY_FINE);
            mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
            mRunning = true;
        }
        enableFusedMockMode();
        mHandler.removeCallbacks(mPushRunnable);
        mHandler.post(mPushRunnable);
    }

    public void stop() {
        if (!mRunning) {
            return;
        }
        mRunning = false;
        mHandler.removeCallbacks(mPushRunnable);
        try {
            mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false);
            mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        } catch (RuntimeException e) {
            Log.w(TAG, "Could not remove GPS test provider: " + e.getMessage());
        }
        if (mFusedMockModeOn && mGoogleApiClient.isConnected()) {
            try {
                LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, false);
            } catch (SecurityException e) {
                Log.w(TAG, "Could not disable fused mock mode: " + e.getMessage());
            }
        }
        mFusedMockModeOn = false;
    }

    /** Call when the GoogleApiClient (re)connects so the fused provider also gets mock locations. */
    public void onGoogleApiConnected() {
        if (mRunning) {
            mFusedMockModeOn = false;
            enableFusedMockMode();
        }
    }

    private void enableFusedMockMode() {
        if (!mFusedMockModeOn && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);
            mFusedMockModeOn = true;
        }
    }

    private void pushLocation() {
        try {
            mLocationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER,
                    buildLocation(LocationManager.GPS_PROVIDER));
            if (mFusedMockModeOn && mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient,
                        buildLocation("fused"));
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to push mock location: " + e.getMessage());
            stop();
        }
    }

    private Location buildLocation(String provider) {
        Location location = new Location(provider);
        location.setLatitude(mLatitude);
        location.setLongitude(mLongitude);
        location.setAltitude(0);
        location.setAccuracy(ACCURACY_METERS);
        location.setTime(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        return location;
    }
}
