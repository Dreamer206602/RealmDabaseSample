package com.mx.rxjavaexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mx.rxjavaexample.api.GithubApi;
import com.mx.rxjavaexample.model.GithubUser;
import com.mx.rxjavaexample.model.Person;
import com.mx.rxjavaexample.model.UserViewModel;

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public class RetrofitActivity extends AppCompatActivity {

    private Realm mRealm;
    private Subscription mSubscription;
    private ViewGroup container;
    private GithubApi mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrofit);
        container= (ViewGroup) findViewById(R.id.list);
        mRealm=Realm.getDefaultInstance();
        mApi=createGitApi();
    }

    private GithubApi createGitApi() {
        RestAdapter.Builder builder=new RestAdapter.Builder()
                .setEndpoint("https://api.github.com/");
        //Set GitHub Oauth token to avoid throtting if example is used a lot
       final String githubToken="";
        if(!TextUtils.isEmpty(githubToken)){
            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addHeader("Authorization",String.format("token %s", githubToken));
                }
            });
        }
        return builder.build().create(GithubApi.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load all persons and merge them with their latest stats from GitHub (if they have any)
        mSubscription=mRealm.where(Person.class).isNotNull("githubUserName")
                .findAllSorted("name").asObservable()
                .filter(new Func1<RealmResults<Person>, Boolean>() {
                    @Override
                    public Boolean call(RealmResults<Person> persons) {
                        // We only want the list once it is loaded.
                        return  persons.isLoaded();
                    }
                })
                .flatMap(new Func1<RealmResults<Person>, Observable<Person>>() {
                    @Override
                    public Observable<Person> call(RealmResults<Person> persons) {
                        // Emit each person individually
                        return Observable.from(persons);
                    }
                })
                .flatMap(new Func1<Person, Observable<GithubUser>>() {
                    @Override
                    public Observable<GithubUser> call(Person person) {
                        // get GitHub statistics. Retrofit automatically does this on a separate thread.
                        return mApi.user(person.getGithubUserName());
                    }
                })
                .map(new Func1<GithubUser, UserViewModel>() {
                    @Override
                    public UserViewModel call(GithubUser githubUser) {
                        return new UserViewModel(githubUser.name,githubUser.public_repos,
                                githubUser.public_gists);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<UserViewModel>() {
                    @Override
                    public void call(UserViewModel user) {

                        // Print user info.
                        TextView userView = new TextView(RetrofitActivity.this);
                        userView.setText(String.format(Locale.US, "%s : %d/%d",
                                user.getUsername(), user.getPublicRepos(), user.getPulicGists()));
                        container.addView(userView);

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
