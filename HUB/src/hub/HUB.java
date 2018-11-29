package hub;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Random;
import com.pi4j.component.lcd.LCDTextAlignment;
import com.pi4j.component.lcd.impl.GpioLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

class Connect extends Thread{
    public int Receive() throws UnknownHostException{
                InetAddress IP;
                String ipAddr2 = "192.168.1.13";
                IP = InetAddress.getByName(ipAddr2);
                boolean receive = true;
                int safe = 0;
 while(receive){
                   
                    try{
                        Socket clientSocket = new Socket(IP, 56560);
                        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        safe = Integer.parseInt(inFromServer.readLine());
                        
                        clientSocket.close();
                        receive = false;
                        
                    }
                   
                catch( Exception e )
                {
                   e.printStackTrace();
                }
                
                  
                
                }
                return safe;
                }

    public boolean Send(int send) throws UnknownHostException{
                InetAddress IP;
                String ipAddr2 = "192.168.1.13";
                IP = InetAddress.getByName(ipAddr2);
                boolean end = true;
                int safe = 0;
            while (end){
                    try{
                        
                        Socket clientSocket = new Socket(IP, 56560);
                    
                        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                        
                        outToServer.writeBytes(Integer.toString(safe));    
                        clientSocket.close();
                        end = false;
              
                       
                        }
                   
                catch( Exception e )
                {
                   e.printStackTrace();
                }
                    }
    return end;}

}
public class HUB {

        public final static int LCD_ROW_1 = 0;
        public final static int LCD_ROW_2 = 1;
	public static final int CLIENT_PING = 0;
	public static final int CLIENT_SCAN = 56;
	public static final int CLIENT_GET_RANGE = 50;
	public final static int PACKETSIZE = 1000;
        public static int safe = 0; //acknowledge varible 0 until safe
	static String ipAddr = "192.168.1.4";
        static String ipAddr2 = "192.168.1.13";// IP address of the Raspberry Pi computer equipped with SEQUITUR Pi board
	static int port = 5678;							// Port used for the UDP connection with SEQUITUR RANGING
	static String uniqueID = "10205FE010003286";	// Unique ID of the SEQUITUR Pi board you want to range with (check the scan result for the available addresses)
        static InetAddress IP;
	static InetAddress IPAddress;
        public final static double set_distance = 5; // Danger zone in meters
	static DatagramSocket socket;
	static DatagramPacket packet;
        static DatagramSocket ds;
	static DatagramPacket dp;
       static DatagramPacket dp1;
       static boolean send = true;
              
       

	public static void main(String[] args) throws InterruptedException, IOException {
	
            // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, "MOSFet", PinState.LOW);
        final GpioPinDigitalOutput ssource = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_28, "Source", PinState.LOW);
        GpioPinDigitalInput sinput = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29);
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
                lcd.write(LCD_ROW_1, "Online Devices:",LCDTextAlignment.ALIGN_CENTER); // Screen Output
                lcd.write(LCD_ROW_2, "ID: Node56",LCDTextAlignment.ALIGN_CENTER);
                ssource.setState(PinState.HIGH);
                Thread.sleep(2000);
                if(sinput.isHigh()){
		try {
			// Create the socket
			socket = new DatagramSocket();  // Opens UDP connection with Ranging software on PI
			IPAddress = InetAddress.getByName(ipAddr);
		} catch (UnknownHostException e) {
			System.out.println("IP address not correct");
		} catch (SocketException e) {
			System.out.println("Socket Error");
		}

		// Ping SEQUITUR RANGING
		String response = UDPMessage(String.valueOf(CLIENT_PING)); 
		
		// Ranging with the selected destination for 5 times
		String command = String.format("%d %s", CLIENT_GET_RANGE, uniqueID);
		String [] splitted = response.split("\\s+");
		double sum=0;
		int counter=0;
		for(int i=0;i<5;i++){
			System.out.println("Ranging request #"+(i+1));  // Loops five times and gets the distance each time
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
		// mean distance
		double location = sum/counter;  //adds all the distances and divides them by 5 to get the average
		// Close the socket
		socket.close();
                
               
                
                if (location < set_distance && location > 0){  // Check within distance. This distance is set at top in meters
                lcd.clear();
                lcd.write(LCD_ROW_1, "Alerting Device:",LCDTextAlignment.ALIGN_CENTER); 
                lcd.write(LCD_ROW_2, "ID: Node56",LCDTextAlignment.ALIGN_CENTER);
                
                
              Connect client = new Connect();
              client.Send(safe);
              
              while (client.isAlive()){
                 
              }
              
              safe = client.Receive();
                
                while (client.isAlive())
                {}
                
               
                }
                        
                
                else{
                    safe = 1;
                }
                
                 if (safe==1){
                   
               pin.setState(PinState.HIGH);//turn on mosfet
               lcd.clear();
               while(sinput.isHigh()){
               lcd.write(LCD_ROW_1, "Success",LCDTextAlignment.ALIGN_CENTER); 
               lcd.write(LCD_ROW_2, "Great Job",LCDTextAlignment.ALIGN_CENTER);
               }}}
                 
                }

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
