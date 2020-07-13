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

public class GameDetailFragment extends Fragment implements  LocationListener{
    private Activity context;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private View root;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_game_detail, container, false);

        TextView listSport = root.findViewById(R.id.detailSport);
        TextView listLocation = root.findViewById(R.id.detailLocation);
        TextView listTime = root.findViewById(R.id.detailTime);
        TextView listDate = root.findViewById(R.id.detailDate);
        final TextView hostName = root.findViewById(R.id.detailHost);
        final TextView hostNumber = root.findViewById(R.id.detailHostNum);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Bundle args = getArguments();
        String sportType = args.getString("sport");
        String location = args.getString("location");
        String time = args.getString("time");
        String date = args.getString("date");
        String hostID = args.getString("hostID");
        final String gameID = args.getString("gameID");

        listSport.setText(sportType);
        listTime.setText(time);
        listDate.setText(date);
        listLocation.setText(location);

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

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
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

}
