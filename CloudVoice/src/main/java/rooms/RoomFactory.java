package rooms;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import serializedObjects.RoomsEnum;

public class RoomFactory {
    private static Properties roomTypeMapping = new Properties();

    static {
        try (InputStream input = RoomFactory.class.getClassLoader().getResourceAsStream("roomTypeMapping.properties")) {
            roomTypeMapping.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ChatRoom createRoom(int id, String name, int serverId, int port, RoomsEnum type) {
        String className = roomTypeMapping.getProperty(type.name());

        if (className != null) {
            try {
                Class<?> roomClass = Class.forName(className);
                return (ChatRoom) roomClass.getDeclaredConstructor(int.class, String.class, int.class, int.class, RoomsEnum.class)
                        .newInstance(id, name, serverId, port, type);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}