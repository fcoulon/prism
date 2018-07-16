package prism.java;

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
import prism.AstAccessor;
import prism.FsmComparator;
import prism.Producer;

public class JavaProducer extends JavaConsumer implements Producer {

	IFile srcFile;
	String id;
	
	public JavaProducer(IFile srcFile) {
		this.srcFile = srcFile;
		this.id = srcFile.getName();
	}

	/**
	 * Compare current file's content with the last state of the history
	 *  
	 * @see prism.Producer#produce()
	 */
	@Override
	public Patch produce() {
		
		Optional<CompilationUnit> oldAst = getOldAst();
		Optional<CompilationUnit> newAst = getNewAst();

		if(oldAst.isPresent() && newAst.isPresent()) {
			List<Expression> oldFsm = JavaConsumer.getFSM(oldAst.get());
			List<Expression> newFsm = JavaConsumer.getFSM(newAst.get());
			return FsmComparator.compare(id, new AstAccessor(oldFsm), new AstAccessor(newFsm));
		}
		
		return new Patch(getId());
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
	public Patch synchronize() {
		Optional<CompilationUnit> newAst = getNewAst();

		if(newAst.isPresent()) {
			List<Expression> newFsm = JavaConsumer.getFSM(newAst.get());
			return FsmComparator.fsmToPatch(id, new AstAccessor(newFsm));
		}
		
		return new Patch(id);
	}

}
