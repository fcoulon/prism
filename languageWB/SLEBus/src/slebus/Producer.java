package slebus;

import edit.Patch;

public interface Producer extends Consumer {

	/*
	 * Produce a Patch representing an iterative change in the internal state
	 */
	Patch produce();
	
	/*
	 * Produce a Patch creating an initial state from scratch.
	 */
	Patch synchronize();
}
