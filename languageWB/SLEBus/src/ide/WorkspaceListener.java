package ide;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lang.ecore.bridge.EMFBridge;
import prism.Bus;
import prism.Producer;
import prism.java.JavaConsumer;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;

import edit.Patch;

/**
 * Watch for save operation on specific files
 */
public class WorkspaceListener implements IResourceChangeListener {
	
	Bus bus = new Bus();
	Map<IFile,Producer> notifier = new HashMap<>();
	
	/*
	 *  count the number of save events
	 */
	int counter = -1; // init -1 to ignore the first save of the .mf file
	
	public void addNotifiyngFile(IFile file, Producer p) {
		notifier.put(file, p);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		
		boolean isContentChange = (event.getDelta().getFlags() & IResourceDelta.CONTENT) != 0;
		boolean isContentReplace = (event.getDelta().getFlags() & IResourceDelta.REPLACED) != 0;
		boolean isChange = (event.getDelta().getFlags() & IResourceDelta.CHANGED) != 0;
		
		Optional<IFile> file = getMatchingFile(event.getDelta());
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			if (file.isPresent()) {
				System.out.println("Save spotted ! ("+counter + ")");
				 if(!isConsuming()) {
					Producer producer = notifier.get(file.get());
					Patch patch = producer.produce();
					bus.publish(patch, "FSM");
				}
				counter++;
			}
		}
	}

	private Optional<IFile> getMatchingFile(IResourceDelta delta) {
		
		for (Entry<IFile, Producer> entry : notifier.entrySet()) {
			IFile file = entry.getKey();
			IResourceDelta deltaTarget = delta.findMember(file.getFullPath());
			if (deltaTarget != null) {
				return Optional.of(file);
			}
		}
		return Optional.empty();
	}

	/*
	 * FIXME: assume notifier.entrySet().size() never change
	 */
	private boolean isConsuming() {
		int numberOfCounsumer = notifier.entrySet().size(); 
		
		return (counter % numberOfCounsumer) != 0;
	}
	
	public Bus getBus() {
		return bus;
	}

}
