package controllers;

import models.User;

import org.apache.commons.lang.StringUtils;

import play.libs.Crypto;

public class Security extends Secure.Security {

	public static boolean authenticate(String username, String password) {
		User user = User.find("login=? and password=?", Crypto.encryptAES(username), Crypto.encryptAES(Crypto.passwordHash(password))).first();
		return user != null ? true : false;
	}

	public static User connectedUser() {
		if (StringUtils.isNotBlank(connected())) {
			User user = User.find("byLogin", Crypto.encryptAES(connected())).<User> first();
			return user;
		}
		return null;
	}
}
