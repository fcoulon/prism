package edit;

public class Insert implements Edit{

	String id;
	String field;
	String value;
	int index;
	
	
	public Insert(String id, String field, String value, int index) {
		this.id = id;
		this.field = field;
		this.value = value;
		this.index = index;
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	public String getField() {
		return field;
	}
	
	public String getValue() {
		return value;
	}
	
	public int getIndex() {
		return index;
	}
}
