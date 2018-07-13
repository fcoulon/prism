package slebus.ui.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;

import ide.Activator;
import prism.Bus;
import prism.Stream;

public class SLEbusView {
	private Label myLabelInView;
	private TableViewer viewer;
	
//	private List<Model> data = Arrays.asList(new Model("Doors","doors.myfsm", "Main.java", "doors.mf"));
	private List<Model> data = Arrays.asList();
	
	private Bus bus;

	@PostConstruct
	public void createPartControl(Composite parent) {
		System.out.println("Enter in SampleE4View postConstruct");

//		myLabelInView = new Label(parent, SWT.BORDER);
//		myLabelInView.setText("This is a sample E4 view");
		
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
	            | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);

		createColumns(viewer);
		
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		data = provideData();
		viewer.setInput(data);
		
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
	}
	
	private List<Model> provideData() {
		bus = Activator.getWorkspaceListener().getBus();
		
		ArrayList<Model> res = new ArrayList<Model>();
		
		for (Stream stream: bus.getStreams()) {
			List<String> consumerIds = stream.getConsumers().stream().map(c -> c.getId()).collect(Collectors.toList());
			
			//FIXME: dirty hack
			java.util.Optional<String> javaConsumerId = consumerIds.stream().filter(id -> id.toLowerCase().contains("java")).findFirst();
			java.util.Optional<String> emfConsumerId = consumerIds.stream().filter(id -> id.toLowerCase().contains("myfsm")).findFirst();
			java.util.Optional<String> rascalConsumerId = consumerIds.stream().filter(id -> id.toLowerCase().contains("mf")).findFirst();
			
			String javaId = "";
			String emfId = "";
			String rascalId = "";
			
			if(javaConsumerId.isPresent()) javaId = javaConsumerId.get();
			if(emfConsumerId.isPresent()) emfId = emfConsumerId.get();
			if(rascalConsumerId.isPresent()) rascalId = rascalConsumerId.get();
			
			res.add(new Model(stream.getId(),emfId, javaId, rascalId));
		}
		
		return res;
	}

	private void createColumns(TableViewer viewer2) {
		
		TableViewerColumn colName = new TableViewerColumn(viewer, SWT.NONE);
		colName.getColumn().setWidth(200);
		colName.getColumn().setText("Model");
		colName.setLabelProvider(new ColumnLabelProvider() {
		    @Override
		    public String getText(Object element) {
		    	Model model = (Model) element;
		        return model.getName();
		    }
		});
		colName.setEditingSupport(new ModelEditingSupport(viewer));
		
		TableViewerColumn colEmf = new TableViewerColumn(viewer, SWT.NONE);
		colEmf.getColumn().setWidth(200);
		colEmf.getColumn().setText("EMF");
		colEmf.setLabelProvider(new ColumnLabelProvider() {
		    @Override
		    public String getText(Object element) {
		    	Model model = (Model) element;
		        return model.getEmfFile();
		    }
		});
		colEmf.setEditingSupport(new EmfEditingSupport(viewer));
		
		TableViewerColumn colJava = new TableViewerColumn(viewer, SWT.NONE);
		colJava.getColumn().setWidth(200);
		colJava.getColumn().setText("Java");
		colJava.setLabelProvider(new ColumnLabelProvider() {
		    @Override
		    public String getText(Object element) {
		    	Model model = (Model) element;
		        return model.getJavaFile();
		    }
		});
		colJava.setEditingSupport(new JavaEditingSupport(viewer));
		
		TableViewerColumn colRascal = new TableViewerColumn(viewer, SWT.NONE);
		colRascal.getColumn().setWidth(200);
		colRascal.getColumn().setText("Rascal");
		colRascal.setLabelProvider(new ColumnLabelProvider() {
		    @Override
		    public String getText(Object element) {
		    	Model model = (Model) element;
		        return model.getRascalFile();
		    }
		});
		colRascal.setEditingSupport(new RascalEditingSupport(viewer));
	}

	@Focus
	public void setFocus() {
		//myLabelInView.setFocus();

	}

	/**
	 * This method is kept for E3 compatiblity. You can remove it if you do not
	 * mix E3 and E4 code. <br/>
	 * With E4 code you will set directly the selection in ESelectionService and
	 * you do not receive a ISelection
	 * 
	 * @param s
	 *            the selection received from JFace (E3 mode)
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) ISelection s) {
		if (s==null || s.isEmpty())
			return;

		if (s instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) s;
			if (iss.size() == 1)
				setSelection(iss.getFirstElement());
			else
				setSelection(iss.toArray());
		}
	}

	/**
	 * This method manages the selection of your current object. In this example
	 * we listen to a single Object (even the ISelection already captured in E3
	 * mode). <br/>
	 * You should change the parameter type of your received Object to manage
	 * your specific selection
	 * 
	 * @param o
	 *            : the current object received
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object o) {

		// Remove the 2 following lines in pure E4 mode, keep them in mixed mode
		if (o instanceof ISelection) // Already captured
			return;

		// Test if label exists (inject methods are called before PostConstruct)
		if (myLabelInView != null)
			myLabelInView.setText("Current single selection class is : " + o.getClass());
	}

	/**
	 * This method manages the multiple selection of your current objects. <br/>
	 * You should change the parameter type of your array of Objects to manage
	 * your specific selection
	 * 
	 * @param o
	 *            : the current array of objects received in case of multiple selection
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object[] selectedObjects) {

		// Test if label exists (inject methods are called before PostConstruct)
		if (myLabelInView != null)
			myLabelInView.setText("This is a multiple selection of " + selectedObjects.length + " objects");
	}
}
