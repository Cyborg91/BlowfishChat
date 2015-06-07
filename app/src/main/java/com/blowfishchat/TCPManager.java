package com.blowfishchat;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * Created by radoslawjarzynka on 06.06.15.
 */

public class TCPManager {

    public final static String SERVER_IP = "";//TODO
    public final static Integer SERVER_PORT = 8000;

    public final static String LOGGER_IP = "";//TODO
    public final static Integer LOGGER_PORT = 8000;

    private BufferedReader in;
    private BufferedReader loggerIn;
    private PrintWriter out;
    private volatile ConcurrentLinkedQueue<String> outgoingMessages;
    private volatile ConcurrentLinkedQueue<String> outgoingLoggerMessages;

    private Thread readerThread;
    private Thread writerThread;
    private Thread loggerWriterThread;
    private Reader reader;
    private Writer writer;
    private Writer loggerWriter;

    private String token;
    private String login;

    List<Contact> clientsList;

    List<TcpManagerObserver> observers;

    private Socket socket;

    private Socket loggerSocket;
    private static volatile TCPManager instance = new TCPManager();

    /**
     * getter obiektu singletona
     * @return
     */
    public static TCPManager getInstance() {
        return instance;
    }

    private TCPManager() {
        outgoingMessages = new ConcurrentLinkedQueue<>();
        outgoingLoggerMessages = new ConcurrentLinkedQueue<>();
        observers = new ArrayList<>();
        clientsList = new ArrayList<>();
        Connect();
    }

    /**
     * passwd -> bytes[] -> sha2 -> base64
     * @param login
     * @param passwd
     */
    public void register(String login, String passwd) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] passBytes = passwd.getBytes();
            byte[] passHash = sha256.digest(passBytes);
            String encodedMsg = Base64.encodeToString(passHash, Base64.DEFAULT);
            sendToServer("REG;" + login + ";" + encodedMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void login(String login, String passwd ) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] passBytes = passwd.getBytes();
            byte[] passHash = sha256.digest(passBytes);
            String encodedMsg = Base64.encodeToString(passHash, Base64.DEFAULT);
            sendToServer("LOGIN;" + login + ";" + encodedMsg);

            this.login = login;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getClients() {
        sendToServer("GET_CLIENTS;" + login + ";" + token);
    }

    public List<Contact> getClientsList() {
        return clientsList;
    }

    public void register(TcpManagerObserver o) {
        observers.add(o);
    }

    public void unregister(TcpManagerObserver o) {
        observers.remove(o);
    }

    /**
     * original message -> byte[] -> encrypted byte[] -> base64
     */
    public void sendMessage(String receiver, String key ,String message) {
        BlowfishEncrypter encrypter = new BlowfishEncrypter();
        encrypter.init(true, key.getBytes());
        byte[] msgBytes = message.getBytes();
        int offset = message.getBytes().length%8 == 0 ? 0 : 8 - message.getBytes().length%8;
        byte[] encryptedBytes = new byte[message.getBytes().length + offset];
        encrypter.transformBlock(msgBytes, 0, encryptedBytes, 0);
        String encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.DEFAULT);
        sendToServer("SEND;" + login + ";" + token + ";" + receiver + ";" + encryptedBase64);
    }

    public void logout() {
        sendToServer("LOGOUT;" + login + ";" + token);
        login = null;
        token = null;
    }

    public void sendLog(String message) {
        outgoingLoggerMessages.add(message);
    }

    /**
     * wysłanie wiadomości do przekaźnika
     * @param str
     */
    public void sendToServer(String str) {
        outgoingMessages.add(str);
    }

    /**
     * Nawiązanie połączenia TCP z przekaźnikiem
     */
    private void Connect() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            reader = new Reader();
            writer = new Writer();
            readerThread = new Thread(reader);
            writerThread = new Thread(writer);
            readerThread.start();
            writerThread.start();
            
            loggerSocket = new Socket(LOGGER_IP, LOGGER_PORT);
            loggerIn = new BufferedReader(new InputStreamReader(loggerSocket.getInputStream()));
            loggerWriter = new Writer(true);
            loggerWriterThread = new Thread(loggerWriter);
            loggerWriterThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void Disconnect() {
        reader.setWork(false);
        writer.setWork(false);
        loggerWriter.setWork(false);
        in = null;
        loggerIn = null;
        out = null;
        socket = null;
        loggerSocket = null;
        reader = null;
        writer = null;
        loggerWriter = null;
    }

    /**
     * klasa wewnętrzna odczytująca przychodzące informacje
     */
    private class Reader implements Runnable {
        private boolean doWork;
        @Override
        public void run() {
            try {
                while (doWork) {
                    try {
                        String line = in.readLine();
                        String[] msgParts = line.split(";");

                        switch (msgParts[0]) {

                            case "CLIENTS":
                                for (int i = 1; i < msgParts.length; i ++) {
                                    boolean userExists = false;
                                    for (Contact c : clientsList) {
                                        if (c.getName().equals(msgParts[i])) {
                                            userExists = true;
                                        }
                                    }
                                    if (!userExists) {
                                        clientsList.add(new Contact(msgParts[i]));
                                    }
                                }
                                for (TcpManagerObserver o : observers) {
                                    o.clientsDownloaded();
                                }
                                break;
                            case "REG_OK":
                                for (TcpManagerObserver observer : observers) {
                                    observer.registerOk();
                                }
                                break;
                            case "REG_NOOK":
                                for (TcpManagerObserver observer : observers) {
                                    observer.registerNook();
                                }
                                break;
                            case "LOGIN_NOOK":
                                for (TcpManagerObserver observer : observers) {
                                    observer.loginNook();
                                }
                                break;
                            case "LOGIN_OK":
                                for (TcpManagerObserver observer : observers) {
                                    token = msgParts[1];
                                    observer.loginOk();
                                }
                                break;
                            case "BAD_TOKEN":
                                for (TcpManagerObserver observer : observers) {
                                    observer.badToken();
                                }
                                break;
                            case "SEND_OK":
                                for (TcpManagerObserver observer : observers) {
                                    observer.sendOk();
                                }
                                break;
                            case "MSG":
                                String senderName = msgParts[1];
                                String encryptedMessage = msgParts[2];
                                for (Contact contact : clientsList) {
                                    if (contact.getName().equals(senderName)) {
                                        contact.getEncryptedMessages().add(encryptedMessage);
                                    }
                                }
                                break;
                            default:
                                System.out.println("Unknown message: " + line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println(e.getClass().getName() + ": " + e.getMessage());
                    }
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                System.out.println(e.getClass().getName() + ": " + e.getMessage());
            }
        }

        public Reader() {
            doWork = true;
        }

        public void setWork(boolean doWork) {
            this.doWork = doWork;
        }
    }

    /**
     * Klasa wysyłająca komunikaty na serwer
     */
    private class Writer implements Runnable {
        private boolean doWork;
        private boolean isLogger = false;
        @Override
        public void run() {
            try {
                while (doWork) {
                    String line = null;
                    line = isLogger ? outgoingLoggerMessages.poll() : outgoingMessages.poll();
                    if (line != null) {
                        out.println(line);
                        System.out.println("Outgoing TCP message: " + line);
                    }
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public Writer() {
            doWork = true;
        }

        public Writer(boolean isLogger) {
            doWork = true;
            this.isLogger = isLogger;
        }

        public void setWork(boolean doWork) {
            this.doWork = doWork;
        }
    }
}

