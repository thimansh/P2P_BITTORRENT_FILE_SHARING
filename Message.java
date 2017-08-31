import java.util.*;
import java.math.BigInteger;
import java.nio.*;
import java.io.IOException;



public class Message {

	public static final int choke = 0;
	public static final int unchoke = 1;
	public static final int interested = 2;
	public static final int not_interested = 3;
	public static final int have = 4;
	public static final int bitfield = 5;
	public static final int request = 6;
	public static final int piece = 7;
	public static final int handshake = 8;


	public byte msg_type;
	public int clID = -1;
	public int length;
	public byte[] lengthB;
	public byte[] msg_pay_load;

	public Message(){}

	public Message(int length, byte type, byte[] payload, int clientID) { 
		this(length, type, payload);
		this.clID = clientID;
	}

	public Message(int length, byte type, byte[] payload) {
		this.lengthB = ByteBuffer.allocate(4).putInt(length).array();
		this.msg_type = type;
		this.length = length;
		
		if (hasPayload((int)type)) {
			this.msg_pay_load = new byte[length];
			this.msg_pay_load = payload;
		}
		else {
			this.msg_pay_load = null;
		}

	}

	public Message(int length, byte[] data) {
		this.length = length;
		this.msg_type = data[0];

		if(hasPayload((int)msg_type)) {
			msg_pay_load = new byte[length];
			System.arraycopy(data, 1, msg_pay_load, 0, length);
		}
		else {
		    msg_pay_load = null;
		}
	}

	public boolean hasPayload(int type) {
		return (type == have || type == bitfield 
			|| type == request || type == piece);
	}

	public String toString() {
		String str = "Length: " + length + 
		", Type = " + (int)msg_type + ", Payload:";
		if ((int)msg_type == 7)
			str += "[File Bytes]";
		else
			str += Arrays.toString(msg_pay_load);
		return str;
	}

	public byte[] getMessageBytes() {
		ByteBuffer msg_buff = ByteBuffer.allocate(5 + length);

		msg_buff.put(lengthB);
		msg_buff.put(msg_type);

		if(hasPayload((int)msg_type)) {
			msg_buff.put(msg_pay_load);
		}

		return msg_buff.array();
	}
	
    public static void sendHandshake(int index, ProcessData pd) {

            byte[] hnd_shake_hdr = new byte[18];
            try {
            hnd_shake_hdr = "P2PFILESHARINGPROJ".getBytes("UTF-8");
            } catch (Exception e) {
            	//e.printStackTrace();
            }
            byte[] zero_bits = new byte[10];
            byte[] peer_id_Arr = ByteBuffer.allocate(4).putInt(pd.my_peer_Id).array();

            ByteBuffer hand_shake_buff = ByteBuffer.allocate(32);

            hand_shake_buff.put(hnd_shake_hdr);
            hand_shake_buff.put(zero_bits);
            hand_shake_buff.put(peer_id_Arr);
            byte[] hand_shake_arr = hand_shake_buff.array();

            sendMessage(hand_shake_arr, index,pd);
            hand_shake_buff.clear();
            pd.writelogs.TcpMakeConnection(pd.my_peer_Id, pd.peer_neighbours[index].peerId);
            
    }
    
    public static void sendMessage(byte[] msg, int socketIndex, ProcessData pd)
    {
        try {
            pd.rqout[socketIndex].writeObject(msg);
            pd.rqout[socketIndex].flush();
        }
        catch(IOException io_excpt){
        	System.err.println("Message not sent error.");
            //io_excpt.printStackTrace();
        }
    }

    public static void messageHandling(ProcessData pd){

		List<Message> msg_to_remove = new ArrayList<Message>();
	
		synchronized (pd.my_server.messages_rcvd) {

		    Iterator<Message> it = pd.my_server.messages_rcvd.iterator();
		    while (it.hasNext()) {
		        Message msg_incom = it.next();
		        int msg_index = pd.clID_to_peerID[msg_incom.clID];

		        if(checkHandshake(msg_incom, msg_index))
		        	continue;
		        
		        msg_index = findIndex(pd, msg_incom, msg_index);
		        if (msg_index != -1) {
		            if (((int)msg_incom.msg_type != Message.bitfield) && 
		            		pd.peer_neighbours[msg_index].has_rcvd_bit_field == false &&
		            		pd.peer_neighbours[msg_index].has_rcvd_handshake == true) {
		                continue;
		            }
		        }
		        
		       	messageProcessing(pd, msg_incom, msg_index);
		        msg_to_remove.add(msg_incom);
		    }

		    for (Message m : msg_to_remove) {
		        pd.my_server.messages_rcvd.remove(m);
		    }
		}
	
    }

	private static void messageProcessing(ProcessData pd, Message msg_incom, int msg_index) {
		switch ((int)msg_incom.msg_type) {
			

		    case Message.bitfield:
		    {
		        bitFieldHandler(pd, msg_incom, msg_index);
		        break;
		    }
		    
		    case Message.have:
		    {
		    	haveMessageHandler(pd, msg_incom, msg_index);

		        break;
		    }
		    case Message.handshake:
		    {
		        handShakeHandler(pd, msg_incom, msg_index);
		        break;
		    }
		    
		    case Message.interested:
		    {
		        interestedNeighborMessage(pd, msg_index);
		        break;
		    }
		    case Message.piece:
		    {
		    	pieceMessageHandler(pd, msg_incom, msg_index);

		           break;
		    }
		    case Message.not_interested:
		    {
		        uninterestedMessage(pd, msg_index);
		        break;
		    }
		   

		    case Message.request:
		    {
		        requestMessage(pd, msg_incom, msg_index);
		        break;
		    }

		    case Message.choke:
		    {
		      chokeMessage(pd, msg_index);
		      break;
		    }

		    case Message.unchoke:
		    {
		      unchokeMessage(pd, msg_index);
		      break;
		    }

		     default:
		     System.out.println("unknown Error which was caused by the following message:" + (int)msg_incom.msg_type);
		     break;
		}
	}

	private static void pieceMessageHandler(ProcessData pd, Message incomingMessage, int msg_index) {
		pd.file_pieces[pd.peer_neighbours[msg_index].piece_num] = incomingMessage.msg_pay_load;

		pd.peer_neighbours[msg_index].is_waiting_for_piece = false;

		BigInteger tempField = new BigInteger(pd.bitfield);

		tempField = tempField.setBit(pd.peer_neighbours[msg_index].piece_num);

		pd.bitfield = tempField.toByteArray();

		pd.recieved_data[msg_index] += pd.size_of_piece;
		pd.writelogs.pieceDownloaded(pd.other_peer_Ids[pd.my_clID], pd.peer_neighbours[msg_index].peerId, pd.peer_neighbours[msg_index].piece_num, ++pd.tot_pieces);
   
		boolean haveFile = true;
		for (int i = 0; i < pd.number_of_bits; i++) {
		    if (!tempField.testBit(i)) {
		        haveFile = false;
		        break;
		    }
		}

		pd.has_complete_file[pd.my_clID] = haveFile;

		if (haveFile){
			pd.writelogs.fileDownloaded(pd.my_peer_Id);
		}
		for (int i = 0; i < pd.other_peer_Ids.length; i++) {
			if (i == pd.my_clID)
				continue;
			Message.sendHave(i, pd.peer_neighbours[msg_index].piece_num, pd);
		}

		pd.peer_neighbours[msg_index].piece_num = -1;
		for (int i = 0; i < pd.peer_neighbours.length; i++) {
		    if (i == pd.my_clID)
		        continue;

		    boolean interested = false;

		    if (pd.peer_neighbours[i].bit_field_map != null)
		        interested = checkNeededPieces(pd.peer_neighbours[i],pd);

		    if (!interested)
		        Message.sendNotInterested(i, pd);
		}

		    boolean all_have_file = true;
		    for (int i = 0; i < pd.other_peer_Ids.length; i++) {
		        if (!pd.has_complete_file[i]) {
		            all_have_file = false;
		            break;
		        }
		    }
		    
		    pd.every_peer_has_file = all_have_file;
	}

	private static void bitFieldHandler(ProcessData pd, Message incomingMessage, int msg_index) {
		pd.peer_neighbours[msg_index].bit_field_map = incomingMessage.msg_pay_load;
		pd.peer_neighbours[msg_index].has_rcvd_bit_field = true;
		if (checkNeededPieces(pd.peer_neighbours[msg_index],pd)) {
		    Message.sendInterested(msg_index,pd);
		} else {
		    Message.sendNotInterested(msg_index,pd);
		}
	}

	private static void handShakeHandler(ProcessData pd, Message incomingMessage, int msg_index) {
		pd.clID_to_peerID[incomingMessage.clID] = incomingMessage.length;

		for (int i = 0; i < pd.other_peer_Ids.length; i++) {
		   		if (pd.other_peer_Ids[i] == incomingMessage.length) {
		   			msg_index = i;
		   			break;
		   		}
		}
		pd.peer_neighbours[msg_index].has_rcvd_handshake = true;
	}

	private static void haveMessageHandler(ProcessData pd, Message incomingMessage, int msg_index) {
		BigInteger tempField = new BigInteger(pd.peer_neighbours[msg_index].bit_field_map);
		ByteBuffer buffer = ByteBuffer.wrap(incomingMessage.msg_pay_load);
		
		int this_indx = buffer.getInt();
		tempField = tempField.setBit(this_indx);

		pd.peer_neighbours[msg_index].bit_field_map = tempField.toByteArray();

		boolean neighborHasFile = true;
		for (int i = 0; i < pd.number_of_bits; i++) {
		    if (!tempField.testBit(i)) {
		        neighborHasFile = false;
		        break;
		    }
		}

		pd.has_complete_file[msg_index] = neighborHasFile;
		
		boolean temp = true;
		for (int i = 0; i < pd.other_peer_Ids.length; i++) {
		    if (!pd.has_complete_file[i]) {
		        temp = false;
		        break;
		    }
		}

		pd.every_peer_has_file = temp;
		pd.writelogs.haveMsgType(pd.other_peer_Ids[pd.my_clID], pd.peer_neighbours[msg_index].peerId, this_indx);
		
		BigInteger myField = new BigInteger(pd.bitfield);
		if (!myField.testBit(this_indx))
		    Message.sendInterested(msg_index,pd);
	}

	private static void interestedNeighborMessage(ProcessData pd, int msg_index) {
		pd.peer_neighbours[msg_index].is_interested = true;
		pd.writelogs.interestedMsgType(pd.other_peer_Ids[pd.my_clID], pd.peer_neighbours[msg_index].peerId);
	}

	private static void uninterestedMessage(ProcessData pd, int msg_index) {
		pd.peer_neighbours[msg_index].is_interested = false;
		pd.writelogs.notInterestedMsgType(pd.other_peer_Ids[pd.my_clID], pd.peer_neighbours[msg_index].peerId);
	}

	private static void requestMessage(ProcessData pd, Message incomingMessage, int msg_index) {
		ByteBuffer buffer = ByteBuffer.wrap(incomingMessage.msg_pay_load);
		int pieceNumber = buffer.getInt();
		Message.sendFilePiece(msg_index, pieceNumber, pd);
	}

	private static void chokeMessage(ProcessData pd, int msg_index) {
		pd.peer_neighbours[msg_index].is_choked = true;
		  pd.writelogs.chokedMsgType(pd.other_peer_Ids[pd.my_clID], pd.peer_neighbours[msg_index].peerId);
	}

	private static void unchokeMessage(ProcessData pd, int msg_index) {
		pd.peer_neighbours[msg_index].is_choked = false;
		  pd.writelogs.unchokedMsgType(pd.other_peer_Ids[pd.my_clID], pd.peer_neighbours[msg_index].peerId);
	}

	private static int findIndex(ProcessData pd, Message incomingMessage, int msg_index) {
		if ((int)incomingMessage.msg_type != Message.handshake) {
		    for (int i = 0; i < pd.other_peer_Ids.length; i++) {
		       		if (pd.other_peer_Ids[i] == msg_index) {
		       			msg_index = i;
		       			break;
				}
		   	}
		}
		return msg_index;
	}

	private static boolean checkHandshake(Message incomingMessage, int msg_index) {
		return (msg_index == -1 && !((int)incomingMessage.msg_type == Message.handshake)) ;
	} 
    public static void sendNotInterested(int index, ProcessData pd) {
        Message message = new Message(0,(byte)Message.not_interested, null);
        sendMessage(message.getMessageBytes(), index, pd);
    }
    
    public static void sendUnchoke(int index, ProcessData pd) {
        Message message = new Message(0,(byte)Message.unchoke, null);
        sendMessage(message.getMessageBytes(), index, pd);
      }

    public static void sendHave(int index, int pieceNumber , ProcessData pd) {
    	byte[] pieceIndex = ByteBuffer.allocate(4).putInt(pieceNumber).array();
    	Message message = new Message(4,(byte)Message.have,pieceIndex);
        sendMessage(message.getMessageBytes(), index, pd);
    }

   	public static void sendFilePiece(int index, int pieceNumber, ProcessData pd) {
   		Message message = new Message(pd.size_of_piece, (byte)Message.piece, pd.file_pieces[pieceNumber]);
   		sendMessage(message.getMessageBytes(), index ,pd);
    }

    public static void sendChoke(int index, ProcessData pd) {
      Message message = new Message(0,(byte)Message.choke, null);
      sendMessage(message.getMessageBytes(), index, pd);
    }
    
    public static void sendBitfield(int index, ProcessData pd) {
        Message bitfieldMessage = new Message(pd.bitfield.length, (byte)Message.bitfield, pd.bitfield);
        sendMessage(bitfieldMessage.getMessageBytes(), index,pd);
    }

    public static void sendInterested(int index, ProcessData pd) {
        Message message = new Message(0,(byte)Message.interested, null);
        sendMessage(message.getMessageBytes(), index, pd);
    }

    public static void sendRequest(int index, int pieceNumber, ProcessData pd) {
        byte[] pieceIndex = ByteBuffer.allocate(4).putInt(pieceNumber).array();
        Message message = new Message(4,(byte)Message.request,pieceIndex);
        sendMessage(message.getMessageBytes(), index, pd);

    }
    public static boolean checkNeededPieces(RemoteNeighbours neighbor,ProcessData pd) {
    	BigInteger self_field = new BigInteger(pd.bitfield);
        BigInteger neighbour_field = new BigInteger(neighbor.bit_field_map);
        
      if (neighbour_field.and(self_field.and(neighbour_field).not()).doubleValue() > 0) {
            return true;
        }
        return false;
    }

}