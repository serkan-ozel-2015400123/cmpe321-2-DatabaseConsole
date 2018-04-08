import java.io.File;

public class record {
	File which;
	String typeName;
	int loc;
	int fieldN;
	int key;
	
	public record(File which, String typeName, int loc, int fieldN, int key) {
		this.which = which;
		this.typeName = typeName;
		this.loc = loc;
		this.fieldN = fieldN;
		this.key = key;
	}
	
	
}
