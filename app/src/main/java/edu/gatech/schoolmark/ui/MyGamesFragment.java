package edu.gatech.schoolmark.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import edu.gatech.schoolmark.R;
import edu.gatech.schoolmark.model.Game;
import edu.gatech.schoolmark.model.SportsLocations;
import edu.gatech.schoolmark.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyGamesFragment extends Fragment { // AppCompatActivity
    private FirebaseAuth mAuth;
    private DatabaseReference currentRef;

    ListView listViewGame;
    List<Game> gameList;
    Game selectedGame;
    String userUID;
    Map<Integer, String> viewTohostUID;

    private View root;

    public MyGamesFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_my_games, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentRef = FirebaseDatabase.getInstance().getReference("gamesList");
        listViewGame = root.findViewById(R.id.listViewGame);
        userUID = mAuth.getCurrentUser().getUid();
        gameList = new ArrayList<>();

        return root;
    }


    @Override
    public void onStart() {
        super.onStart();

        currentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String gameKey = dataSnapshot.get
                gameList.clear();
                for (DataSnapshot gameSnapshot : dataSnapshot.getChildren()) {
                    Game game = gameSnapshot.getValue(Game.class);
                    //Log.i(TAG, gam)
                    if (game.getPlayerUIDList().contains(userUID)) {
                        gameList.add(game);
                    }
                }
                if (getActivity()!=null) {
                    myGameListAdapter adapter = new myGameListAdapter(getActivity(), gameList, dataSnapshot);
                    listViewGame.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}