package dark2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import com.pi4j.component.lcd.LCDTextAlignment;
import com.pi4j.component.lcd.impl.GpioLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.system.NetworkInfo;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HUB {

        public final static int LCD_ROW_1 = 0;
        public final static int LCD_ROW_2 = 1;
	public static final int CLIENT_PING = 0;
	public static final int CLIENT_SCAN = 56;
	public static final int CLIENT_GET_RANGE = 50;
	public final static int PACKETSIZE = 1000;
        public static int safe = 0; //acknowledge varible 0 until safe
	static String ipAddr = "192.168.0.16";			// IP address of the Raspberry Pi computer equipped with SEQUITUR Pi board
	static int port = 5678;							// Port used for the UDP connection with SEQUITUR RANGING
	static String uniqueID = "10205EA910000EC9";	// Unique ID of the SEQUITUR Pi board you want to range with (check the scan result for the available addresses)

	static InetAddress IPAddress;
        static InetAddress IP;
	static DatagramSocket socket;
	static DatagramPacket packet;

	public static void main(String[] args) throws InterruptedException, IOException {
	
            // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // initialize LCD
        final GpioLcdDisplay lcd = new GpioLcdDisplay(2,    // number of row supported by LCD
                                                16,       // number of columns supported by LCD
                                                RaspiPin.GPIO_09,  // LCD RS pin
                                                RaspiPin.GPIO_08,  // LCD strobe pin
                                                RaspiPin.GPIO_25,  // LCD data bit D4
                                                RaspiPin.GPIO_15,  // LCD data bit D5
                                                RaspiPin.GPIO_16,  // LCD data bit D6
                                                RaspiPin.GPIO_24); // LCD data bit D7
        lcd.clear();
while (true){		
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
		
		
		// SCAN for other nodes running SEQUITUR RANGING
		
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
			
			// Ranging request
			response = UDPMessage(command);
			
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
		double location = sum/counter;
		// Close the socket
		socket.close();
                
                DatagramSocket ds = new DatagramSocket();
                byte[] safecheck = (safe+"").getBytes(); //changes varible into bytes
                IP = InetAddress.getByName(ipAddr);
                DatagramPacket dp = new DatagramPacket(safecheck,safecheck.length,IP,5656);
                ds.send(dp);
                

        
         
          
          
           
        
        
        gpio.shutdown();
    
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
