package slebus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;

public class AstAccessor {

	static final String INITIAL = "initial";
	static final String STATE = "state";
	static final String STATES = "@states";
	static final String TARGET = "target";
	static final String ON = "on";
	static final String END = "end";
	static final String TRANSITION = "transition";
	static final String TRANSITIONS = "@transitions";

	Map<Expression, String> exp2Id = new HashMap<>(); // store Id for new Fsm() & each invoke
	Map<String, List<Expression>> fsm2Exp = new HashMap<>(); // store new Fsm(), state(), target() & on()
	Map<String, List<Expression>> state2Exp = new HashMap<>(); // store state(), target() & on()
	Map<String, List<Expression>> transition2Exp = new HashMap<>(); // store target() & on()

	private AST ast;

	public AstAccessor(List<Expression> fsm) {

		Expression currentFsm = fsm.get(0);
		Expression currentState = null;
		Expression currentTransition = null;

		this.ast = currentFsm.getAST();

		int stateCounter = 0;
		int transitionCounter = 0;

		exp2Id.put(currentFsm, "/");
		fsm2Exp.put(exp2Id.get(currentFsm), new ArrayList<Expression>());
		fsm2Exp.get(exp2Id.get(currentFsm)).add(currentFsm);

		for (int i = 1; i < fsm.size(); i++) {

			Expression exp = fsm.get(i);

			if (exp instanceof MethodInvocation) {
				MethodInvocation invoke = (MethodInvocation) exp;
				String name = invoke.getName().toString();

				if (name.equals(STATE) || name.equals(INITIAL)) {

					if (currentState != null)
						stateCounter++;

					String stateId = exp2Id.get(currentFsm) + "/" + STATES + "." + stateCounter;
					exp2Id.put(invoke, stateId);

					currentState = invoke;

					state2Exp.put(stateId, new ArrayList<Expression>());
					state2Exp.get(stateId).add(invoke);
					fsm2Exp.get(exp2Id.get(currentFsm)).add(invoke);

					currentTransition = null;
					transitionCounter = 0;
				} else if (name.equals(TARGET)) {
					if (currentTransition != null)
						transitionCounter++;

					String transitionId = exp2Id.get(currentState) + "/" + TRANSITIONS + "." + transitionCounter;
					exp2Id.put(invoke, transitionId);

					currentTransition = invoke;

					transition2Exp.put(transitionId, new ArrayList<Expression>());
					transition2Exp.get(transitionId).add(invoke);
					state2Exp.get(exp2Id.get(currentState)).add(invoke);
					fsm2Exp.get(exp2Id.get(currentFsm)).add(invoke);
				} else if (name.equals(ON)) {
					String transitionId = exp2Id.get(currentTransition);
					transition2Exp.get(transitionId).add(invoke);
					state2Exp.get(exp2Id.get(currentState)).add(invoke);
					fsm2Exp.get(exp2Id.get(currentFsm)).add(invoke);
				}
				if (name.equals(END)) {
					fsm2Exp.get(exp2Id.get(currentFsm)).add(invoke);

					currentFsm = null;
					currentState = null;
					currentTransition = null;
					stateCounter = 0;
					transitionCounter = 0;
				}
			} else {
				// TODO: error?
			}
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		fsm2Exp.entrySet().stream().forEach(entry -> {
			sb.append(entry.getKey());
			for (Expression e : entry.getValue()) {
				sb.append(" ");
				sb.append(forHuman(e));
			}
			sb.append("\n");
		});
		state2Exp.entrySet().stream().forEach(entry -> {
			sb.append(entry.getKey());
			for (Expression e : entry.getValue()) {
				sb.append(" ");
				sb.append(forHuman(e));
			}
			sb.append("\n");
		});
		transition2Exp.entrySet().stream().forEach(entry -> {
			sb.append(entry.getKey());
			for (Expression e : entry.getValue()) {
				sb.append(" ");
				sb.append(forHuman(e));
			}
			sb.append("\n");
		});
		return sb.toString();
	}

	private static String forHuman(Expression exp) {
		if (exp instanceof ClassInstanceCreation) {
			String className = ((SimpleType) ((ClassInstanceCreation) exp).getType()).getName().toString();
			return "new " + className + "()";
		} else if (exp instanceof MethodInvocation) {
			return ((MethodInvocation) exp).getName().toString() + "()";
		}
		return "";
	}

	public void addMachine(String id) {
		// Update real AST
		MethodInvocation endExp = (MethodInvocation) fsm2Exp.get("/").get(fsm2Exp.get("/").size() - 1);
		Expression newFsm = buildFsm(ast);
		endExp.setExpression(newFsm);

		// Reset all IDs
		exp2Id = new HashMap<>();
		fsm2Exp = new HashMap<>();
		state2Exp = new HashMap<>();
		transition2Exp = new HashMap<>();

		// Update IDs
		exp2Id.put(newFsm, "/");
		fsm2Exp.put("/", new ArrayList<Expression>());
		fsm2Exp.get("/").add(newFsm);
		fsm2Exp.get("/").add(endExp);
	}

	public void addState(String id) {
		// Update real AST
		MethodInvocation insertionPoint = findInsertionPoint(id);
		MethodInvocation newState = buildMethodInvocation(STATE, ast);

		boolean isReplacing = state2Exp.get(id) != null;
		if (isReplacing) { // insert before old state
			MethodInvocation oldState = (MethodInvocation) state2Exp.get(id).get(0);
			Expression callingExp = oldState.getExpression();
			oldState.setExpression(newState);
			newState.setExpression(callingExp);
		} else { // append state
			Expression callingExpr = insertionPoint.getExpression();
			insertionPoint.setExpression(newState);
			newState.setExpression(callingExpr);
		}

		// Update IDs
		if (isReplacing) {
			List<Expression> toRemove = state2Exp.get(id);
			int index = fsm2Exp.get("/").indexOf(toRemove.get(0));
			fsm2Exp.get("/").removeAll(toRemove);
			fsm2Exp.get("/").add(index, newState);
			shiftUpIds(state2Exp, id);
		} else {
			int index = fsm2Exp.get("/").indexOf(insertionPoint);
			fsm2Exp.get("/").add(index - 1, newState);
		}
		state2Exp.put(id, new ArrayList<Expression>());
		state2Exp.get(id).add(newState);
	}

	public void addTransition(String id) {
		// Update real AST
		MethodInvocation newTransition = buildMethodInvocation(TARGET, ast);
		MethodInvocation insertionPoint = findInsertionPoint(id);

		boolean isReplacing = transition2Exp.get(id) != null;
		if (isReplacing) { // insert before old transition
			MethodInvocation oldTrans = (MethodInvocation) transition2Exp.get(id).get(0);
			Expression callingExp = oldTrans.getExpression();
			oldTrans.setExpression(newTransition);
			newTransition.setExpression(callingExp);
		} else {
			Expression callingExpr = insertionPoint.getExpression();
			insertionPoint.setExpression(newTransition);
			newTransition.setExpression(callingExpr);
		}

		// Update IDs
		if (isReplacing) {
			Expression oldTansition = transition2Exp.get(id).get(0);
			int index = fsm2Exp.get("/").indexOf(oldTansition);
			fsm2Exp.get("/").add(index, newTransition);
			String stateId = id.substring(0, id.lastIndexOf("/"));
			int indexInState = state2Exp.get(stateId).indexOf(oldTansition);
			state2Exp.get(stateId).add(indexInState, newTransition);
			shiftUpIds(transition2Exp, id);
		} else {
			int index = fsm2Exp.get("/").indexOf(insertionPoint);
			fsm2Exp.get("/").add(index - 1, newTransition);

			String stateId = id.substring(0, id.lastIndexOf("/"));
			int indexInState = state2Exp.get(stateId).indexOf(insertionPoint);
			if (indexInState == -1) {
				state2Exp.get(stateId).add(newTransition);
			} else {
				state2Exp.get(stateId).add(indexInState - 1, newTransition);
			}
		}

		transition2Exp.put(id, new ArrayList<Expression>());
		transition2Exp.get(id).add(newTransition);
	}

	/*
	 * Increment the number part of all IDs upper to id's number (included)
	 */
	private void shiftUpIds(Map<String, List<Expression>> id2Exp, String id) {
		int idx = id.lastIndexOf(".");
		String basePart = id.substring(0, idx + 1);
		String numberPart = id.substring(idx + 1);

		Integer lowerBound = Integer.decode(numberPart);
		int upperBound = id2Exp.entrySet().size() - 1;

		for (int i = upperBound; i >= lowerBound; i--) {
			List<Expression> exp = id2Exp.get(basePart + i);
			id2Exp.put(basePart + (i + 1), exp);
		}
	}

	/*
	 * Decrement the number part of all IDs upper to id's number (excluded)
	 */
	private void shiftDownIds(Map<String, List<Expression>> id2Exp, String id) {
		int idx = id.lastIndexOf(".");
		String basePart = id.substring(0, idx + 1);
		String numberPart = id.substring(idx + 1);

		Integer lowerBound = Integer.decode(numberPart);
		int upperBound = id2Exp.entrySet().size() - 1;

		for (int i = lowerBound + 1; i <= upperBound; i++) {
			List<Expression> exp = id2Exp.get(basePart + i);
			id2Exp.put(basePart + (i - 1), exp);
		}

		id2Exp.put(basePart + upperBound, null); // remove the last one
	}

	public void deleteMachine(String id) {
		// TODO huu ?
	}

	public void deleteState(String id) {
		List<Expression> toRemove = state2Exp.get(id);
		if(toRemove != null) {
			MethodInvocation deletedState = (MethodInvocation) toRemove.get(0);
			MethodInvocation insertionPoint = findInsertionPoint(id);
			Expression callingExp = deletedState.getExpression();
			deletedState.setExpression(null);
			insertionPoint.setExpression(callingExp);
			
			fsm2Exp.get("/").removeAll(toRemove);
			state2Exp.put(id, null);
			shiftDownIds(state2Exp, id);
		}
	}

	public void deleteTransition(String id) {
		List<Expression> toRemove = transition2Exp.get(id);
		if(toRemove != null) {
			MethodInvocation deletedTrans = (MethodInvocation) toRemove.get(0);
			MethodInvocation insertionPoint = findInsertionPoint(id);
			Expression callingExp = deletedTrans.getExpression();
			deletedTrans.setExpression(null);
			insertionPoint.setExpression(callingExp);
			
			fsm2Exp.get("/").removeAll(toRemove);
			String stateId = id.substring(0, id.lastIndexOf("/"));
			state2Exp.get(stateId).removeAll(toRemove);
			transition2Exp.put(id, null);
			shiftDownIds(transition2Exp, id);
		}
	}

	public void setFsm_Name(String id, String value) {
		ClassInstanceCreation newFsm = (ClassInstanceCreation) fsm2Exp.get("/").get(0);
		newFsm.arguments().clear();
		StringLiteral stringLit = ast.newStringLiteral();
		stringLit.setLiteralValue(value);
		newFsm.arguments().add(stringLit);

	}

	public void setFsm_initial(String id, String value) {
		List<Expression> calls = state2Exp.get(value);
		if(calls != null) {
			MethodInvocation state = (MethodInvocation) calls.get(0);
			state.getName().setIdentifier(INITIAL);
		}
	}

	public void setState_Name(String id, String value) {
		List<Expression> calls = state2Exp.get(id);
		if(calls != null) {
			MethodInvocation state = (MethodInvocation) calls.get(0);
			state.arguments().clear();
			StringLiteral stringLit = ast.newStringLiteral();
			stringLit.setLiteralValue(value);
			state.arguments().add(stringLit);
		}
	}

	public void setTransition_Target(String id, String value) {
		List<Expression> calls = state2Exp.get(value);
		if(calls != null) {
			MethodInvocation state = (MethodInvocation) state2Exp.get(value).get(0);
			StringLiteral name = (StringLiteral) state.arguments().get(0);
			
			MethodInvocation target = (MethodInvocation) transition2Exp.get(id).get(0);
			target.arguments().clear();
			StringLiteral stringLit = ast.newStringLiteral();
			stringLit.setLiteralValue(name.getLiteralValue());
			target.arguments().add(stringLit);
		}
	}

	public void setTransition_Event(String id, String value) {
		if (transition2Exp.get(id).size() == 2) {
			MethodInvocation on = (MethodInvocation) transition2Exp.get(id).get(1);
			on.arguments().clear();
			StringLiteral stringLit = ast.newStringLiteral();
			stringLit.setLiteralValue(value);
			on.arguments().add(stringLit);
		} else {
			MethodInvocation insertionPoint = findInsertionPoint(id);
			MethodInvocation on = buildMethodInvocation(ON, ast);
			StringLiteral stringLit = ast.newStringLiteral();
			stringLit.setLiteralValue(value);
			on.arguments().clear();
			on.arguments().add(stringLit);
			Expression callingExp = insertionPoint.getExpression();
			insertionPoint.setExpression(on);
			on.setExpression(callingExp);
		}
	}

	public void unsetTransition_Event(String id) {
		MethodInvocation target = (MethodInvocation) transition2Exp.get(id).get(0);
		MethodInvocation on = (MethodInvocation) transition2Exp.get(id).get(1);
		MethodInvocation insertionPoint = findInsertionPoint(id);
		on.setExpression(null);
		insertionPoint.setExpression(target);

		fsm2Exp.get("/").remove(on);
		String stateId = id.substring(0, id.indexOf("/"));
		state2Exp.get(stateId).remove(on);
		transition2Exp.get(id).remove(on);
	}

	/*
	 * Return the next state() or transition() if it exists. Return end() in any
	 * other case
	 */
	private MethodInvocation findInsertionPoint(String id) {

		if (id.equals("/")) {
			return (MethodInvocation) fsm2Exp.get("/").get(fsm2Exp.get("/").size() - 1);
		}

		String[] segments = id.substring(2).split("/"); // remove first '//' then split

		if (segments.length == 1) { // state
			// Find next state
			int idx = id.lastIndexOf(".");
			String numberPart = id.substring(idx + 1);
			Integer number = Integer.decode(numberPart);
			String nextStateLocation = id.substring(0, idx + 1) + (number + 1);
			List<Expression> nextState = state2Exp.get(nextStateLocation);
			if (nextState != null) {
				return (MethodInvocation) nextState.get(0);
			}
		} else if (segments.length == 2) { // transition
			// Find next transition
			int idx = id.lastIndexOf(".");
			String numberPart = id.substring(idx + 1);
			Integer number = Integer.decode(numberPart);
			String nextTransitionLocation = id.substring(0, idx + 1) + (number + 1);
			List<Expression> nextTranstion = transition2Exp.get(nextTransitionLocation);
			if (nextTranstion != null) {
				return (MethodInvocation) nextTranstion.get(0);
			}
			else { //look for next state
				String stateID =  "//"+segments[0];
				return findInsertionPoint(stateID);
			}
		}

		return (MethodInvocation) fsm2Exp.get("/").get(fsm2Exp.get("/").size() - 1);
	}

	private ClassInstanceCreation buildFsm(AST ast) {
		ClassInstanceCreation newInstance = ast.newClassInstanceCreation();
		newInstance.setType(ast.newSimpleType(ast.newSimpleName("Fsm")));

		StringLiteral emtpyString = ast.newStringLiteral();
		newInstance.arguments().add(emtpyString);

		return newInstance;
	}

	private MethodInvocation buildMethodInvocation(String name, AST ast) {
		MethodInvocation res = ast.newMethodInvocation();
		res.setName(ast.newSimpleName(name));

		StringLiteral emtpyString = ast.newStringLiteral();
		res.arguments().add(emtpyString);

		return res;
	}

	public Optional<String> getFsm_Name(String id) {
		List<Expression> fsm = fsm2Exp.get(id);

		if (fsm != null) {
			ClassInstanceCreation newFsm = (ClassInstanceCreation) fsm.get(0);
			StringLiteral arg = (StringLiteral) newFsm.arguments().get(0);
			return Optional.of(arg.getLiteralValue());
		}

		return Optional.empty();
	}

	public Optional<String> getFsm_Initial(String id) {

		for (Entry<String, List<Expression>> entry : state2Exp.entrySet()) {
			MethodInvocation state = (MethodInvocation) entry.getValue().get(0);
			if (state.getName().getIdentifier().equals(INITIAL)) {
				return Optional.of(entry.getKey());
			}
		}

		return Optional.empty();
	}

	public Optional<String> getState_Name(String id) {

		List<Expression> state = state2Exp.get(id);
		if (state != null) {
			MethodInvocation stateCall = (MethodInvocation) state.get(0);
			StringLiteral arg = (StringLiteral) stateCall.arguments().get(0);
			return Optional.of(arg.getLiteralValue());
		}

		return Optional.empty();
	}

	public Optional<String> getTransition_Target(String id) {

		List<Expression> trans = transition2Exp.get(id);
		if (trans != null) {
			MethodInvocation transCall = (MethodInvocation) trans.get(0);
			StringLiteral arg = (StringLiteral) transCall.arguments().get(0);
			String targetedStateName = arg.getLiteralValue();
			for(Entry<String, List<Expression>> entry : state2Exp.entrySet()) {
				MethodInvocation stateCall = (MethodInvocation)entry.getValue().get(0);
				StringLiteral stateArg = (StringLiteral) stateCall.arguments().get(0);
				if(targetedStateName.equals(stateArg.getLiteralValue())) {
					return Optional.of(entry.getKey()); //return the ID of the state matching the target's name
				}
			}
		}

		return Optional.empty();
	}

	public Optional<String> getTransition_Event(String id) {

		List<Expression> trans = transition2Exp.get(id);
		if (trans != null && trans.size() == 2) {
			MethodInvocation transCall = (MethodInvocation) trans.get(1);
			StringLiteral arg = (StringLiteral) transCall.arguments().get(0);
			return Optional.of(arg.getLiteralValue());
		}

		return Optional.empty();
	}
	
	public List<String> getStates() {
		return
			state2Exp
			.entrySet()
			.stream()
			.map(entry -> entry.getKey())
			.collect(Collectors.toList());
	}
	
	public List<String> getTransitions() {
		return
			transition2Exp
			.entrySet()
			.stream()
			.map(entry -> entry.getKey())
			.collect(Collectors.toList());
	}
}
