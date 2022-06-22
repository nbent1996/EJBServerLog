package HA;

import EJB.LogSender;

public class WildflyInstance {
    private String user;
    private String password;
    private String url;
    private LogSender instance;

    public WildflyInstance(String user, String password, String url) {
        this.user = user;
        this.password = password;
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LogSender getInstance() {
        return instance;
    }

    public void setInstance(LogSender instance) {
        this.instance = instance;
    }
}
