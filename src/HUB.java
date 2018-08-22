/*
 * SEQUITUR RANGING
 * JAVA example
 * 
 * (c) UNISET s.r.l. 2016
 * v1.0 Oct. 2016
 * 
 * Simple JAVA example which connects to SEQUITUR RANGING, performs ping, scan 
 * and 5 consecutive ranging requests between 2 nodes.
 * 
 * You have to set the IP address of the Raspberry Pi computer equipped with SEQUITUR Pi board and
 * the unique ID of the SEQUITUR Pi board you want to range with
 * 
 */



import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;        
        
public class HUB {
    public static final int CLIENT_PING = 0;
    public static final int CLIENT_SCAN = 56;
    public static final int CLIENT_GET_RANGE = 50;
    public final static int PACKETSIZE = 1000;
 
    static String ipAddr = "192.168.1.10";			// IP address of the Raspberry Pi computer equipped with SEQUITUR Pi board
    static int port = 5678;							// Port used for the UDP connection with SEQUITUR RANGING
    static String uniqueID = "TYKBCFDO";	// Unique ID of the SEQUITUR Pi board you want to range with (check the scan result for the available addresses)

    static InetAddress IPAddress;
    static DatagramSocket socket;
    static DatagramPacket packet;
        
    public static final int MCP23008_ADDRESS = 0x20;

    private static final int IODIRA_REGISTER = 0x00; //IODIRA Register. Responsible for input or output
    private static final int IODIRB_REGISTER = 0x01; //IODIRB Register. Responsible for input or output
    
    private static final int GPIOA_REGISTER = 0x12; //GPIOA Register. Write or read value
    private static final int GPIOB_REGISTER = 0x13; //GPIOB Register. Write or read value
    
    //private static final int GPPUA_REGISTER = 0x0C; //PORT A Pull-up value. If set configures the internal pull-ups
    private static final int GPPUB_REGISTER = 0x0D; ///PORT B Pull-up value. If set configures the internal pull-ups
    
    public static void main(String args[]) throws InterruptedException, UnsupportedBusNumberException, IOException {
        System.out.println("MCP23017 Example");
        I2CBus i2c = I2CFactory.getInstance(I2CBus.BUS_1);
        I2CDevice device = i2c.getDevice(MCP23008_ADDRESS);
        
    device.write(0, "WeAreGenius");
    device.write(1, "Of course")
        /*device.write(IODIRA_REGISTER, (byte) 0x00);
        
        device.write(IODIRB_REGISTER, (byte) 0xFF);
        device.write(GPPUB_REGISTER, (byte) 0xFF);
        
        
//While true loop
            System.out.println(device.read(GPIOB_REGISTER));
        	Thread.sleep(2000);
            device.write(GPIOA_REGISTER, (byte) 0x00);
        	Thread.sleep(2000);
            device.write(GPIOA_REGISTER, (byte) 0xFF);
        */
        
    
	


		System.out.println("************************");
		System.out.println("*   SEQUITUR RANGING   *");
		System.out.println("*     JAVA example     *");
		System.out.println("*                      *");  
		System.out.println("* (c) UNISET srl 2016  *");
		System.out.println("*    v1.0 Oct. 2016    *");
		System.out.println("************************");
		System.out.println("");
		
		try {
			// Create the socket
			socket = new DatagramSocket();
			IPAddress = InetAddress.getByName(ipAddr);
		} catch (UnknownHostException e) {
			System.out.println("IP address not correct");
		} catch (SocketException e) {
			System.out.println("Socket Error");
		}

		// Ping SEQUITUR RANGING
		String response = UDPMessage(String.valueOf(CLIENT_PING));
		System.out.println("PING SEQUITUR RANGING at " + ipAddr);
		System.out.println("Response: " + response);
		System.out.println("");
		
		// SCAN for other nodes running SEQUITUR RANGING
		System.out.println("SCAN");
		String command=String.format("%d %d", CLIENT_SCAN, 1000);
		response=UDPMessage(command);
		String[] splitted = response.split("\\s+");
		if(Integer.parseInt(splitted[1])>0){
			for(int i =0;i<Integer.parseInt(splitted[1]);i++){
				System.out.println("Found device #"+(i+1)+". Unique ID: "+splitted[2+i*2]);
			}
		}else{
			System.out.println("No devices found");
		}
		System.out.println("");
		
		// Ranging with the selected destination for 5 times
		command = String.format("%d %s", CLIENT_GET_RANGE, uniqueID);
		
		double sum=0;
		int counter=0;
		for(int i=0;i<5;i++){
			System.out.println("Ranging request #"+(i+1));
			// Ranging request
			response = UDPMessage(command);
			System.out.println("Response: " + response);
			// Extract the distance value from the received message
			splitted = response.split("\\s+");
			if(splitted[1].equals("255")){
				System.out.println("Ranging Error");
			}else{
				sum=sum+Double.parseDouble(splitted[2]);
				counter=counter+1;
			}
		}
		// Print the mean distance
		System.out.println("");
		System.out.println("Mean distance: "+sum/counter+" m");
		// Close the socket
		socket.close();
	}

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
