package com.blowfishchat;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class ChatActivity extends ActionBarActivity {
    List<Message> messages;
    Contact contact;
    String myId;
    String myName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        messages = new ArrayList<Message>();
        contact = (Contact) getIntent().getSerializableExtra("contact");
        myId = getIntent().getStringExtra("myId");
        myName = getIntent().getStringExtra("myName");
        Button ChatNewMessageSend = (Button) findViewById(R.id.czat_nowa_wiadomosc_wyslij);
        ChatNewMessageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ChatNewMessageText = (EditText) findViewById(R.id.czat_nowa_wiadomosc_tekst);
                Message message = new Message(myName, contact.getName(), ChatNewMessageText.getText().toString());
                SaveandShowMessage(message);
                ChatNewMessageText.getEditableText().clear();
                ChatNewMessageText.setText("");
            }
        });
    }

    public void showMessage(List<Message> messages) {
        for (Message message :  messages) {
            showMessage(message);
        }
    }

    public void SaveandShowMessage(Message message) {
        messages.add(message);
        showMessage(message);
    }

    public void showMessage(Message message) {
        LinearLayout czatLayout = (LinearLayout) findViewById(R.id.czatLayout);

        RelativeLayout MessagesContainer;
        if (myName.equals(message.getNadawca())) {
            MessagesContainer = MessageStyleFromUser(message);
        } else {
            MessagesContainer = MessageStyleToUser(message);
        }

        czatLayout.addView(MessagesContainer);
    }

    public RelativeLayout MessageStyle(Message message) {
        LinearLayout czatLayout = (LinearLayout) findViewById(R.id.czatLayout);
        RelativeLayout MessageStyle = new RelativeLayout(czatLayout.getContext());
        TextView Text = new TextView(czatLayout.getContext());
        Text.setId(message.hashCode());
        Text.setTextAppearance(getApplicationContext(), R.style.Base_TextAppearance_AppCompat_Medium);
        Text.append(message.getTresc());
        MessageStyle.addView(Text);
        TextView MessageDate = new TextView(czatLayout.getContext());
        MessageDate.setTextAppearance(getApplicationContext(), R.style.Base_TextAppearance_AppCompat_Small);
        MessageDate.append(new SimpleDateFormat("HH:mm:ss yyyy-MM-dd").format(message.getData()));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.BELOW, Text.getId());
        MessageStyle.addView(MessageDate, layoutParams);

        return MessageStyle;
    }

    public RelativeLayout MessageStyleFromUser(Message message) {
        RelativeLayout MessageStyle = MessageStyle(message);
        MessageStyle.setGravity(Gravity.LEFT);
        MessageStyle.setBackgroundColor(getResources().getColor(R.color.background_wiadomosc_od_uzytkownika));
        return MessageStyle;
    }

    public RelativeLayout MessageStyleToUser(Message message) {
        RelativeLayout MessageStyle = MessageStyle(message);
        MessageStyle.setGravity(Gravity.RIGHT);
        MessageStyle.setBackgroundColor(getResources().getColor(R.color.background_wiadomosc_do_uzytkownika));
        return MessageStyle;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_ustawienia) {
            return true;
        } else if (id == R.id.action_czat_wyczysc) {
            messages.clear();
            LinearLayout czatLayout = (LinearLayout) findViewById(R.id.czatLayout);
            czatLayout.removeAllViews();
        }
        return super.onOptionsItemSelected(item);
    }

}
