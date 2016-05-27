package com.mx.rxjavaexample.model;

import io.realm.RealmObject;

/**
 * Created by hww on 2016/5/27.
 */
public class Person extends RealmObject {

    //All  fields are by deefault

    private String name;
    private int age;
    private String githubUserName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGithubUserName() {
        return githubUserName;
    }

    public void setGithubUserName(String githubUserName) {
        this.githubUserName = githubUserName;
    }
}
