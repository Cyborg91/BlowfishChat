package com.blowfishchat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TwoLineListItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ContactActivity extends Activity {
    private ListView contactListView;

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
        setContentView(R.layout.activity_contact);

        contactListView = (ListView) findViewById(R.id.contactListView);
        setupContacts();
    }
}

