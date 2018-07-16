package prism.emf;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.sirius.business.api.session.Session;

import edit.Create;
import edit.Delete;
import edit.Edit;
import edit.Patch;
import edit.Set;
import edit.UnSet;
import myfsm.Machine;
import myfsm.State;
import myfsm.Trans;
import prism.Producer;

public class EmfProducer extends EmfConsumer implements Producer{

	IFile srcFile;

	public EmfProducer(Session session, Resource model) {
		super(session,model);
		srcFile = WorkspaceSynchronizer.getFile(model);
	}

	@Override
	public Patch produce() {

		Optional<Resource> oldResource = getOldResource();
		Optional<Resource> newResource = getNewResource();

		if (oldResource.isPresent() && newResource.isPresent()) {
			return compare(oldResource.get(), newResource.get());
		}

		return new Patch(getId());
	}

	private Optional<Resource> getNewResource() {
		Resource res = null;
		try {
			res = load("newFSM", srcFile.getContents());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return Optional.ofNullable(res);
	}

	private Optional<Resource> getOldResource() {
		Resource res = null;
		try {
			IFileState[] history;
			history = srcFile.getHistory(new NullProgressMonitor());

			if (history.length > 0) {
				IFileState lastState = history[0];

				res = load("oldFSM", lastState.getContents());
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return Optional.ofNullable(res);
	}

	private Resource load(String uri, InputStream input) {
		ResourceSet rs = new ResourceSetImpl();

		Resource res = rs.createResource(URI.createURI(uri));
		try {
			res.load(input, null);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return res;
	}

	private Patch compare(Resource oldResource, Resource newResource) {

		List<Edit> edits = new ArrayList<>();

		Machine oldFsm = (Machine) oldResource.getContents().get(0);
		Machine newFsm = (Machine) newResource.getContents().get(0);

		if (!oldFsm.getName().equals(newFsm.getName())) {
			edits.add(new Set("/", "name", newFsm.getName()));
		}

		// Make fsm1 subset of fsm2
		for (State oldState : oldFsm.getStates()) {
			String oldStateID = oldResource.getURIFragment(oldState);
			State newState = (State) newResource.getEObject(oldStateID);

			if (newState == null) {
				edits.add(new Delete(oldStateID));
			} else if (!oldState.getName().equals(newState.getName())) {
				edits.add(new Set(oldStateID, "name", newState.getName()));
			}
		}
		for (State oldState : oldFsm.getStates()) {
			for (Trans oldTransition : oldState.getTransitions()) {
				String oldTransitionID = oldResource.getURIFragment(oldTransition);
				Trans newTransition = (Trans) newResource.getEObject(oldTransitionID);

				if (newTransition == null) {
					edits.add(new Delete(oldTransitionID));
				} else {
					String oldTarget = oldResource.getURIFragment(oldTransition.getTarget());
					String newTarget = newResource.getURIFragment(newTransition.getTarget());
					if (oldTarget != newTarget) {
						edits.add(new Set(oldTransitionID, "target", newTarget));
					}

					if (oldTransition.getEvent() != null && !oldTransition.getEvent().equals(newTransition.getEvent())) {
						String event = newTransition.getEvent();
						if (event == null) {
							edits.add(new UnSet(oldTransitionID, "event"));
						} else {
							edits.add(new Set(oldTransitionID, "event", newTransition.getEvent()));
						}
					}
					else if (oldTransition.getEvent() == null && newTransition.getEvent() != null) {
						edits.add(new Set(oldTransitionID, "event", newTransition.getEvent()));
					}
				}
			}
		}

		// Complete fsm1 with fsm2 stuff
		for (State newState : newFsm.getStates()) {
			String newStateID = newResource.getURIFragment(newState);
			EObject oldState = oldResource.getEObject(newStateID);

			if (oldState == null) {
				edits.add(new Create(newStateID, "State"));
				edits.add(new Set(newStateID, "name", newState.getName()));
			}
		}
		for (State newState : newFsm.getStates()) {
			for (Trans newTransition : newState.getTransitions()) {
				String newTransitionID = newResource.getURIFragment(newTransition);
				Trans oldTransition = (Trans) oldResource.getEObject(newTransitionID);

				if (oldTransition == null) {
					String newTarget = newResource.getURIFragment(newTransition.getTarget());
					edits.add(new Create(newTransitionID, "Trans"));
					edits.add(new Set(newTransitionID, "target", newTarget));
					if (newTransition.getEvent() != null) {
						edits.add(new Set(newTransitionID, "event", newTransition.getEvent()));
					}
				} else {
					String event = newTransition.getEvent();
					if (event == null && oldTransition.getEvent() != null) {
						edits.add(new UnSet(newTransitionID, "event"));
					} else if (event != null && !event.equals(oldTransition.getEvent())) {
						edits.add(new Set(newTransitionID, "event", event));
					}
				}
			}
		}
		
		if(oldFsm.getInitial() != newFsm.getInitial() && newFsm.getInitial() != null) {
			String value = newResource.getURIFragment(newFsm.getInitial());
			edits.add(new Set("/", "initial", value));
		}
		else if(oldFsm.getInitial() != null && newFsm.getInitial() == null) {
			edits.add(new UnSet("/", "initial"));
		}
		

		Patch patch = new Patch(getId());
		patch.getEdits().addAll(edits);
		return patch;
	}

	@Override
	public Patch synchronize() {
		ResourceSet rs = new ResourceSetImpl();
		Resource emptyRes = rs.createResource(URI.createURI("EmptyResource"));
		Optional<Resource> newResource = getNewResource();

		if (newResource.isPresent()) {
			return compare(emptyRes, getNewResource().get());
		}
		else {
			//TODO: error?
			return new Patch(getId());
		}
	}
}
