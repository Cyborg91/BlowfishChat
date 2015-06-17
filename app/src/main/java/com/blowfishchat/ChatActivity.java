package com.blowfishchat;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ChatActivity extends ActionBarActivity implements TcpManagerObserver {
    List<String> messages;
    Contact contact;
    String myId;
    String myName;
    EditText keyField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        TCPManager.getInstance().register(this);
        setContentView(R.layout.activity_chat);

        keyField = (EditText) findViewById(R.id.keyField);

        contact = (Contact) getIntent().getSerializableExtra("contact");

        messages = contact.getEncryptedMessages();
        myId = getIntent().getStringExtra("myId");
        myName = getIntent().getStringExtra("myName");
        Button ChatNewMessageSend = (Button) findViewById(R.id.czat_nowa_wiadomosc_wyslij);
        ChatNewMessageSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ChatNewMessageText = (EditText) findViewById(R.id.czat_nowa_wiadomosc_tekst);
                String message = ChatNewMessageText.getText().toString();
                TCPManager.getInstance().sendMessage(contact.getName(), keyField.getText().toString(), ChatNewMessageText.getText().toString());
                SaveandShowMessage(message, true);
                ChatNewMessageText.getEditableText().clear();
                ChatNewMessageText.setText("");
            }
        });
        for (String message : messages) {
            showMessage(message, false);
        }
    }

    public void showMessage(List<String> messages) {
        for (String message : messages) {
            showMessage(message, false);
        }
    }

    public void SaveandShowMessage(String message) {
        messages.add(message);
        showMessage(message, false);
    }

    public void SaveandShowMessage(String message, boolean isFromUser) {
        showMessage(message, true);
    }

    public void showMessage(String message, boolean isFromUser) {
        LinearLayout czatLayout = (LinearLayout) findViewById(R.id.czatLayout);

        RelativeLayout MessagesContainer;
        if (isFromUser) {
            MessagesContainer = MessageStyleFromUser(message);
        } else {
            MessagesContainer = MessageStyleToUser(message);
        }

        czatLayout.addView(MessagesContainer);
    }

    public RelativeLayout MessageStyle(String message) {
        LinearLayout czatLayout = (LinearLayout) findViewById(R.id.czatLayout);
        RelativeLayout MessageStyle = new RelativeLayout(czatLayout.getContext());
        TextView Text = new TextView(czatLayout.getContext());
        Text.setId(message.hashCode());
        Text.setTextAppearance(getApplicationContext(), R.style.Base_TextAppearance_AppCompat_Medium);
        Text.append(message);
        MessageStyle.addView(Text);
        TextView MessageDate = new TextView(czatLayout.getContext());
        MessageDate.setTextAppearance(getApplicationContext(), R.style.Base_TextAppearance_AppCompat_Small);
        MessageDate.append(new SimpleDateFormat("HH:mm:ss yyyy-MM-dd").format(new Date()));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.BELOW, Text.getId());
        MessageStyle.addView(MessageDate, layoutParams);

        Text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView text = (TextView) view;
                String key = keyField.getText().toString();
                if (key.equals("")) {
                    ChatActivity.this.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Enter Key !", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    final String encryptedMessage = text.getText().toString();
                    byte[] decodedBase64EncryptedMessage = Base64.decode(encryptedMessage, Base64.DEFAULT);
                    BlowfishEncrypter decrypter = new BlowfishEncrypter();
                    decrypter.init(false, key.getBytes());
                    int offset = encryptedMessage.getBytes().length % 8 == 0 ? 0 : 8 - encryptedMessage.getBytes().length % 8;
                    byte[] decryptedBytes = new byte[encryptedMessage.getBytes().length + offset];
                    try {
                        decrypter.transformBlock(decodedBase64EncryptedMessage, 0, decryptedBytes, 0);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        ChatActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Could not decrypt "+ encryptedMessage +"!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    String message = new String(decryptedBytes);
                    text.setText(message);
                }
            }
        });

        return MessageStyle;
    }

    public RelativeLayout MessageStyleFromUser(String message) {
        RelativeLayout MessageStyle = MessageStyle(message);
        MessageStyle.setGravity(Gravity.LEFT);
        MessageStyle.setBackgroundColor(getResources().getColor(R.color.background_wiadomosc_od_uzytkownika));
        return MessageStyle;
    }

    public RelativeLayout MessageStyleToUser(String message) {
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

    public void badToken() {
        Intent intent = new Intent(ChatActivity.this, LoginActivity.class);
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
    }

    public void sendOk() {
        ChatActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void msgReceived(final String fromUser, final String message) {
        ChatActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                if (fromUser.equals(contact.getName())) {
                    showMessage(message, false);
                }
                Toast.makeText(getApplicationContext(), "Receiver message from" + fromUser + "!", Toast.LENGTH_LONG).show();
            }
        });
    }
}
