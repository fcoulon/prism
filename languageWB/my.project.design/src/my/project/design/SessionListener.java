package my.project.design;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManagerListener;
import org.eclipse.sirius.viewpoint.description.Viewpoint;

public class SessionListener implements SessionManagerListener {

	private URI watchedResource = URI.createURI("platform:/resource/example/Simple.myfsm");

	@Override
	public void notifyAddSession(Session newSession) {
		
		System.out.println("Start listening Session");
		
		IFile emfFile = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember("example/Simple.myfsm");
		Optional<EmfProducer> emfProducer = createProducerFor(newSession, watchedResource);
		
		if(emfFile != null && emfProducer.isPresent()) {
			ide.Activator.getWorkspaceListener().getBus().subscribe(emfProducer.get(), "FSM");
			ide.Activator.getWorkspaceListener().addNotifiyngFile(emfFile, emfProducer.get());
		}
	}
	
	private Optional<EmfProducer> createProducerFor(Session newSession, URI resourceURI) {
		for (Resource resource : newSession.getSemanticResources()) {
			if(resource.getURI().equals(watchedResource)) {
				return Optional.of(new EmfProducer(newSession, resource));
			}
		}
		return Optional.empty();
	}

	@Override
	public void notifyRemoveSession(Session removedSession) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void viewpointSelected(Viewpoint selectedSirius) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void viewpointDeselected(Viewpoint deselectedSirius) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notify(Session updated, int notification) {
		// TODO Auto-generated method stub
		
	}

}
