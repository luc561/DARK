package node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Random;
import com.pi4j.component.lcd.LCDTextAlignment;
import com.pi4j.component.lcd.impl.GpioLcdDisplay;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import static node.Node.safe;


   class Connect extends Thread{
                public void Status(int saferesponse) throws IOException{
                boolean go = true;
                ServerSocket welcomeSocket = new ServerSocket(56560);  
               while(go){
                Socket connectionSocket = welcomeSocket.accept();
                    try{
                
                
                System.out.println("Sandwich One?");
                
                
                BufferedReader inFromClient =
                new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream()); 
                safe = Integer.parseInt(inFromClient.readLine());
                
                go= false;
                }            
                    catch (NullPointerException np){
                    
                }
              catch( Exception e )
                {
                   e.printStackTrace();
                }
                finally{
                      connectionSocket.close();
                      welcomeSocket.close();
                      
                    }}
             
             }
                public void Reply(int saferesponse) throws IOException{
                boolean go = true;
                ServerSocket welcomeSocket = new ServerSocket(56560);  
               while(go){
                Socket connectionSocket = welcomeSocket.accept();
                    try{
                
                
                System.out.println("Sandwich One?");
                
                
                
                DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream()); 
                
                outToClient.writeBytes(Integer.toString(saferesponse));
                go= false;
                }            
                    catch (NullPointerException np){
                    
                }
              catch( Exception e )
                {
                   e.printStackTrace();
                }
                finally{
                      connectionSocket.close();
                      welcomeSocket.close();
                      
                    }}
             
             }
   }

public class Node {

   public final static int LCD_ROW_1 = 0;
        public final static int LCD_ROW_2 = 1;
	public static final int CLIENT_PING = 0;
	public static final int CLIENT_SCAN = 56;
	public static final int CLIENT_GET_RANGE = 50;
	public final static int PACKETSIZE = 1000;
        public static int safe = 2; //acknowledge varible 2 until activated by HUB
        public static int saferesponse = 2;
        static String ipAddr = "192.168.0.13";
        static String ipAddr2 = "192.168.0.5";// IP address of the Raspberry Pi computer equipped with SEQUITUR Pi board
	static int port = 5678;	// Port used for the UDP connection with SEQUITUR RANGING
	static int hubport = 61342;	
        static String uniqueID = "10205FE010000379";	// Unique ID of the SEQUITUR Pi board you want to range with (check the scan result for the available addresses)
        
        
        
	static InetAddress IPAddress;
        static InetAddress IP;
	static DatagramSocket socket;
        static DatagramSocket ds;
         static DatagramPacket dp;
         static DatagramPacket dp1;
	static DatagramPacket packet;
 
    public static void main(String[] args) throws InterruptedException, IOException {
       
      // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();
        GpioPinDigitalInput acknowledge = gpio.provisionDigitalInputPin(RaspiPin.GPIO_26);
        final GpioPinDigitalOutput vib = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_21, "vibrating motor", PinState.LOW);
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
           
           IPAddress = InetAddress.getByName(ipAddr);
                IP = InetAddress.getByName(ipAddr2);     
while (true){		
                		
    
                
                lcd.write(LCD_ROW_1, "ID: Node56",LCDTextAlignment.ALIGN_CENTER); 
                lcd.write(LCD_ROW_2, "All Clear",LCDTextAlignment.ALIGN_CENTER);
                
                
                Connect areU =  new Connect();
                
                areU.Status(saferesponse);
                while(areU.isAlive()){
                }
                 
              
                
                if (safe==0){
                lcd.clear();
                
                while (acknowledge.isLow()){
                

                lcd.write(LCD_ROW_1, "Alert!!",LCDTextAlignment.ALIGN_CENTER); 
                lcd.write(LCD_ROW_2, "Central Hub",LCDTextAlignment.ALIGN_CENTER);
                vib.setState(PinState.HIGH);//vibrate until button pushed
                }
                
                saferesponse = 1;
                safe = saferesponse;
               
                
                Connect respond =  new Connect();
                respond.Reply(saferesponse);
                
                vib.setState(PinState.LOW);
                
                lcd.write(LCD_ROW_1, "Safe Status Sent",LCDTextAlignment.ALIGN_CENTER); 
                lcd.write(LCD_ROW_2, "Please Resume",LCDTextAlignment.ALIGN_CENTER);
              while(respond.isAlive()){
                } 
                Thread.sleep(2000);
                lcd.clear();}
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