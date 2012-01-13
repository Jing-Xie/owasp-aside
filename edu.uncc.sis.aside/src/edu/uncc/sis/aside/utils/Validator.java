package edu.uncc.sis.aside.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {

	public static boolean validate(String regex, String input) {
		boolean valid = false;

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(input);
		valid = matcher.matches();
		return valid;
	}

}
