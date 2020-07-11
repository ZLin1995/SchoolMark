package edu.gatech.schoolmark.ui;

import android.app.Activity;
import android.app.Fragment;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import edu.gatech.schoolmark.model.User;
import java.util.HashMap;

import edu.gatech.schoolmark.R;

import static android.content.Context.LOCATION_SERVICE;

public class GameDetailFragment extends Fragment implements OnMapReadyCallback, LocationListener{
    private Activity context;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private View root;
    private MapView mapViewTwo;
    private GoogleMap map;
    private LatLng userLatLng;

    private GoogleApiClient mGoogleApiClient;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_game_detail, container, false);

        TextView listSport = (TextView) root.findViewById(R.id.detailSport);
        TextView listLocation = (TextView) root.findViewById(R.id.detailLocation);
        TextView listTime = (TextView) root.findViewById(R.id.detailTime);
        TextView listDate = (TextView) root.findViewById(R.id.detailDate);
        RatingBar listIntensityBar = (RatingBar) root.findViewById(R.id.listIntensityBar);
        final TextView hostName = (TextView) root.findViewById(R.id.detailHost);
        final TextView hostNumber = (TextView) root.findViewById(R.id.detailHostNum);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        MapsInitializer.initialize(this.getActivity());

        mapViewTwo = (MapView) root.findViewById(R.id.mapView2);
        mapViewTwo.onCreate(savedInstanceState);

        Bundle args = getArguments();
        String sportType = args.getString("sport");
        String location = args.getString("location");
        String time = args.getString("time");
        String date = args.getString("date");
        Float intensity = args.getFloat("intensity");
        String hostID = args.getString("hostID");
        final String gameID = args.getString("gameID");

        listSport.setText(sportType);
        listTime.setText(time);
        listDate.setText(date);
        listLocation.setText(location);
        listIntensityBar.setRating(intensity);

        DatabaseReference hostRef = mDatabase.child("userList").child(hostID);
        hostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User hostUser = dataSnapshot.getValue(User.class);
                hostName.setText("Host: " + hostUser.getDisplayName());
                hostNumber.setText("Contact Number: " + hostUser.getPhoneNumber().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DatabaseReference gamesRef = mDatabase.child("gamesList");
        final HashMap<String, Integer> gameMap = new HashMap<>();;
        gamesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    try {
                        if(child.getKey().equals(gameID)) {
                            String locationTitle = child.child("locationTitle").getValue(String.class);
                            if (locationTitle != null) {
                                Integer locationCount = gameMap.get(locationTitle);
                                if (locationCount == null) {
                                    gameMap.put(locationTitle, 1);
                                }
                            }
                        }

                        LatLng CRCCourts = new LatLng(33.7757, -84.4040);
                        LatLng CRCFields = new LatLng(33.7767, -84.4037);
                        LatLng BurgerBowl = new LatLng(33.7790, -84.4028);
                        LatLng TechGreen = new LatLng(33.7747, -84.3973);
                        LatLng PetersParking = new LatLng(33.7753, -84.3936);
                        LatLng NorthAveGym = new LatLng(33.7700, -84.3911);

                        if (gameMap.get("CRC Fields") != null) {
                            map.addMarker(new MarkerOptions().position(CRCFields).title("CRC Fields").snippet("Games Available"));
                        }

                        if (gameMap.get("CRC 4th floor Courts") != null) {
                            map.addMarker(new MarkerOptions().position(CRCCourts).title("CRC Courts").snippet("Games Available"));
                        }

                        if (gameMap.get("Burger Bowl") != null) {
                            map.addMarker(new MarkerOptions().position(BurgerBowl).title("Burger Bowl").snippet("Games Available"));
                        }

                        if (gameMap.get("Tech Green") != null) {
                            map.addMarker(new MarkerOptions().position(TechGreen).title("Tech Green").snippet("Games Available"));
                        }

                        if (gameMap.get("Peters Parking Deck") != null) {
                            map.addMarker(new MarkerOptions().position(PetersParking).title("Peters Parking Deck").snippet("Games Available"));
                        }

                        if (gameMap.get("North Avenue Gym") != null || gameMap.get("North Ave Courtyard") != null) {
                            map.addMarker(new MarkerOptions().position(NorthAveGym).title("North Avenue Gym/Courtyard").snippet("Games Available"));
                        }

                    } catch (Exception e) {

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mapViewTwo.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                try {
                    map = googleMap;
                    //googleMap.setMyLocationEnabled(true);
                    LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
                    LatLng latLng = new LatLng(33.7762, -84.3981);
                    userLatLng = latLng;
                    //googleMap.addMarker(new MarkerOptions().position(userLatLng));
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
                } catch (SecurityException e) {
                    System.out.println(e.toString());

                }

                mapViewTwo.onResume();
            }});

        if (mGoogleApiClient == null) {

        }

        return root;
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

}
