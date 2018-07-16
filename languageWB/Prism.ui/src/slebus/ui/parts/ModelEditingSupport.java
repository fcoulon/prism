package slebus.ui.parts;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

public class ModelEditingSupport extends EditingSupport {


	protected final TableViewer viewer;
	protected final CellEditor editor;
    
    public ModelEditingSupport(TableViewer viewer) {
    	super(viewer);
    	this.viewer = viewer;
    	this.editor = new TextCellEditor(viewer.getTable());
    }
	
	@Override
	protected CellEditor getCellEditor(Object element) {
		return editor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		Model model = (Model) element;
		return model.getName();
	}

	@Override
	protected void setValue(Object element, Object value) {
		Model model = (Model) element;
		model.setName((String) value);
		viewer.update(element, null);
	}

}
