package com.example.snowwhite.homeassignment1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    private ListView mListView;
    private ProgressDialog pDialog;
    private Handler updateBarHandler;
    ArrayList<String> contactList;
    Cursor cursor;
    int counter;
    String fullName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading contacts...");
        pDialog.setCancelable(false);
        pDialog.show();
        mListView = (ListView) findViewById(R.id.list);
        final TextView selectionText = (TextView) findViewById(R.id.textView2);
        updateBarHandler = new Handler();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        // Since reading contacts takes more time, let's run it on a separate thread.
        new Thread(new Runnable() {
            @Override
            public void run() {
                getContacts();
            }
        }).start();
        // Set onclicklistener to the list item.
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {


                //TODO Do whatever you want with the list data
                Toast.makeText(getApplicationContext(), "item clicked : \n"+
                        contactList.get(position), Toast.LENGTH_SHORT).show();
                Log.v("Details: ", contactList.get(position));

                //Extracting the Email
                String text = contactList.get(position).toString().trim();
                String emailGathered = "";
                String emailGathered1 = "";
                Log.v("Details2: ",text);

                //Extracting the name of the contact
                String[] arr = text.split("\\s+");
                fullName = arr[0] + " " + arr[1];
                Log.v("fullName: ",fullName);

                Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")
                        .matcher(text);
                while (m.find()) {
                    Log.v("Email: ",m.group());
                    emailGathered1 = m.group();
                    emailGathered = emailGathered1.substring(emailGathered1
                            .lastIndexOf(":") + 1);
                    Log.v("Email1: ",emailGathered);
                    selectionText.setText("Selected: " + fullName);
                }
                //Starting a new activity
                Intent intent = new Intent(MainActivity.this, EmailSending.class);
                intent.putExtra("EmailID", emailGathered);

                startActivityForResult(intent, 1);
            }
        });
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }

    public void getContacts() {
        contactList = new ArrayList<String>();
        String phoneNumber = null;
        String email = null;
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        Uri EmailCONTENT_URI =  ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;
        StringBuffer output;
        ContentResolver contentResolver = getContentResolver();
        cursor = contentResolver.query(CONTENT_URI, null,null, null, null);
        // Iterate every contact in the phone
        if (cursor.getCount() > 0) {
            counter = 0;
            while (cursor.moveToNext()) {
                output = new StringBuffer();
                // Update the progress message
                updateBarHandler.post(new Runnable() {
                    public void run() {
                        pDialog.setMessage("Loading contacts : "+ counter++ +"/"+cursor.getCount());

                    }
                });
                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex
                        ( HAS_PHONE_NUMBER )));
                if (hasPhoneNumber > 0) {
                    //output.append("\n First Name:" + name);
                    output.append("\n " + name);
                    //This is to read multiple phone numbers associated with the same contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null,
                            Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        output.append("\n Phone number:" + phoneNumber);
                    }
                    phoneCursor.close();
                    // Read every email id associated with the contact
                    Cursor emailCursor = contentResolver.query(EmailCONTENT_URI,
                            null, EmailCONTACT_ID+ " = ?", new String[] { contact_id }, null);
                    while (emailCursor.moveToNext()) {
                        email = emailCursor.getString(emailCursor.getColumnIndex(DATA));
                        output.append("\n Email:" + email);
                    }
                    emailCursor.close();
                }
                // Add the contact to the ArrayList
                contactList.add(output.toString());
            }
            // ListView has to be updated using a ui thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>
                            (getApplicationContext(), R.layout.list_item, R.id.text1, contactList);
                    mListView.setAdapter(adapter);
                }
            });
            // Dismiss the progressbar after 500 milliseconds
            updateBarHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    pDialog.cancel();
                }
            }, 500);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        TextView emailSentText = (TextView) findViewById(R.id.textView2);
        if (requestCode == 1) {
            //If the email has been sent successfully
            if (resultCode == Activity.RESULT_OK) {
                //Resets the MainActivity page
                emailSentText.setText("Email sent to: " + fullName);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                //Gives user a message of successful message sent
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Email Sent Successfully!").show();
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                return;
            }
            //If the email was not sent
            if (resultCode == Activity.RESULT_CANCELED) {
                //Gives user a message of unsuccessful message sent
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Email not sent! Kindly retry after login to your gmail " +
                        "from your Mobile!").show();
                return;
            }
        }
    }

    public void searchContacts(View view) {
        Log.v("Button clicked: ","Yes");
        ArrayList<String> arrayList = new ArrayList<String>(mListView.getCount());
        EditText srhText = (EditText) findViewById(R.id.searchText);
        String sT = srhText.getText().toString();
        if (sT.matches("")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Kindly, search something!").show();
            return;
        }

        for(int i = 0; i< mListView.getCount();i++){
            String text1 = mListView.getItemAtPosition(i).toString().trim();
            String[] arr = text1.split("\\s+");
            String fullNameTest = arr[0];
            //
            int countTotal = 1;
            //arrayList.add(fullNameTest);

            if(fullNameTest.toLowerCase().startsWith(sT.toLowerCase())){
                for (int countT = 1; countT < mListView.getCount(); countT++){
                    String text11 = mListView.getItemAtPosition(countT).toString().trim();
                    Log.v("Found name: ",text1);
                    String[] arr1 = text11.split("\\s+");
                    String fullNameTest1 = arr1[0];
                    Log.v("Found: ",fullNameTest1);
                    arrayList.add(fullNameTest1);
                }
                ArrayAdapter<String> resultAdapter = new ArrayAdapter<String>(
                        this, android.R.layout.simple_list_item_1, arrayList);
                mListView.setAdapter(resultAdapter);
                resultAdapter.notifyDataSetChanged();

                // Here we update the view
                return;
            }
        }
    }
}
