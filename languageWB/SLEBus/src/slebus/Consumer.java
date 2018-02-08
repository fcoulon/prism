package slebus;

import edit.Patch;

public interface Consumer {

	String getID();
	void consume(Patch p);
	
}
