package user;

public class UserSettings {
	
	private int id;
	private double splitNum;
	private double dbNum;
	private double topSpltNum;
	private boolean filter;
	private String prefMic;
	private String prefSpeaker;
	private boolean pushToTalk;
	private int pushToTalkKey;
	
	public UserSettings(int id, double splitNum, double dbNum, double topSpltNum, boolean filter, String prefMic, String prefSpeaker, boolean pushToTalk, int pushToTalkKey) {
		super();
		this.id=id;
		this.splitNum = splitNum;
		this.dbNum = dbNum;
		this.topSpltNum = topSpltNum;
		this.filter = filter;
		this.prefMic=prefMic;
		this.prefSpeaker=prefSpeaker;
		this.pushToTalk=pushToTalk;
		this.pushToTalkKey=pushToTalkKey;
	}

	public double getSplitNum() {
		return splitNum;
	}

	public void setSplitNum(double splitNum) {
		this.splitNum = splitNum;
	}

	public double getDbNum() {
		return dbNum;
	}

	public void setDbNum(double dbNum) {
		this.dbNum = dbNum;
	}

	public double getTopSpltNum() {
		return topSpltNum;
	}

	public void setTopSpltNum(double topSpltNum) {
		this.topSpltNum = topSpltNum;
	}

	public boolean isFilter() {
		return filter;
	}

	public void setFilter(boolean filter) {
		this.filter = filter;
	}

	public String getPrefMic() {
		return prefMic;
	}

	public void setPrefMic(String prefMic) {
		this.prefMic = prefMic;
	}

	public String getPrefSpeaker() {
		return prefSpeaker;
	}

	public void setPrefSpeaker(String prefSpeaker) {
		this.prefSpeaker = prefSpeaker;
	}

	public boolean isPushToTalk() {
		return pushToTalk;
	}

	public void setPushToTalk(boolean pushToTalk) {
		this.pushToTalk = pushToTalk;
	}

	public int getPushToTalkKey() {
		return pushToTalkKey;
	}

	public void setPushToTalkKey(int pushToTalkKey) {
		this.pushToTalkKey = pushToTalkKey;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
