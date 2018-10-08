package ide;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import io.usethesource.vallang.impl.persistent.ValueFactory;
import prism.java.JavaConsumer;
import prism.java.JavaProducer;
import prism.rascal.RascalProducer;

public class Activator extends AbstractUIPlugin {

	static WorkspaceListener listener;
	
    public Activator() {
    	
    }
	
    public void start(BundleContext context) throws Exception {
        super.start(context);
        
        closeRascalEditor();
        
        ClassLoader cl = this.getClass().getClassLoader();
        
        WorkspaceListener listener = getWorkspaceListener();
        
        IFile sourceFile = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(JavaConsumer.FSM_Project+"/src/"+JavaConsumer.PKG+"/"+JavaConsumer.CLASS);
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
    
    private void closeRascalEditor() {
    	IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		if(win != null) {
			IWorkbenchPage page = win.getActivePage();
			
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("TestIt");
					if (project != null && page != null) {
						IEditorInput input = new FileEditorInput(project.getFile("src/doors.mf"));
						IEditorPart editor = page.findEditor(input);
						if(editor != null) {
							page.closeEditor(editor, true);
						}
					}
				}
			});
		}
    }
    
    public static WorkspaceListener getWorkspaceListener() {
    	if(listener == null) {
    		listener = new WorkspaceListener();
    	}
    	return listener;
    }
}
