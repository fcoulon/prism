package slebus.ui.parts;

import org.eclipse.jface.viewers.TableViewer;

public class JavaEditingSupport extends ModelEditingSupport {

	public JavaEditingSupport(TableViewer viewer) {
		super(viewer);
	}

	@Override
	protected Object getValue(Object element) {
		Model model = (Model) element;
		return model.getJavaFile();
	}

	@Override
	protected void setValue(Object element, Object value) {
		Model model = (Model) element;
		model.setJavaFile((String) value);
		viewer.update(element, null);
	}
	
}
