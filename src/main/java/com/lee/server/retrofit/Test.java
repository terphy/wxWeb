package com.lee.server.retrofit;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lee.server.retrofit.account.Account;
import org.apache.log4j.BasicConfigurator;

public class Test {

	public static void main(String[] args) {
		BasicConfigurator.configure();
		String result = "{\"userName\":\"dghjjj\",\"password\":\"aaaaaa\"}";
		Gson gson = new Gson();
		Type type = new TypeToken<Account>() {
		}.getType();
		Account account = gson.fromJson(result, type);
		System.out.println(account);
		
	}
}
