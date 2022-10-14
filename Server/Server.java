package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Server
{
	public static void main(String[] args) throws IOException
	{
		Commands.Initialize();
		
		Creation.createWorld();
		startWorldTime();
		
		@SuppressWarnings("resource")
		ServerSocket ss = new ServerSocket(1234);
		Socket cs;
		
		createAccount(null, "", "");
		
		while (true)
		{
			cs = ss.accept();

			System.out.println("Connection from: " + cs);

			ClientProcess cp = new ClientProcess(cs);
			new Thread(cp).start();
		}
	}
	
	// WORLD ============================
	static Object world = new Object();
	static Object startingRoom = world;
	public static LocalTime worldTime;
	static float worldTick = 2.5f;		// how many seconds before world time updates
	static int worldTickMinutes = 1;	// how many minutes to advance in-game time per tick
	public static LocalTime lastWorldTick;
	
	private static Timer timer = new Timer();
	private static void startWorldTime()
	{
		worldTime = LocalTime.of(7, 0);
		schedule(new ScheduleTask() {
			public void run()
			{
				lastWorldTick = LocalTime.now();
				worldTime = worldTime.plusMinutes(worldTickMinutes);
				
				schedule(this, worldTick);
			}
		}, worldTick);
	}
	public static void schedule(ScheduleTask task, float delay)
	{
		TimerTask tt = new TimerTask() {
	        public void run()
	        {
	        	try
	    		{
	    			lock.writeLock().lock();
	    			
	    			task.run();
	    		}
	    		catch(Exception e)
	    		{
	    			System.out.println("Exception from scheduled task");
	    		}
	    		finally
	    		{
	    			lock.writeLock().unlock();
	    		}
	        }
	    };
	    
	    timer.schedule(tt, (long)(delay * 1000.f));
	}
	public static void schedule(ScheduleTask task, LocalTime time)
	{
		float seconds = (Duration.between(worldTime, time).toMinutes() / worldTickMinutes) * worldTick - Duration.between(lastWorldTick, LocalTime.now()).toSeconds();
		schedule(task, seconds);
	}	
	
	// CLIENT HANDLING ==================
	private static ClientProcess curClient;
	static Vector<ClientProcess> clients = new Vector<>();
	static HashMap<String, PlayerAccount> accounts = new HashMap<String, PlayerAccount>();
	static int clientID = 0;
	static ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	
	public static void handleClientCommand(ClientProcess cp, String command)
	{
		try
		{
			lock.writeLock().lock();
			
			Server.curClient = cp;
			Commands.parseCommand(cp.account.controlling, command);
		}
		catch(Exception e)
		{
			System.out.println("Exception from '" + cp.account.controlling.getName() + "', Cmd '" + command + "':\n" + e);
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
	
	// ACCOUNTS =========================
	public static PlayerAccount createAccount(ClientProcess cp, String name, String pass)
	{
		try
		{
			lock.writeLock().lock();
			
			PlayerAccount account = new PlayerAccount();
			accounts.put(name, account);
			account.name = name;
			account.password = pass;
			
			Object player = Creation.createPlayer(startingRoom, name, cp);
			player.wasIn = Server.startingRoom;
			
			account.controlled.add(player);
			account.controlling = player;
			
			if(cp != null)
			{
				Server.curClient = cp;
				printToClient("Account creation successful, welcome to CSMUD!\nType \"help\" for... well, you get it.");
				
				attemptLogin(cp, name, pass);
			}
			else
				player.storeIn(world);
			
			if(checkDebug(DBG_LGN))
				System.out.println("account created, returning " + account.name);

			return account;
		}
		catch(Exception e)
		{
			System.out.println("EXCEPTION in createAccount: " + e);
			return null;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
	public static boolean checkAccountExists(String name)
	{
		try
		{
			lock.readLock().lock();
			
			return accounts.containsKey(name);
		}
		catch(Exception e)
		{
			return false;
		}
		finally
		{
			lock.readLock().unlock();
		}
	}
	public static PlayerAccount attemptLogin(ClientProcess cp, String name, String pass)
	{
		try
		{
			lock.writeLock().lock();
			
			Server.curClient = cp;
			
			PlayerAccount account = accounts.get(name);
			
			if(account != null)
			{
				if(account.password.equals(pass))
				{
					if(account.client == null)
					{
						account.client = cp;
						Object player = account.controlling;
						player.storeIn(player.wasIn);
						
//						printToClient("Login Success! Welcome, " + name + "!");
						printToClient("A swirling flash of multicolored light heralds your arrival into the world.");
						printToRoom(player.getName() + " appears in a swirling flash of multicolored light.", player.containedIn);
						
						Commands.parseCommand(player, "look");
						Commands.parseCommand(player, "think");
						
						clients.add(cp);
						
						return account;
					}
					else
					{
						printToClient("This account is already logged in!");
						return null;
					}
				}
				else
				{
					printToClient("Incorrect login.");
					return null;
				}
			}
			else
				return null;
		}
		catch(Exception e)
		{
			return null;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
	public static void logout(ClientProcess cp)
	{
		try
		{
			lock.writeLock().lock();
			
			Server.curClient = cp;
			Object player = cp.account.controlling;
			
			printToRoom(player.getName() + " disappears in a flash of multicolored light, leaving emptiness behind.", player.containedIn);
			printToClient("Your view of the world quickly dissipates to nothing as you are enveloped by a multicolored light.");
			
			player.storeIn(world);
			
			cp.account.client = null;
			clients.remove(cp);
			
			//player.StoreIn(player);
		}
		catch(Exception e)
		{
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
	
	// PRINTING =========================
	public static void printToClient(String msg)
	{
		try
		{
			curClient.dos.writeUTF(msg.strip() + "\n\n");
		}
		catch (IOException e)
		{
		}
	}
	public static void printToRoom(String msg, Object room)
	{
		try
		{
			for(ClientProcess cp : clients)
				if(cp.account.controlling.containedIn == room && cp != curClient)
					cp.dos.writeUTF(msg.strip() + "\n\n");			
		}
		catch (IOException e)
		{
			System.out.println("an exception " + e);
		}
	}
	public static void printToRoomAll(String msg, Object room)
	{
		try
		{
			for(ClientProcess cp : clients)
				if(cp.account.controlling.containedIn == room)
					cp.dos.writeUTF(msg.strip() + "\n\n");			
		}
		catch (IOException e)
		{
			System.out.println("an exception " + e);
		}
	}
	
	public static void broadcast(String msg)
	{
		for(ClientProcess cp : clients)
		{
			try
			{
				cp.dos.writeUTF(msg.strip() + "\n\n");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	public static void broadcast(String msg, Object toIgnore)
	{
		for(ClientProcess cp : clients)
		{
			if(cp.account.controlling != toIgnore)
				try
				{
					cp.dos.writeUTF(msg);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
		}
	}
	
	// DEBUG ============================
	static final int DBG_INV = 1;
	static final int DBG_CMD = 2;
	static final int DBG_LGN = 4;
	static int debugFlags = 0;
	static boolean checkDebug(int flag) { return (debugFlags & flag) > 0; }
}

class PlayerAccount
{
	ClientProcess client;
	String name, password;
	ArrayList<Object> controlled = new ArrayList<Object>();
	Object controlling;
}