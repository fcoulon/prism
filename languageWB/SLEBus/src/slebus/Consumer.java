package slebus;

import edit.Patch;

public interface Consumer {

	/*
	 * Unique identifier
	 */
	String getId();
	
	/*
	 * Consume a Patch to update its internal state
	 */
	void consume(Patch p);
	
	/*
	 * Initialize the internal state by applying the Patch which construct the state from scratch
	 */
	void synchronize(Patch p);
}
