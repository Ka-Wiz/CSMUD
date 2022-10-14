package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client
{
	final static int ServerPort = 1234;
	
	static Socket s;
	static DataInputStream dis;
	static DataOutputStream dos;
	static boolean terminated = false;
	
	static Scanner scn = new Scanner(System.in);
	
	static ArrayList<String> messageLog = new ArrayList<String>();
	
	static AtomicBoolean writing = new AtomicBoolean();
	
	static String input = "";
	static boolean skipped = false;

	public static void main(String args[]) throws UnknownHostException, IOException
	{
		s = new Socket("localhost", ServerPort);
		
		dis = new DataInputStream(s.getInputStream());
		dos = new DataOutputStream(s.getOutputStream());

		Thread sendMessage = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (!terminated)
				{
					try
					{
//						System.out.print("=======================\n\nWhat would you like to do?\n");
//						input = scn.nextLine().strip();
						dos.writeUTF(scn.nextLine());
//						if(input.contains("\n"))
//						{
//							input.strip();
//							//writing.set(true);
//							dos.writeUTF(input);
//							input = "";
//							CSMUDClient.skipped = false;
//						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					
					System.out.println();
				}
			}
		});
		
		Thread readMessage = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					try
					{
						String recv = dis.readUTF();
						System.out.print(recv);
						
//						if(CSMUDClient.input.length() > 0 && !skipped)
//						{
//							System.out.print("\n\n" + recv);
//							CSMUDClient.skipped = true;
////							writing.set(false);
//						}
//						else
//							System.out.print(recv);
							
//						messageLog.add(recv);
//						System.out.print("\033[H\033[2J");  
//					    System.out.flush();
//					    
//						for(String s : messageLog)
//							System.out.print(s);
					}
					catch (IOException e)
					{
						System.out.println("Disconnected!");
						System.exit(0);
						scn.close();
						sendMessage.interrupt();
						terminated = true;
						return;
					}
				}
			}
		});

		sendMessage.start();
		readMessage.start();
	}
}