package edu.gatech.schoolmark.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import edu.gatech.schoolmark.R;
import edu.gatech.schoolmark.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyEventFragment extends Fragment { // AppCompatActivity
    private FirebaseAuth mAuth;
    private DatabaseReference currentRef;

    FloatingActionButton hostEvent;

    ListView listViewGame;
    List<Event> eventList;
    Event selectedEvent;
    String userUID;
    Map<Integer, String> viewTohostUID;

    private View root;

    public MyEventFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_my_event, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentRef = FirebaseDatabase.getInstance().getReference("eventsList");
        listViewGame = root.findViewById(R.id.listViewGame);
        userUID = mAuth.getCurrentUser().getUid();
        eventList = new ArrayList<>();

        hostEvent = root.findViewById(R.id.createEvent);
        hostEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new HostEventFragment();
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.home_frame, fragment).addToBackStack( "tag" ).commit();
            }
        });

        return root;
    }


    @Override
    public void onStart() {
        super.onStart();

        currentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String gameKey = dataSnapshot.get
                eventList.clear();
                for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                    Event event = gameSnapshot.getValue(Event.class);
                    //Log.i(TAG, gam)
                    if (event.getPlayerUIDList().contains(userUID)) {
                        eventList.add(event);
                    }
                }
                if (getActivity()!=null) {
                    myEventListAdapter adapter = new myEventListAdapter(getActivity(), eventList, dataSnapshot);
                    listViewGame.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}