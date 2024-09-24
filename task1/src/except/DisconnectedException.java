package except;

public class DisconnectedException extends Exception {
    private static final long serialVersionUID = 1L;

	public DisconnectedException() {
        super("Channel has been disconnected.");
    }
}
