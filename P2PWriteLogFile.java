import java.io.*;
import java.util.*;

public class P2PWriteLogFile {

	public String getCurrentDateTime() {
	       Date date = new Date();
	       return (date.toString());
	   }
	
	public String fileNameExists(int PeerId1){	//this function is a sanity check for file existence
	String fName = "peer_" + PeerId1+"/log_peer_"+PeerId1+".log";
	File file1 = new File(fName);
	if (!file1.exists()) {
		newLogFile(PeerId1);
	}
	return fName;
	}
	
	public void newLogFile(int peerId)
	  {
	     try {
	    	 	String fName = "peer_" + peerId+"/log_peer_"+peerId+".log";
	            File directory = new File("peer_" + peerId);
		        directory.mkdir();
		        File file1 = new File(fName);
		        if (!file1.exists()) {
					file1.createNewFile();
				}} 
	     catch (IOException e) {
				e.printStackTrace();
			}   
	  }
	
	public void TcpMakeConnection(int peerId1, int peerId2)
	{        
		String fName=fileNameExists(peerId1);
		try
		{
			FileWriter f = new FileWriter(fName,true); 
			BufferedWriter b = new BufferedWriter(f);
			b.write(getCurrentDateTime()+": Peer "+peerId1+" makes a connection to Peer "+peerId2+".");
			b.newLine();
			b.close();
		}
		
		catch(IOException e)	
		{
			e.printStackTrace();
		}
}

	public void tcpConnect(int peerId1, int peerId2)
	  {        
		String fName=fileNameExists(peerId1);
		   try
	           {
	               FileWriter f = new FileWriter(fName,true);
	               BufferedWriter b = new BufferedWriter(f);
	               b.write(getCurrentDateTime()+": Peer "+peerId1+" is connected from Peer "+peerId2+".");	
	               b.newLine();
	               b.close();
	            }
	            catch(IOException e)
	            {
	                e.printStackTrace();
	            }
	  }
	  public void updatedListOfPreferredNeighbours(int peerId1, String prefPeerList)
	  {
		  String fName=fileNameExists(peerId1);
			        try
	           {
	               FileWriter f = new FileWriter(fName,true);
	               BufferedWriter b = new BufferedWriter(f);
	               b.write(getCurrentDateTime()+": Peer "+peerId1+" has the Preferred neighbours "+prefPeerList+".");
	               b.newLine();
	               b.close();
	            }
	            catch(IOException e)
	            {
	                e.printStackTrace();
	            }
	      
	  }
	  
	  public void updatedOptimisticallyUnchokedNeighbours(int peerId1, int peerId2)
	  {
		  String fName=fileNameExists(peerId1);
			    try
	           {
	               FileWriter f = new FileWriter(fName,true);
	               BufferedWriter b = new BufferedWriter(f);
	               b.write(getCurrentDateTime()+": Peer "+peerId1+" has the optimistically unchoked neighbour Peer "+peerId2+".");
	               b.newLine();
	               b.close();
	            }
	            catch(IOException e)
	            {
	                e.printStackTrace();
	            }
	      
	  }
	  public void unchokedMsgType(int peerId1, int peerId2)
	  {
		  String fName=fileNameExists(peerId1);
			        try
	           {
	               FileWriter f = new FileWriter(fName,true);
	               BufferedWriter b = new BufferedWriter(f);
	               b.write(getCurrentDateTime()+": Peer "+peerId1+" is unchoked by Peer "+peerId2+".");
	               b.newLine();
	               b.close();
	            }
	            catch(IOException e)
	            {
	                e.printStackTrace();
	            }
	      
	  }
	  
	  public void chokedMsgType(int peerId1, int peerId2)
	  {
		  String fName=fileNameExists(peerId1);
			    try
	           {
	               FileWriter f = new FileWriter(fName,true);
	               BufferedWriter b = new BufferedWriter(f);
	               b.write(getCurrentDateTime()+": Peer "+peerId1+" is choked by Peer "+peerId2+".");
	               b.newLine();
	               b.close();
	            }
	            catch(IOException e)
	            {
	                e.printStackTrace();
	            }
	      
	  }
	  
	  public void haveMsgType(int peerId1, int peerId2, int piece_index)
	  {
		  String fName=fileNameExists(peerId1);
			    try
	           {
	               FileWriter f = new FileWriter(fName,true);
	               BufferedWriter b = new BufferedWriter(f);
	               b.write(getCurrentDateTime()+": Peer "+peerId1+" received the 'have' message from Peer "+peerId2+" for the piece "+piece_index+".");
	               b.newLine();
	               b.close();
	            }
	            catch(IOException e)
	            {
	                e.printStackTrace();
	            }
	      
	  }	  
	  public void interestedMsgType(int peerId1, int peerId2)
	  {
		  String fName=fileNameExists(peerId1);
			      try
	           {
	               FileWriter f = new FileWriter(fName,true);
	               BufferedWriter b = new BufferedWriter(f);
	               b.write(getCurrentDateTime()+": Peer "+peerId1+" received the 'interested' message from Peer "+peerId2+".");
	               b.newLine();
	               b.close();
	            }
	            catch(IOException e)
	            {
	                e.printStackTrace();
	            }
	  }	  
	  public void notInterestedMsgType(int peerId1, int peerId2)
	  {
		  String fName=fileNameExists(peerId1);
			      try
	           {
	               FileWriter f = new FileWriter(fName,true);
	               BufferedWriter b = new BufferedWriter(f);
	               b.write(getCurrentDateTime()+": Peer "+peerId1+" received the 'not interested' message from Peer "+peerId2+".");
	               b.newLine();
	               b.close();
	            }
	            catch(IOException e)
	            {
	                e.printStackTrace();
	            }    
	  }	  
	  public void pieceDownloaded(int peerId1, int peerId2, int piece_index, int numberofpieces)
	  {
		  String fName=fileNameExists(peerId1);
			      try
	           {
	               FileWriter f = new FileWriter(fName,true);
	               BufferedWriter b = new BufferedWriter(f);
	               b.write(getCurrentDateTime()+": Peer "+peerId1+" has downloaded the piece "+piece_index+" from Peer "+peerId2+". Now the number of pieces it has is "+numberofpieces+".");
	               b.newLine();
	               b.close();
	            }
	            catch(IOException e)
	            {
	                e.printStackTrace();
	            }
	  }
	  public void fileDownloaded(int peerId1)
	  {
		  String fName = "peer_" + peerId1+"/log_peer_"+peerId1+".log";
	            try
	           {
	               FileWriter f = new FileWriter(fName,true);
	               BufferedWriter b = new BufferedWriter(f);
	               b.write(getCurrentDateTime()+": Peer "+peerId1+" has downloaded the complete file.");
	               b.newLine();
	               b.close();
	            }
	            catch(IOException e)
	            {
	                e.printStackTrace();
	            }
	  }
}