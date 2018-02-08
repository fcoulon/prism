package edit;

public class Remove implements Edit {
	String id;
	String field;
	int index;
	
	
	public Remove(String id, String field, int index) {
		this.id = id;
		this.field = field;
		this.index = index;
	}
	
	public String getId() {
		return id;
	}
	
	public String getField() {
		return field;
	}
	
	public int getIndex() {
		return index;
	}
}
