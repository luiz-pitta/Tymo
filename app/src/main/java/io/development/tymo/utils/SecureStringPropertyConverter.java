package io.development.tymo.utils;

import org.jasypt.util.text.BasicTextEncryptor;

public class SecureStringPropertyConverter {

	private static final String ENCRYPTED_TOKEN_START = "ENC(";
	private static final String ENCRYPTED_TOKEN_END = ")";
	
	private BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
	
	public SecureStringPropertyConverter() {
		textEncryptor.setPassword("Tymo@S.AA.S@omyT");
	}
	
	/**
	 * Encrypt the value before it is persisted to the database.
	 */
	public String toGraphProperty(String value) {

		StringBuilder sb = new StringBuilder(ENCRYPTED_TOKEN_START);
		
		String encryptedText = textEncryptor.encrypt(value);
		sb.append(encryptedText);
		sb.append(ENCRYPTED_TOKEN_END);
		
		return sb.toString();
		
	}

	/**
	 * Decrypt the database value to the value set on the object model.
	 */
	public String toEntityAttribute(String value) {

		String result = value;
		
		boolean encrypted = isEncrypted(value);
		
		if (encrypted) {
			result = value.substring(4, result.length() - 1);
			result = textEncryptor.decrypt(result);
		}
		
		return result;
	}

	/**
	 * Determine if a value is encrypted or not.
	 * 
	 * This is a convenience to help facilitate scenarios where 
	 * <code>SecureStringPropertyConverter</code> is applied to properties who's values 
	 * are not all encrypted within the database, for example.
	 * 
	 * @param value
	 * @return True if the value is determined to be encrypted, otherwise false.
	 */
	private boolean isEncrypted(String value) {
		
		boolean result = false;
		
		if (value.startsWith(ENCRYPTED_TOKEN_START) && value.endsWith(ENCRYPTED_TOKEN_END)) {
			result = true;
		}
		
		return result;
	}
	
}