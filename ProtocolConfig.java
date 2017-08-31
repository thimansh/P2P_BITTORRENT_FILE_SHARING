import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class ProtocolConfig {
	
	 int numberOfPreferredNeighbors;
	 int unchokingInterval;
	 int optimisticUnchokingInterval;
	 String fileName;
	 int fileSize;
	 int pieceSize;
	 
	 public ProtocolConfig(){
		 
	 }
	 
	 int getNumPrefNeighbours(){
		 return numberOfPreferredNeighbors;
	 }
	
	 int getUnChokingInterval(){
		 return unchokingInterval;
	 }
	 
	 int getOptimisticUnchokingInterval(){
		 return optimisticUnchokingInterval;
	 }
	 
	 String getfileName(){
		 return fileName;
	 }
	 
	 int getFileSize(){
		 return fileSize;
	 }
	 
	 int getPieceSize(){
		 return pieceSize;
	 }
	 
	public void parseConfigFile() {
		BufferedReader rd;

		try {
			File file = new File("Common.cfg");
			String line;
			rd = new BufferedReader(new FileReader(file));

			while ((line = rd.readLine()) != null) {

				if (line.contains("NumberOfPreferredNeighbors "))
					numberOfPreferredNeighbors = Integer.parseInt((
						line.split("NumberOfPreferredNeighbors ")[1]));

				else if (line.contains("OptimisticUnchokingInterval "))
					optimisticUnchokingInterval = Integer.parseInt((
						line.split("OptimisticUnchokingInterval ")[1]));

				else if (line.contains("UnchokingInterval "))
					unchokingInterval = Integer.parseInt((
						line.split("UnchokingInterval ")[1]));

				else if (line.contains("FileName "))
					fileName = line.split("FileName ")[1];

				else if (line.contains("FileSize "))
					fileSize = Integer.parseInt((
						line.split("FileSize ")[1]));

				else if (line.contains("PieceSize "))
					pieceSize = Integer.parseInt((
						line.split("PieceSize ")[1]));
				else
					throw new Exception();
			}

			rd.close();
			
		} catch (Exception e) {
			System.out.println("Some error in the common.cfg file .");
			System.exit(0);
		}

	}
}
