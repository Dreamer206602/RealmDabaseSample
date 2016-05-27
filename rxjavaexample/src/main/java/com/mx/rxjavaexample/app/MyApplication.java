package com.mx.rxjavaexample.app;

import android.app.Application;

import java.util.TreeMap;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by hww on 2016/5/27.
 */
public class MyApplication extends Application {

    private  static  MyApplication sMyApplication;
    private static  final TreeMap<String,String>testPersons=new TreeMap<>();
    static {
        testPersons.put("Chris", null);
        testPersons.put("Christian", "cmelchior");
        testPersons.put("Christoffer", null);
        testPersons.put("Emanuele", "emanuelez");
        testPersons.put("Dan", null);
        testPersons.put("Donn", "donnfelker");
        testPersons.put("Nabil", "nhachicha");
        testPersons.put("Ron", null);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sMyApplication=this;
        RealmConfiguration mConfiguration=new RealmConfiguration.Builder(this).build();
        Realm.deleteRealm(mConfiguration);
        Realm.setDefaultConfiguration(mConfiguration);

        createTestData();


    }

    // Create test data
    private void createTestData() {




    }
}
