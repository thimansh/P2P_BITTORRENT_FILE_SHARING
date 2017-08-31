public enum MessageType {
	 choke(0),
	 unchoke(1),
	 interested(2),
	 not_interested(3),
	 have(4),
	 bitfield(5),
	 request(6),
	 piece(7),
	 handshake(8);
	private int value;
	 
	private MessageType(int value) {
		this.value = value;
	}
}