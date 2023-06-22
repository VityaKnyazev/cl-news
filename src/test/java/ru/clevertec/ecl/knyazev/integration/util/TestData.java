package ru.clevertec.ecl.knyazev.integration.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class TestData {
	
	private static int removeCounter = 5;
	
	public static String commentDTOOnId() {
		return getDataFromFile("data/files/getCommentDTOOnId.txt");
	}
	
	public static String newsDTOOnId() {
		return getDataFromFile("data/files/getNewsDTOOnId.txt");
	}
	
	public static String savingCommentDTO() {
		return getDataFromFile("data/files/savingCommentDTO.txt");
	}
	
	public static String savingNewsDTO() {
		return getDataFromFile("data/files/savingNewsDTO.txt");
	}
	
	public static String savingInvalidCommentDTO() {
		return getDataFromFile("data/files/savingInvalidCommentDTO.txt");
	}
	
	public static String savingInvalidNewsDTO() {
		return getDataFromFile("data/files/savingInvalidNewsDTO.txt");
	}
	
	public static String changingCommentDTO() {
		return getDataFromFile("data/files/changingCommentDTO.txt");
	}
	
	public static String changingNewsDTO() {
		return getDataFromFile("data/files/changingNewsDTO.txt");
	}
	
	public static String changingInvalidCommentDTO() {
		return getDataFromFile("data/files/changingInvalidCommentDTO.txt");
	}
	
	public static String changingInvalidNewsDTO() {
		return getDataFromFile("data/files/changingInvalidNewsDTO.txt");
	}
	
	
	
	public static String removingEntity() {
		
		String entity = "{\"id\":" + removeCounter + "}"; 
		
		removeCounter++;
		
		return entity;
	}
	
	public static String removingInvalidEntity() {
		return getDataFromFile("data/files/removingInvalidEntity.txt");
	}
	
	
	
	
	
	
	public static String getUserWithRoleSubscriber() {
		return getDataFromFile("data/files/userWithRoleSubscriber.txt");
	}
	
	public static String getUserWithRoleAdmin() {
		return getDataFromFile("data/files/userWithRoleAdmin.txt");
	}
	
	public static String getUserWithRoleJournalist() {
		return getDataFromFile("data/files/userWithRoleJournalist.txt");
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
