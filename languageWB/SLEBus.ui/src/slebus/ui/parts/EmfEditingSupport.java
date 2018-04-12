package slebus.ui.parts;

import org.eclipse.jface.viewers.TableViewer;

public class EmfEditingSupport extends ModelEditingSupport {

	public EmfEditingSupport(TableViewer viewer) {
		super(viewer);
	}
	
	@Override
	protected Object getValue(Object element) {
		Model model = (Model) element;
		return model.getEmfFile();
	}

	@Override
	protected void setValue(Object element, Object value) {
		Model model = (Model) element;
		model.setEmfFile((String) value);
		viewer.update(element, null);
	}

}
