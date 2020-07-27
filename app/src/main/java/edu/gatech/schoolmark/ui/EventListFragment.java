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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.gatech.schoolmark.R;
import edu.gatech.schoolmark.model.Event;
import edu.gatech.schoolmark.model.EventsLocations;
import edu.gatech.schoolmark.model.User;


public class EventListFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private FirebaseAuth mAuth;
    private DatabaseReference currentRef;
    private DatabaseReference mDatabase;


    private static final String TAG = "Join Activity";

    private final String typesListURL = "typesList/";
    private final String eventsListURL = "eventsList/";


    private final String spSel = "Any Sport";
    private final String loSel = "Any Location";
    private final String inSel = "Any Intensity";
    private final String plSel = "Any Player";


    private boolean eventsExist;

    ExpandableListView filterBy;
    ListView listViewEvent;
    List<Event> eventList;
    String userUID;
    Map<Integer, String> viewTohostUID;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;

    Spinner eventSpinner;
    Spinner locationSpinner;
    Spinner playerSpinner;
    Spinner capacitySpinner;
    FloatingActionButton hostEvent;

    List<String> event;
    List<String> location;
    List<String> player;
    List<String> intensity;
    List<EventsLocations> EventTypeLocations;

    String spSelected;
    String loSelected;
    String plSelected;
    String inSelected;

    boolean isExclusive;
    boolean isStudent;

    private View root;

    public EventListFragment() {
        //required empty constructor
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
        playerSpinner = (Spinner) root.findViewById(R.id.player_spinner);
        capacitySpinner = (Spinner) root.findViewById(R.id.capacity_spinner);

        event = new ArrayList<String>();
        location = new ArrayList<String>();
        player = new ArrayList<String>();
        intensity = new ArrayList<String>();
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

        //populating the spinners
        spSelected = spSel;
        loSelected = loSel;
        inSelected = inSel;
        plSelected = plSel;


        event.add(spSel);
        location.add(loSel);
        player.add(plSel);
        intensity.add(inSel);

        DatabaseReference userRef = mDatabase.child("userList").child(mAuth.getCurrentUser().getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                if (currentUser != null) {
                    if (currentUser.getIsStudent()) {
                        player.add("Students Only");
                        isStudent = true;
                    } else {
                        player.add("Faculty Only");
                        isStudent = false;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        intensity.add("10");
        intensity.add("20");
        intensity.add("30");
        intensity.add("40");
        intensity.add("50");

        // Add sports and locations to the spinners
        // Get Sports List and Locations List from the Database
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
                    Log.e(TAG, databaseError.getMessage());
                }
            });

        } catch (NullPointerException ex) {
            Log.e(TAG, "database reference retrieved was null");
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

        ArrayAdapter<String> playerAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, player);
        playerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playerSpinner.setAdapter(playerAdapter);


        ArrayAdapter<String> intensityAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, intensity);
        intensityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        capacitySpinner.setAdapter(intensityAdapter);


        eventSpinner.setOnItemSelectedListener(this);
        capacitySpinner.setOnItemSelectedListener(this);
        locationSpinner.setOnItemSelectedListener(this);
        playerSpinner.setOnItemSelectedListener(this);

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
                // Need this to properly prompt the user when there are legit no games.
                eventsExist = dataSnapshot.exists();
                for(DataSnapshot gameSnapshot: dataSnapshot.getChildren()) {
                    Event event = gameSnapshot.getValue(Event.class);
                    if ((fitsFilter(event)) && (!userUID.equals(event.getHostUID()))
                            && (!(event.getPlayerUIDList().contains(userUID)))
                            && event.getCapacity() > event.getPlayerUIDList().size()
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
            if (parent.getId() == playerSpinner.getId()) {
                plSelected = temp;
            }
            if (parent.getId() == capacitySpinner.getId()) {
                inSelected = temp;
                isExclusive = !temp.equals(plSel);
            }

        }

        // Refreshes the listView, without this call the filters don't change anything
        onStart();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        spSelected = spSel;
        loSelected = loSel;
        inSelected = inSel;
        plSelected = plSel;
    }


    private boolean fitsFilter(Event g) {
        if ((g.getEvent().equals(spSelected) || spSelected.equals(spSel))
                && (g.getLocationTitle().equals(loSelected) || loSelected.equals(loSel))) {
            if ((isExclusive && (isStudent == g.getIsHostStudent())) || plSelected.equals(plSel)) {
                return true;
            }
        }

        return false;
    }
}
