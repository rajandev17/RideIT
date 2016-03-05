package com.rajanks.rideit.UI;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.rajanks.rideit.R;
import com.rajanks.rideit.Utils.Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_LOCATION = 1629 ;
    Button rideIT;
    private Timer selfieTimer;
    private static String ride_folder_path = "";
    SurfaceView preview;
    SurfaceHolder holder;
    private GoogleApiClient mGoogleApiClient;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;
    private double currentLatitude;
    private double currentLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rideIT = (Button) findViewById(R.id.btn_ride_txt);
        ((TextView) findViewById(R.id.tv_title_main)).setTypeface(Util.getFontWithName(this, "head"));

        //setup camera
        preview = (SurfaceView) findViewById(R.id.surface_view);
        holder = preview.getHolder();

        //Prompt to enable Enable GPS
        Util.checkAndEnableGPS(this);
        if (checkPlayServices()) {
            buildGoogleApiClient();
        }

        rideIT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag().toString().equals("ON")) {
                    v.setTag("OFF");
                    v.setBackgroundColor(getResources().getColor(R.color.start));
                    ((Button) v).setText(getResources().getString(R.string.ride_text));
                    Util.showLongToast(MainActivity.this, "Succesfully completed Ride.");
                    endRide();
                } else {
                    Util.showLongToast(MainActivity.this, "Started Ride");
                    ((Button) v).setText(getResources().getString(R.string.ride_text_off));
                    v.setBackgroundColor(getResources().getColor(R.color.stop));
                    v.setTag("ON");
                    startRide();
                }
            }
        });
    }

    /**
     * Method to display the location on UI
     * */
    private void updateLocation() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_LOCATION);

                // MY_PERMISSIONS_REQUEST_ACCESS_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
            mLastLocation = LocationServices.FusedLocationApi
                    .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            currentLatitude = mLastLocation.getLatitude();
            currentLongitude = mLastLocation.getLongitude();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLocation();
                } else {
                    this.finish();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     * */
    @SuppressWarnings("deprecation")
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();
    }

    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }

    @Override
    public void onConnected(Bundle arg0) {
        updateLocation();
        Util.showLongToast(this, "fetching location");
        Util.showLongToast(this, "lat - " + currentLatitude + "long - " + currentLongitude);
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }


    public String createFolderForRide() {
        File root_directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/RideIT");
        if (!root_directory.exists()) {
            root_directory.mkdir();
        }
        String sub_path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/RideIT/" + new SimpleDateFormat("yyyy-MMM-dd' at 'hh:mm a").format(new Date());
        File sub_directory = new File(sub_path);
        try {
            sub_directory.mkdir();
        } catch (Exception e) {

        }
        return sub_path;
    }


    public void createFileWithSource() {
        try {
            File ride_details = new File(ride_folder_path, "RideDetails.txt");
            FileWriter writer = new FileWriter(ride_details);
            String start = getAddress();
            updateLocation();
            String time = new SimpleDateFormat("hh:mm a' on 'dd-MMM-yyyy'.'").format(new Date());
            String file_content = "\n*****************************************************\n\n" +
                    "\t# Ride Started at " + time + "\n" +
                    "\t# Start Address\n" + start +
                    "\t# GeoLocation\n\t\tLatitude - " + currentLatitude + "\n\t\tLongitude - " + currentLongitude + "\n";
            writer.write(file_content);
            writer.flush();
            writer.close();
        } catch (IOException e) {

        }
    }

    public void updateFileWithDestination() {
        try {
            File ride_details = new File(ride_folder_path, "RideDetails.txt");
            FileWriter textFileWriter = new FileWriter(ride_details, true);
            BufferedWriter out = new BufferedWriter(textFileWriter);
            String end = getAddress();
            updateLocation();
            String time = new SimpleDateFormat("hh:mm a' on 'dd-MMM-yyyy'.'").format(new Date());
            String file_content = "\n*****************************************************\n\n" +
                    "\t# Ride Ended at " + time + "\n" +
                    "\t# End Address\n" + end +
                    "\t# GeoLocation\n\t\tLatitude - " + currentLatitude + "\n\t\tLongitude - " + currentLongitude + "\n" +
                    "\n*****************************************************";
            out.write(file_content);
            out.close();
        } catch (Exception e) {
        }
    }

    public void startRide() {
        ride_folder_path = createFolderForRide();
        createFileWithSource();
        takeSelfieEveryFiveMinutes();
    }

    public void endRide() {
        stopTakingSelfie();
        updateFileWithDestination();
    }

    public void takeSelfieEveryFiveMinutes() {
        selfieTimer = new Timer();
        selfieTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                CaptureSelfieAndSave();
            }
        }, 0, 300000);
    }

    public void stopTakingSelfie() {
        selfieTimer.cancel();
    }

    @SuppressWarnings("deprecation")
    private Camera openFrontCamera() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    cam = Camera.open(camIdx);
                } catch (RuntimeException e) {
                }
            }
        }
        return cam;
    }

    @SuppressWarnings("deprecation")
    public void CaptureSelfieAndSave() {
        Log.d("rideit", "taking selfie");
        Camera camera = null;
        try {
            camera = openFrontCamera();
            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            camera.startPreview();
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    camera.release();
                    Log.d("rideit", "Taken selfie");
                    saveTakenSelfie(data);
                }
            });
        } catch (Exception e) {
            if (camera != null)
                camera.release();
        }
    }

    private void saveTakenSelfie(byte[] imagedata) {
        try {
            String imageName = "default";
            updateLocation();
            imageName = getFormattedLocationInDegree(currentLatitude, currentLongitude);
            String path = ride_folder_path + "/" + imageName + ".jpg";
            FileOutputStream stream = new FileOutputStream(path);
            stream.write(imagedata);
            stream.close();
            Log.d("rideit", "saved selfie");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioException) {

        }
    }

    public static String getFormattedLocationInDegree(double latitude, double longitude) {
        try {
            int latSeconds = (int) Math.round(latitude * 3600);
            int latDegrees = latSeconds / 3600;
            latSeconds = Math.abs(latSeconds % 3600);
            int latMinutes = latSeconds / 60;
            latSeconds %= 60;

            int longSeconds = (int) Math.round(longitude * 3600);
            int longDegrees = longSeconds / 3600;
            longSeconds = Math.abs(longSeconds % 3600);
            int longMinutes = longSeconds / 60;
            longSeconds %= 60;
            String latDegree = latDegrees >= 0 ? "N" : "S";
            String lonDegrees = latDegrees >= 0 ? "E" : "W";

            return Math.abs(latDegrees) + "°" + latMinutes + "'" + latSeconds
                    + "\"" + latDegree + " " + Math.abs(longDegrees) + "°" + longMinutes
                    + "'" + longSeconds + "\"" + lonDegrees;
        } catch (Exception e) {

            return "" + String.format("%8.5f", latitude) + "  "
                    + String.format("%8.5f", longitude);
        }
    }

    public String getAddress() {
        String completeAddress = "";
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1);
            if (addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0).length() > 0 ? "\t\t\t" + addresses.get(0).getAddressLine(0) + ",\n" : "";
                String city = addresses.get(0).getLocality().length() > 0 ? "\t\t\t" + addresses.get(0).getLocality() + "," : "";
                String state = addresses.get(0).getAdminArea().length() > 0 ? addresses.get(0).getAdminArea() : "";
                String country = addresses.get(0).getCountryName().length() > 0 ? "\n\t\t\t" + addresses.get(0).getCountryName() : "";
                String postalCode = addresses.get(0).getPostalCode().length() > 0 ? " - " + addresses.get(0).getPostalCode() + "\n" : "";
                completeAddress = address + city + state + country + postalCode;
            }
        } catch (IOException e) {
        }
        return completeAddress;
    }

}
