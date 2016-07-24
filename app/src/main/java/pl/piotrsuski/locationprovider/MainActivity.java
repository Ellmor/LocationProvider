package pl.piotrsuski.locationprovider;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    protected LocationManager locationManager;

    protected Button retrieveLocationButton;
    protected TextView tvLocation;
    private TextView qth;
    private Button exitbutton;
    private String locationFormater = "\nLat:%1$s\nLng:%2$s";
    private ImageButton copy;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        retrieveLocationButton = (Button) findViewById(R.id.btnGetLocation);
        exitbutton = (Button) findViewById(R.id.btnExit);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        qth = (TextView) findViewById(R.id.qth);
        copy = (ImageButton) findViewById(R.id.copyButton);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        LocationListener mlp = new MyLocationListener();

        if (locationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    MINIMUM_TIME_BETWEEN_UPDATES, MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                    mlp);

        if (locationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MINIMUM_TIME_BETWEEN_UPDATES, MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
                    mlp);

        retrieveLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCurrentLocation();
            }
        });

        exitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
                //finish();
            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gets a handle to the clipboard service.
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                // Creates a new text clip to put on the clipboard
                ClipData clip = ClipData.newPlainText("Qth", qth.getText().toString());
                // Set the clipboard's primary clip.
                clipboard.setPrimaryClip(clip);

                Toast.makeText(MainActivity.this, "Text copied: " + qth.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private String newLineIfLand() {
        return getScreenOrientation() == 1 ? "\n" : "";
    }

    public int getScreenOrientation() {
        Display getOrient = this.getWindowManager().getDefaultDisplay();
        int orientation = Configuration.ORIENTATION_UNDEFINED;
        if (getOrient.getWidth() == getOrient.getHeight()) {
            orientation = Configuration.ORIENTATION_SQUARE;
        } else {
            if (getOrient.getWidth() < getOrient.getHeight()) {
                orientation = Configuration.ORIENTATION_PORTRAIT;
            } else {
                orientation = Configuration.ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }

    protected void showCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_FINE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
//            String position = String.format("Current Location \n Longitude: %1$s \n Latitude: %2$s",
//                    location.getLongitude(), location.getLatitude());
            //Toast.makeText(MainActivity.this, position, Toast.LENGTH_LONG).show();
            String position = String.format("Current Location" + newLineIfLand() + locationFormater,
                    getdms(location.getLatitude(), true), getdms(location.getLongitude(), false));

            String GPSdate = getDateFromGPSasString(location.getTime(), location);
            tvLocation.setText(position + GPSdate);
            qth.setText(returnQth(location.getLatitude(), location.getLongitude()));
        }

    }

    private class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location location) {
            String position = String.format("New Location" + newLineIfLand() + locationFormater,
                    getdms(location.getLatitude(), true), getdms(location.getLongitude(), false));

            String GPSdate = getDateFromGPSasString(location.getTime(), location);
            tvLocation.setText(position + GPSdate);
            qth.setText(returnQth(location.getLatitude(), location.getLongitude()));
        }

        public void onStatusChanged(String s, int i, Bundle b) {
            Toast.makeText(MainActivity.this, "Provider status changed",
                    Toast.LENGTH_SHORT).show();
        }

        public void onProviderDisabled(String s) {
            Toast.makeText(MainActivity.this,
                    "Provider disabled by the user. GPS turned off", Toast.LENGTH_SHORT).show();
        }

        public void onProviderEnabled(String s) {
            Toast.makeText(MainActivity.this,
                    "Provider enabled by the user. GPS turned on", Toast.LENGTH_SHORT).show();
        }

    }

    private String getDateFromGPSasString(long time, Location location) {
        String date = "";
        if (location.getProvider().equals(android.location.LocationManager.GPS_PROVIDER)) {
            //Convert date to GMT
            DateFormat df = DateFormat.getTimeInstance();
            date = df.format(new Date(time));
            df.setTimeZone(TimeZone.getTimeZone("gmt"));
            date = "\nTime GPS: " + date;
        } else {
            date = "\nTime Device (" + location.getProvider() + "): " + new Date(time);
        }
        return date;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(MainActivity.this,
                            "GPS Permission granted", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this,
                            "GPS Permission DENIED", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public String returnQth(double lat, double lng) {
        String qth = "";
        lat += 90;
        lng += 180;
        lat = lat / 10 + 0.0000001;
        lng = lng / 20 + 0.0000001;
        qth += (char) (65 + lng) + "" + (char) (65 + lat);
        lat = 10 * (lat - Math.floor(lat));
        lng = 10 * (lng - Math.floor(lng));
        qth += (char) (48 + lng) + "" + (char) (48 + lat);
        lat = 24 * (lat - Math.floor(lat));
        lng = 24 * (lng - Math.floor(lng));
        qth += (char) (65 + 32 + lng) + "" + (char) (65 + 32 + lat);
        lat = 10 * (lat - Math.floor(lat));
        lng = 10 * (lng - Math.floor(lng));
        qth += (char) (48 + lng) + "" + (char) (48 + lat);
        lat = 24 * (lat - Math.floor(lat));
        lng = 24 * (lng - Math.floor(lng));
        qth += (char) (65 + 32 + lng) + "" + (char) (65 + 32 + lat);
        return qth;
    }


    public String dec2dms(double lat, double lng) {
        String dmslat = getdms(lat, true);
        String dmslng = getdms(lng, false);
        return dmslat + "\n" + dmslng;
    }

    public String getdms(double pos, boolean t) {
        int deg = 0, min = 0;
        double sec = 0;
        String a = t && 0 > pos ? "S" : !t && 0 > pos ? "W" : t ? "N" : "E";
        double d = Math.abs(pos);
        deg = (int) Math.floor(d);
        sec = 3600 * (d - deg);
        min = (int) Math.floor(sec / 60);
        sec = Math.round(1e4 * (sec - 60 * min)) / 1e4;
        return (t == true ? String.format("%4d", deg) : String.format(" %03d", deg)) + "°" + String.format("%02d", min) + "'" + String.format("%05.2f", sec) + "\"" + a;
        //((deg<100&&!t)?"0"+deg:" "+deg)
    }


}
