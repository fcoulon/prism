package slebus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;

import edit.Create;
import edit.Delete;
import edit.Edit;
import edit.Insert;
import edit.Patch;
import edit.Set;
import edit.UnSet;

public class FsmComparator {
	
	/**
	 * Return a Patch appliable on fsm1 to equal fsm2
	 */
	public static Patch compare(String sourceID, AstAccessor fsm1, AstAccessor fsm2) {
		
		List<Edit> edits = new ArrayList<>();
		
		String fsmName1 = fsm1.getFsm_Name("/").get();
		String fsmName2 = fsm2.getFsm_Name("/").get();
		
		if(!fsmName1.equals(fsmName2)) {
			edits.add(new Set("/", "name", fsmName2));
		}
		
		//Make fsm1 subset of fsm2
		for(String stateID : fsm1.getStates()) {
			String stateName1 = fsm1.getState_Name(stateID).get();
			Optional<String> stateName2 = fsm2.getState_Name(stateID);
			
			if(!stateName2.isPresent()) {
				edits.add(new Delete(stateID));
			}
			else if(!stateName2.get().equals(stateName1)) {
				edits.add(new Set(stateID, "name", stateName2.get()));
			}
		}
		
		for(String transID : fsm1.getTransitions()) {
			Optional<String> transEvent1 = fsm1.getTransition_Event(transID);
			Optional<String> transEvent2 = fsm2.getTransition_Event(transID);
			if(transEvent1.isPresent()) {
				if(!transEvent2.isPresent()) {
					edits.add(new UnSet(transID,"event"));
				}
				else if(!transEvent2.get().equals(transEvent1.get())) {
					edits.add(new Set(transID, "event", transEvent2.get()));
				}
				
				Optional<String> transTarget1 = fsm1.getTransition_Target(transID);
				Optional<String> transTarget2 = fsm2.getTransition_Target(transID);
				
				if(!transTarget2.isPresent()) {
					edits.add(new Delete(transID));
				}
				else if(transTarget1.isPresent() && !transTarget2.get().equals(transTarget1.get())) {
					edits.add(new Set(transID, "target", transTarget2.get()));
				}
			}
		}
		
		//Complete fsm1 with fsm2 stuff
		List<String> statesIDs = new ArrayList<>(fsm2.getStates());
		Collections.sort(statesIDs);
		for(String stateID : statesIDs) {
			String stateName2 = fsm2.getState_Name(stateID).get();
			Optional<String> stateName1 = fsm1.getState_Name(stateID);
			
			if(!stateName1.isPresent()) {
				edits.add(new Create(stateID, "State"));
				edits.add(new Set(stateID, "name", stateName2));
//				edits.add(new Insert("/","states",stateID,0));
			}
		}
		
		List<String> transIDs = new ArrayList<>(fsm2.getTransitions());
		Collections.sort(statesIDs);
		for(String transID : transIDs) {
			Optional<String> transTarget2 = fsm2.getTransition_Target(transID);
			if(transTarget2.isPresent()) {
				Optional<String> transTarget1 = fsm1.getTransition_Target(transID);
				
				if(!transTarget1.isPresent()) {
					edits.add(new Create(transID, "Trans"));
					edits.add(new Set(transID, "target", transTarget2.get()));
				}
				
				Optional<String> transEvent2 = fsm2.getTransition_Event(transID);
				Optional<String> transEvent1 = fsm1.getTransition_Event(transID);
				
				if(transEvent2.isPresent() && !transEvent1.isPresent()) {
					edits.add(new Set(transID,"event",transEvent2.get()));
				}
			}
		}
		
		if(!fsm1.getFsm_Initial("/").isPresent() && fsm2.getFsm_Initial("/").isPresent()) {
			edits.add(new Set("/","initial",fsm2.getFsm_Initial("/").get()));
		}
		else if(fsm1.getFsm_Initial("/").isPresent() && !fsm2.getFsm_Initial("/").isPresent()){
			edits.add(new UnSet("/","initial"));
		}
		
		Patch patch = new Patch(sourceID);
		patch.getEdits().addAll(edits);
		
		return patch;
	}
	
	public static Patch fsmToPatch(String sourceID, AstAccessor fsm) {
		AST ast = AST.newAST(AST.JLS8);
		List<Expression> oldFsm = AstUpdater.createEmptyFSM(ast);
		AstAccessor fsm1 = new AstAccessor(oldFsm);
		Patch p = compare(sourceID, fsm1, fsm);
		p.getEdits().add(0, new Create("/", "Machine"));
		return p;
	}
}
