package edit;

public class UnSet implements Edit {

	String id;
	String field;
	
	public UnSet(String id, String field) {
		this.id = id;
		this.field = field;
	}
	
	@Override
	public String getId() {
		return id;
	}
	
	public String getField() {
		return field;
	}

}
