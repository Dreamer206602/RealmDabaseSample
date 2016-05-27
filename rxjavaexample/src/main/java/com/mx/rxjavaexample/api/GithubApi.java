package com.mx.rxjavaexample.api;

import com.mx.rxjavaexample.model.GithubUser;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

/**
 * Created by hww on 2016/5/27.
 */
public interface GithubApi {

    @GET("/users/{user}")
    Observable<GithubUser>user(@Path("user")String user);
}
