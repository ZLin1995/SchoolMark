package edu.gatech.schoolmark.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import edu.gatech.schoolmark.R;
import edu.gatech.schoolmark.model.User;

/**
 * Created by Altan on 9/7/2017.
 */

public class UserProfileFragment extends Fragment implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private View root;

    public UserProfileFragment() {
        //required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_user_profile, container, false);

        Button delete = root.findViewById(R.id.accountDelete);
        Button save = root.findViewById(R.id.userProfileSaveButton);
        Button profileBack = root.findViewById(R.id.profileBackButton);
        delete.setOnClickListener(this);
        save.setOnClickListener(this);
        profileBack.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Populate fields with data from the logged in user
        DatabaseReference userRef = mDatabase.child("userList").child(mAuth.getCurrentUser().getUid());
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);

                EditText displayNameTextBox = root.findViewById(R.id.userProfileDisplayName);
                EditText phoneNumberTextBox = root.findViewById(R.id.userProfilePhoneNumber);
                EditText ageTextBox = root.findViewById(R.id.userProfileAge);
                RadioGroup selectGenderGroup = root.findViewById(R.id.userProfileGender);
                RadioGroup selectAffiliationGroup = root.findViewById(R.id.userProfileAffiliation);
                EditText introductionTextBox = root.findViewById(R.id.personalIntroduction);

                if (currentUser != null) {
                    displayNameTextBox.setText(currentUser.getDisplayName());
                    phoneNumberTextBox.setText(Long.toString(currentUser.getPhoneNumber()));
                    ageTextBox.setText(Integer.toString(currentUser.getAge()));
                    introductionTextBox.setText(currentUser.getIntroduction());
                    if (currentUser.getGender().equals("Male")) {
                        selectGenderGroup.check(R.id.userProfileMaleSelect);
                    } else {
                        selectGenderGroup.check(R.id.userProfileFemaleSelect);
                    }
                    if (currentUser.getIsStudent()) {
                        selectAffiliationGroup.check(R.id.userProfileStudentSelect);
                    } else {
                        selectAffiliationGroup.check(R.id.userProfileStaffSelect);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return root;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.accountDelete) {
            deleteAccount(v);
        } else if (v.getId() == R.id.userProfileSaveButton) {
            saveUserProfile(v);
        } else if (v.getId() == R.id.profileBackButton) {
            profileOnBack(v);
        }
    }

    public void saveUserProfile(View view) {

        EditText displayNameTextBox = root.findViewById(R.id.userProfileDisplayName);
        EditText phoneNumberTextBox = root.findViewById(R.id.userProfilePhoneNumber);
        EditText ageTextBox = root.findViewById(R.id.userProfileAge);
        RadioGroup selectGenderGroup = root.findViewById(R.id.userProfileGender);
        RadioGroup selectAffiliationGroup = root.findViewById(R.id.userProfileAffiliation);
        EditText introductionTextBox = root.findViewById(R.id.personalIntroduction);

        if(displayNameTextBox.getText().toString().length() > 15) {
            Toast.makeText(getActivity(), "You can only have up to 15 characters in your name :(",
                    Toast.LENGTH_LONG).show();
        } else {
            String displayName = displayNameTextBox.getText().toString();
            String phoneNumber = phoneNumberTextBox.getText().toString();
            int age = Integer.parseInt(ageTextBox.getText().toString());
            String gender;
            if (selectGenderGroup.getCheckedRadioButtonId() == R.id.userProfileMaleSelect) {
                gender = "Male";
            } else {
                gender = "Female";
            }
            boolean isStudent;
            if (selectAffiliationGroup.getCheckedRadioButtonId() == R.id.userProfileStudentSelect) {
                isStudent = true;
            } else {
                isStudent = false;
            }
            String introduction = introductionTextBox.getText().toString();

            User currentUser = new User(displayName, age, gender, Long.parseLong(phoneNumber), isStudent, introduction);

            mDatabase = FirebaseDatabase.getInstance().getReference();
            DatabaseReference userRef = mDatabase.child("userList").child(mAuth.getCurrentUser().getUid());

            userRef.setValue(currentUser);

            Toast.makeText(getActivity(), "Your changes have been saved!",
                    Toast.LENGTH_SHORT).show();
            Fragment fragment = new EventListFragment();
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().replace(R.id.home_frame, fragment).commit();
        }

    }

    public void profileOnBack(View view) {
        Toast.makeText(getActivity(), "You exited without saving your changes.",
                Toast.LENGTH_SHORT).show();
        Fragment fragment = new EventListFragment();
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.home_frame, fragment).commit();

    }



    public void deleteAccount(View view) {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Account")
                .setMessage("Are you sure you wanna leave us? :(")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleting();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        startActivity(intent);

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "We're glad you're staying :)",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .show();

    }

    public void deleting() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userRef = mDatabase.child("userList").child(mAuth.getCurrentUser().getUid());
        userRef.removeValue();

        FirebaseUser user = mAuth.getCurrentUser();
        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("", "User account deleted.");
                        }
                    }
                });

    }
}
