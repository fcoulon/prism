package slebus.ui.parts;

public class Model {
	
	String name;
	String emfFile;
	String rascalFile;
	String javaFile;
	
	public Model(String name, String emfFile, String javaFile, String rascalFile) {
		this.name = name;
		this.emfFile = emfFile;
		this.rascalFile = rascalFile;
		this.javaFile = javaFile;
	}
	
	public String getName() {
		return name;
	}
	
	public String getEmfFile() {
		return emfFile;
	}
	
	public String getJavaFile() {
		return javaFile;
	}

	public String getRascalFile() {
		return rascalFile;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public void setEmfFile(String emfFile) {
		this.emfFile = emfFile;
	}
	
	public void setJavaFile(String javaFile) {
		this.javaFile = javaFile;
	}
	
	public void setRascalFile(String rascalFile) {
		this.rascalFile = rascalFile;
	}
}
