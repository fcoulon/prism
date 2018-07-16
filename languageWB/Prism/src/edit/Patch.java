package edit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Patch {

	String sourceID;
	List<Edit> edits;

	public Patch(String sourceID) {
		edits = new ArrayList<Edit>();
		this.sourceID = sourceID;
		// edits.add(new Create("/","Machine"));
		// edits.add(new Create("//state.1","State"));
		// edits.add(new Create("//state.1/transition.0","Trans"));
		// edits.add(new Set("//state.1/transition.0","event","pastek"));
		// edits.add(new Set("//state.1/transition.0","target","//state.0"));
	}

	public List<Edit> getEdits() {
		return edits;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Edit edit : edits) {
			if (edit instanceof Create) {
				sb.append("Create "+  edit.getId() + " " + ((Create) edit).getType() + "\n");
			} else if (edit instanceof Delete) {
				sb.append("Delete "+  edit.getId() + "\n");
			} else if (edit instanceof Set) {
				sb.append("Set "+  edit.getId() + " " + ((Set) edit).getField() + " = " + ((Set) edit).getValue() + "\n");
			} else if (edit instanceof UnSet) {
				sb.append("UnSet "+  edit.getId() + " " + ((UnSet) edit).getField() + "\n");
			} else if (edit instanceof Insert) {
				sb.append("Insert "+  edit.getId() + " " + ((Insert) edit).getField() + " = " + ((Insert) edit).getValue() + " at " + ((Insert) edit).getIndex() + "\n");
			} else if (edit instanceof Remove) {
				sb.append("Remove "+  edit.getId() + " " + ((Remove) edit).getField() + " at " + ((Remove) edit).getIndex() + "\n");
			}
		}
		return sb.toString();
	}
	
	public String getSourceID() {
		return sourceID;
	}
}
