package user;

import java.util.HashMap;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class UsersListView extends ListView<ConnectedUserBox>{

	private HashMap<Integer, ConnectedUserBox> users;

	public UsersListView() {
		this.users=new HashMap<Integer, ConnectedUserBox>();
		this.setFocusTraversable(false);
		this.setCellFactory(new Callback<ListView<ConnectedUserBox>, ListCell<ConnectedUserBox>>() {
			@Override
			public ListCell<ConnectedUserBox> call(ListView<ConnectedUserBox> param) {
				return new ListCell<ConnectedUserBox>() {
					@Override
					protected void updateItem(ConnectedUserBox user, boolean empty) {
						//int currentIndex = getIndex();
						super.updateItem(user, empty);
						setStyle("-fx-padding: 1 0 1px 0;");
						if (user != null && !empty) {
							setGraphic(user);
						} else {
							setGraphic(null);
						}
					}
				};
			}
		});

	}

	public void addUser(ConnectedUserBox user) {
		this.getItems().add(user);
		this.users.put(user.getUser().getId(), user);
	}

	public void removeUser(User user) {
		this.getItems().remove(this.users.get(user.getId()));
		this.users.remove(user.getId());
	}

	public HashMap<Integer, ConnectedUserBox> getUsers() {
		return users;
	}

	public void setUsers(HashMap<Integer, ConnectedUserBox> users) {
		this.users = users;
	}
}