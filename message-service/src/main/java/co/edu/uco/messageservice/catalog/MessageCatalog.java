package co.edu.uco.messageservice.catalog;

import java.util.HashMap;
import java.util.Map;

public class MessageCatalog {

	private static Map<String, Message> messages = new HashMap<>(
	);
	
	static {
		messages.put("AdvertenciaMismoCorreo", new Message("FechaDefectoMaxima","31/12/2500"));
		messages.put("AdvertenciaMismoNumeroTelefono", new Message("AdvertenciaMismoNumeroTelefono","admin@uco.edu.co"));
		messages.put("AdvertenciaMismoIdTypeYNumeroId", new Message("AdvertenciaMismoIdTypeYNumeroId","8"));
	}
	
	public static Message getMessageValue(String key) {
		return messages.get(key);
	}
	
	public static void synchronizeMessageValue(Message message) {
		messages.put(message.getKey(),message);
	}
	
	public static Message removeMessage(String key) {
		return messages.remove(key);
	}
	
	public static Map<String, Message> getAllMessages() {
		return messages;
	}
	
}
