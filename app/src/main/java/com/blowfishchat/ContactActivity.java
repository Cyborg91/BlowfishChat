package com.blowfishchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends Activity implements TcpManagerObserver {
    private ListView contactListView;
    private Button getContactsButton;
    private Button logoutButton;

    public List<Contact> getContacts() {
        return TCPManager.getInstance().getClientsList();
    }

    public void setupContacts() {
        final List<Contact> contactList = getContacts();
        final ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_2, contactList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TwoLineListItem row;
                if (convertView == null) {
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    row = (TwoLineListItem) inflater.inflate(android.R.layout.simple_list_item_2, null);
                } else {
                    row = (TwoLineListItem) convertView;
                }
                Contact data = contactList.get(position);
                row.getText1().setTextColor(Color.DKGRAY);
                row.getText1().setText(data.getName());
                return row;
            }
        };
        contactListView.setAdapter(adapter);
        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = new ArrayList<>(contactList).get(position);
                Intent intent = new Intent(ContactActivity.this, ChatActivity.class);
                intent.putExtra("contact",(Serializable)contact);
                intent.putExtra("myId","myId");
                intent.putExtra("myName","myName");
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        TCPManager.getInstance().register(this);
        setContentView(R.layout.activity_contact);

        getContactsButton = (Button) findViewById(R.id.getClientsButton);
        getContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TCPManager.getInstance().getClients();
            }
        });

        logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TCPManager.getInstance().logout();
                Intent intent = new Intent(ContactActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        contactListView = (ListView) findViewById(R.id.contactListView);
        setupContacts();
    }
    public void badToken() {
        Intent intent = new Intent(ContactActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void loginOk() {
    }

    public void registerOk() {
    }
    public void registerNook() {
    }

    public void loginNook() {
    }

    public void clientsDownloaded() {
        ContactActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                setupContacts();
            }
        });
    }

    public void sendOk() {

    }

    public void msgReceived(final String fromUser, final String message) {
        ContactActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "Receiver message from " + fromUser + "!", Toast.LENGTH_LONG).show();
            }
        });
    }
}

