package utils;

public class Message {
	
	public Message(byte[] msg, int i, int length) {
		this.bytes = msg;
		this.offset = i;
		this.length = length;
	}
	public byte[] bytes;
	public int offset;
	public int length;
}
