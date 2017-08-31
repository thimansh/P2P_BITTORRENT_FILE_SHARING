<<<<<<< HEAD
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.Timer;
import java.util.logging.FileHandler;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Arrays;

public class ProcessData {
     int size_of_file;
     int size_of_piece;
     int my_peer_Id;
     int my_clID;
     boolean[] has_complete_file;

     int[] other_peer_Ids;
     FileHandler file_handler;
     byte[][] file_pieces;
     String file_name;
     int[] clID_to_peerID; 

    public  RemoteNeighbours[] peer_neighbours;
    public  byte[] bitfield;
    public  int number_of_bits;
    public  int number_of_bytes;
    public  boolean every_peer_has_file;

     PeerServer my_server;
     int tot_pieces = 0;

     int[] recieved_data;
     int[] pref_neighbours_clIDs;  
     int opt_neighbour_clID; 
     Random random_num_gen;
     Timer pref_neighbours_scheduler;
     Timer opt_neighbour_scheduler;
     
     static Socket rqSocket[];           		
     static ObjectOutputStream rqout[];
     
     public P2PWriteLogFile writelogs;
      
     public ProcessData(int peerId, PeerConfig pconfig, ProtocolConfig cconfig) {
         this.my_peer_Id = peerId;
         this.other_peer_Ids = pconfig.getPeerIDArr();
         this.size_of_file = cconfig.getFileSize();
         this.size_of_piece = cconfig.getPieceSize();
         this.file_name = cconfig.getfileName();
         this.has_complete_file = pconfig.gethasFileArr();
 		 this.my_clID = Arrays.binarySearch(other_peer_Ids, peerId);
 		 this.writelogs=new P2PWriteLogFile();
         initProcessData(peerId, pconfig, cconfig);
     }
     
     
 	public void initProcessData(int peerId,  PeerConfig pconfig, ProtocolConfig cconfig) {

        initClid2PidMapAndNeighbours(pconfig.getPeerIDArr(), pconfig.gethostNamesArr(), pconfig.getportNumbersArr(), pconfig.gethasFileArr());
        
        initNumBitsAndBytes(cconfig.getFileSize(), cconfig.getPieceSize());
        
        initBitField(pconfig.gethasFileArr());

        initializeFilePieces(has_complete_file[my_clID]);

        rqSocket = new Socket[other_peer_Ids.length];
        rqout = new ObjectOutputStream[other_peer_Ids.length];
        
        random_num_gen = new Random();
        recieved_data = new int[other_peer_Ids.length];
        pref_neighbours_clIDs = new int[cconfig.getNumPrefNeighbours()];
        pref_neighbours_scheduler = new Timer();
        opt_neighbour_scheduler = new Timer();
        writelogs.newLogFile(peerId);
	}

	public void initClid2PidMapAndNeighbours(int[] peerIds, String[] hostNames, int[] portNumbers, boolean[] hasFile) {
		clID_to_peerID = new int[peerIds.length];
		peer_neighbours = new RemoteNeighbours[peerIds.length];
        for (int i = 0; i < peerIds.length; i++) {
            
            peer_neighbours[i] = new RemoteNeighbours();
            peer_neighbours[i].port_num = portNumbers[i];
            peer_neighbours[i].host_name = hostNames[i];
            peer_neighbours[i].peerId = peerIds[i];
            peer_neighbours[i].has_file = hasFile[i];
            clID_to_peerID[i] = -1;
        }
	}


	public void initNumBitsAndBytes(int fileSize, int pieceSize) {
		
		if (fileSize%pieceSize != 0){
            number_of_bits=(int)(fileSize/pieceSize + 1);
		}
        else{
            number_of_bits=(int)(fileSize/pieceSize);
        }
		
        if (number_of_bits%8 != 0){
            number_of_bytes=(int)(number_of_bits/8 + 1);
        }
        else{
            number_of_bytes=(int)(number_of_bits/8);
        }

	}

	public void initBitField(boolean[] hasFile) {
		bitfield=new byte[number_of_bytes+1];
        if (hasFile[my_clID]) {
            BigInteger tmp_field = new BigInteger("0");
            for (int i=0; i<number_of_bits; i++) {
                tmp_field=tmp_field.setBit(i);
            }
            byte[] array=tmp_field.toByteArray();
            bitfield=array;
         }
	}
	
    private void initializeFilePieces(boolean hasFile) {
    	file_pieces=new byte[number_of_bits][size_of_piece];

    	if (hasFile == true){
	    	try {
	    	 	File file=new File(file_name);
	    	 	FileInputStream inputStream=new FileInputStream("peer_" + my_peer_Id + "//" + file);
	            int currentPieceIndex = 0;
	            while (currentPieceIndex < file_pieces.length) {
	            	inputStream.read(file_pieces[currentPieceIndex++]);
	            }
	            inputStream.close();
	            
	        } catch (Exception e) {
	        	System.out.println("file pieces generation error");
	            System.exit(0);
	        }
    	}
    }
    
    public void combinePiecesOfFile() {

        try {
            FileOutputStream os = new FileOutputStream("peer_" + my_peer_Id + "//" + file_name);

            for (int i = 0; i < number_of_bits; i++) {
                if (i+1 == number_of_bits)
                    os.write(P2PUtils.adjustpadding(file_pieces[i]));
                else
                    os.write(file_pieces[i]);
            }

            os.close();
        } catch (Exception e) {
            //Error assembling file pieces
            System.exit(0);
        }

    }
}

=======
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import java.util.Timer;
import java.util.logging.FileHandler;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Arrays;

public class ProcessData {
     int size_of_file;
     int size_of_piece;
     int my_peer_Id;
     int my_clID;
     boolean[] has_complete_file;

     int[] other_peer_Ids;
     FileHandler file_handler;
     byte[][] file_pieces;
     String file_name;
     int[] clID_to_peerID; 

    public  RemoteNeighbours[] peer_neighbours;
    public  byte[] bitfield;
    public  int number_of_bits;
    public  int number_of_bytes;
    public  boolean every_peer_has_file;

     PeerServer my_server;
     int tot_pieces = 0;

     int[] recieved_data;
     int[] pref_neighbours_clIDs;  
     int opt_neighbour_clID; 
     Random random_num_gen;
     Timer pref_neighbours_scheduler;
     Timer opt_neighbour_scheduler;
     
     static Socket rqSocket[];           		
     static ObjectOutputStream rqout[];
     
     public P2PWriteLogFile writelogs;
      
     public ProcessData(int peerId, PeerConfig pconfig, ProtocolConfig cconfig) {
         this.my_peer_Id = peerId;
         this.other_peer_Ids = pconfig.getPeerIDArr();
         this.size_of_file = cconfig.getFileSize();
         this.size_of_piece = cconfig.getPieceSize();
         this.file_name = cconfig.getfileName();
         this.has_complete_file = pconfig.gethasFileArr();
 		 this.my_clID = Arrays.binarySearch(other_peer_Ids, peerId);
 		 this.writelogs=new P2PWriteLogFile();
         initProcessData(peerId, pconfig, cconfig);
     }
     
     
 	public void initProcessData(int peerId,  PeerConfig pconfig, ProtocolConfig cconfig) {

        initClid2PidMapAndNeighbours(pconfig.getPeerIDArr(), pconfig.gethostNamesArr(), pconfig.getportNumbersArr(), pconfig.gethasFileArr());
        
        initNumBitsAndBytes(cconfig.getFileSize(), cconfig.getPieceSize());
        
        initBitField(pconfig.gethasFileArr());

        initializeFilePieces(has_complete_file[my_clID]);

        rqSocket = new Socket[other_peer_Ids.length];
        rqout = new ObjectOutputStream[other_peer_Ids.length];
        
        random_num_gen = new Random();
        recieved_data = new int[other_peer_Ids.length];
        pref_neighbours_clIDs = new int[cconfig.getNumPrefNeighbours()];
        pref_neighbours_scheduler = new Timer();
        opt_neighbour_scheduler = new Timer();
        writelogs.newLogFile(peerId);
	}

	public void initClid2PidMapAndNeighbours(int[] peerIds, String[] hostNames, int[] portNumbers, boolean[] hasFile) {
		clID_to_peerID = new int[peerIds.length];
		peer_neighbours = new RemoteNeighbours[peerIds.length];
        for (int i = 0; i < peerIds.length; i++) {
            
            peer_neighbours[i] = new RemoteNeighbours();
            peer_neighbours[i].port_num = portNumbers[i];
            peer_neighbours[i].host_name = hostNames[i];
            peer_neighbours[i].peerId = peerIds[i];
            peer_neighbours[i].has_file = hasFile[i];
            clID_to_peerID[i] = -1;
        }
	}


	public void initNumBitsAndBytes(int fileSize, int pieceSize) {
		
		if (fileSize%pieceSize != 0){
            number_of_bits=(int)(fileSize/pieceSize + 1);
		}
        else{
            number_of_bits=(int)(fileSize/pieceSize);
        }
		
        if (number_of_bits%8 != 0){
            number_of_bytes=(int)(number_of_bits/8 + 1);
        }
        else{
            number_of_bytes=(int)(number_of_bits/8);
        }

	}

	public void initBitField(boolean[] hasFile) {
		bitfield=new byte[number_of_bytes+1];
        if (hasFile[my_clID]) {
            BigInteger tmp_field = new BigInteger("0");
            for (int i=0; i<number_of_bits; i++) {
                tmp_field=tmp_field.setBit(i);
            }
            byte[] array=tmp_field.toByteArray();
            bitfield=array;
         }
	}
	
    private void initializeFilePieces(boolean hasFile) {
    	file_pieces=new byte[number_of_bits][size_of_piece];

    	if (hasFile == true){
	    	try {
	    	 	File file=new File(file_name);
	    	 	FileInputStream inputStream=new FileInputStream("peer_" + my_peer_Id + "//" + file);
	            int currentPieceIndex = 0;
	            while (currentPieceIndex < file_pieces.length) {
	            	inputStream.read(file_pieces[currentPieceIndex++]);
	            }
	            inputStream.close();
	            
	        } catch (Exception e) {
	        	System.out.println("file pieces generation error");
	            System.exit(0);
	        }
    	}
    }
    
    public void combinePiecesOfFile() {

        try {
            FileOutputStream os = new FileOutputStream("peer_" + my_peer_Id + "//" + file_name);

            for (int i = 0; i < number_of_bits; i++) {
                if (i+1 == number_of_bits)
                    os.write(P2PUtils.adjustpadding(file_pieces[i]));
                else
                    os.write(file_pieces[i]);
            }

            os.close();
        } catch (Exception e) {
            //Error assembling file pieces
            System.exit(0);
        }

    }
}

>>>>>>> a3152f2d83ab2b33eac2d3c48a1538a7cc15e892
