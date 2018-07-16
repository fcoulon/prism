package prism.java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import edit.Create;
import edit.Delete;
import edit.Edit;
import edit.Insert;
import edit.Patch;
import edit.Remove;
import edit.Set;
import edit.UnSet;
import prism.AstAccessor;
import prism.Consumer;
import prism.FsmFinder;

public class JavaConsumer implements Consumer {

	public static final String FSM_Project = "foo";
	public static final String PKG = "main";
	public static final String CLASS = "Main.java";

	private AstAccessor fsmAccessor;

	private ICompilationUnit getAstRoot() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects();
		IProject targetProject = Stream.of(projects).filter(prj -> prj.getName().equals(FSM_Project)).findFirst().get(); // FIXME
		IJavaProject javaProject = JavaCore.create(targetProject);

		List<IPackageFragmentRoot> roots = new ArrayList<>();
		try {
			roots = Arrays.asList(javaProject.getAllPackageFragmentRoots());
		} catch (JavaModelException e) {
			// TODO;
		}

		IPackageFragmentRoot srcFolder = roots.stream().filter(pkg -> pkg.getElementName().equals("src")).findFirst()
				.get();

		IPackageFragment mainPkg = srcFolder.getPackageFragment(PKG);

		return mainPkg.getCompilationUnit(CLASS);
	}

	public static List<Expression> getFSM(CompilationUnit astRoot) {
		FsmFinder fsmFinder = new FsmFinder();
		astRoot.accept(fsmFinder);

		return fsmFinder.getFsm();
	}
	
	public static List<Expression> createEmptyFSM(AST ast) {
		ClassInstanceCreation newFsm = buildFsm(ast);
		MethodInvocation end = buildEndInvocation(ast);
		end.setExpression(newFsm);
		return Arrays.asList(newFsm,end);
	}
	
	private static ClassInstanceCreation buildFsm(AST ast) {
		ClassInstanceCreation newInstance = ast.newClassInstanceCreation();
		newInstance.setType(ast.newSimpleType(ast.newSimpleName("Fsm")));

		StringLiteral emtpyString = ast.newStringLiteral();
		newInstance.arguments().add(emtpyString);

		return newInstance;
	}
	
	private static MethodInvocation buildEndInvocation(AST ast) {
		MethodInvocation res = ast.newMethodInvocation();
		res.setName(ast.newSimpleName("end"));

		return res;
	}

	@Override
	public void consume(Patch patch) {
		
		Job job = Job.create("Update Java file", monitor -> {
//			IWorkspaceRunnable myRunnable = new IWorkspaceRunnable() {
//				@Override
//				public void run(IProgressMonitor monitor) throws CoreException {
//					apply(patch);
//				}
//			};
//			
//			IProject myProject = ResourcesPlugin.getWorkspace().getRoot().getProject(FSM_Project);
//			IWorkspace workspace = ResourcesPlugin.getWorkspace();
//			try {
//				workspace.run(myRunnable, myProject, IWorkspace.AVOID_UPDATE, null);
//			} catch (CoreException e) {
//				e.printStackTrace();
//			}
			
			apply(patch);
		});
		
		job.schedule();
	}
	
	public void apply(Patch patch) {
		ICompilationUnit cu = getAstRoot();

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(cu);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

		List<Expression> fsm = getFSM(astRoot);
		if(fsm.isEmpty())
			return; //TODO: raise error?
		this.fsmAccessor = new AstAccessor(fsm);

		String source;
		try {
			source = cu.getSource();
			Document document = new Document(source);

			astRoot.recordModifications();

			apply(patch.getEdits(), astRoot.getAST());

			// computation of the text edits
			TextEdit edits = astRoot.rewrite(document, cu.getJavaProject().getOptions(true));

			// computation of the new source code
			edits.apply(document);

			// update of the compilation unit
			cu.getBuffer().setContents(document.get());
			cu.getBuffer().save(null, true);

		} catch (JavaModelException e) {
			e.printStackTrace(System.out);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Dispatch to specific apply()
	 */
	public void apply(List<Edit> edits, AST ast) {
		for (Edit edit : edits) {
			if (edit instanceof Create)
				apply((Create) edit, ast);
			else if (edit instanceof Delete)
				apply((Delete) edit, ast);
			else if (edit instanceof Set)
				apply((Set) edit, ast);
			else if (edit instanceof UnSet)
				apply((UnSet) edit, ast);
			else if (edit instanceof Insert)
				apply((Insert) edit, ast);
			else if (edit instanceof Remove)
				apply((Remove) edit, ast);
		}
	}

	public void apply(Create create, AST ast) {
		String id = create.getId();
		String type = create.getType();

		if (type.equals("Machine")) {

			fsmAccessor.addMachine(id);

		} else if (type.equals("State")) {

			fsmAccessor.addState(id);

		} else if (type.equals("Trans")) {

			fsmAccessor.addTransition(id);

		} else {
			// TODO: error
		}
	}

	public void apply(Delete delete, AST ast) {
		String id = delete.getId();

		if (id.equals("/")) {

			fsmAccessor.deleteMachine(id);

			return;
		}

		String[] segments = id.substring(2).split("/"); // remove first '//' then split

		if (segments.length == 1) { // state

			fsmAccessor.deleteState(id);

		} else if (segments.length == 2) { // transition*

			fsmAccessor.deleteTransition(id);

		}
	}

	public void apply(Set set, AST ast) {
		String id = set.getId();
		String field = set.getField();
		String value = set.getValue();

		if (id.equals("/")) {
			if (field.equals("name")) {

				fsmAccessor.setFsm_Name(id, value);

			} else if (field.equals("initial")) {

				fsmAccessor.setFsm_initial(id, value);

			} else {
				// TODO: error
			}
			return;
		}

		String[] segments = id.substring(2).split("/"); // remove first '//' then split

		if (segments.length == 1) { // state
			if (field.equals("name")) {

				fsmAccessor.setState_Name(id, value);

			} else {
				// TODO: error
			}
		} else if (segments.length == 2) { // transition
			if (field.equals("target")) {

				fsmAccessor.setTransition_Target(id, value);

			} else if (field.equals("event")) {

				fsmAccessor.setTransition_Event(id, value);

			} else {
				// TODO: error
			}
		}
	}

	public void apply(UnSet unset, AST ast) {
		String id = unset.getId();
		String field = unset.getField();

		String[] segments = id.substring(2).split("/"); // remove first '//' then split

		if (segments.length == 2 && field.equals("event")) {

			fsmAccessor.unsetTransition_Event(id);

		} else {
			// TODO: error
		}
	}

	public void apply(Insert insert, AST ast) {
		// FIXME: not appliable for FSM ?
	}

	public void apply(Remove remove, AST ast) {
		String id = remove.getId();
		String field = remove.getField();
		int index = remove.getIndex();

		// TODO: just craft a Delete() command?
	}

	@Override
	public String getId() {
		return FSM_Project+"/"+PKG+"/"+CLASS;
	}

	@Override
	public void synchronize(Patch patch) {
		//FIXME: copy/past from apply(Patch)
		ICompilationUnit cu = getAstRoot();

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(cu);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);

		List<Expression> fsm = getFSM(astRoot);
		if(fsm.isEmpty())
			return; //TODO: raise error?
		this.fsmAccessor = new AstAccessor(fsm);

		String source;
		try {
			source = cu.getSource();
			Document document = new Document(source);

			astRoot.recordModifications();

			this.fsmAccessor.deleteMachine("/");
			apply(patch.getEdits(), astRoot.getAST());

			// computation of the text edits
			TextEdit edits = astRoot.rewrite(document, cu.getJavaProject().getOptions(true));

			// computation of the new source code
			edits.apply(document);

			// update of the compilation unit
			cu.getBuffer().setContents(document.get());
			cu.getBuffer().save(null, true);

		} catch (JavaModelException e) {
			e.printStackTrace(System.out);
		} catch (MalformedTreeException e) {
			e.printStackTrace();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		
	}
}
