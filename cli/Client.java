package cli;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client
{
	final static int ServerPort = 1234;
	
	static Socket s;
	static DataInputStream dis;
	static DataOutputStream dos;
	static boolean terminated = false;
	
	static Scanner scn = new Scanner(System.in);

	public static void main(String args[]) throws UnknownHostException, IOException
	{
		s = new Socket("localhost", ServerPort);
		
		dis = new DataInputStream(s.getInputStream());
		dos = new DataOutputStream(s.getOutputStream());

		Thread sendMessage = new Thread(new Runnable()
		{
			public void run()
			{
				while (!terminated)
					try
					{
						dos.writeUTF(scn.nextLine());
						System.out.println();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
			}
		});
		
		Thread readMessage = new Thread(new Runnable()
		{
			public void run()
			{
				while (true)
					try
					{
						System.out.print(dis.readUTF());
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
		});

		sendMessage.start();
		readMessage.start();
	}
}