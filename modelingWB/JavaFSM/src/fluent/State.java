package fluent;

import java.util.ArrayList;
import java.util.List;

public class State {
	
	private String name;
	private Fsm container;
	List<Transition> transitions = new ArrayList<>();

	public State(Fsm fsm, String name) {
		this.name = name;
		this.container = fsm;
	}

	public State state(String name) {
		return container.state(name);
	}
	
	public Transition target(String stateName) {
		Transition trans = new Transition(this,stateName);
		transitions.add(trans);
		return trans;
	}
	
	public Fsm end() {
		return container;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\t" + name + "\n");
		for (Transition transition : transitions) {
			sb.append(transition.toString());
		}
		return sb.toString();
	}
	
}
