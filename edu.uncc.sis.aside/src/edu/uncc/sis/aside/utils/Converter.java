package edu.uncc.sis.aside.utils;

public class Converter {

	private static final String DELIMITER = "\\p{Punct}";

	public static String arrayToString(String[] stringArray) {
		String output = "";
		
		if(stringArray == null){
			return output;
		}
		
		for (int i = 0; i < stringArray.length; i++) {
			if (i == stringArray.length - 1) {
				output = output.concat(stringArray[i]);
			} else {
				output = output.concat(stringArray[i]).concat("|");
			}
		}
		return output;
	}

	public static String arrayToString(int[] intArray) {
		String output = "";
		if(intArray == null){
			return output;
		}
		
        for(int i=0; i<intArray.length; i++){
        	if(i==intArray.length - 1){
        		output = output.concat(String.valueOf(intArray[i]));
        	}else{
        		output = output.concat(String.valueOf(intArray[i])).concat(",");
        	}
        }
		return output;
	}

	public static int[] stringToIntArray(String input) {

		String[] result = input.split(DELIMITER);
		int[] output = new int[result.length];
		for (int i = 0; i < result.length; i++) {
			output[i] = Integer.parseInt(result[i]);
		}
		return output;
	}

	public static String[] stringToStringArray(String input) {
		
		if(input == null)
			return null;
		
		String[] output = input.split(DELIMITER);
		String[] output_copy = new String[output.length];
		for(int i = 0; i < output.length; i++){
			String trimed_copy = output[i].trim();
			output_copy[i] = trimed_copy;
		}
		
		return output_copy;
	}
}
