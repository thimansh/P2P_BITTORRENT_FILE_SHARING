import java.io.File;
import java.util.Arrays;

public class P2PUtils {
	
	public static void createPeerDirs(int peerId) {
		String workingDir = System.getProperty("user.dir");
		File file = new File (workingDir + "//" + "peer_" + peerId);
		if (!file.isDirectory()) {
			file.mkdir();
		}
	}
	
   public static byte[] adjustpadding(byte[] data) {
        int i = data.length-1;
        while (i >= 0 && data[i] == 0)
            i=i-1;
        return Arrays.copyOf(data, i + 1);
    }
}