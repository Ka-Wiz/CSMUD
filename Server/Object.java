package Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Object
{	
	public Object() { Initialize(); }
	public Object(Object in) { Initialize(); storeIn(in); }
	public Object(String name) { Initialize(); setName(name); }
	public Object(Object in, String name) { Initialize(); setName(name); storeIn(in); }
	public Object(String name, String desc) { Initialize(); setName(name); setDescription(desc); }
	public Object(Object in, String name, String desc) { Initialize(); setName(name); setDescription(desc); storeIn(in); }
	void Initialize() {};
	
	// OBJECT INFO ===========================
	private String name = "Object";
	private String description = "A miscellaneous object. The world is composed of such things.";
	
	public String getName() { return name; }
	public String getDescription() { return description; }
	
	public void setName(String s)
	{
		if(containedIn != null)
		{
			Integer idx = containedIn.contentIndexByName.remove(name.toLowerCase());
			containedIn.contentIndexByName.put(s.toLowerCase(), idx);
		}
		
		name = s;
	}
	public void setDescription(String s) { description = s; }
	public void setLocked(String s) { lockMessage = s; locked = true; }
	
	String buildDescription()
	{
		String desc = description;
		
		for(var entry : decorators.entrySet())
			desc += entry.getValue().buildDescription();
		
//		if(contents.size() > 0)
//		{
//			String interact = "You know you can interact with:\n";
//			
//			for(int i = 0; i < contents.size(); ++i)
//				interact += contents.get(i).name + "\n";
//			
//			desc += FormatBlock(interact);
//		}
		
//		if(commandStrings.size() > 0)
//			desc += FormatBlock(GetCommands());
		
		return FormatBlock(desc);
	}
	
	// COMMANDS ==============================
	HashMap<String, ArrayList<Object>> preListeners;
	HashMap<String, ArrayList<Object>> postListeners;
	
	HashMap<String, Command> commandStrings = new HashMap<String, Command>();
	
	Command getCommand(String commandName)
	{
		Command c = commandStrings.get(commandName);
		if(c != null)
			return c;
		else
		{
			for(var entry : decorators.entrySet())
			{
				c = entry.getValue().commandStrings.get(commandName);
				if(c != null)
					return c;
			}
		}
		
		return null;
	}
	String getCommands()
	{
		String cmd = "";
		
		for(String s : commandStrings.keySet())
			cmd += s + ", ";
		
		for(var entry : decorators.entrySet())
			cmd += entry.getValue().GetCommands();
		
		if(cmd.length() > 0)
		{
			cmd = ("\nYou know you can:\n" + cmd);
			cmd = (cmd.substring(0, cmd.length() - 2) + "\n");
		}
		else
			cmd = "You know there is nothing special you can do with this object.";
		
		return cmd;
	}

	boolean locked;
	String lockMessage = "You cannot take that object.";
	
	// DECORATORS ============================
	private HashMap<Class<? extends Decorator>, Decorator> decorators = new HashMap<Class<? extends Decorator>, Decorator>();
	
	public HashMap<Class<? extends Decorator>, Decorator> getDecorators() { return decorators; }
	public <D extends Decorator> D addDecorator(Class<D> d)
	{
		Decorator tmp = decorators.get(d);
		
		if(tmp == null)
			try
			{
				tmp = d.getDeclaredConstructor().newInstance();
				tmp.obj = this;
				decorators.put(d, tmp);
				return d.cast(tmp);
			}
			catch(Exception e) { e.printStackTrace(); return null;}
		else
			return null;
	}
	public <D extends Decorator> D addDecorator(D d)
	{
		Decorator tmp = decorators.get(d.getClass());
		
		if(tmp == null)
			try
			{
				d.obj = this;
				decorators.put(d.getClass(), d);
				return (D)d;
			}
			catch(Exception e) { e.printStackTrace(); return null;}		
		else
			return null;
	}
	public <D extends Decorator> D getDecorator(Class<D> d)
	{
		Decorator tmp = decorators.get(d);
		if(tmp == null)
			return null;
		
		return d.cast(tmp);
	}
	public void removeDecorator(Class<? extends Decorator> d)
	{
		decorators.remove(d);
	}
	public boolean hasDecorator(Class<? extends Decorator> d)
	{
		Decorator dec = decorators.get(d);
		
		return dec != null;
	}
	
	// INTEGRITY =============================
	private int integrity = 100;
	
	public int getIntegrity() { return integrity; }
	public void changeIntegrityBy(int chg) { integrity += chg; }
	
	// CONTAINMENT ===========================
	public ArrayList<Object> contents = new ArrayList<Object>();
	public ArrayList<Object> parts = new ArrayList<Object>();
	private Map<String, Integer> contentIndexByName = new HashMap<String, Integer>();
	
	public Object containedIn;
	Object wasIn;
	public String containmentPreposition = "in";	// you are "in", "on", "under", etc
	
	public Object getContainedObjectFromString(String str)
	{
		if(Server.checkDebug(Server.DBG_INV))
		{
			String s = "inv:\n";
			for(int i = 0; i < contents.size(); ++i)
				s += contents.get(i).name + "\n";
			
			s += "\nkeys:\n";
			for(int i = 0; i < contents.size(); ++i)
				s += contentIndexByName.keySet().toArray()[i] + "\n";
			
			printSelf(s);
		}
		
		Integer idx = contentIndexByName.get(str.toLowerCase());
		
		if(idx != null)
			return contents.get(idx);
		else
			return null;
	}
	public void storeIn(Object targ)
	{
		if(containedIn != null)
		{
			Integer rm = containedIn.contentIndexByName.remove(name.toLowerCase());
			containedIn.contents.remove(rm.intValue());
			
			wasIn = containedIn;
		}
		
		targ.contents.add(this);
		targ.contentIndexByName.put(name.toLowerCase(), targ.contents.size()-1);
		containedIn = targ;
	}
	String getInteractables()
	{
		String ntr = "";
		if(contents.size() > 0)
		{
			String interact = "You know you can interact with:\n";
			
			for(int i = 0; i < contents.size(); ++i)
				interact += contents.get(i).name + ", ";
			
			ntr += FormatBlock(interact.substring(0, interact.length()-2));
		}
		
		return ntr;
	}
	
	// =================================================
	
	public String FormatBlock(String str)
	{
		if(str.length() > 0)
			return str.strip() + "\n\n";
		else
			return "";
	}
	public void printSelf(String str)
	{
		if(str.length() > 0)
			Server.printToClient(str.strip() + "\n\n");
	}
	public void printRoom(String str)
	{
		if(str.length() > 0)
			Server.printToRoom(str.strip() + "\n\n", containedIn);
	}
	public void printRoom(String str, Object room)
	{
		if(str.length() > 0)
			Server.printToRoom(str.strip() + "\n\n", room);
	}
	public void printRoomAll(String str)
	{
		if(str.length() > 0)
			Server.printToRoomAll(str.strip() + "\n\n", containedIn);
	}
}