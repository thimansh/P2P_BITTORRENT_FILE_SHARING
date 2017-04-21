import java.util.Arrays;


public class RemoteNeighbours {
    public int peerId;
	public String host_name = "";
    public int port_num;
	public byte[] bit_field_map;

    public boolean is_conn_refused = false;
    public boolean is_conn = false;
    public int piece_num = -1;
    public boolean is_waiting_for_piece = false;
    public boolean has_file = false;
    public boolean has_sent_handshake = false;
    public boolean has_sent_bitfield = false;
    public boolean has_rcvd_handshake = false;
    public boolean has_rcvd_bit_field = false;
    public boolean is_interested = false;
    public boolean is_choked = true;
    
    public RemoteNeighbours() {
        
    }
    
    public static double calculateDownloadRate(int peer, ProcessData pd) {
      
      if (peer != pd.my_clID && pd.peer_neighbours[peer].is_conn && pd.peer_neighbours[peer].is_interested ) {
        int val = pd.recieved_data[peer]/peerProcess.cconfig.unchokingInterval;
        pd.recieved_data[peer] = 0;
        return val;
      }
      else {
        return 0;
      }
    }
    

	public static void decideChokeUnchokeNeighbours(ProcessData pd) {
		boolean neighbour_identified = false;
		 for (int i = 0; i < pd.pref_neighbours_clIDs.length; i++) { 
		  for (int j = 0; j < pd.peer_neighbours.length; j++) {
		   if(pd.pref_neighbours_clIDs[i] == j) {
		    neighbour_identified = true; 
		    break;
		   }
		 }
		 if (i != pd.my_clID) {
		   if (neighbour_identified) { 
		    Message.sendUnchoke(i, pd); 
		    neighbour_identified = false; 
		   }
		   else
		     Message.sendChoke(i,pd);
		 }
		}
	}

    public static void determinePreferredNeighbors(ProcessData pd) {
        double[] rates_unordered = new double[pd.peer_neighbours.length]; 

        for (int i = 0; i < pd.peer_neighbours.length; i++) {
    		  rates_unordered[i] = calculateDownloadRate(i,pd); 
        }
        
       double[] temp = new double[pd.peer_neighbours.length]; 
       revArray(rates_unordered, temp);

       //select in an unbiased manner the top k neighbours randomly.
       pickTopPrefNeighbours(pd, rates_unordered, temp);
      
      int[] actual_ids=new int[pd.pref_neighbours_clIDs.length];
      for(int i=0;i<pd.pref_neighbours_clIDs.length;i++){
    	 actual_ids[i]=pd.peer_neighbours[pd.pref_neighbours_clIDs[i]].peerId;
      }
      
      pd.writelogs.updatedListOfPreferredNeighbours(pd.my_peer_Id, Arrays.toString(actual_ids));

     decideChokeUnchokeNeighbours(pd);
   }

	public static void pickTopPrefNeighbours(ProcessData pd, double[] downloads, double[] rates_desc_order) {
		int rnd_indx;
		int st_indx = 0;
		int tie_length = 1;
		boolean[] top_pick = new boolean[pd.peer_neighbours.length]; 
		double[] best_download_rates = new double [pd.pref_neighbours_clIDs.length];
		best_download_rates = Arrays.copyOfRange(rates_desc_order,0,pd.pref_neighbours_clIDs.length); 
		
	    if ((rates_desc_order.length > pd.pref_neighbours_clIDs.length) && (rates_desc_order[pd.pref_neighbours_clIDs.length-1] == rates_desc_order[pd.pref_neighbours_clIDs.length])) {
	     for (int i = pd.pref_neighbours_clIDs.length-1; i > 0; i--) { 
	       if((i != 1) && (rates_desc_order[i] == rates_desc_order[i-1])) {
	         st_indx = i-1;
	       } else {
	         break;
	       }
	     }
	     for (int i = st_indx; i < rates_desc_order.length; i++) {
	        if((i != rates_desc_order.length-1) && (rates_desc_order[i] == rates_desc_order[i+1])) {
	          tie_length += 1;
	          }
	     }
	    }
	    
	  for (int i = 0; i < pd.pref_neighbours_clIDs.length; i++) {
	    for(int j = 0; j < downloads.length; j++) {
	      if ((tie_length == 1 || i < st_indx) && (best_download_rates[i] == downloads[j]) && !top_pick[j]) {
	        pd.pref_neighbours_clIDs[i] = j;
	        top_pick[j] = true;
	        break;
	      }
	    }
	    if (i >= st_indx && tie_length != 0) {
	      while(true) {
	        rnd_indx = pd.random_num_gen.nextInt(downloads.length);
	        if(downloads[rnd_indx] == best_download_rates[i] && !top_pick[rnd_indx]) { 
	          pd.pref_neighbours_clIDs[i] = rnd_indx; 
	          top_pick[rnd_indx] = true;
	          break;
	        }
	      }
	    }
	  }
	}

	public static void revArray(double[] downloads, double[] temp) {
		System.arraycopy(downloads, 0, temp, 0, downloads.length); 
		   Arrays.sort(temp); 
		   for (int i = 0; i < temp.length / 2; i++) {
		    double placeholder = temp[i];
		    temp[i] = temp[temp.length - 1 - i];
		    temp[temp.length - 1 - i] = placeholder;
		   }
	}


    public static int determineOptimisticNeighbor(ProcessData pd) {
        int[] values = new int[pd.peer_neighbours.length];
        int indx = 0;
        for (int i = 0; i < pd.peer_neighbours.length; i++) {
            if (pd.peer_neighbours[i].is_interested) {
                values[indx++] = i;
            }
        }
        int rnd_neighbour = 0;
        if (indx > 0) {
            rnd_neighbour = values[pd.random_num_gen.nextInt(indx)];
            Message.sendUnchoke(rnd_neighbour,pd);
        }

        return rnd_neighbour;
    }
}