package myfsm.menu;


import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

import myfsm.Machine;

public class AddStateTrafo implements org.eclipse.ui.IObjectActionDelegate {
	private Machine m;

	@Override
	public void run(IAction action) {
		if (m != null) {
			Command cmd = lang.ecore.bridge.EMFBridge.runRascal("myfsm.model.edit", 
					m, "lang::myfsm::Trafos", "runAddState"); 
			AdapterFactoryEditingDomain.getEditingDomainFor(m).getCommandStack().execute(cmd);
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		m = (Machine) ((IStructuredSelection) selection).getFirstElement();
	}

	@Override
	public void setActivePart(IAction arg0, IWorkbenchPart arg1) {
		// Ignore
	}
}
