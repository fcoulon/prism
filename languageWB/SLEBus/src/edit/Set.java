package edit;

public class Set implements Edit {

	String id;
	String field;
	String value;
	
	public Set(String id, String field, String value) {
		this.id = id;
		this.field = field;
		this.value = value;
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
}
