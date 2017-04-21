import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class PeerConfig {

	 int[] peerIds;
	 String[] hostNames;
	 int[] portNumbers;
	 boolean[] hasFile;
	 int peer_count;
	
	 public PeerConfig(){
		 
	 }
	 
	 public void parsePeerInfoFile() {
			BufferedReader reader;
			Scanner sc;

			try {
				File file = new File("PeerInfo.cfg");
				peer_count = 0;
				String line;
	 			reader = new BufferedReader(new FileReader(file));
				while (reader.readLine() != null) {
	 				peer_count++;
	 			}
	 			reader.close();

	 			reader = new BufferedReader(new FileReader(file));
	 			peerIds = new int[peer_count];
	 			hostNames = new String[peer_count];
	 			portNumbers = new int[peer_count];
				hasFile = new boolean[peer_count];

				for (int i = 0; i < peer_count; i++) {

					if ((line = reader.readLine()) == null)
						throw new Exception();

					sc = new Scanner(line);
					peerIds[i] = sc.nextInt();
					hostNames[i] = sc.next();
					portNumbers[i] = sc.nextInt();
					hasFile[i] = (sc.nextInt() == 1);

				}

			} catch (Exception e) {
				System.out.println("Error reading peer configuration file");
				System.exit(0);
			}
		}
	 
	 public int[] getPeerIDArr(){
		 return peerIds;
	 }
	 
	 public int getPeerIDCount(){
		 return peer_count;
	 }
	 
	 public String[] gethostNamesArr(){
		 return hostNames;
	 }
	 
	 public int[] getportNumbersArr(){
		 return portNumbers;
	 }
	 
	 public boolean[] gethasFileArr(){
		 return hasFile;
	 }
}
