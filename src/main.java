import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;



public class main {

	public static void main(String args[]) throws IOException {
		
		
		RandomAccessFile systemCatFile = new RandomAccessFile("syscatalog.dat", "rw");
		initializeSystemCatalog(systemCatFile);
		System.out.print(systemCatFile.length());
		systemCatFile.close();
	}
	public static void initializeSystemCatalog(RandomAccessFile _systemCatFile) throws IOException
	{
		byte[] page = new byte[1600];
		
		for(int i =0;i<100;i++) {
			
				for(int j=0;j<1600;j++) {
					_systemCatFile.write(page[j]);
				}
				
			
		}
		
	}
}
