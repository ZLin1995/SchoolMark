package edu.gatech.schoolmark.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

import edu.gatech.schoolmark.R;
import edu.gatech.schoolmark.model.Event;
import edu.gatech.schoolmark.model.EventsLocations;
import edu.gatech.schoolmark.model.User;


public class EventListFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private FirebaseAuth mAuth;
    private DatabaseReference currentRef;
    private DatabaseReference mDatabase;


    private final String typesListURL = "typesList/";
    private final String eventsListURL = "eventsList/";


    private final String spSel = "Any Sport";
    private final String loSel = "Any Location";

    ListView listViewEvent;
    List<Event> eventList;
    String userUID;

    Spinner eventSpinner;
    Spinner locationSpinner;
    FloatingActionButton hostEvent;

    List<String> event;
    List<String> location;
    List<EventsLocations> EventTypeLocations;

    String spSelected;
    String loSelected;


    boolean isStudent;

    private View root;

    public EventListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_join_event, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentRef = FirebaseDatabase.getInstance().getReference(eventsListURL);
        listViewEvent = (ListView) root.findViewById(R.id.listViewGame);
        userUID = mAuth.getCurrentUser().getUid();
        eventList = new ArrayList<>();

        eventSpinner = (Spinner) root.findViewById(R.id.sport_spinner);
        locationSpinner = (Spinner) root.findViewById(R.id.location_spinner);

        event = new ArrayList<String>();
        location = new ArrayList<String>();
        EventTypeLocations = new ArrayList<EventsLocations>();

        hostEvent = root.findViewById(R.id.createEvent);
        hostEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new HostEventFragment();
                FragmentManager fm = getFragmentManager();
                fm.beginTransaction().replace(R.id.home_frame, fragment).addToBackStack( "tag" ).commit();
            }
        });
        spSelected = spSel;
        loSelected = loSel;


        event.add(spSel);
        location.add(loSel);

        DatabaseReference userRef = mDatabase.child("userList").child(mAuth.getCurrentUser().getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null) {
                    if (currentUser.getIsStudent()) {
                        isStudent = true;
                    } else {
                        isStudent = false;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        try {
            currentRef = mDatabase.child(typesListURL);
            currentRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshotChunk: dataSnapshot.getChildren()) {
                        EventTypeLocations
                                .add(snapshotChunk.getValue(EventsLocations.class));
                    }

                    // This Adds all possible sports to the event list.
                    for (EventsLocations s: EventTypeLocations) {
                        event.add(s.getEvent());
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });

        } catch (NullPointerException ex) {
            Fragment fragment = new EventListFragment();
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.home_frame, fragment).commit();
        }

        ArrayAdapter<String> sportAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, event);
        sportAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventSpinner.setAdapter(sportAdapter);

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, location);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);

        eventSpinner.setOnItemSelectedListener(this);
        locationSpinner.setOnItemSelectedListener(this);

        return root;
    }



    @Override
    public void onStart() {
        super.onStart();
        currentRef = mDatabase.child(eventsListURL);
        currentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventList.clear();
                for(DataSnapshot gameSnapshot: dataSnapshot.getChildren()) {
                    Event event = gameSnapshot.getValue(Event.class);
                    if ((fitsFilter(event)) && (!userUID.equals(event.getHostUID()))
                            && (!(event.getParticipantUIDList().contains(userUID)))
                            && event.getCapacity() > event.getParticipantUIDList().size()
                            && !(event.getIsExclusive() && isStudent != event.getIsHostStudent())) {
                        eventList.add(event);
                    }

                }
                if (getActivity()!=null) {
                    EventListAdapter adapter = new EventListAdapter(getActivity(), eventList, dataSnapshot);
                    listViewEvent.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void hostGame() {
        Fragment fragment = new HostEventFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.home_frame, fragment).addToBackStack( "tag" ).commit();
    }

    private void homeScreen() {
        Fragment fragment = new EventListFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.home_frame, fragment).addToBackStack( "tag" ).commit();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getItemAtPosition(position) instanceof String){
            String temp = (String) parent.getItemAtPosition(position);
            // clear the list back to default, if a event is selected then populate it

            if (parent.getId() == eventSpinner.getId()) {
                location.clear();
                location.add(loSel);
                // If they selected a event, then fill that spinner with a list of valid locations
                for (EventsLocations s: EventTypeLocations) {
                    // Log.v(TAG, "TEMP: " + temp + " SportsLocations: " + s.toString() + " comparison: " + (s.equals(temp)));
                    if (s.equals(temp)) {
                        location.addAll(s.getLocations());
                    }
                }

                spSelected = temp;
            }
            if (parent.getId() == locationSpinner.getId()) {
                loSelected = temp;
            }
        }

        onStart();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        spSelected = spSel;
        loSelected = loSel;
    }


    private boolean fitsFilter(Event g) {
        if ((g.getEvent().equals(spSelected) || spSelected.equals(spSel))
                && (g.getLocationTitle().equals(loSelected) || loSelected.equals(loSel))) {
            return true;
        }

        return false;
    }
}
