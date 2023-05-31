package ru.clevertec.ecl.knyazev.integration.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class TestData {
	
	public static String commentDTOOnId() {
		return getDataFromFile("data/files/getCommentDTOOnId.txt");
	}
	
	public static String savingCommentDTO() {
		return getDataFromFile("data/files/savingCommentDTO.txt");
	}
	
	public static String savingInvalidCommentDTO() {
		return getDataFromFile("data/files/savingInvalidCommentDTO.txt");
	}
	
	public static String changingCommentDTO() {
		return getDataFromFile("data/files/changingCommentDTO.txt");
	}
	
	public static String changingInvalidCommentDTO() {
		return getDataFromFile("data/files/changingInvalidCommentDTO.txt");
	}
	
	
	
	public static String removingEntity() {
		return getDataFromFile("data/files/removingEntity.txt");
	}
	
	public static String removingInvalidEntity() {
		return getDataFromFile("data/files/removingInvalidEntity.txt");
	}
	
	private static final String getDataFromFile(String file) {
		
		String data = null;
				
		try (InputStream streamData = TestData.class.getClassLoader().getResourceAsStream(file)) {
			data = new String(streamData.readAllBytes(), Charset.forName("UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		return data;
	}
}
