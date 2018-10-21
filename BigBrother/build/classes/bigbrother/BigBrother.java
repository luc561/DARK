
package bigbrother;



import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

public class BigBrother {
    static String ipAddr = "192.168.1.13";
    static InetAddress IPAddress;
    static int port = 5678;
    static DatagramSocket socket;
    static DatagramPacket packet;
    public final static int PACKETSIZE = 1000;
    static String uniqueID = "10205FE010000379";
    public static final int CLIENT_GET_SENSORS = 51;
    static double x = 0;
    static double y = 0;
    static int quad = 0;
    static int currentquad = 0;
    
    
    
    
    public static void main(String[] args) throws UnknownHostException, FileNotFoundException {
    PrintStream o = new PrintStream(new File("/home/pi/Desktop/NodeData.txt"));
    System.setOut(o);
    
                System.out.println("************************");
		System.out.println("*  Node 56 is Online   *");
		System.out.println("*  Quadrants Tracking  *");
                System.out.println("*       Initiated      *");
		System.out.println("************************");
		System.out.println("");
    
        while(true){
    
    try {
			// Create the socket
			socket = new DatagramSocket();  // Opens UDP connection with Ranging software on PI
			IPAddress = InetAddress.getByName(ipAddr);
		} catch (UnknownHostException e) {
			System.out.println("IP address not correct");
		} catch (SocketException e) {
			System.out.println("Socket Error");
		}
    
    String command = String.format("%d", CLIENT_GET_SENSORS);
    String response = UDPMessage(command);
    String [] splitted = response.split("\\s+");
			if(splitted[1].equals("255")){
				System.out.println("Ranging Error");
			}else{
				x = Double.parseDouble(splitted[3]);
                                y = Double.parseDouble(splitted[4]);
                                
                                
				
			}
               if (x>0 && y>0){
                   quad=1;
               }
               if (x<0 && y>0){
                   quad=2;
               }
               if (x<0 && y<0){
                   quad=3;
               }
               if (x>0 && y<0){
                   quad=4;
               }
               if (currentquad != quad){
                currentquad = quad;
               System.out.printf("Node Entered Quadrant %d at %s %n",currentquad,java.time.LocalDateTime.now());
               
               }
               
    }}

    
    // Method that implements the UDP connection. It sends the desired command
	// and return the response read from the socket
	public static String UDPMessage(String command) {
		String response;

		// Create and add the hashtag to the command
		Random rdm = new Random();
		int sendHashtag = rdm.nextInt(1000);
		String cmd = String.format("{%d %s}", sendHashtag, command);

		// Create the UDP packet
		byte[] data = cmd.getBytes();
		packet = new DatagramPacket(data, data.length, IPAddress, port);
		packet.setData(data);
		packet.setLength(data.length);

		try {
			// Send the message
			socket.send(packet);

			// Set the desired timeout
			socket.setSoTimeout(2000);
			packet.setData(new byte[PACKETSIZE]);
			// Read the received message and convert it to string
			socket.receive(packet);
			response = (new String(packet.getData()));

			// Delete the message delimiters
			response = response.replace("{", "");
			response = response.replace("}", "");

			return response.substring(0, packet.getLength() - 2);

		} catch (IOException e) {
			return "Communication Error";
		}
	}

}
