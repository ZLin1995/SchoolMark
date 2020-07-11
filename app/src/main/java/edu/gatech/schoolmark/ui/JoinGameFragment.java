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
import edu.gatech.schoolmark.model.Game;
import edu.gatech.schoolmark.model.SportsLocations;
import edu.gatech.schoolmark.model.User;


public class JoinGameFragment extends Fragment implements AdapterView.OnItemSelectedListener {
    private FirebaseAuth mAuth;
    private DatabaseReference currentRef;
    private DatabaseReference mDatabase;


    private static final String TAG = "Join Activity";

    private final String sportsListURL = "sportsList/";
    private final String gamesListURL = "gamesList/";


    private final String spSel = "Any Sport";
    private final String loSel = "Any Location";
    private final String inSel = "Any Intensity";
    private final String plSel = "Any Player";


    private boolean gamesExist;

    ExpandableListView filterBy;
    ListView listViewGame;
    List<Game> gameList;
    String userUID;
    Map<Integer, String> viewTohostUID;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listDataChild;

    Spinner sportSpinner;
    Spinner locationSpinner;
    Spinner playerSpinner;
    Spinner intensitySpinner;


    List<String> sport;
    List<String> location;
    List<String> player;
    List<String> intensity;
    List<SportsLocations> lSportsLocations;

    String spSelected;
    String loSelected;
    String plSelected;
    String inSelected;

    boolean isExclusive;
    boolean isStudent;

    private View root;

    public JoinGameFragment() {
        //required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_join_game, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentRef = FirebaseDatabase.getInstance().getReference(gamesListURL);
        listViewGame = (ListView) root.findViewById(R.id.listViewGame);
        userUID = mAuth.getCurrentUser().getUid();
        gameList = new ArrayList<>();

        sportSpinner = (Spinner) root.findViewById(R.id.sport_spinner);
        locationSpinner = (Spinner) root.findViewById(R.id.location_spinner);
        playerSpinner = (Spinner) root.findViewById(R.id.player_spinner);
        intensitySpinner = (Spinner) root.findViewById(R.id.intensity_spinner);

        sport = new ArrayList<String>();
        location = new ArrayList<String>();
        player = new ArrayList<String>();
        intensity = new ArrayList<String>();
        lSportsLocations = new ArrayList<SportsLocations>();

        //populating the spinners
        spSelected = spSel;
        loSelected = loSel;
        inSelected = inSel;
        plSelected = plSel;


        sport.add(spSel);
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

        intensity.add("1");
        intensity.add("2");
        intensity.add("3");
        intensity.add("4");
        intensity.add("5");

        // Add sports and locations to the spinners
        // Get Sports List and Locations List from the Database
        try {
            currentRef = mDatabase.child(sportsListURL);
            currentRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshotChunk: dataSnapshot.getChildren()) {
                        lSportsLocations
                                .add(snapshotChunk.getValue(SportsLocations.class));
                    }

                    // This Adds all possible sports to the sport list.
                    for (SportsLocations s: lSportsLocations) {
                        sport.add(s.getGame());
                    }

                }



                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, databaseError.getMessage());
                }
            });

        } catch (NullPointerException ex) {
            Log.e(TAG, "database reference retrieved was null");
            Fragment fragment = new HomeScreenFragment();
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.home_frame, fragment).commit();
        }

        ArrayAdapter<String> sportAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, sport);
        sportAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sportSpinner.setAdapter(sportAdapter);

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
        intensitySpinner.setAdapter(intensityAdapter);


        sportSpinner.setOnItemSelectedListener(this);
        intensitySpinner.setOnItemSelectedListener(this);
        locationSpinner.setOnItemSelectedListener(this);
        playerSpinner.setOnItemSelectedListener(this);

        return root;
    }



    @Override
    public void onStart() {
        super.onStart();
        currentRef = mDatabase.child(gamesListURL);
        currentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                gameList.clear();
                // Need this to properly prompt the user when there are legit no games.
                gamesExist = dataSnapshot.exists();
                for(DataSnapshot gameSnapshot: dataSnapshot.getChildren()) {
                    Game game = gameSnapshot.getValue(Game.class);
                    if ((fitsFilter(game)) && (!userUID.equals(game.getHostUID()))
                            && (!(game.getPlayerUIDList().contains(userUID)))
                            && game.getCapacity() > game.getPlayerUIDList().size()
                            && !(game.getIsExclusive() && isStudent != game.getIsHostStudent())) {
                        gameList.add(game);
                    }

                }
                if (getActivity()!=null) {
                    JoinGameListAdapter adapter = new JoinGameListAdapter(getActivity(), gameList, dataSnapshot);
                    listViewGame.setAdapter(adapter);
                }
                isGameListEmpty();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void isGameListEmpty() {
        if (!gamesExist) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(getActivity());
            // AlertDialog alert = builder.create();
            builder.setTitle("There aren't any games available right now")
                    .setMessage("Would you like to host your own?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // go host a game
                            hostGame();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // go back to home screen
                            homeScreen();
                        }
                    })
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .show();
        }
    }

    private void hostGame() {
        Fragment fragment = new HostGameFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.home_frame, fragment).commit();
    }

    private void homeScreen() {
        Fragment fragment = new HomeScreenFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.home_frame, fragment).commit();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getItemAtPosition(position) instanceof String){
            String temp = (String) parent.getItemAtPosition(position);
            // clear the list back to default, if a sport is selected then populate it

            if (parent.getId() == sportSpinner.getId()) {
                location.clear();
                location.add(loSel);
                // If they selected a sport, then fill that spinner with a list of valid locations
                for (SportsLocations s: lSportsLocations) {
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
            if (parent.getId() == intensitySpinner.getId()) {
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


    private boolean fitsFilter(Game g) {
        if ((g.getSport().equals(spSelected) || spSelected.equals(spSel))
            && (g.getLocationTitle().equals(loSelected) || loSelected.equals(loSel))
            && (inSelected.equals(Integer.toString(g.getIntensity())) || inSelected.equals(inSel))) {
            if ((isExclusive && (isStudent == g.getIsHostStudent())) || plSelected.equals(plSel)) {
                return true;
            }
        }
        
        return false;
    }
}
