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
import edu.gatech.schoolmark.R;
import edu.gatech.schoolmark.model.Event;

import java.util.ArrayList;
import java.util.List;


public class myEventListAdapter extends ArrayAdapter<Event> {
    static java.util.Calendar cal = java.util.Calendar.getInstance();
    private Activity context;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference currentRef;
    private DataSnapshot eventsList;
    private List<Event> eventList;
    //private String gameKey;
    //private Event game;


    public myEventListAdapter(Activity context, List<Event> eventList, DataSnapshot eventsList) {
        super(context, R.layout.join_event_list_layout, eventList);
        this.context = context;
        this.eventList = eventList;
        this.eventsList = eventsList;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        mAuth = FirebaseAuth.getInstance();
        LayoutInflater inflater = context.getLayoutInflater();
        View listViewItem = inflater.inflate(R.layout.my_event_list_layout, null, true);
        TextView listEventName = listViewItem.findViewById(R.id.listEventName);
        TextView listEventType = listViewItem.findViewById(R.id.listEventType);
        TextView listLocation = listViewItem.findViewById(R.id.listLocation);
        TextView listTime = listViewItem.findViewById(R.id.listTime);
        TextView listDate = listViewItem.findViewById(R.id.listDate);
        TextView listCapacity = listViewItem.findViewById(R.id.listCapacity);
        Button quitEvent = listViewItem.findViewById(R.id.quitEvent);

        final java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
        final java.text.DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getContext());
        final Event event = eventList.get(position);
        String eventKey = "";



        cal.setTime(event.getTimeOfEvent());
        listEventName.setText(event.getEventName());
        listEventType.setText(event.getEvent());
        listTime.setText(timeFormat.format(event.getTimeOfEvent()));
        listDate.setText(dateFormat.format(event.getTimeOfEvent()));
        listLocation.setText(event.getLocationTitle());
        listCapacity.setText("Capacity: " + event.getPlayerUIDList().size() + " / " + event.getCapacity());

        mDatabase = FirebaseDatabase.getInstance().getReference();
        currentRef = mDatabase.child("eventsList");


        for (DataSnapshot gameSnapshot : eventsList.getChildren()) {
            Event g = gameSnapshot.getValue(Event.class);
            if (g.equals(event)) {
                eventKey = gameSnapshot.getKey();
            }
        }

        final String event_key = eventKey;
        quitEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> editedList = event.getPlayerUIDList();
                String user = mAuth.getCurrentUser().getUid();
                editedList.remove(user);
                event.setPlayerUIDList((ArrayList<String>) editedList);
                if (editedList.size() == 0 || event.getHostUID().equals(user)) {
                    mDatabase.child("eventsList").child(event_key).removeValue();
                } else {
                    mDatabase.child("eventsList").child(event_key).child("playerUIDList").setValue(editedList);
                }
                String toastText = "You have successfully quited the " + event.getEvent() + " event on " + event.getTimeOfEvent().toString().substring(0, 10);
                Toast temp = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
                temp.setGravity(Gravity.CENTER,0,0);
                temp.show();
            }
        });

        listViewItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString("eventName", event.getEventName());
                bundle.putString("eventType", event.getEvent());
                bundle.putString("location", event.getLocationTitle());
                bundle.putString("time", timeFormat.format(event.getTimeOfEvent()));
                bundle.putString("date", dateFormat.format(event.getTimeOfEvent()));
                bundle.putString("hostID", event.getHostUID());
                bundle.putString("eventID", event_key);
                Fragment fragment = new EventDetailFragment();
                fragment.setArguments(bundle);
                FragmentManager fm = ((Activity)context).getFragmentManager();
                fm.beginTransaction().replace(R.id.home_frame, fragment).addToBackStack( "tag" ).commit();
            }
        });

        return listViewItem;
    }
}