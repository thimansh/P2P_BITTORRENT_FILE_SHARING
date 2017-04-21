import java.net.*;
import java.io.*;
import java.util.*;
import java.math.BigInteger;



public class Process {
   
	static ProcessData pd;
	static boolean only_once=false;
    
    public Process(int peerId,PeerConfig pconfig,ProtocolConfig cconfig) {
    	
    	pd=new ProcessData(peerId, pconfig, cconfig);
        initiateCommunications();
    }
	
    private static void initiateCommunications() {
	    try {
	    	pd.my_server = new PeerServer(pd.peer_neighbours[pd.my_clID].port_num);
	        initClientConnections();
	        
	        for (int i = 0; i < pd.other_peer_Ids.length; i++) {
	            if (i != pd.my_clID && pd.peer_neighbours[i].is_conn) {
	                pd.rqout[i] = new ObjectOutputStream(pd.rqSocket[i].getOutputStream());
	                pd.rqout[i].flush();
	            }
	        }
	        
	        while (!pd.every_peer_has_file) {
		        for (int i = 0; i < pd.other_peer_Ids.length; i++) {
		            if (i != pd.my_clID) {
		                conditionalSendHanshake(i);
		                conditionalSendBitField(i);
		                conditionalRequestRandomPiece(i);
		            }
		        }
		        if (only_once == false) {
		          initiateNeighbourTaskSchedulers();
		           only_once=true;
		        }
		
		        Message.messageHandling(pd);
		    }
		    try { Thread.sleep(1000); } catch (Exception e) {};
	    }
        catch (ConnectException e) {
          // e.printStackTrace();
        }
        catch(UnknownHostException u_host) {
        	//u_host.printStackTrace();
        }
        catch(IOException ioException) {
            //ioException.printStackTrace();
        }
        finally {
            
            try {
                for (int i = 0; i < pd.other_peer_Ids.length; i++) {
                    if (i != pd.my_clID) {
                        pd.rqout[i].close();
                        pd.rqSocket[i].close();
                    }
                }
            }
            catch(IOException io_except){
                //io_except.printStackTrace();
            }
        }
        
	    pd.combinePiecesOfFile();
        System.exit(0);
    }

	public static void initiateNeighbourTaskSchedulers() {
		pd.pref_neighbours_scheduler.scheduleAtFixedRate(new TimerTask() {
		     @Override
		      public void run(){
		         RemoteNeighbours.determinePreferredNeighbors(pd);
		      }
		     },0, peerProcess.cconfig.unchokingInterval * 1000);

		   pd.opt_neighbour_scheduler.scheduleAtFixedRate(new TimerTask() {
		     @Override
		      public void run(){
		         int temp = pd.opt_neighbour_clID;
		         pd.opt_neighbour_clID = RemoteNeighbours.determineOptimisticNeighbor(pd);
		         if (temp != pd.opt_neighbour_clID)
		        	 pd.writelogs.updatedOptimisticallyUnchokedNeighbours(pd.my_peer_Id, pd.peer_neighbours[pd.opt_neighbour_clID].peerId);
		      }
		    },0, peerProcess.cconfig.optimisticUnchokingInterval * 1000);
	}

	public static void conditionalRequestRandomPiece(int i) {
		if (pd.peer_neighbours[i].has_rcvd_bit_field &&
		    pd.peer_neighbours[i].has_rcvd_handshake &&
		    pd.peer_neighbours[i].is_choked == false &&
		    pd.peer_neighbours[i].is_waiting_for_piece == false) {
			
		    pd.peer_neighbours[i].is_waiting_for_piece = true;
		    int rqst_piece_num = getReqPieceAtRandom(pd.peer_neighbours[i]);
		    pd.peer_neighbours[i].piece_num = rqst_piece_num;	
		    if (rqst_piece_num != -1) {
		        Message.sendRequest(i, rqst_piece_num,pd);
		    }
		}
	}

	public static void conditionalSendBitField(int i) {
		if (!pd.peer_neighbours[i].has_sent_bitfield && pd.peer_neighbours[i].is_conn && pd.peer_neighbours[i].has_rcvd_handshake) {
		    Message.sendBitfield(i,pd);
		    pd.peer_neighbours[i].has_sent_bitfield = true;
		}
	}

	public static void conditionalSendHanshake(int i) {
		if (!pd.peer_neighbours[i].has_sent_handshake && pd.peer_neighbours[i].is_conn) {
		    Message.sendHandshake(i,pd);
		    pd.peer_neighbours[i].has_sent_handshake = true;
		    pd.writelogs.tcpConnect(pd.my_peer_Id, pd.peer_neighbours[i].peerId);
		}
	}

    public static int getReqPieceAtRandom(RemoteNeighbours neighbor) {
    	BigInteger self_field = new BigInteger(pd.bitfield);
        BigInteger neighbour_field = new BigInteger(neighbor.bit_field_map);
        BigInteger interesting_field = neighbour_field.and(self_field.and(neighbour_field).not());
        int[] values = new int[interesting_field.bitLength()];
        int k = 0;
        boolean interesting_bit_exists = false;
        for (int i = 0; i < interesting_field.bitLength(); i++) {
            if (interesting_field.testBit(i)) {
                interesting_bit_exists = true;
                values[k++] = i;
            }
        }        
        if (interesting_bit_exists) {
            return values[pd.random_num_gen.nextInt(k)];
        } else {
            return -1;
        }
    }

    public static void initClientConnections() {
        int remaining_connections = pd.other_peer_Ids.length-1;
            while (remaining_connections > 0) {
                System.out.println();

                for (int i = 0; i < pd.other_peer_Ids.length; i++) {
                    if (i != pd.my_clID && !pd.peer_neighbours[i].is_conn) {
                        System.out.println("Trying to establish connection to " + pd.peer_neighbours[i].host_name + " on port " + pd.peer_neighbours[i].port_num);
                        try {
                            pd.rqSocket[i] = new Socket(pd.peer_neighbours[i].host_name, pd.peer_neighbours[i].port_num);
                            if (pd.rqSocket[i].isConnected()) {
                            remaining_connections=remaining_connections-1;
                            pd.peer_neighbours[i].is_conn = true;
                            }
                        } catch (ConnectException e) {
                            pd.peer_neighbours[i].is_conn_refused = true;
                        } catch (IOException e) {
                            pd.peer_neighbours[i].is_conn_refused = true;
                        }
                    }
                }
                System.out.println();
                for (int i = 0; i < pd.other_peer_Ids.length; i++) {
                    if (pd.peer_neighbours[i].is_conn_refused) {
                        
                    }
                }
                if (remaining_connections > 0) {
                    try {
                    System.out.println("Waiting to reconnect..");
                    Thread.sleep(1000);
                    } catch (Exception e) {

                    }
                }
            }
    }

}
