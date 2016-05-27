package com.mx.rxjavaexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;

import com.mx.rxjavaexample.model.Person;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;

public class AnimationActivity extends AppCompatActivity {

    private Realm mRealm;
    private Subscription mSubscription;
    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation);
        container= (ViewGroup) findViewById(R.id.list);

        mRealm=Realm.getDefaultInstance();


    }

    @Override
    protected void onResume() {
        super.onResume();

        mSubscription=mRealm.where(Person.class).findAllAsync().asObservable()
                .flatMap(new Func1<RealmResults<Person>, Observable<Person>>() {
                    @Override
                    public Observable<Person> call(RealmResults<Person> persons) {
                        return Observable.from(persons);
                    }
                })
               .zipWith(Observable.interval(1, TimeUnit.SECONDS), new Func2<Person, Long, Person>() {
                   @Override
                   public Person call(Person person, Long aLong) {
                       return person;
                   }
               })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Person>() {
                    @Override
                    public void call(Person person) {

                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!mSubscription.isUnsubscribed()){
            mSubscription.unsubscribe();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
}
