package com.inforoeste.mocklocationdetector.sample;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.inforoeste.mocklocationdetector.MockLocationDetector;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private TextView mLastUpdateTimeTextView;
    private TextView mIsMockTextView;
    private TextView mAreMockLocationAppsPresentTextView;
    private TextView mIsMockLocationsOnTextView;
    private LocationRequest mLocationRequest;

    // Nguyen Tat Thanh University (coordinates provided by DAVITA)
    private static final double NTTU_LATITUDE = 10.7610342;
    private static final double NTTU_LONGITUDE = 106.7103536;

    private MockLocationSimulator mMockLocationSimulator;
    private EditText mMockLatitudeEditText;
    private EditText mMockLongitudeEditText;
    private Button mMockToggleButton;
    private TextView mMockStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLatitudeTextView = (TextView) findViewById(R.id.txt_location_latitude);
        mLongitudeTextView = (TextView) findViewById(R.id.txt_location_longitude);
        mLastUpdateTimeTextView = (TextView) findViewById(R.id.txt_location_last_update_time);
        mIsMockTextView = (TextView) findViewById(R.id.txt_is_mock_text);
        mAreMockLocationAppsPresentTextView = (TextView) findViewById(R.id.txt_are_mock_location_apps_present);
        mIsMockLocationsOnTextView = (TextView) findViewById(R.id.txt_is_allow_mock_locations_on);

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        setupMockSimulator();
    }

    private void setupMockSimulator() {
        mMockLocationSimulator = new MockLocationSimulator(this, mGoogleApiClient);
        mMockLatitudeEditText = (EditText) findViewById(R.id.edt_mock_latitude);
        mMockLongitudeEditText = (EditText) findViewById(R.id.edt_mock_longitude);
        mMockToggleButton = (Button) findViewById(R.id.btn_mock_toggle);
        mMockStatusTextView = (TextView) findViewById(R.id.txt_mock_status);
        fillNttuCoordinates();

        findViewById(R.id.btn_mock_preset_nttu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillNttuCoordinates();
            }
        });
        mMockToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMockLocationSimulator.isRunning()) {
                    mMockLocationSimulator.stop();
                } else {
                    startMock();
                }
                updateMockUI();
            }
        });
    }

    private void fillNttuCoordinates() {
        mMockLatitudeEditText.setText(String.valueOf(NTTU_LATITUDE));
        mMockLongitudeEditText.setText(String.valueOf(NTTU_LONGITUDE));
    }

    private void startMock() {
        double latitude;
        double longitude;
        try {
            latitude = Double.parseDouble(mMockLatitudeEditText.getText().toString().trim());
            longitude = Double.parseDouble(mMockLongitudeEditText.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.mock_invalid_coordinates, Toast.LENGTH_SHORT).show();
            return;
        }
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            Toast.makeText(this, R.string.mock_invalid_coordinates, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mMockLocationSimulator.start(latitude, longitude);
        } catch (SecurityException e) {
            mMockLocationSimulator.stop();
            Toast.makeText(this, R.string.mock_not_allowed, Toast.LENGTH_LONG).show();
        }
    }

    private void updateMockUI() {
        if (mMockLocationSimulator.isRunning()) {
            mMockToggleButton.setText(R.string.mock_stop);
            mMockStatusTextView.setText(getString(R.string.mock_status_running,
                    mMockLatitudeEditText.getText().toString().trim(),
                    mMockLongitudeEditText.getText().toString().trim()));
        } else {
            mMockToggleButton.setText(R.string.mock_start);
            mMockStatusTextView.setText(R.string.mock_status_idle);
        }
    }

    @Override
    protected void onDestroy() {
        mMockLocationSimulator.stop();
        super.onDestroy();
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            mMockLocationSimulator.onGoogleApiConnected();
        } catch (SecurityException e) {
            mMockLocationSimulator.stop();
            updateMockUI();
        }
        createLocationRequest();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
//        mLocationRequest.setInterval(10000);
//        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUI();
    }

    private void updateUI() {
        mLatitudeTextView.setText(String.valueOf(mCurrentLocation.getLatitude()));
        mLongitudeTextView.setText(String.valueOf(mCurrentLocation.getLongitude()));
        mLastUpdateTimeTextView.setText(mLastUpdateTime);
        boolean isMock = MockLocationDetector.isLocationFromMockProvider(this, mCurrentLocation);
        mIsMockTextView.setText(String.valueOf(isMock));
        if (isMock) {
            mIsMockTextView.setTextColor(ContextCompat.getColor(this, R.color.c_red));
        } else {
            mIsMockTextView.setTextColor(ContextCompat.getColor(this, R.color.c_green));
        }
        boolean mockLocationAppsPresent = MockLocationDetector.checkForAllowMockLocationsApps(this);
        mAreMockLocationAppsPresentTextView.setText(String.valueOf(mockLocationAppsPresent));
        if (mockLocationAppsPresent) {
            mAreMockLocationAppsPresentTextView.setTextColor(ContextCompat.getColor(this, R.color.c_red));
        } else {
            mAreMockLocationAppsPresentTextView.setTextColor(ContextCompat.getColor(this, R.color.c_green));
        }
        boolean isAllowMockLocationsON = MockLocationDetector.isAllowMockLocationsOn(this);
        mIsMockLocationsOnTextView.setText(String.valueOf(isAllowMockLocationsON));
        if (isAllowMockLocationsON) {
            mIsMockLocationsOnTextView.setTextColor(ContextCompat.getColor(this, R.color.c_red));
        } else {
            mIsMockLocationsOnTextView.setTextColor(ContextCompat.getColor(this, R.color.c_green));
        }
    }
}
