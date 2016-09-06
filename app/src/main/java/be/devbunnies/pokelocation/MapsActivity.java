package be.devbunnies.pokelocation;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final int CIRCLE_FILL = 0x22FF0000;
    public static final int CIRCLE_STROKE = Color.RED;

    public static final boolean DEBUG = true;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private List<LatLng> locations = new ArrayList<>();

    private List<Circle> circles = new ArrayList<>();
    private List<Marker> markers = new ArrayList<>();




    private List<Marker> pokemon = new ArrayList<>();
    private double nearbyRadius = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
            return;
        } else {
            requestLocation();
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    requestLocation();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void requestLocation() {

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            System.out.println("mGoogleApiClient");
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();

        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add locations or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.addMarker(new MarkerOptions().position(latLng));
                locations.add(latLng);

                try {
                    calculate();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }


        });
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setMyLocationEnabled(true);


    }

    public static Double distanceBetween(LatLng point1, LatLng point2) {
        if (point1 == null || point2 == null) {
            return null;
        }

        return SphericalUtil.computeDistanceBetween(point1, point2);
    }

//
//    MProjection.prototype.fromLatLngToPoint = function(latlng) {
//        var x = (latlng.lng() + 180) / 360 * 256;
//        var y = ((1 - Math.log(Math.tan(latlng.lat() * Math.PI / 180) + 1 / Math.cos(latlng.lat() * Math.PI / 180)) / Math.PI) / 2 * Math.pow(2, 0)) * 256;
//        return new google.maps.Point(x, y);
//    };
//    MProjection.prototype.fromPointToLatLng = function(point) {
//        var lng = point.x / 256 * 360 - 180;
//        var n = Math.PI - 2 * Math.PI * point.y / 256;
//        var lat = (180 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n))));
//        return new google.maps.LatLng(lat, lng);
//    };


    private Point2D.Double fromLatLngToPoint(LatLng latLng) {
        // var x = (latlng.lng()     + 180) / 360 * 256;
        double x = (latLng.longitude + 180) / 360 * 256;
        // var y = ((1 - Math.log(Math.tan(latlng.lat()    * Math.PI / 180) + 1 / Math.cos(latlng.lat()    * Math.PI / 180)) / Math.PI) / 2 * Math.pow(2, 0)) * 256;
        double y = ((1 - Math.log(Math.tan(latLng.latitude * Math.PI / 180) + 1 / Math.cos(latLng.latitude * Math.PI / 180)) / Math.PI) / 2 * Math.pow(2, 0)) * 256;

        return new Point2D.Double(x,y);
    }

    private LatLng fromPointToLatLng(Point2D.Double point) {
        // var lng = point.x / 256 * 360 - 180;
        double lng = point.x / 256 * 360 - 180;
        // var n = Math.PI - 2 * Math.PI * point.y / 256;
        double n = Math.PI - 2 * Math.PI * point.y / 256;
        // var lat = (180 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n))));
        double lat = (180 / Math.PI * Math.atan(0.5 * (Math.exp(n) - Math.exp(-n))));

        return new LatLng(lat, lng);
    }

    public Bitmap getPokeBallMarker() {
        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.raw.pokeball);
        Bitmap b = bitmapdraw.getBitmap();
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);

        return smallMarker;
    }

    private void calculate() throws Exception {

        if (locations.size() == 2) {
            double realDistance = distanceBetween(locations.get(0), locations.get(1));

            // 1: get the projection points of the points

            Point2D.Double p0 = fromLatLngToPoint(locations.get(0));
            Point2D.Double p1 = fromLatLngToPoint(locations.get(1));

            double d = Math.sqrt(Math.pow(p1.x - p0.x, 2) + Math.pow(p1.y - p0.y,2));
            double scale = d / realDistance;

            System.out.println("We track points: ");
            System.out.println(p0);
            System.out.println(p1);
            System.out.println("They lie a distance " + d + " apart.");

            // 2: calculate the mid point
//                     var q = {x: (p0.x + p1.x) / 2.0, y: (p0.y + p1.y) / 2.0};
            Point2D.Double q = new Point2D.Double((p0.x + p1.x)/2.0, (p0.y + p1.y) / 2.0);

            System.out.println("Center " + q.x + ", " + q.y);

            // calculate the distance of q from the center of the circle we are looking for using soscastoa
            // no, solve it using pythagoras!
            double R = nearbyRadius * scale;
            System.out.println("Radius by local scale: " + R);

            // we allow some leeway because of GPS issues
            boolean properRange = false;
            double radiusMultiplier = 1.0;
            double distanceOfQ = 0.0;
            while (!properRange && radiusMultiplier < 1.2) {
                double halfDistance = d / 2.0;
                double pythagorasPart = R*R - halfDistance*halfDistance;
                if (pythagorasPart > 0.0) {
                    distanceOfQ = Math.sqrt(pythagorasPart);
                    System.out.println("Pythagoras distanceOfQ: " + distanceOfQ);
                    properRange = true;
                }
                else {
                    radiusMultiplier *= 1.05;
                    R = nearbyRadius * scale * radiusMultiplier;
                    System.out.println("Increased by local scale: " + R);
                }
            }
            System.out.println("Final distance after multiplier " + radiusMultiplier + ": " + (R / scale));


            // invalid!
            if (!properRange) {
                throw new Exception("Invalid GPS coordinates!\nNo spawn point possible.");
            }

            // calculate the slope of the line
            double mOriginalLine = (p1.y - p0.y) / (p1.x - p0.x);
            double m = -1.0 / mOriginalLine;

            System.out.println("mOriginal: " + mOriginalLine);
            System.out.println("m: " + m);

            // we now calculate the points in the line with slope m going through the origin,
            // that are a distance L from the origin

            // solve the system y = mx, xÂ²+yÂ²=distanceOfQ
            double l = distanceOfQ;
            double absSolution = Math.sqrt(l*l / (1.0 + m*m));
            System.out.println("absSolution: " + absSolution);
            double x0 = absSolution;
            double y0 = m * x0;
            double x1 = (-absSolution);
            double y1 = m * x1;
            System.out.println("First solution: " + x0 + "," + y0);
            System.out.println("Second solution: " + x1 + "," + y1);
            x0 += q.x; y0 += q.y;
            x1 += q.x; y1 += q.y;

            LatLng c0 = fromPointToLatLng(new Point2D.Double(x0, y0));
            LatLng c1 = fromPointToLatLng(new Point2D.Double(x1, y1));

            System.out.println("New point0: " + c0);
            System.out.println("New point1: " + c1);

            if (DEBUG) {
                circles.add(mMap.addCircle(new CircleOptions()
                        .center(new LatLng(c0.latitude, c0.longitude))
                        .radius(nearbyRadius)
                        .fillColor(CIRCLE_FILL)
                        .strokeColor(CIRCLE_STROKE)))
                ;

                circles.add(mMap.addCircle(new CircleOptions()
                        .center(new LatLng(c1.latitude, c1.longitude))
                        .radius(nearbyRadius)
                        .fillColor(CIRCLE_FILL)
                        .strokeColor(CIRCLE_STROKE)))
                ;
            }

            pokemon.add(mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(getPokeBallMarker())).position(new LatLng(c0.latitude, c0.longitude))));
            pokemon.add(mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(getPokeBallMarker())).position(new LatLng(c1.latitude, c1.longitude))));
            return;
        }
        if (locations.size() > 2) {
            try {


                Point2D.Double[] points = new Point2D.Double[locations.size()];

                for (int i = 0; i < locations.size(); i++) {
                    points[i] = fromLatLngToPoint(locations.get(i));
                }

                DecimalFormat format =
                        new DecimalFormat("000.00000000",
                                new DecimalFormatSymbols(Locale.US));

                // fit a circle to the test points
                CircleFitter fitter = new CircleFitter();
                fitter.initialize(points);
                System.out.println("initial circle: "
                        + format.format(fitter.getCenter().x)
                        + " " + format.format(fitter.getCenter().y)
                        + " " + format.format(fitter.getRadius()));

                // minimize the residuals
                int iter = fitter.minimize(100, 0.1, 1.0e-12);
                System.out.println("converged after " + iter + " iterations");
                System.out.println("final circle: "
                        + format.format(fitter.getCenter().x)
                        + " " + format.format(fitter.getCenter().y)
                        + " " + format.format(fitter.getRadius()));


                LatLng c = fromPointToLatLng(fitter.getCenter());


                clearAllPokemon();
                clearAllCircles();

                if (DEBUG) {
                    circles.add(mMap.addCircle(new CircleOptions()
                            .center(new LatLng(c.latitude, c.longitude))
                            .radius(nearbyRadius)
                            .fillColor(CIRCLE_FILL)
                            .strokeColor(CIRCLE_STROKE)));
                }

                pokemon.add(mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(getPokeBallMarker())).position(c)));

            } catch (Exception e) {

            }
        }
    }

    public void clearAllPokemon() {
        for (Marker marker : pokemon) {
            marker.remove();
        }
    }

    public void clearAllCircles() {
        for (Circle circle : circles) {
            circle.remove();
        }
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onConnected(Bundle connectionHint) {
        System.out.println("onConnected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);


        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);


        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        return;


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location
                            .getLongitude()), 14));
        }
    }
}
