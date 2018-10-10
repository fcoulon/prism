package prism.emf;

import java.util.List;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.sirius.business.api.session.Session;

import edit.Create;
import edit.Delete;
import edit.Edit;
import edit.Insert;
import edit.Patch;
import edit.Remove;
import edit.Set;
import edit.UnSet;
import myfsm.Machine;
import myfsm.MyfsmPackage;
import prism.Consumer;

public class EmfConsumer implements Consumer {

	Session session;
	Resource model;

	public EmfConsumer(Session session, Resource model) {
		this.session = session;
		this.model = model;
	}

	@Override
	public void consume(Patch p) {
		RecordingCommand cmd = new RecordingCommand(session.getTransactionalEditingDomain()) {
			@Override
			protected void doExecute() {
				apply(p);
			}
		};
		CommandStack commandStack = session.getTransactionalEditingDomain().getCommandStack();
		commandStack.execute(cmd);
		
		Job job = Job.create("Save Sirius", (ICoreRunnable) monitor -> {
			session.save(new NullProgressMonitor());
		});
		job.schedule();
	}

	private void apply(Patch p) {
		for (Edit edit : p.getEdits()) {
			if (edit instanceof Create)
				apply((Create) edit);
			else if (edit instanceof Delete)
				apply((Delete) edit);
			else if (edit instanceof Set)
				apply((Set) edit);
			else if (edit instanceof UnSet)
				apply((UnSet) edit);
			else if (edit instanceof Insert)
				apply((Insert) edit);
			else if (edit instanceof Remove)
				apply((Remove) edit);
		}
	}

	private void apply(Remove edit) {
		EObject obj = model.getEObject(edit.getId());
		String featureName = edit.getField();
		int position = edit.getIndex();

		if (obj != null) {
			EStructuralFeature feature = obj.eClass().getEStructuralFeature(featureName);
			EList<EObject> elems = (EList<EObject>) obj.eGet(feature);
			elems.remove(position);
		}
	}

	private void apply(Insert edit) {
		EObject obj = model.getEObject(edit.getId());
		String featureName = edit.getField();
		int position = edit.getIndex();
		EObject value = model.getEObject(edit.getValue());

		if (obj != null) {
			EStructuralFeature feature = obj.eClass().getEStructuralFeature(featureName);
			EList<EObject> elems = (EList<EObject>) obj.eGet(feature);
			elems.add(position, value);
		}
	}

	private void apply(UnSet edit) {
		EObject obj = model.getEObject(edit.getId());
		String featureName = edit.getField();

		if (obj != null) {
			EStructuralFeature feature = obj.eClass().getEStructuralFeature(featureName);
			obj.eUnset(feature);
		}
	}

	private void apply(Set edit) {
		EObject obj = model.getEObject(edit.getId());
		String featureName = edit.getField();

		if (obj != null) {
			EStructuralFeature feature = obj.eClass().getEStructuralFeature(featureName);

			if (feature instanceof EReference) {
				EObject value = model.getEObject(edit.getValue());
				obj.eSet(feature, value);
			} else {
				String valueAsString = edit.getValue();
				
				switch (feature.getEType().getName()) {
				case "EBoolean":
					obj.eSet(feature, Boolean.getBoolean(valueAsString));
					break;
				case "EByte":
					obj.eSet(feature, Byte.decode(valueAsString));
					break;
				case "EChar":
					obj.eSet(feature, valueAsString.toCharArray()[0]);
					break;
				case "EDouble":
					obj.eSet(feature, Double.parseDouble(valueAsString));
					break;
				case "EFloat":
					obj.eSet(feature, Float.parseFloat(valueAsString));
					break;
				case "EInt":
					obj.eSet(feature, Integer.decode(valueAsString));
					break;
				case "ELong":
					obj.eSet(feature, Long.parseLong(valueAsString));
					break;
				case "EShort":
					obj.eSet(feature, Short.parseShort(valueAsString));
					break;
				case "EString":
					obj.eSet(feature, valueAsString);
					break;
				case "EBooleanObject":
					obj.eSet(feature, Boolean.getBoolean(valueAsString));
					break;
				case "EByteObject":
					obj.eSet(feature, Byte.decode(valueAsString));
					break;
				case "ECharacterObject":
					obj.eSet(feature, valueAsString.toCharArray()[0]);
					break;
				case "EDoubleObject":
					obj.eSet(feature, Double.parseDouble(valueAsString));
					break;
				case "EFloatObject":
					obj.eSet(feature, Float.parseFloat(valueAsString));
					break;
				case "EIntegerObject":
					obj.eSet(feature, Integer.decode(valueAsString));
					break;
				case "ELongObject":
					obj.eSet(feature, Long.parseLong(valueAsString));
					break;
				case "EShortObject":
					obj.eSet(feature, Short.parseShort(valueAsString));
					break;
				default:
					break;
				}
			}
		}
	}

	private void apply(Delete edit) {
		EObject obj = model.getEObject(edit.getId());
		if (obj != null) {
			EcoreUtil.remove(obj);
		}
	}

	private void apply(Create edit) {
		
		//FIXME: code specific to FSM :(
		// grab factory through EPackage registry ?
		
		if(model.getEObject(edit.getId()) != null) {
			return; //we ignore Create if already exits
		}
		
		String id = edit.getId();
		String type = edit.getType();
		
		EObject instance = null;
		if(type.equals("Machine")) {
			instance = MyfsmPackage.eINSTANCE.getMyfsmFactory().createMachine();
		}
		else if(type.equals("State")) {
			instance = MyfsmPackage.eINSTANCE.getMyfsmFactory().createState();
		}
		else if(type.equals("Trans")) {
			instance = MyfsmPackage.eINSTANCE.getMyfsmFactory().createTrans();
		}
		
		if(instance != null) {
			if(id.equals("/")) {
				model.getContents().clear();
				model.getContents().add(instance);
				return;
			}
			
			int lastSlash = id.lastIndexOf("@");
			int lastDot = id.lastIndexOf(".");
			
			String featureName = id.substring(lastSlash+1, lastDot);
			
			String containerID = id.substring(0, id.lastIndexOf("/"));
			EObject container = model.getEObject(containerID);
			
			EStructuralFeature feature = container.eClass().getEStructuralFeature(featureName);
			if(feature.isMany()) {
				List<EObject> contained = (List<EObject>) container.eGet(feature);
				contained.add(instance);
			}
			else {
				container.eSet(feature, instance);
			}
		}
	}

	@Override
	public String getId() {
		return model.getURI().toString();
	}

	@Override
	public void synchronize(Patch p) {
		System.out.println("Synchro\n-----------");
		System.out.println(p.toString());
		RecordingCommand cmd = new RecordingCommand(session.getTransactionalEditingDomain()) {
			@Override
			protected void doExecute() {
				Machine fsm = (Machine) model.getContents().get(0);
				fsm.setName("");
				fsm.getStates().clear();
				fsm.setInitial(null);
				apply(p);
			}
		};
		CommandStack commandStack = session.getTransactionalEditingDomain().getCommandStack();
		commandStack.execute(cmd);
		
	}
}
