package controllers;

import models.User;

public class Security extends Secure.Security {

	public static boolean authenticate(String username, String password) {
		User user = User.find("login=? and password=?", username, password).first();
		return user != null ? true : false;
	}

	public static User connectedUser() {
		User user = User.find("byLogin", connected()).<User> first();
		return user;
	}
}
