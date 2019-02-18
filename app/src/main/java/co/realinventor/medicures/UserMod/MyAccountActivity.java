package co.realinventor.medicures.UserMod;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import co.realinventor.medicures.AmbulanceService.ServiceDetails;
import co.realinventor.medicures.MedStore.MedStoreDetails;
import co.realinventor.medicures.R;

public class MyAccountActivity extends AppCompatActivity {
    private String uid;
    UserDetails userDetails;
    TextView nameEditTextView, ageEditTextView, localityEditTextView, phoneEditTextView, mailEditTextView, medicalEditTextView, ambulanceEditTextView;
    private EditText input;
    private DatabaseReference ref;
    private ArrayList<String> medicalList, ambulanceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        Log.d("Activity", "MyAccountActivity");

        nameEditTextView = findViewById(R.id.nameEditTextView);
        ageEditTextView = findViewById(R.id.ageEditTextView);
        localityEditTextView = findViewById(R.id.localityEditTextView);
        phoneEditTextView = findViewById(R.id.phoneEditTextView);
        mailEditTextView = findViewById(R.id.mailEditTextView);
        medicalEditTextView = findViewById(R.id.medicalEditTextView);
        ambulanceEditTextView = findViewById(R.id.ambulanceEditTextView);

        medicalList = new ArrayList<String>();
        ambulanceList = new ArrayList<String>();


        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ref = FirebaseDatabase.getInstance().getReference();

        ref.child("User").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("FirebaseDatabase", "Got user data");
                userDetails = dataSnapshot.getValue(UserDetails.class);
                fillTheFields();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("FirebaseDatabase", "Error retrieving data");
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    private void fillTheFields(){
        nameEditTextView.setText(userDetails.first_name);
        ageEditTextView.setText(userDetails.age);
        localityEditTextView.setText(userDetails.locality);
        phoneEditTextView.setText(userDetails.phone);
        mailEditTextView.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
    }

    public void nameEditButtonPressed(View view){
        Log.d("NameEditButton", "Pressed");
        showDialogs("Type new name", nameEditTextView, InputType.TYPE_CLASS_TEXT);
    }

    public void ageEditButtonPressed(View view){
        Log.d("AgeEditButton", "Pressed");
        showDialogs("Type age", ageEditTextView, InputType.TYPE_CLASS_NUMBER);
    }

    public void localityEditButtonPressed(View view){
        Log.d("LocalityEditButton", "Pressed");
        showDialogs("Type new locality", localityEditTextView, InputType.TYPE_CLASS_TEXT);
    }

    public void phoneEditButtonPressed(View view){
        Log.d("PhoneEditButton", "Pressed");
        showDialogs("Type new phone number", phoneEditTextView, InputType.TYPE_CLASS_PHONE);
    }

    public void mailEditButtonPressed(View view){
        Log.d("MailEditButton", "Pressed");
        showDialogs("Type new mail ID", mailEditTextView, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
    }

    public void medicalEditButtonPressed(View view){
        Log.d("MedicalEditButton", "Pressed");
        Query medQuery = ref.child("MedStores");
        medQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("FirebaseDatabase", "Got medstore data");
                medicalList.clear();
                String element = "";
                for (DataSnapshot medSnapShot : dataSnapshot.getChildren()){
                    MedStoreDetails medStoreDetails = medSnapShot.getValue(MedStoreDetails.class);
                    element = medStoreDetails.shopName + " | " + medStoreDetails.locality;
                    medicalList.add(element);
                }

                showMedStoreList(medicalList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("FirebaseDatabase", databaseError.getMessage());
            }
        });
    }

    public void ambulanceEditButtonPressed(View view){
        Log.d("AmbulanceEditButton", "Pressed");

        Query ambQuery = ref.child("Ambulances");
        ambQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ambulanceList.clear();
                String element = "";
                for (DataSnapshot ambSnapShot : dataSnapshot.getChildren()){
                    Log.d("FirebaseDatabase", "Got ambulance details");
                    ServiceDetails serviceDetails = ambSnapShot.getValue(ServiceDetails.class);
                    element = serviceDetails.driverName+ " | " + serviceDetails.driverLocality;
                    ambulanceList.add(element);
                }

                showAmbulanceList(ambulanceList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("FirebaseDatabase", databaseError.getMessage());
            }
        });
    }


    private void showAmbulanceList(ArrayList<String> list){
        Log.d("Alert", "Showing ambulance list");
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose some ambulance service");

        // add a checkbox list
        String[] ambs = list.toArray(new String[0]);
        boolean[] checkedItems = new boolean[list.size()];
        builder.setMultiChoiceItems(ambs, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                // user checked or unchecked a box
            }
        });

        // add OK and Cancel buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user clicked OK
            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private void showMedStoreList(ArrayList<String> list){
        Log.d("Alert", "Showing med store list");
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose some medical stores");

        // add a checkbox list
        String[] meds = list.toArray(new String[0]);
        boolean[] checkedItems = new boolean[list.size()];
        builder.setMultiChoiceItems(meds, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                // user checked or unchecked a box
            }
        });

        // add OK and Cancel buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // user clicked OK
            }
        });
        builder.setNegativeButton("Cancel", null);

        // create and show the alert dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private void showDialogs(String title, final TextView textview, int inputType){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(title);

        // Set up the input
        input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(inputType);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textview.setText(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    public void saveEditsButtonClicked(View view){
        UserDetails updatedUserDetails = new UserDetails(nameEditTextView.getText().toString(), userDetails.last_name, userDetails.gender,
                ageEditTextView.getText().toString(),localityEditTextView.getText().toString(), phoneEditTextView.getText().toString());


        ref.child("User").child(uid).setValue(updatedUserDetails);

        FirebaseAuth.getInstance().getCurrentUser().updateEmail(mailEditTextView.getText().toString());

        Toast.makeText(this, "Account Details updated!", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onBackPressed() {
        finish();
    }
}
