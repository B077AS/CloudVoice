package user;

import javafx.scene.image.Image;
import serializedObjects.UserDetailsObject;
import java.time.LocalDate;

public class User {

	private int id;
	private String username;
	private String password;
	private String email;
	private Image avatar;
	private LocalDate dateOfBirth;
	private UserSettings settings;
	private UserDB userDb;
	private UserDetailsObject userDetails;

	public User(int id, String username, String password, String email, Image avatar, LocalDate dateOfBirth) {
		this.id=id;
		this.username = username;
		this.password = password;
		this.email = email;
		this.avatar = avatar;
		this.dateOfBirth = dateOfBirth;
		this.userDb=new UserDB();
		this.settings=userDb.fetchSettings(this);
		this.userDetails=userDb.fetchUserDetails(this);
	}

	// Metodi getter e setter per gli attributi
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Image getAvatar() {
		return avatar;
	}

	public void setAvatar(Image avatar) {
		this.avatar = avatar;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public UserSettings getSettings() {
		return settings;
	}

	public void setSettings(UserSettings settings) {
		this.settings = settings;
	}

	public UserDB getUserDb() {
		return userDb;
	}

	public void setUserDb(UserDB userDb) {
		this.userDb = userDb;
	}

	public UserDetailsObject getUserDetails() {
		return userDetails;
	}

	public void setUserDetails(UserDetailsObject userDetails) {
		this.userDetails = userDetails;
	}
}