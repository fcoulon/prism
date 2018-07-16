package edit;

public class Create implements Edit{

	String id; // location
	String type; // type of the created instance
	
	public Create(String id, String type) {
		this.id = id;
		this.type = type;
	}
	
	public String getId() {
		return id;
	}
	
	public String getType() {
		return type;
	}
}
