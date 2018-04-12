package menu;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.NullRascalMonitor;

import edit.Delete;
import edit.Patch;
import edit.Set;
import io.usethesource.vallang.IList;
import io.usethesource.vallang.IValue;
import io.usethesource.vallang.type.Type;
import lang.ecore.bridge.EMFBridge;
import rascal.RascalProducer;
import slebus.AstUpdater;
import slebus.Bus;

public class SampleHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
//		new AstUpdater().consume(new Patch());
		
		System.out.println("Menu Browse AST");
		
		System.out.println(" --- Init Rascal --- ");
		Evaluator eval = EMFBridge.getEvaluator("myfsm_rascal", "lang::myfsm::IDE");
		
		RascalProducer slebus = new RascalProducer(eval.getValueFactory());
		Patch patch = new Patch("TestPatch");
		patch.getEdits().add(new Set("project://TestIt/src/doors.myfsm#//@states.0/@transitions.0","event","pastek"));
		IValue p = slebus.patchToValue(patch, eval.getCurrentEnvt());
		
//		IValue pa = eval.call("barfoo2");
//		Type types = p.getType();
		eval.call("main");
		eval.call("applyPatch", p);
		System.out.println(" --- Init done --- ");
		
//		|project://TestIt/src/doors.myfsm#/|
//		[id(|project://TestIt/src/doors.myfsm#/|), [<id(|project://TestIt/src/doors.myfsm#//@states.0/@transitions.0|),put("event","open23")>]]
		
		return null;
	}
	
	
	private void initTheBus() {
		Bus bus = new Bus();
		//TODO: bus.createStream(base, "FSM");
		bus.subscribe(new AstUpdater(), "FSM");
		//TODO: others
	}
}
