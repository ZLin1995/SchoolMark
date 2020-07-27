package edu.gatech.schoolmark.ui;

import android.app.Fragment;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import edu.gatech.schoolmark.model.User;

import edu.gatech.schoolmark.R;

public class EventDetailFragment extends Fragment implements  LocationListener{
    private DatabaseReference mDatabase;
    private View root;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_event_detail, container, false);

        TextView listEventName = root.findViewById(R.id.detailEventName);
        TextView listEventType = root.findViewById(R.id.detailEventType);
        TextView listLocation = root.findViewById(R.id.detailLocation);
        TextView listTime = root.findViewById(R.id.detailTime);
        TextView listDate = root.findViewById(R.id.detailDate);
        final TextView hostName = root.findViewById(R.id.detailHost);
        final TextView hostNumber = root.findViewById(R.id.detailHostNum);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Bundle args = getArguments();
        String eventName = args.getString("eventName");
        String eventType = args.getString("eventType");
        String location = args.getString("location");
        String time = args.getString("time");
        String date = args.getString("date");
        String hostID = args.getString("hostID");

        listEventName.setText(eventName);
        listEventType.setText(eventType);
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
