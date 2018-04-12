package my.project.design;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;

import edit.Create;
import edit.Insert;
import edit.Patch;
import edit.Remove;
import edit.Set;
import edit.UnSet;

/**
 * 
 * 
 * FIXME: record also recieved Patchs -> this listener is bad idea ? :(
 * 
 */
@Deprecated
public class ResourceChangeListener extends EContentAdapter {
	
	Patch patch = new Patch("Deprecated");
	
	/**
	 * Return all changes since the last call of getPatch()
	 */
	public Patch getPatch() {
		Patch res = patch;
		patch = new Patch("Deprecated");
		return res;
	}

	@Override
	public void notifyChanged(Notification notification) {
		super.notifyChanged(notification);

		try {
			int event = notification.getEventType();
			
			if(notification.getFeature() instanceof EReference) {
				EReference feature = (EReference) notification.getFeature();
				boolean isContainment = feature.isContainment();
				
				EObject source = (EObject) notification.getNotifier();
				Resource srcResource = source.eResource();
				String id = srcResource.getURIFragment(source);
				
				EObject newValue = (EObject) notification.getNewValue();
				String valueId = "";
				if(newValue != null) {
					valueId = srcResource.getURIFragment(newValue);
				}
				
				if (event == Notification.ADD) {
					if(isContainment) {
						patch.getEdits().add(new Create(valueId, newValue.eClass().getName()));
					}
					else {
						patch.getEdits().add(new Insert(id, valueId, valueId, notification.getPosition()));
					}
				} else if (event == Notification.REMOVE) {
					patch.getEdits().add(new Remove(id, feature.getName(), notification.getPosition()));
				} else if (event == Notification.SET) {
					patch.getEdits().add(new Set(id, feature.getName(), valueId));
				} else if (event == Notification.UNSET) {
					patch.getEdits().add(new UnSet(id, feature.getName()));
				}
			}
			else if(notification.getFeature() instanceof EAttribute) {
				
				EAttribute feature = (EAttribute) notification.getFeature();
				
				Object oldValue = notification.getOldValue();
				Object newValue = notification.getNewValue();
				
				String oldValueStr = oldValue == null? "" : notification.getOldValue().toString();
				String newValueStr = newValue == null? "" : notification.getNewValue().toString();
				
				EObject source = (EObject) notification.getNotifier();
				Resource srcResource = source.eResource();
				String id = srcResource.getURIFragment(source);
				
				if (event == Notification.ADD) {
					patch.getEdits().add(new Insert(id, feature.getName(), newValueStr, notification.getPosition()));
				} else if (event == Notification.REMOVE) {
					patch.getEdits().add(new Remove(id, feature.getName(), notification.getPosition()));
				} else if (event == Notification.SET) {
					patch.getEdits().add(new Set(id, feature.getName(), newValueStr));
				} else if (event == Notification.UNSET) {
					patch.getEdits().add(new UnSet(id, feature.getName()));
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
