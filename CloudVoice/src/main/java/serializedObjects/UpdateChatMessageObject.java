package serializedObjects;

import java.io.Serializable;

public class UpdateChatMessageObject implements Serializable{
	
	private boolean delete=false;
	private boolean update=false;
	private long messageId;
	private String newContent;
	
	public UpdateChatMessageObject(boolean delete, long messageId) {
		this.delete=delete;
		this.messageId=messageId;
	}
	
	public UpdateChatMessageObject(boolean update, long messageId, String newContent) {
		this.update=update;
		this.newContent=newContent;
		this.messageId=messageId;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	public String getNewContent() {
		return newContent;
	}

	public void setNewContent(String newContent) {
		this.newContent = newContent;
	}

	public long getMessageId() {
		return messageId;
	}

	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}
}