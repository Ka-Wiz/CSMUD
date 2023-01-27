package Server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import Server.Decorators.PlayerControlled;

public class Object
{	
	public Object() { Initialize(); }
	public Object(Object in) { Initialize(); storeIn(in); }
	public Object(String name) { Initialize(); setName(name); }
	public Object(Object in, String name) { Initialize(); setName(name); storeIn(in); }
	public Object(String name, String desc) { Initialize(); setName(name); setDescription(desc); }
	public Object(Object in, String name, String desc) { Initialize(); setName(name); setDescription(desc); storeIn(in); }
	void Initialize() {};
	
	private static Random rand = new Random();
	public float organicRandomFloat(float range) { return rand.nextFloat() * range; }
	public int organicRandomInt(int range) { return rand.nextInt() % range; }
	
	// OBJECT INFO ===========================
	private String name = "Object";
	private String description = "A miscellaneous object. The world is composed of such things.";
	private HashMap<String, String> descriptionModifiers = new HashMap<>();
	
	public String getName() { return namedIndex == 0 ? name : name + " " + namedIndex; }
	public String toString() { return getName(); }
	String buildDescription()
	{
		String desc = description;
		
		for(var entry : decorators.entrySet())
			desc += entry.getValue().buildDescription();
		
		for(var entry : descriptionModifiers.entrySet())
			desc += "\n" + entry.getValue();
		
		return FormatBlock(desc);
	}
	
	public void setName(String newName)
	{
		if(containedIn != null)
			containedIn.changeContentName(this, newName);
		else
			name = newName;
	}
	public void setDescription(String s) { description = s; }
	public void addDescriptionModifier(String key, String desc) { descriptionModifiers.put(key, desc); }
	public void removeDescriptionModifier(String key, String desc) { if(descriptionModifiers.containsKey(key)) descriptionModifiers.remove(key); }
	
	public void setLocked(String s) { lockMessage = s; locked = true; }
	
	// CONTAINMENT ===========================
	public ArrayList<Object> contents = new ArrayList<Object>();
	private HashMap<String, ArrayList<Object>> contentsByName = new HashMap<>();
	private int namedIndex = 0;
	
	public Object containedIn;
	Object wasIn;
	public String containmentPreposition = "in";	// you are "in", "on", "under", etc
	
	boolean locked;
	String lockMessage = "You cannot take that object.";
	
	public Object getContained(String str, boolean recurse)
	{
		if(Server.checkDebug(Server.DBG_INV))
		{
			String s = "inv:\n";
			for(int i = 0; i < contents.size(); ++i)
				s += contents.get(i).name + "\n";
			
			s += "\nkeys:\n";
			for(int i = 0; i < contents.size(); ++i)
				s += contentsByName.keySet().toArray()[i] + "\n";
			
			printSelf(s);
		}
		
		str = str.toLowerCase();
		
		int idx = 0;
		
		String[] words = str.split(" ");
		if(words.length > 1)
		{
			String last = words[words.length-1];
			if(last.length() <= 2)
			{
				try { idx = Integer.parseInt(last); } // i hate java sometimes
				catch(Exception e) {}
				
				if(idx > 0)
				{
					--idx;
					str = str.substring(0, str.length() - last.length() - 1);
				}
			}
		}

		ArrayList<Object> named = contentsByName.get(str);
		
		if(named != null && idx < named.size())
			return named.get(idx);
		else if(recurse)
		{
			Object found = null;
			for(Object o : contents)
				if((found = o.getContained(str, recurse)) != null)
					break;
			
			return found;
		}
		else
			return null;
	}
	public Object getContained(String str)
	{
		return getContained(str, false);
	}
	public void storeIn(Object targ)
	{
		if(containedIn != null)
		{
			containedIn.contents.remove(this);
			containedIn.removeContentName(this);
			namedIndex = 0;
			
			wasIn = containedIn;
		}
		
		targ.contents.add(this);
		targ.addContentName(this);
		containedIn = targ;
	}
	private void addContentName(Object obj)
	{
		String lowName = obj.name.toLowerCase();
		ArrayList<Object> named = contentsByName.get(lowName);
		if(named == null)
			contentsByName.put(lowName, named = new ArrayList<Object>());
		else
		{
			obj.namedIndex = named.size() + 1;
			named.get(0).namedIndex = 1;
		}
		named.add(obj);
	}
	private void removeContentName(Object obj)
	{
		String lowName = obj.name.toLowerCase();
		ArrayList<Object> named = contentsByName.get(lowName);
		named.remove(obj);
		if(named.size() == 0)
			contentsByName.remove(lowName);
		else
			if(named.size() == 1)
				named.get(0).namedIndex = 0;
			else
				for(int i = 0; i < named.size(); ++i)
					named.get(i).namedIndex = i + 1;
	}
	private void changeContentName(Object obj, String newName)
	{
		removeContentName(obj);
		obj.name = newName;
		addContentName(obj);
	}
	String getInteractables()
	{
		String ntr = "";
		if(contents.size() > 0)
		{
			String interact = "You know you can interact with:\n";
			
//			for(var entry : contentsByName.entrySet())
//			{
//				ArrayList<Object> list = entry.getValue();
//				for(int i = 0; i < list.size(); ++i)
//				{
//					interact += list.get(i).getName() + (i > 0 ? " " + (i+1) : "") + ", ";
//				}
//			}
			
			for(int i = 0; i < contents.size(); ++i)
				interact += contents.get(i).getName() + ", ";
			
			ntr += FormatBlock(interact.substring(0, interact.length()-2));
		}
		
		return ntr;
	}
	public Object getRoom()
	{
		return containedIn;
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
			for(var entry : decorators.entrySet())
				if((c = entry.getValue().commandStrings.get(commandName)) != null)
					return c;
		
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
	
	Combat combat;
	
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
	public <D extends Decorator> D findDecoratorInChildren(Class<D> d)
	{
		D dec = null;
		for(Object o : contents)
			if((dec = o.getDecorator(d)) != null)
				break;
		
		return dec;
	}
	public <D extends Decorator> D findDecoratorInChildrenRecursive(Class<D> d) // will return deepest found
	{
		D dec = null;
		
		for(Object o : contents)
			if((dec = o.findDecoratorInChildrenRecursive(d)) != null)
				return dec;	
		
		if((dec = getDecorator(d)) != null)
			return dec;
		
		return dec;
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
	private int maxIntegrity = 100;
	private boolean defeated = false;
	
	boolean isDefeated() { return defeated; }
	
	public int getIntegrity() { return integrity; }
	public void setMaxIntegrity(int max) { maxIntegrity = max; integrity = maxIntegrity;}
	public void changeIntegrityBy(int chg)
	{
		changeIntegrityBy(chg, 0);
	}
	public void changeIntegrityBy(int chg, int overMaxAmount)
	{
		int newValue = integrity + chg;
		
		if(newValue > maxIntegrity)
		{
			int overMax = newValue - maxIntegrity;
			if(overMax > overMaxAmount)
				overMax = overMaxAmount;
			
			newValue = maxIntegrity + overMax;
		}
		
		integrity = newValue;
		
		if(integrity < 0)
			defeat.alwaysOnDefeat();
	}
	public DefeatBehavior defeat = new DefeatBehavior(this);
	
	public class DefeatBehavior
	{
		Object obj;
		DefeatBehavior(Object o) { obj = o; }
		
		void alwaysOnDefeat()
		{
			obj.defeated = true;
			obj.integrity = 0;
			
			if(obj.combat != null)
				combat.removeParticipant(obj, false);
			
			onDefeat();
		}
		
		void onDefeat()
		{
			if(getDecorator(PlayerControlled.class) == null)
			{
				printRoom(getName() + " faints, defeated.");
				obj.addDescriptionModifier("defeat", "It has fainted and is not likely to get up for a long time.");
			}
			else
			{
				printRoom(getName() + " faints, hitting the ground and exploding into a swirl of multicolored wisps which soon disappear.");
				obj.storeIn(Server.startingRoom);
			}
		}
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
			Server.printToCurrentClient(str.strip() + "\n\n");
	}
	public void printOther(String str, Object other)
	{
		if(str.length() > 0)
			Server.printToCurrentClient(str.strip() + "\n\n");
	}
	public void printRoom(String str)
	{
		if(str.length() > 0)
			Server.printToRoom(str.strip() + "\n\n", getRoom());
	}
	public void printRoom(String str, Object room)
	{
		if(str.length() > 0)
			Server.printToRoom(str.strip() + "\n\n", room);
	}
	public void printRoomAll(String str)
	{
		if(str.length() > 0)
			Server.printToRoomAll(str.strip() + "\n\n", getRoom());
	}
}