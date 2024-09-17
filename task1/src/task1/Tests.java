package task1;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class Tests {

	@ParameterizedTest
	@MethodSource("createBroker")
	void testEchoServer(Broker broker) {
		int clientCount = 5;
		int port = 1234;

		for (int i = 0; i < clientCount; i++) {
			Channel channel = broker.connect("localhost", port);
			assertNotNull(channel, "Channel should not be null");

			byte[] sendBytes = new byte[255];
			for (int j = 0; j < 255; j++) {
				sendBytes[j] = (byte) (j + 1);
			}

			int bytesWritten = channel.write(sendBytes, 0, sendBytes.length);
			assertEquals(255, bytesWritten, "Failed to write all bytes to the channel");

			byte[] receivedBytes = new byte[255];

			int bytesRead = channel.read(receivedBytes, 0, receivedBytes.length);
			assertEquals(255, bytesRead, "Failed to read all bytes from the channel");

			assertArrayEquals(sendBytes, receivedBytes, "The echoed bytes do not match the sent bytes");

			channel.disconnect();
			assertTrue(channel.disconnected(), "Channel should be disconnected");
		}
	}

	private static Stream<Broker> createBroker() {
		return Stream.of(
				new Broker("EchoBroker") {
					@Override
					Channel accept(int port) {
						return null;
					}

					@Override
					Channel connect(String name, int port) {
						return null;
					}
				}
			);
	}

}
