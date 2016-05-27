package com.mx.rxjavaexample.model;

/**
 * Created by hww on 2016/5/27.
 */
public class UserViewModel {
    private final String username;
    private final int publicRepos;
    private final int pulicGists;

    public UserViewModel(String username, int publicRepos, int pulicGists) {
        this.username = username;
        this.publicRepos = publicRepos;
        this.pulicGists = pulicGists;
    }

    public String getUsername() {
        return username;
    }

    public int getPublicRepos() {
        return publicRepos;
    }

    public int getPulicGists() {
        return pulicGists;
    }
}
