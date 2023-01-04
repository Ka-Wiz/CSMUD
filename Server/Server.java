package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import Server.Decorators.PlayerControlled;

public class Server
{
	private static boolean running = false;
	public static boolean isRunning() { return running; }
	
	public static void main(String[] args) throws IOException
	{
		Commands.Initialize();
		
		startWorldTime();
		Creation.createWorld();
		
		running = true;
		
		ServerSocket ss = new ServerSocket(1234);
		
		createAccount(null, "", "");
		
		while (running)
			try
			{
				new Thread(new ClientProcess(ss.accept())).start();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		
		ss.close();
	}
	
	// WORLD ============================
	static Object world = new Object();
	static Object startingRoom = world;
	public static LocalTime worldTime;
	static float worldTick = 2.5f;		// how many seconds before world time updates
	static int worldTickMinutes = 1;	// how many minutes to advance in-game time per tick
	public static LocalTime lastWorldTick;
	
	private static Timer timer = new Timer();
	public static class ScheduleTask
	{
		private TimerTask tt;
		public void setTimerTask(TimerTask t) { tt = t; }
		public boolean isScheduled() { return tt != null; } // no guarantee tt is nulled after running, but mostly used for resetting periodic tasks via cancel()
		public void run() {};
		public void cancel() { tt.cancel(); tt = null; }
	}
	public static ScheduleTask schedule(ScheduleTask task, float delay)
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
	    			e.printStackTrace();
	    		}
	    		finally
	    		{
	    			lock.writeLock().unlock();
	    		}
	        }
	    };
	    task.setTimerTask(tt);
	    
	    timer.schedule(tt, (long)(delay * 1000.f));
	    return task;
	}
	public static ScheduleTask schedule(ScheduleTask task, LocalTime time)
	{
		float seconds = (Duration.between(worldTime, time).toMinutes() / worldTickMinutes) * worldTick - Duration.between(lastWorldTick, LocalTime.now()).toSeconds();
		return schedule(task, seconds);
	}
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
			Server.curClient = null;
		}
		catch(Exception e)
		{
			System.out.println("Exception from '" + cp.account.controlling.getName() + "', Cmd '" + command + "':\n" + e);
			e.printStackTrace();
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
				printToCurrentClient("Account creation successful, welcome to CSMUD!\nType \"help\" for... well, you get it.");
				
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
			e.printStackTrace();
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
			e.printStackTrace();
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
						Object player = account.controlling;
						player.storeIn(player.wasIn);
						
						player.getDecorator(PlayerControlled.class).client = account.client = cp;
						
						printToCurrentClient("A swirling flash of multicolored light heralds your arrival into the world.");
						printToRoom(player.getName() + " appears in a swirling flash of multicolored light.", player.containedIn);
						
						Commands.parseCommand(player, "look");
						Commands.parseCommand(player, "think");
						
						clients.add(cp);
						
						return account;
					}
					else
					{
						printToCurrentClient("This account is already logged in!");
						return null;
					}
				}
				else
				{
					printToCurrentClient("Incorrect login.");
					return null;
				}
			}
			else
				return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
	public static void logout(ClientProcess cp, boolean fail)
	{
		try
		{
			lock.writeLock().lock();
			
			if(!fail)
			{
				Server.curClient = cp;
				printToCurrentClient("Your view of the world quickly dissipates to nothing as you are enveloped by a multicolored light.");
			}
			
			Object player = cp.account.controlling;
			player.getDecorator(PlayerControlled.class).client = cp.account.client = null;
			clients.remove(cp);
			
			printToRoom(player.getName() + " disappears in a flash of multicolored light, leaving emptiness behind.", player.containedIn);
			player.storeIn(world);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			lock.writeLock().unlock();
		}
	}
	
	// PRINTING =========================
	public static void printToCurrentClient(String msg)
	{
		try
		{
			if(curClient != null)
				curClient.dos.writeUTF(msg.strip() + "\n\n");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public static void printToClient(String msg, ClientProcess cli)
	{
		try
		{
			cli.dos.writeUTF(msg.strip() + "\n\n");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public static void printToOther(String msg, Object other)
	{
		PlayerControlled pc = other.getDecorator(PlayerControlled.class);
		if(pc != null)
		{
			try
			{
				pc.client.dos.writeUTF(msg.strip() + "\n\n");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	public static void printToRoom(String msg, Object room)
	{
		try
		{
			for(ClientProcess cp : clients)
				if(cp.account.controlling.getRoom() == room && cp != curClient)
					cp.dos.writeUTF(msg.strip() + "\n\n");			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public static void printToRoomExcluding(String msg, Object room, Object... toIgnore )
	{
		try
		{
			for(ClientProcess cp : clients)
				if(cp.account.controlling.getRoom() == room && !new ArrayList<Object>(Arrays.asList(toIgnore)).contains(cp.account.controlling))
					cp.dos.writeUTF(msg.strip() + "\n\n");			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public static void printToRoomAll(String msg, Object room)
	{
		try
		{
			for(ClientProcess cp : clients)
				if(cp.account.controlling.getRoom() == room)
					cp.dos.writeUTF(msg.strip() + "\n\n");			
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void broadcast(String msg)
	{
		for(ClientProcess cp : clients)
			try
			{
				cp.dos.writeUTF(msg.strip() + "\n\n");
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
	}
	public static void broadcast(String msg, Object toIgnore)
	{
		for(ClientProcess cp : clients)
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