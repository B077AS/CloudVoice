package main.model;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

public class HashGenerator {

	private LinkedList<String> packagesList;
	private LinkedList<String> hashes;
	private final String emailPackage="classpath*:email/**/*";
	private final String loginPackage="classpath*:login/**/*";
	private final String mainPackage="classpath*:main/**/*";
	private final String registrationPackage="classpath*:registration/**/*";
	private final String roomsPackage="classpath*:rooms/**/*";
	private final String serializedObjectsPackage="classpath*:serializedObjects/**/*";
	private final String serverPackage="classpath*:server/**/*";
	private final String settingsPackage="classpath*:settings/**/*";
	private final String userPackage="classpath*:user/**/*";

	public HashGenerator() {
		this.packagesList=new LinkedList<String>();
		this.hashes=new LinkedList<String>();
		this.packagesList.add(emailPackage);
		this.packagesList.add(loginPackage);
		this.packagesList.add(mainPackage);
		this.packagesList.add(registrationPackage);
		this.packagesList.add(roomsPackage);
		this.packagesList.add(serializedObjectsPackage);
		this.packagesList.add(serverPackage);
		this.packagesList.add(settingsPackage);
		this.packagesList.add(userPackage);
	}

	public String generateHashFromFile(String filePath){
		try {
			Path path = Paths.get(filePath);
			byte[] fileData = Files.readAllBytes(path);

			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(fileData);

			StringBuilder hexString = new StringBuilder();
			for (byte b : hash) {
				hexString.append(String.format("%02x", b));
			}
			return hexString.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String combineHashes(List<String> hashes){
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			for (String hash : hashes) {
				byte[] hashBytes = hexStringToByteArray(hash);
				digest.update(hashBytes);
			}

			byte[] combinedHash = digest.digest();

			// Converte il byte[] in una rappresentazione esadecimale
			StringBuilder hexString = new StringBuilder();
			for (byte b : combinedHash) {
				hexString.append(String.format("%02x", b));
			}
			return hexString.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[] hexStringToByteArray(String hexString) {
		int len = hexString.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
					+ Character.digit(hexString.charAt(i + 1), 16));
		}
		return data;
	}

	public void extractHashes(String packageName) {
		try {
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver.getResources(packageName);
			for (Resource resource : resources) {
				File file = resource.getFile();
				if (file.isFile() && !file.getName().toLowerCase().endsWith(".css")) {
					String filePath = file.getAbsolutePath();
					String hash = generateHashFromFile(filePath);
					System.out.println("Signle Hash : " + hash + " " + file.getName());
					hashes.add(hash);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getCombinedHash() {
		
		for(String string: packagesList) {
			extractHashes(string);
		}
		
		String combinedHash = combineHashes(hashes);
		System.out.println("Combined Hash: " + combinedHash);
		return combinedHash;
	}

	public static void main(String[] args) {
		HashGenerator hashGenerator=new HashGenerator();
		hashGenerator.getCombinedHash();
	}
}