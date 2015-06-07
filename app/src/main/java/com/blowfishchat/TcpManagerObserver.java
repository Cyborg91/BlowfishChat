package com.blowfishchat;

/**
 * Created by radoslawjarzynka on 07.06.15.
 */
public interface TcpManagerObserver {

    void registerOk();
    void registerNook();
    void badToken();
    void loginOk();
    void loginNook();
    void clientsDownloaded();
    void sendOk();
    void msgReceived(String fromUser);
}
