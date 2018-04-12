package lang.ecore.bridge;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class Main {
	public static void main(String[] args) {
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		Resource res = rs.getResource(URI.createURI("file:/home/fcoulon/git/rascal-ecore/src/lang/ecore/tests/MyFSM.ecore"),true);
		EPackage pkg = (EPackage) res.getContents().get(0);
		System.out.println(pkg);
		
		EMFBridge.runRascal("rascal-ecore", pkg, "lang::ecore::tests::A", "foo");
	}
}
