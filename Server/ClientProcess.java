package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientProcess implements Runnable
{
	Socket s;
	DataInputStream dis;
	DataOutputStream dos;
	
	boolean connected = true;
	
	PlayerAccount account;
	
	public ClientProcess(Socket cs)
	{
		try
		{
			dis = new DataInputStream(cs.getInputStream());
			dos = new DataOutputStream(cs.getOutputStream());
		}
		catch(IOException e1)
		{
			disconnect(true);
			return;
		}
		
		s = cs;
	}
	
	void disconnect(boolean fail)
	{
		connected = false;
		Server.logout(this, fail);
	}

	@Override
	public void run()
	{
		try
		{
			dos.writeUTF("Welcome to CSMUD!\n\n");
			
			while(account == null)
			{
				dos.writeUTF("What is your name?\n");
				String name = dis.readUTF().strip();
				
				if((name.matches(".*\\s.*") || !name.matches("[a-zA-Z]+")) && name.length() > 0)
				{
					dos.writeUTF("Names can only use letters and cannot contain whitespace.\n\n");
					continue;
				}
				
				if(Server.checkAccountExists(name))
				{
					dos.writeUTF("What is your password?\n");
					String pass = dis.readUTF().strip();
					
					account = Server.attemptLogin(this, name, pass);
				}
				else
				{
					dos.writeUTF("Account not found. Would you like to create a new character with this login? y/n\n");
					String response = dis.readUTF().strip();
					
					if(response.equals("y") || response.equals("yes"))
					{
						dos.writeUTF("What is your password?\n");
						String pass = dis.readUTF().strip();
						
						account = Server.createAccount(this, name, pass);
					}
				}
			}
		}
		catch (IOException e1)
		{
			disconnect(true);
		}
		
		String command;
		while (connected)
		{
			try
			{
				command = dis.readUTF().strip();
				
				System.out.println(command + " from " + account.name);
				
				if(command.equals("quit") || command.equals("exit") || command.equals("logout"))
				{
					disconnect(false);
					break;
				}
				else
					Server.handleClientCommand(this, command);
				
			}
			catch (IOException e)
			{
				disconnect(true);
				break;
			}
			
		}
		
		try
		{
			dis.close();
			dos.close();
			s.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}