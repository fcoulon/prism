package slebus;

import edit.Patch;

public interface Producer {

	String getID();
	Patch produce();
	
}
