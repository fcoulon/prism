package ide;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;

import edit.Patch;
import slebus.AstAccessor;
import slebus.AstUpdater;
import slebus.PatchProducer;
import slebus.Producer;

public class JavaProducer implements Producer {

	IFile srcFile;

	public JavaProducer(IFile srcFile) {
		this.srcFile = srcFile;
	}

	/**
	 * Compare current file's content with the last state of the history
	 *  
	 * @see slebus.Producer#produce()
	 */
	@Override
	public Patch produce() {
		
		List<Expression> oldFsm = AstUpdater.getFSM(getOldAst().get());
		List<Expression> newFsm = AstUpdater.getFSM(getNewAst().get());

		if(!oldFsm.isEmpty() && !newFsm.isEmpty()) {
			AstAccessor oldAst = new AstAccessor(oldFsm);
			AstAccessor newAst = new AstAccessor(newFsm);
			return PatchProducer.compare(oldAst, newAst);
		}
		
		return new Patch();
	}
	
	private Optional<CompilationUnit> getCompilationUnit(InputStream input) {
		
		BufferedReader buffer = new BufferedReader(new InputStreamReader(input));
		String source = buffer.lines().collect(Collectors.joining("\n"));
		return Optional.ofNullable(getAst(source));
		
	}
	
	private Optional<CompilationUnit> getNewAst() {
		try {
			return getCompilationUnit(srcFile.getContents());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return Optional.empty();
	}
	
	private Optional<CompilationUnit> getOldAst() {
		try {
			IFileState[] history;
			history = srcFile.getHistory(new NullProgressMonitor());

			if (history.length > 0) {
				IFileState lastState = history[0];

				return getCompilationUnit(lastState.getContents());
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return Optional.empty();
	}

	private CompilationUnit getAst(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setSource(source.toCharArray());
		return (CompilationUnit) parser.createAST(null);
	}

	@Override
	public String getID() {
		return "JavaProducer";
	}

}
