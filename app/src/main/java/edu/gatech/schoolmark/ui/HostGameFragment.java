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
import edu.gatech.schoolmark.model.Game;
import edu.gatech.schoolmark.model.SportsLocations;
import edu.gatech.schoolmark.model.User;

public class HostGameFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference currentRef;
    private static final String TAG = "Game";

    private List<String> sportsList;
    private List<SportsLocations> sportsLocationsList;
    private ArrayAdapter<String> sportsLocationsAdapter;
    private ArrayList<String> playersList;

    private TimePicker timePicker;
    private DatePicker datePicker;
    private Spinner locationSpinner;
    private Spinner sportSpinner;
    private String sportSelected;
    private String locationSelected;
    static java.util.Calendar cal = java.util.Calendar.getInstance();
    private RatingBar intensity;
    private NumberPicker numberOfPlayers;
    private CheckBox checkBox;

    private final String sportsListURL = "sportsList/";
    private final String locationListURL = "sportsList/locations/";

    private boolean isHostStudent;

    private View root;

    public HostGameFragment() {
        //required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_host_game, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Button datePickerButton = root.findViewById(R.id.datePickButton);
        Button timePickerButton = root.findViewById(R.id.timePickButton);
        Button hostButton = root.findViewById(R.id.hostGameButton);
        Button hostBackButton = root.findViewById(R.id.hostGameBackButton);
        Button helpButton =  root.findViewById(R.id.helpButton);

        datePickerButton.setOnClickListener(this);
        timePickerButton.setOnClickListener(this);
        hostButton.setOnClickListener(this);
        hostBackButton.setOnClickListener(this);
        helpButton.setOnClickListener(this);

        //timePicker = (TimePicker) findViewById(R.id.timePicker);
        //datePicker = (DatePicker) findViewById(R.id.datePicker);

        sportsLocationsList = new ArrayList<>();
        sportsList = new ArrayList<>();
        playersList = new ArrayList<>();
        locationSpinner = root.findViewById(R.id.locationSpinner);
        intensity = root.findViewById(R.id.intensityBar);
        numberOfPlayers = root.findViewById(R.id.numberOfPlayers);
        checkBox = root.findViewById(R.id.checkBox);
        numberOfPlayers.setMinValue(0);
        numberOfPlayers.setMaxValue(30);

        try {
            currentRef = mDatabase.child(sportsListURL);
            currentRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshotChunk: dataSnapshot.getChildren()) {
                        sportsLocationsList.add(snapshotChunk.getValue(SportsLocations.class));
                    }

                    for (SportsLocations s: sportsLocationsList) {
                        sportsList.add(s.getGame());
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
            Fragment fragment = new GameListFragment();
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
        } else if (v.getId() == R.id.hostGameButton) {
            hostNewGame(v);
        } else if (v.getId() == R.id.hostGameBackButton) {
            cancelGame(v);
        } else if (v.getId() == R.id.helpButton) {
            showHelp(v);
        }
    }

    public void showHelp(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Intensity Meter");
        builder.setMessage("The intensity meter gauges how competitive or intense you expect a game to be. For example, an intensity of 5 would be a game played on a really high level," +
                            " while an intensity of 3 would be a moderately intense game.");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void attachListenerToSpinner() {
        // Log.i(TAG, "Current Size of sportsLocationList: " + sportsLocationsList.size());
        // Log.i(TAG, "Current Size of sportsList: " + sportsList.size());
        // Populate the Sports dropdown with the Sports pulled from the database
        sportSpinner = root.findViewById(R.id.sportSpinner);
        ArrayAdapter<String> sportsAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, sportsList);
        sportsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sportSpinner.setAdapter(sportsAdapter);

        // Add an adapter for locations spinner, put garbage data for now
        //sportsLocationsAdapter = new ArrayAdapter<>(
        //        this, android.R.layout.simple_spinner_item, sportsLocationsList);

        // adds listener so that on ItemSelected is called
        sportSpinner.setOnItemSelectedListener(this);
        locationSpinner.setOnItemSelectedListener(this);

    }



    public void hostNewGame(View view) {
        playersList.add(mAuth.getCurrentUser().getUid());
        Game newGame = new Game();
        newGame.setPlayerUIDList(playersList);
        newGame.setHostUID(mAuth.getCurrentUser().getUid());
        newGame.setSport(sportSelected);
        newGame.setLocationTitle(locationSelected);
        newGame.setIsExclusive(checkBox.isChecked());
        newGame.setIsHostStudent(isHostStudent);

        if (intensity.getRating() > 0) {
            newGame.setIntensity((int) intensity.getRating());
        }
        if (numberOfPlayers.getValue() > 0) {
            newGame.setCapacity(numberOfPlayers.getValue());
        }
        // storing the date from Calendar object
        // MONTH IS STORED FROM 0-11, ADD 1 WHEN CALLED FROM DATABASE
        // WHY NOT JUST ADD 1 HERE DUDE
        // Because the Calendar class itself is what converts 1-12 to 0-11.
        // We are giving inputs from 1-12 already when we select the month from the picker.
        newGame.setTimeOfGame(cal.getTime());

        currentRef = mDatabase.child("gamesList");
        currentRef.push().setValue(newGame);

        Toast.makeText(getActivity(),
                "Your game was hosted!",
                Toast.LENGTH_SHORT).show();

        Fragment fragment = new GameListFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.home_frame, fragment).commit();
    }

    public void cancelGame(View view) {
        Fragment fragment = new GameListFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.home_frame, fragment).commit();
    }


    // These two methods are required for OnItemSelectedListener
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Log.i(TAG, "OnSelectedCalled");
        if (parent.getItemAtPosition(pos) instanceof String
                && parent.getId() == sportSpinner.getId()) {
            String currentItem = (String) parent.getItemAtPosition(pos);
            for (String s: sportsList) {
                Log.i(TAG, "OnItemSelectedSportsList: " + s);
            }
            Log.i(TAG, (String) parent.getItemAtPosition(pos));
            sportSelected = currentItem;
            for (SportsLocations s: sportsLocationsList) {
                Log.i(TAG, "iterating through SportsLocation in onItemSelected: " + s.getGame());
                // SportsLocation equals has been made to accept Strings and SportsLocations
                if (s.equals(currentItem)) {
                    // Populate location spinner based on the sport selected
                    //ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    //        this, android.R.layout.simple_spinner_item, s.getLocations());
                    ArrayAdapter<String> sportsLocationsAdapter = new ArrayAdapter<>(
                            getActivity(), android.R.layout.simple_spinner_item, s.getLocations());
                    sportsLocationsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    locationSpinner.setAdapter(sportsLocationsAdapter);
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

}
