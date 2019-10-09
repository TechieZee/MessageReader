package com.believe.messagereader;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import static android.R.attr.data;
import static android.R.attr.id;
import static android.R.attr.label;
import static android.R.attr.name;
import static java.sql.Types.INTEGER;
import static java.sql.Types.VARCHAR;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fetchInbox();
        exportIntoExcel();
    }

    public void fetchInbox() {
        DataBaseHelper dbHelper = new DataBaseHelper(this);
        Uri uriSms = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(uriSms, new String[]{"_id", "address", "date", "body"}, null, null, null);
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            String mobileNumber = cursor.getString(1);
            String messageBody = cursor.getString(3);
            String[] data = messageBody.split("#");
            String Tushar = "I am a good boy.";
            /
            ** mobileNumber = Mobile Number Of Participant
            * data[0] = Name of Participant
            * data[1] = Name of Coaching
            * data[2] = Name of Batch
            */
            if (data.length == 3) {
                if (dbHelper.checkDataNumber(mobileNumber)) {
                    data[0] = data[0].toUpperCase();
                    data[1] = data[1].toUpperCase();
                    data[2] = data[2].toUpperCase();
                    String uniqueId = uniqueIdGenerator(data[1]);
                    if (dbHelper.checkDataId(uniqueId)) {
                        dbHelper.insertData(data[0], data[1], data[2], mobileNumber, uniqueId);
                         sendSMS(mobileNumber, uniqueId);
                        Log.v(data[0] + " messages ::", "Found");
                    }
                }
            } else {
                Log.v("Sync Status::", "Not going on properly");
            }

        }
        cursor.close();
    }

    public void exportIntoExcel() {

        DataBaseHelper dbHelper = new DataBaseHelper(this);
        final Cursor cursor = dbHelper.getuser();
        File sd = Environment.getExternalStorageDirectory();

        String csvFile = "event.xls";

        File directory = new File(sd.getAbsolutePath());
        //create directory if not exist
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }
        try {
            //file path
            File file = new File(directory, csvFile);
            WorkbookSettings wbSettings = new WorkbookSettings();
            wbSettings.setLocale(new Locale("en", "EN"));
            WritableWorkbook workbook;
            workbook = Workbook.createWorkbook(file, wbSettings);
            //Excel sheet name. 0 represents first sheet
            WritableSheet sheet = workbook.createSheet("eventParticipationList", 0);
            // column and rows
            sheet.addCell(new Label(0, 0, "Registration Id"));
            sheet.addCell(new Label(1, 0, "Name"));
            sheet.addCell(new Label(2, 0, "Coaching"));
            sheet.addCell(new Label(3, 0, "Batch"));
            sheet.addCell(new Label(4, 0, "MobileNumber"));
            if (cursor.moveToFirst()) {
                do {
                    String regId = cursor.getString(cursor.getColumnIndex("regId"));
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    String coaching = cursor.getString(cursor.getColumnIndex("coaching"));
                    String batch = cursor.getString(cursor.getColumnIndex("batch"));
                    String phoneNumber = cursor.getString(cursor.getColumnIndex("mobileNumber"));
                    int i = cursor.getPosition() + 1;
                    sheet.addCell(new Label(0, i, regId));
                    sheet.addCell(new Label(1, i, name));
                    sheet.addCell(new Label(2, i, coaching));
                    sheet.addCell(new Label(3, i, batch));
                    sheet.addCell(new Label(4, i, phoneNumber));

                } while (cursor.moveToNext());
            }
            //closing cursor
            cursor.close();
            workbook.write();
            workbook.close();
            Toast.makeText(getApplication(),
                    "Data Exported in a Excel Sheet", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView textview = (TextView) findViewById(R.id.totalTextView);
        int total = (int) dbHelper.getProfilesCount();
        String totalCount = Integer.toString(total);
        textview.setText(totalCount);

    }

    public void refresh(View view) {
        fetchInbox();
        exportIntoExcel();
    }
    public void sendToAll(View view){

        DataBaseHelper dbHelper = new DataBaseHelper(this);
        final Cursor cursor = dbHelper.getuser();
        if (cursor.moveToFirst()) {
            do {
                String regId = cursor.getString(cursor.getColumnIndex("regId"));
                String phoneNumber = cursor.getString(cursor.getColumnIndex("mobileNumber"));
                 sendSMS(phoneNumber,regId);
            } while (cursor.moveToNext());
        }

        Toast.makeText(getApplication(),
                "Sms Sended to All Students", Toast.LENGTH_SHORT).show();

    }

    public void sendSMS(String phoneNumber, String id) {
        String smsBody = "Thank You!Your Unique  ID is :  "
                + id +
                ". Please ask your other friends also to register.";
        // Get the default instance of SmsManager
        SmsManager smsManager = SmsManager.getDefault();
        // Send a text based SMS
        smsManager.sendTextMessage(phoneNumber, null, smsBody, null, null);

    }

    public String uniqueIdGenerator(String coaching) {
        Random rand = new Random();
        String id = null;
        int mCodeNum;
        String coachingCode = coaching.substring(0, 2);
        String year = "17";
        mCodeNum = rand.nextInt(2000) + 1;
        String num;
        if (mCodeNum >= 0 && mCodeNum <= 9) {
            num = "000" + mCodeNum;
        } else if (mCodeNum >= 10 && mCodeNum <= 99) {
            num = "00" + mCodeNum;
        } else if (mCodeNum >= 100 && mCodeNum <= 999) {
            num = "0" + mCodeNum;
        } else {
            num = Integer.toString(mCodeNum);
        }
        id = coachingCode + year + num;
        return id;
    }
}
