package slebus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;

public class FsmFinder extends ASTVisitor {

	
	static final String END = "end";
	static final String FSM = "Fsm";
	
	
	List<Expression> fsm = new ArrayList<>();
	
	@Override
	public boolean visit(MethodInvocation node) {
		
		SimpleName name = node.getName();
		if(name.toString().equals(END)) {
			
			fsm = new ArrayList<>();
			fsm.add(node);
			
			/*
			 * Browse previous MethodInvocation 
			 */
			Expression currentExp = node.getExpression();
			while(currentExp instanceof MethodInvocation) {
				
				fsm.add(currentExp);
				
				MethodInvocation currentInvoke = (MethodInvocation) currentExp;
				currentExp = currentInvoke.getExpression();
			}
			
			/*
			 * Now the caller should be new Fsm()
			 */
			if(currentExp instanceof ClassInstanceCreation) {
				
				ClassInstanceCreation currentNew = (ClassInstanceCreation) currentExp;
				
				String className = "";
				if(currentNew.getType() instanceof SimpleType) {
					className = ((SimpleType)currentNew.getType()).getName().toString();
				}
				
				if(className.equals(FSM)) {
					fsm.add(currentExp);
					Collections.reverse(fsm);
				}
			}
		}
		
		return super.visit(node);
	}
	
	public List<Expression> getFsm() {
		return fsm;
	}
}
