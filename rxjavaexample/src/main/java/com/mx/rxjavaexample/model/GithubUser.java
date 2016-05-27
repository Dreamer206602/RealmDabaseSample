package com.mx.rxjavaexample.model;

/**
 * Created by hww on 2016/5/27.
 */

/**
 * Model class for GitHub users: https://developer.github.com/v3/users/#get-a-single-user
*/
@SuppressWarnings("unused")
 public class GithubUser {
    public String name;
    public int public_repos;
    public int public_gists;

}
