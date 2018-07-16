package slebus.ui.parts;

import org.eclipse.jface.viewers.TableViewer;

public class RascalEditingSupport extends ModelEditingSupport {

	public RascalEditingSupport(TableViewer viewer) {
		super(viewer);
	}

	@Override
	protected Object getValue(Object element) {
		Model model = (Model) element;
		return model.getRascalFile();
	}

	@Override
	protected void setValue(Object element, Object value) {
		Model model = (Model) element;
		model.setRascalFile((String) value);
		viewer.update(element, null);
	}
	
}
