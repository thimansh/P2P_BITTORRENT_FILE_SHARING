import java.lang.*;
import java.util.*;
import java.io.*;

public class peerProcess {

	static int peerId;
	
	static ProtocolConfig cconfig=new ProtocolConfig();
	static PeerConfig pconfig=new PeerConfig();
	
	public static void main(String[] args) {

		initpeerProcess(args);
		Process logic =new Process (peerId,pconfig,cconfig);
	}

	
	public static void initpeerProcess(String[] args){
		
		cconfig.parseConfigFile();
		pconfig.parsePeerInfoFile();
		
		try {
			peerId = Integer.parseInt(args[0]);

		} catch (Exception e) {
			System.out.println("no input peer Id specified.");
			System.exit(0);
		}
		
		int[] peerIds=pconfig.getPeerIDArr();
		
		if (Arrays.binarySearch(peerIds, peerId) < 0) {
			System.out.println("peer id not found in the PeerInfo.cfg file");
			return;
		}
		
		P2PUtils.createPeerDirs(peerId);
		
	}

}
