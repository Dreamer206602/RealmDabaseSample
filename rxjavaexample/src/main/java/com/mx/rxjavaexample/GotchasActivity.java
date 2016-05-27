package com.mx.rxjavaexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mx.rxjavaexample.model.Person;

import java.util.List;
import java.util.Random;

import io.realm.Realm;
import io.realm.Sort;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class GotchasActivity extends AppCompatActivity {

    private Realm mRealm;
    private Subscription mSubscription;
    private ViewGroup contanier;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gotchas);

        contanier= (ViewGroup) findViewById(R.id.list);
        mRealm=Realm.getDefaultInstance();

    }

    @Override
    protected void onResume() {
        super.onResume();

        Subscription distinctSubscription=testDistinct();
        Subscription bufferSubscription=testBuffer();
        Subscription subscribeOnSubscription=testSubscribeOn();



        // Triggger update
        mRealm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                mRealm.where(Person.class).findAllSorted("name", Sort.ASCENDING).get(0).setAge(
                        new Random().nextInt(100)
                );
            }
        });

        mSubscription=new CompositeSubscription(
                distinctSubscription,
                bufferSubscription,
                subscribeOnSubscription);
    }

    /**
     * shows how to be careful with  'SubscribeOn()'
     * @return
     */
    private Subscription testSubscribeOn() {
        Subscription subscribeOn=mRealm.asObservable()
                .map(new Func1<Realm, Person>() {
                    @Override
                    public Person call(Realm realm) {
                        return realm.where(Person.class).findAllSorted("name").get(0);
                    }
                })
                // The Realm was created on UI Thread
                // Accessing it on `Schedulers.io()` will crash
                //  Avoid using subscribeOn() and use Realms `findAllAsync()` methods instead
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Person>() {
                    @Override
                    public void call(Person person) {
                        // Do nothing
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.d("subscribeOn:",throwable.toString());
                    }
                });
        Subscription asyncSubscribeOn=mRealm.where(Person.class)
                .findAllSortedAsync("name").get(0).<Person>asObservable()
                .subscribe(new Action1<Person>() {
                    @Override
                    public void call(Person person) {
                        Log.d("subscribeOn/async", person.getName());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e("subscribeOn/async",throwable.toString());
                    }
                });

        return  new CompositeSubscription(subscribeOn,asyncSubscribeOn);

    }

    /**
     * show how to be careful with `buffer()`
     * @return
     */
    private Subscription testBuffer() {

        final Observable<Person>personObserver=mRealm.asObservable()
                .map(new Func1<Realm, Person>() {
                    @Override
                    public Person call(Realm realm) {
                        return mRealm.where(Person.class).findAllSorted("name").get(0);
                    }
                });

        // buffer() caches objects until the buffer is full
        //Due to Realms auto-update of all objects it means
        // that all objects int the cache will contain the same data.
        // Either avoid using buffer or copy data in an unmanaged object


        return personObserver
                .buffer(2)
                .subscribe(new Action1<List<Person>>() {
                    @Override
                    public void call(List<Person> persons) {
                        Log.d("Buffer[0]:",persons.get(0).getName()+":"+persons.get(0).getAge());
                        Log.d("Buffer[1]:",persons.get(1).getName()+":"+persons.get(1).getAge());
                    }
                });
    }

    /**
     * show how to be careful when using `distinct`
     * @return
     */
    private Subscription testDistinct() {
        Observable<Person>personObserver=mRealm.asObservable()
                .map(new Func1<Realm, Person>() {
                    @Override
                    public Person call(Realm realm) {
                        return realm.where(Person.class).findAllSorted("name").get(0);
                    }
                });
        // distinct() and distinctUntilChanged() uses standard equals with older objects stored in a HashMap.
        // Realm objects auto-update which means the objects stored will also auto-update.
        // This makes comparing against older objects impossible (even if the new object has changed) because the
        // cached object will also have changed.
        // Use a keySelector function to work around this.

        Subscription distinctItenTest=personObserver
                .distinct()// Because old==new  This will only allow the
                // first version of the "Chris" object to pass
                .subscribe(new Action1<Person>() {
                    @Override
                    public void call(Person person) {
                        Log.d("distinct()",person.getName()+":"+person.getAge());
                    }
                });

       Subscription distinctKeySelectItemTest=personObserver
               .distinct(new Func1<Person, Integer>() {
                   @Override
                   public Integer call(Person person) {
                       return person.getAge();
                   }
               })
               .subscribe(new Action1<Person>() {
                   @Override
                   public void call(Person person) {
                       Log.d("distinct(keySelector)",person.getName()+":"+person.getAge());
                   }
               });


        return new CompositeSubscription(distinctItenTest,distinctKeySelectItemTest);
    }

    private void showStatus(String message){
        TextView textView=new TextView(this);
        textView.setText(message);
        contanier.addView(textView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSubscription.unsubscribe();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }
}
