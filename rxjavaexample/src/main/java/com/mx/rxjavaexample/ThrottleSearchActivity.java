package com.mx.rxjavaexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;
import com.mx.rxjavaexample.model.Person;

import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class ThrottleSearchActivity extends AppCompatActivity {

    private Realm mRealm;
    private Subscription mSubscription;
    private EditText mEditText;
    private ViewGroup container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_throttle_search);
        mRealm=Realm.getDefaultInstance();
        mEditText= (EditText) findViewById(R.id.search);
        container= (ViewGroup) findViewById(R.id.search_results);


    }

    @Override
    protected void onResume() {
        super.onResume();
        // Listen to key presses and only start search after user paused to avoid excessive redrawing on the screen.
        mSubscription= RxTextView.textChangeEvents(mEditText)
                .debounce(200, TimeUnit.MICROSECONDS)
                .observeOn(AndroidSchedulers.mainThread())
              .flatMap(new Func1<TextViewTextChangeEvent, Observable<RealmResults<Person>>>() {
                  @Override
                  public Observable<RealmResults<Person>> call(TextViewTextChangeEvent event) {
                      return mRealm.where(Person.class)
                              .beginsWith("name",event.text().toString())
                              .findAllSortedAsync("name").asObservable();
                  }
              })
                .filter(new Func1<RealmResults<Person>, Boolean>() {
                    @Override
                    public Boolean call(RealmResults<Person> persons) {
                        return persons.isLoaded();
                    }
                })
                .subscribe(new Action1<RealmResults<Person>>() {
                    @Override
                    public void call(RealmResults<Person> persons) {
                        container.removeAllViews();
                        for(Person person:persons){

                            TextView textView=new TextView(ThrottleSearchActivity.this);
                            textView.setText(person.getName());
                            container.addView(textView);
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();

                    }
                });
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
