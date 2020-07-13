package edu.gatech.schoolmark.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import edu.gatech.schoolmark.R;
import edu.gatech.schoolmark.model.Game;

public class EventListAdapter extends ArrayAdapter<Game> {
    static java.util.Calendar cal = java.util.Calendar.getInstance();
    private Activity context;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference currentRef;
    private DataSnapshot gamesList;
    private List<Game> gameList;



     public EventListAdapter(Activity context, List<Game> gameList, DataSnapshot gamesList) {
        super(context, R.layout.join_event_list_layout, gameList);
        this.context = context;
        this.gameList = gameList;
        this.gamesList = gamesList;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        mAuth = FirebaseAuth.getInstance();
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.join_event_list_layout, null, true);
        TextView listSport = listViewItem.findViewById(R.id.listSport);
        TextView listLocation = listViewItem.findViewById(R.id.listLocation);
        TextView listTime = listViewItem.findViewById(R.id.listTime);
        TextView listDate = listViewItem.findViewById(R.id.listDate);

        Button joinGame = listViewItem.findViewById(R.id.joinGame);

        final java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
        final java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getContext());
        final Game game = gameList.get(position);
        String gameKey = "";

        cal.setTime(game.getTimeOfGame());
        listSport.setText(game.getSport());
        listTime.setText(timeFormat.format(game.getTimeOfGame()));
        listDate.setText(dateFormat.format(game.getTimeOfGame()));
        listLocation.setText(game.getLocationTitle());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentRef = mDatabase.child("gamesList");

        //final String game_key = mDatabase.child("gamesList").push().getKey();
        //currentRef.child(gameKey).removeValue();
        //System.out.print("Game selected: " + gameKey);


        for(DataSnapshot gameSnapshot: gamesList.getChildren()) {
            Game g = gameSnapshot.getValue(Game.class);
            if (g == null) { break; }
            if (g.equals(game)) {
                gameKey = gameSnapshot.getKey();
            }
        }

        final String game_key = gameKey;

        joinGame.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 List<String> editedList = game.getPlayerUIDList();
                 editedList.add(mAuth.getCurrentUser().getUid());
                 game.setPlayerUIDList((ArrayList<String>) editedList);
                 mDatabase.child("gamesList").child(game_key).child("playerUIDList").setValue(editedList);
                 String toastText = "You have successfully joined the " + game.getSport() + " game on " + game.getTimeOfGame().toString().substring(0, 10);
                 Toast temp = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
                 temp.setGravity(Gravity.CENTER,0,0);
                 temp.show();
             }
         });

        listViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("sport", game.getSport());
                bundle.putString("location", game.getLocationTitle());
                bundle.putString("time", timeFormat.format(game.getTimeOfGame()));
                bundle.putString("date", dateFormat.format(game.getTimeOfGame()));
                bundle.putFloat("intensity", game.getIntensity());
                bundle.putString("hostID", game.getHostUID());
                bundle.putString("gameID", game_key);
                Fragment fragment = new EventDetailFragment();
                fragment.setArguments(bundle);
                FragmentManager fm = ((Activity)context).getFragmentManager();
                fm.beginTransaction().replace(R.id.home_frame, fragment).commit();
            }
        });

        return listViewItem;
    }
}