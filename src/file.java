import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class file extends RandomAccessFile {
	
	int currentPage=0;
	page pages[];
	
	
	public file(String path,String mode) throws IOException {
		super(path,mode);
		pages = new page[1600];
		for(int i =1;i<=160000;i++) { // Initialization
			
			this.write("0".getBytes());
		
		}
		if(currentPage == 0) {
			pages[currentPage].writeLocation = 11;  // Because we dont want to writeLocation and numOfFull pages.
		}else {
			pages[currentPage].writeLocation = 10;
		}
		
		
	}


	@Override
	public void write(byte[] arg0) throws IOException {
		if(pages[currentPage].writeLocation + arg0.length >= 1600) {
			pages[currentPage].writeLocation += arg0.length;
		}
		super.write(arg0);
	}


	@Override
	public void write(int arg0) throws IOException {
		if(pages[currentPage].writeLocation + 1 >= 1600) {
			pages[currentPage].writeLocation ++;
		}
		super.write(arg0);
	}
	
	
}
