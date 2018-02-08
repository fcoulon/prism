package fluent;

import java.util.ArrayList;
import java.util.List;

public class Fsm {
	
	String name;
	List<State> states = new ArrayList<>();
	
	public Fsm(String name) {
		this.name = name;
	}
	
	public State initial(String name) {
		State state = new State(this,name);
		states.add(state);
		return state;
	}
	
	public State state(String name) {
		State state = new State(this,name);
		states.add(state);
		return state;
	}
	
	public Fsm end() {
		//FIXME: should ignore any further state() & initial() invocations
		return this;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("FSM "+ name + " {\n");
		for (State state : states) {
			sb.append(state.toString());
		}
		sb.append("}");
		return sb.toString();
	}
}
