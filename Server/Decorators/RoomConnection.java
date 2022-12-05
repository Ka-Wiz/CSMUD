package Server.Decorators;

import Server.Command;
import Server.Commands;
import Server.Decorator;
import Server.Object;

public class RoomConnection extends Decorator
{
	public Object connectionTo = null; // should be another RoomConnection 99% of the time
	public String moveAdverb; // "You walk: out/down/through/across" etc
	
	boolean closeable = false;
	boolean isOpen = true;
	String closeableString = "door"; // "The door/portal/window/hatch is open" etc
//	public Class<? extends Decorator> requiredToOpen = Movement.class;
	
	public boolean getCloseable() { return closeable; }
	public boolean getIsOpen() { return isOpen; }
	
	protected void Initialize()
	{
		commandStrings.put("use", new Command()
		{
			public void invoke()
			{
				Object dest = connectionTo.getDecorator(RoomConnection.class) == null ? connectionTo : connectionTo.containedIn;
				Object sender = Commands.sender;
				Movement mv = null;
				for(Object o : sender.contents)
				{
					Movement m = o.getDecorator(Movement.class);
					if(m != null && (mv == null || m.getMovePriority() < mv.getMovePriority()))
						mv = m;
				}
				
				if(mv == null)
				{
					obj.printSelf("You have nothing to move with!");
					return;
				}
				
				if(closeable)
				{
					if(!isOpen)
						Commands.parseCommand(sender, "open " + obj.getName());
					
					if(!isOpen)
					{
						obj.printSelf("Could not open, movement failed.");
						return;
					}
				}
				
				obj.printSelf("You " + mv.moveString + " " + moveAdverb + " the " + obj.getName() + " with your " + mv.obj.getName() + ".");
				obj.printRoom(sender.getName() + " " + mv.moveString + "s " + moveAdverb + " the " + obj.getName() + " to " + dest.getName() + ".");
				
				String arrive = sender.getName() + " arrives from ";
				if(connectionTo.getDecorator(RoomConnection.class) != null)
				{
					arrive += connectionTo.getName() + " to " + obj.containedIn.getName() + ".";
					sender.storeIn(connectionTo.containedIn);
					obj.printRoom(arrive, connectionTo.containedIn);
				}
				else
				{
					arrive += obj.containedIn.getName() + ".";
					sender.storeIn(connectionTo);
					obj.printRoom(arrive, connectionTo);
				}
				
				Commands.parseCommand(sender, "look");
				Commands.parseCommand(sender, "think");
			}
		});
		
		commandStrings.put("look", new Command()
		{
			public void invoke()
			{
				Object c = connectionTo.getDecorator(RoomConnection.class) == null ? connectionTo : connectionTo.containedIn;
				String msg = "A " + obj.getName() + " to " + c.getName() + ".";
				
				if(closeable)
					msg += " The " + closeableString + " is " + (isOpen ? "open" : "closed") + ".";
				
				obj.printSelf(msg);
			}
		});
		
		commandStrings.put("open", new Command()
		{
			public void invoke()
			{
				if(!closeable || isOpen)
				{
					obj.printSelf("The " + obj.getName() + " is not closed.");
				}
				else
				{
					Movement mv = getMovement();
					if(mv != null)
					{
						obj.printSelf("You open the " + closeableString + " with your " + mv.obj.getName() + ".");
						obj.printRoom(Commands.sender.getName() + " opens the " + closeableString + " with their " + mv.obj.getName() + ".");
						obj.printRoom("The " + closeableString + " to " + obj.getRoom().getName() + " opens.", connectionTo.getRoom());
						
						isOpen = true;
						connectionTo.getDecorator(RoomConnection.class).isOpen = true;
					}
					else
					{
						obj.printSelf("You are unable to open the " + closeableString);
					}
				}
			}
		});
		
		commandStrings.put("close", new Command()
		{
			public void invoke()
			{
				if(!closeable || !isOpen)
				{
					obj.printSelf("The " + obj.getName() + " cannot be closed.");
				}
				else
				{
					Movement mv = getMovement();
					if(mv != null)
					{
						obj.printSelf("You close the " + closeableString + " with your " + mv.obj.getName() + ".");
						obj.printRoom(Commands.sender.getName() + " closes the " + closeableString + " with their " + mv.obj.getName() + ".");
						obj.printRoom("The " + closeableString + " to " + obj.getRoom().getName() + " closes.", connectionTo.getRoom());
						
						isOpen = false;
						connectionTo.getDecorator(RoomConnection.class).isOpen = false;
					}
					else
					{
						obj.printSelf("You are unable to close the " + closeableString);
					}
				}
			}
		});
	}
	
	Movement getMovement()
	{
		Movement mv = null;
		for(Object o : Commands.sender.contents)
		{
			Movement m = o.getDecorator(Movement.class);
			if(m != null && (mv == null || m.getMovePriority() < mv.getMovePriority()))
				mv = m;
		}
		
		return mv;
	}
	
	public void makeCloseable(String str, boolean open)
	{
		closeable = true;
		isOpen = open;
		closeableString = str;
		
		RoomConnection rc = connectionTo.getDecorator(RoomConnection.class);
		rc.closeable = true;
		rc.isOpen = open;
		rc.closeableString = str;
	}
}