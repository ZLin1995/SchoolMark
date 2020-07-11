package edu.gatech.schoolmark.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

/**
 * Created by pranshav on 11/3/2017.
 */

public class HomeScreenFragment extends Fragment implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private MapView mapView;
    private GoogleMap map;
    private LatLng userLatLng;

    private GoogleApiClient mGoogleApiClient;

    private View root;

    public HomeScreenFragment() {
        //required empty constructor for fragment subclasses
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);

        Button hostGame = (Button) root.findViewById(R.id.hostGame);
        Button joinGame = (Button) root.findViewById(R.id.joinGame);
        Button myGames = (Button) root.findViewById(R.id.myGames);
        hostGame.setOnClickListener(this);
        joinGame.setOnClickListener(this);
        myGames.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        MapsInitializer.initialize(this.getActivity());

        mapView = (MapView) root.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        DatabaseReference gamesRef = mDatabase.child("gamesList");
        final HashMap<String, Integer> gameMap = new HashMap<>();;
        gamesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    try {
                        String locationTitle = child.child("locationTitle").getValue(String.class);
                        if (locationTitle != null) {
                            Integer locationCount = gameMap.get(locationTitle);
                            if (locationCount == null) {
                                gameMap.put(locationTitle, 1);
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

        Log.e("TEST:", gameMap.toString());

        mapView.getMapAsync(new OnMapReadyCallback() {
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

                mapView.onResume();
            }});

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        DatabaseReference userRef = mDatabase.child("userList").child(mAuth.getCurrentUser().getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                TextView t = (TextView) root.findViewById(R.id.toBe);

                if (currentUser != null) {
                    String tempText = currentUser.getDisplayName();
                    t.setText("Welcome, " + tempText);
                } else {
                    t.setText("Welcome");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return root;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.hostGame){
            Fragment fragment = new HostGameFragment();
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.home_frame, fragment).commit();

        } else if (v.getId() == R.id.joinGame) {
            Fragment fragment = new JoinGameFragment();
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.home_frame, fragment).commit();

        } else if (v.getId() == R.id.myGames) {
            Fragment fragment = new MyGamesFragment();
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.home_frame, fragment).commit();
        }
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

    //required methods for interfaces

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

}
