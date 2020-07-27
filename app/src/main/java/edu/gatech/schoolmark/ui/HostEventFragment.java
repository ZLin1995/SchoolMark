package edu.gatech.schoolmark.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.gatech.schoolmark.R;
import edu.gatech.schoolmark.model.Event;
import edu.gatech.schoolmark.model.EventsLocations;
import edu.gatech.schoolmark.model.User;


public class HostEventFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference currentRef;
    private static final String TAG = "Event";

    private List<String> eventsList;
    private List<EventsLocations> eventsLocationsList;
    private ArrayAdapter<String> eventsLocationsAdapter;
    private ArrayList<String> playersList;

    private TimePicker timePicker;
    private DatePicker datePicker;
    private Spinner locationSpinner;
    private Spinner eventSpinner;
    private String eventSelected;
    private String locationSelected;
    static java.util.Calendar cal = java.util.Calendar.getInstance();
    private RatingBar intensity;
    private NumberPicker numberOfPlayers;
    private CheckBox checkBox;
    private EditText eventName;
    private EditText eventDescription;

    private final String eventsListURL = "eventsList/";
    private final String typesListURL = "typesList/";
    private final String locationListURL = "typesList/locations/";

    private boolean isHostStudent;

    private View root;

    public HostEventFragment() {
        //required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_host_event, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Button datePickerButton = (Button) root.findViewById(R.id.datePickButton);
        Button timePickerButton = (Button) root.findViewById(R.id.timePickButton);
        Button hostButton = (Button) root.findViewById(R.id.hostEventButton);
        Button hostBackButton = (Button) root.findViewById(R.id.hostEventBackButton);

        datePickerButton.setOnClickListener(this);
        timePickerButton.setOnClickListener(this);
        hostButton.setOnClickListener(this);
        hostBackButton.setOnClickListener(this);

        eventsLocationsList = new ArrayList<>();
        eventsList = new ArrayList<>();
        playersList = new ArrayList<>();
        locationSpinner = (Spinner) root.findViewById(R.id.locationSpinner);
        numberOfPlayers = (NumberPicker) root.findViewById(R.id.maxCapacity);
        checkBox = (CheckBox) root.findViewById(R.id.checkBox);
        numberOfPlayers.setMinValue(0);
        numberOfPlayers.setMaxValue(30);

        eventName = root.findViewById(R.id.eventName);
        eventDescription = root.findViewById(R.id.eventDescription);

        try {
            currentRef = mDatabase.child(typesListURL);
            currentRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshotChunk: dataSnapshot.getChildren()) {
                        eventsLocationsList.add(snapshotChunk.getValue(EventsLocations.class));
                    }

                    for (EventsLocations s: eventsLocationsList) {
                        eventsList.add(s.getEvent());
                    }
                    attachListenerToSpinner();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, databaseError.getMessage());
                }
            });

            DatabaseReference userRef = mDatabase.child("userList").child(mAuth.getCurrentUser().getUid());
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User currentUser = dataSnapshot.getValue(User.class);

                    if (currentUser != null) {
                        if (currentUser.getIsStudent()) {
                            checkBox.setText("Students Only");
                            isHostStudent = true;
                        } else {
                            checkBox.setText("Faculty Only");
                            isHostStudent = false;
                        }
                    } else {
                        checkBox.setVisibility(View.INVISIBLE);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        } catch (NullPointerException ex) {
            Log.e(TAG, "database reference retrieved was null");
            Fragment fragment = new EventListFragment();
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.home_frame, fragment).commit();
        }

        return root;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.datePickButton) {
            showDatePicker(v);
        } else if (v.getId() == R.id.timePickButton) {
            showTimePicker(v);
        } else if (v.getId() == R.id.hostEventButton) {
            hostNewGame(v);
        } else if (v.getId() == R.id.hostEventBackButton) {
            cancelGame(v);
        }
    }

    private void attachListenerToSpinner() {
         Log.i(TAG, "Current Size of sportsLocationList: " + eventsLocationsList.size());
         Log.i(TAG, "Current Size of sportsList: " + eventsList.size());
        // Populate the Sports dropdown with the Sports pulled from the database
        eventSpinner = (Spinner) root.findViewById(R.id.eventSpinner);
        ArrayAdapter<String> eventsAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, eventsList);
        eventsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        eventSpinner.setAdapter(eventsAdapter);
        eventSpinner.setOnItemSelectedListener(this);
        locationSpinner.setOnItemSelectedListener(this);

    }

    public void hostNewGame(View view) {
        playersList.add(mAuth.getCurrentUser().getUid());
        Event newGame = new Event();
        newGame.setPlayerUIDList(playersList);
        newGame.setHostUID(mAuth.getCurrentUser().getUid());
        newGame.setEvent(eventSelected);
        newGame.setLocationTitle(locationSelected);
        newGame.setIsExclusive(checkBox.isChecked());
        newGame.setIsHostStudent(isHostStudent);

        if (numberOfPlayers.getValue() > 0) {
            newGame.setCapacity(numberOfPlayers.getValue());
        }
        newGame.setTimeOfEvent(cal.getTime());
        newGame.setEventName(eventName.getText().toString());
        newGame.setDescription(eventDescription.getText().toString());

        currentRef = mDatabase.child("eventsList");
        currentRef.push().setValue(newGame);

        Toast.makeText(getActivity(),
                "Your game was hosted!",
                Toast.LENGTH_SHORT).show();

        Fragment fragment = new EventListFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.home_frame, fragment).commit();
    }

    public void cancelGame(View view) {
        Fragment fragment = new EventListFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.home_frame, fragment).commit();
    }


    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (parent.getItemAtPosition(pos) instanceof String
                && parent.getId() == eventSpinner.getId()) {
            String currentItem = (String) parent.getItemAtPosition(pos);
            Log.i(TAG, (String) parent.getItemAtPosition(pos));
            eventSelected = currentItem;
            for (EventsLocations s: eventsLocationsList) {

                if (s.equals(currentItem)) {
                    ArrayAdapter<String> eventsLocationsAdapter = new ArrayAdapter<>(
                            getActivity(), android.R.layout.simple_spinner_item, s.getLocations());
                    eventsLocationsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    locationSpinner.setAdapter(eventsLocationsAdapter);
                    locationSelected = (String) locationSpinner.getSelectedItem();
                }
            }
        }

        if (parent.getItemAtPosition(pos) instanceof String
                && parent.getId() == locationSpinner.getId()) {
            locationSelected = (String) parent.getItemAtPosition(pos);
        }


    }

    public void onNothingSelected(AdapterView<?> parent) {

    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            final java.util.Calendar c = java.util.Calendar.getInstance();
//            int hour = c.get(java.util.Calendar.HOUR_OF_DAY);
//            int minute = c.get(java.util.Calendar.MINUTE);
            int hour = cal.get(java.util.Calendar.HOUR_OF_DAY);
            int minute = cal.get(java.util.Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            java.util.Calendar rightNow = java.util.Calendar.getInstance();
            java.util.Calendar userInput = java.util.Calendar.getInstance();

            userInput.set(java.util.Calendar.YEAR, cal.get(java.util.Calendar.YEAR));
            userInput.set(java.util.Calendar.MONTH, cal.get(java.util.Calendar.MONTH));
            userInput.set(java.util.Calendar.DAY_OF_MONTH, cal.get(java.util.Calendar.DAY_OF_MONTH));
            userInput.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
            userInput.set(java.util.Calendar.MINUTE, minute);

            if (!userInput.after(rightNow)){
                CharSequence text = "Please select a time in the future.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(this.getActivity().getApplicationContext(), text,
                        duration);
                toast.show();

                // Set button text to invalid selection
                Button timePickButton = (Button)this.getActivity().findViewById(R.id.timePickButton);
                timePickButton.setText("Pick a time");

            } else {
                cal.set(java.util.Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(java.util.Calendar.MINUTE, minute);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);

                // Set button text to selected time
                Button timePickButton = (Button)this.getActivity().findViewById(R.id.timePickButton);
                SimpleDateFormat fmt = new SimpleDateFormat("hh:mm aa", Locale.US);
                String dateString = fmt.format(userInput.getTime());
                timePickButton.setText(dateString);
            }
        }
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int year = cal.get(java.util.Calendar.YEAR);
            int month = cal.get(java.util.Calendar.MONTH);
            int day = cal.get(java.util.Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            java.util.Calendar rightNow = java.util.Calendar.getInstance();
            java.util.Calendar userInput = java.util.Calendar.getInstance();

            userInput.set(java.util.Calendar.YEAR, year);
            userInput.set(java.util.Calendar.MONTH, month);
            userInput.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth);

            if (userInput.compareTo(rightNow) == -1) {
                CharSequence text = "Please select a date in the future.";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(this.getActivity().getApplicationContext(), text,
                        duration);
                toast.show();

                // Set button text to invalid selection
                Button timePickButton = (Button)this.getActivity().findViewById(R.id.timePickButton);
                timePickButton.setText("Pick a date");

            } else {
                cal.set(java.util.Calendar.YEAR, year);
                cal.set(java.util.Calendar.MONTH, month);
                cal.set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth);

                // Set button text to selected date
                Button datePickButton = (Button)this.getActivity().findViewById(R.id.datePickButton);
                SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                String dateString = fmt.format(userInput.getTime());
                datePickButton.setText(dateString);
            }
        }
    }

    public void showTimePicker(View v) {
        DialogFragment fragment = new TimePickerFragment();
        fragment.show(getFragmentManager(), "timePicker");
    }

    public void showDatePicker(View v) {
        DialogFragment fragment = new DatePickerFragment();
        fragment.show(getFragmentManager(), "datePicker");
    }


    public void populateDatabase() {
        List<String> listOfLocations = new ArrayList<>();
        EventsLocations test;

        //currentRef = mDatabase.child("sportsList");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentRef = mDatabase.child("typesList/");


        listOfLocations.add("Memorial Library");
        listOfLocations.add("Student Center");
        listOfLocations.add("Clough Commons");
        listOfLocations.add("College of Computing");
        listOfLocations.add("Rich Computer Center");
        listOfLocations.add("Ferst Center");
        listOfLocations.add("Centergy One");
        listOfLocations.add("Global Learning Center");
        test = new EventsLocations("StudySession", listOfLocations);
        //currentRef.setValue(test);
        currentRef.push().setValue(test);

        // ===========


        listOfLocations = new ArrayList<>();
        listOfLocations.add("Memorial Library");
        listOfLocations.add("Student Center");
        listOfLocations.add("Clough Commons");
        listOfLocations.add("College of Computing");
        listOfLocations.add("Ferst Center");
        listOfLocations.add("Centergy One");
        listOfLocations.add("Global Learning Center");
        listOfLocations.add("Crosland Tower");
        listOfLocations.add("Holland Building");
        listOfLocations.add("College of Business");
        test = new EventsLocations("PublicSpeaking", listOfLocations);
        currentRef.push().setValue(test);
        // ===========


        listOfLocations = new ArrayList<>();
        listOfLocations.add("Tech Green");
        listOfLocations.add("Student Center");
        listOfLocations.add("Ferst Center");
        listOfLocations.add("Boggs Building");
        listOfLocations.add("Klaus Building");
        listOfLocations.add("Whitaker Building");
        listOfLocations.add("Centergy One");
        listOfLocations.add("Global Learning Center");
        test = new EventsLocations("ClubMeeting", listOfLocations);
        currentRef.push().setValue(test);
        // ===========

    }

}