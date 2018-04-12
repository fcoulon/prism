package ide;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import io.usethesource.vallang.impl.persistent.ValueFactory;
import rascal.RascalProducer;
import slebus.AstUpdater;

public class Activator extends AbstractUIPlugin {

	static WorkspaceListener listener;
	
    public Activator() {
    	
    }
	
    public void start(BundleContext context) throws Exception {
        super.start(context);
        
        ClassLoader cl = this.getClass().getClassLoader();
        
        WorkspaceListener listener = getWorkspaceListener();
        
        IFile sourceFile = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(AstUpdater.FSM_Project+"/src/"+AstUpdater.PKG+"/"+AstUpdater.CLASS);
        JavaProducer javaProducer = new JavaProducer(sourceFile);
        RascalProducer rascalProducer = new RascalProducer(ValueFactory.getInstance());
//        
        listener.getBus().createStream(javaProducer, "FSM");
        listener.getBus().subscribe(rascalProducer, "FSM");
        listener.addNotifiyngFile(sourceFile, javaProducer);
        
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
