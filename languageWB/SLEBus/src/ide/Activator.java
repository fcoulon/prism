package ide;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import slebus.AstUpdater;

public class Activator extends AbstractUIPlugin {

	static WorkspaceListener listener;
	
    public Activator() {
    	
    }
	
    public void start(BundleContext context) throws Exception {
        super.start(context);
        
        //FIXME: register through UI / extension point
        IFile sourceFile = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(AstUpdater.FSM_Project+"/src/"+AstUpdater.PKG+"/"+AstUpdater.CLASS);
        WorkspaceListener listener = getWorkspaceListener();
        listener.addNotifiyngFile(sourceFile, new JavaProducer(sourceFile));
        
        //Watch the workspace
        ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
        
        System.out.println("SLEBus started");
    }
    
    public static WorkspaceListener getWorkspaceListener() {
    	if(listener == null) {
    		listener = new WorkspaceListener();
    	}
    	return listener;
    }
}
