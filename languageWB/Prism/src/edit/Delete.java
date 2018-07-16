package edit;

public class Delete implements Edit {

	String id; // location
	
	public Delete(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
}
