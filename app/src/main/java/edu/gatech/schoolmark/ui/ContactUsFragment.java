package edu.gatech.schoolmark.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import edu.gatech.schoolmark.R;

public class ContactUsFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private View root;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_contact_us, container, false);

        Button submitButton = root.findViewById(R.id.button_submitFeedback);
        submitButton.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        return root;
    }

    @Override
    public void onClick(View view) {
        String comment = ((EditText) root.findViewById(R.id.editText_comment)).getText().toString();
        DatabaseReference commentRef = mDatabase.child("commentList").child(mAuth.getCurrentUser().getUid()).child(Integer.toString(comment.hashCode()));

        commentRef.setValue(comment);

        Toast.makeText(getActivity(), "Thanks for the feedback!",
                Toast.LENGTH_SHORT).show();
        Fragment fragment = new GameListFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.home_frame, fragment).commit();
    }

}
