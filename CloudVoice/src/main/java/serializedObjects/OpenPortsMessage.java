package serializedObjects;
import java.io.Serializable;

public class OpenPortsMessage implements Serializable{
	
	private int start;
	private int end;
	
	public OpenPortsMessage(int start, int end) {
		this.start=start;
		this.end=end;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

}
