package co.realinventor.medicures.UserMod;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.kevalpatel.ringtonepicker.RingtonePickerDialog;
import com.kevalpatel.ringtonepicker.RingtonePickerListener;

import java.util.Calendar;

import co.realinventor.medicures.Common.AlarmsOpenHelper;
import co.realinventor.medicures.Common.DoctorReminderHelper;
import co.realinventor.medicures.R;

public class DocReminderActivity extends AppCompatActivity {

    DatePickerDialog datePickerDialog;
    TextView dateText, ringtoneText;
    Uri selectedRingtoneUri;
    EditText inputDoctorName;
    String doctorName;
    TimeEntity time;
    DateEntity date;
    TimePicker timePicker;
    int nYear, nMonth, nDay;
    static DoctorReminderHelper doctorReminderHelper;
    static SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc_reminder);
        dateText = findViewById(R.id.dateText);
        ringtoneText = findViewById(R.id.ringtoneText);
        inputDoctorName = findViewById(R.id.inputDoctorName);
        timePicker = findViewById(R.id.timePick);

    }

    public void dateSelectButtonClicked(View view){
        final int mYear = 2019, mMonth = 01, mDay = 01;
        datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        dateText.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                        nYear = year;
                        nMonth = monthOfYear;
                        nDay = dayOfMonth;

                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.show();
    }

    public void ringtoneSelectButtonPressed(View view){
        RingtonePickerDialog.Builder ringtonePickerBuilder = new RingtonePickerDialog
                .Builder(DocReminderActivity.this, getSupportFragmentManager())
                .setTitle("Select ringtone")
                .displayDefaultRingtone(true)
                .setPositiveButtonText("Set Ringtone")
                .setCancelButtonText("Cancel")
                .setPlaySampleWhileSelection(true)
                .setListener(new RingtonePickerListener() {
                    @Override
                    public void OnRingtoneSelected(@NonNull String ringtoneName, Uri ringtoneUri) {
                        //Do someting with selected uri...
                        selectedRingtoneUri = ringtoneUri;
                        ringtoneText.setText(selectedRingtoneUri.getLastPathSegment());
                    }
                });

        //Add the desirable ringtone types.
        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_MUSIC);
        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_RINGTONE);
        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM);

        //Display the dialog.
        ringtonePickerBuilder.show();
    }

    public void nextButtonClicked(View view){
        doctorName = inputDoctorName.getText().toString();
        time = new TimeEntity(timePicker.getCurrentHour(), timePicker.getCurrentMinute(), 0);
        date = new DateEntity(nYear, nMonth, nDay);


        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getApplicationContext(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getApplicationContext());
        }
        builder.setTitle("Set Reminder")
                .setMessage("Doctor Name : "+doctorName+"\nDate :"+date.getDate()+"\nTime :"+time.getTime() + "\nSelected Ringtone : " +selectedRingtoneUri.getLastPathSegment())
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // set alarm
                        createReminder();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void createReminder(){
        initDatabase();
        addAlarmToDatabase(doctorName, date.getDate().toString(), ""+time.getHour(), ""+time.getMinute(), selectedRingtoneUri.toString());



        // Set the alarm to start at approximately 2:00 p.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, time.getHour());
        calendar.set(Calendar.MINUTE, time.getMinute());
        calendar.set(nYear,nMonth,nDay,time.getHour(),time.getMinute(),0);

        Intent intent = new Intent(this, DocVisitReminderReceiver.class);
        intent.putExtra("doctor", doctorName);
        intent.putExtra("ringtone", selectedRingtoneUri.toString());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this.getApplicationContext(), 234324245, intent, 0);

        // With setInexactRepeating(), you have to use one of the AlarmManager interval
        // constants--in this case, AlarmManager.INTERVAL_DAY.
        AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Toast.makeText(this, "Alarm set!",Toast.LENGTH_LONG).show();

    }

    static void addAlarmToDatabase(String doctor, String date, String hour, String minute, String tone) {
        db.execSQL("INSERT INTO "
                + DoctorReminderHelper.TABLE_NAME
                + " ("
                + DoctorReminderHelper.COLUMN_DOCTOR
                + ", "
                + DoctorReminderHelper.COLUMN_DATE
                + ", "
                + DoctorReminderHelper.COLUMN_HOUR
                + ","
                + DoctorReminderHelper.COLUMN_MINUTE
                + ", "
                + DoctorReminderHelper.COLUMN_TONE
                + ") VALUES ('"
                + doctor
                + "', '"
                + date
                + "', '"
                + hour
                + "', '"
                + minute
                + "', '"
                + tone
                + "');");
    }

    private void initDatabase() {
        doctorReminderHelper = new DoctorReminderHelper(getApplicationContext());
        db = doctorReminderHelper.getWritableDatabase();
    }


}