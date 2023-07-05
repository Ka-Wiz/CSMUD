package srv.cmp;

import srv.Command;
import srv.Commands;
import srv.Component;
import srv.Entity;

public class RoomConnection extends Component
{
	public Entity connectionTo = null; // should be another RoomConnection 99% of the time
	public String moveAdverb; // "You walk: out/down/through/across" etc
	
	boolean closeable = false;
	boolean isOpen = true;
	String closeableString = "door"; // "The door/portal/window/hatch is open" etc
//	public Class<? extends Component> requiredToOpen = Movement.class;
	
	public boolean getCloseable() { return closeable; }
	public boolean getIsOpen() { return isOpen; }
	
	protected void Initialize()
	{
		commandStrings.put("use", new Command()
		{
			public void invoke()
			{
				Entity dest = connectionTo.getComponent(RoomConnection.class) == null ? connectionTo : connectionTo.containedIn;
				Entity sender = Commands.sender;
				Movement mv = null;
				for(Entity o : sender.contents)
				{
					Movement m = o.getComponent(Movement.class);
					if(m != null && (mv == null || m.getMovePriority() < mv.getMovePriority()))
						mv = m;
				}
				
				if(mv == null)
				{
					ent.printSelf("You have nothing to move with!");
					return;
				}
				
				if(closeable)
				{
					if(!isOpen)
						Commands.parseCommand(sender, "open " + ent.getName());
					
					if(!isOpen)
					{
						ent.printSelf("Could not open, movement failed.");
						return;
					}
				}
				
				ent.printSelf("You " + mv.moveString + " " + moveAdverb + " the " + ent.getName() + " with your " + mv.ent.getName() + ".");
				ent.printRoom(sender.getName() + " " + mv.moveString + "s " + moveAdverb + " the " + ent.getName() + " to " + dest.getName() + ".");
				
				String arrive = sender.getName() + " arrives from ";
				if(connectionTo.getComponent(RoomConnection.class) != null)
				{
					arrive += connectionTo.getName() + " to " + ent.containedIn.getName() + ".";
					sender.storeIn(connectionTo.containedIn);
					ent.printRoom(arrive, connectionTo.containedIn);
				}
				else
				{
					arrive += ent.containedIn.getName() + ".";
					sender.storeIn(connectionTo);
					ent.printRoom(arrive, connectionTo);
				}
				
				Commands.parseCommand(sender, "look");
				Commands.parseCommand(sender, "think");
			}
		});
		
		commandStrings.put("look", new Command()
		{
			public void invoke()
			{
				Entity c = connectionTo.getComponent(RoomConnection.class) == null ? connectionTo : connectionTo.containedIn;
				String msg = "A " + ent.getName() + " to " + c.getName() + ".";
				
				if(closeable)
					msg += " The " + closeableString + " is " + (isOpen ? "open" : "closed") + ".";
				
				ent.printSelf(msg);
			}
		});
		
		commandStrings.put("open", new Command()
		{
			public void invoke()
			{
				if(!closeable || isOpen)
				{
					ent.printSelf("The " + ent.getName() + " is not closed.");
				}
				else
				{
					Prehensile prh = Commands.sender.findComponentInChildrenWithPriority(Prehensile.class, 3);
					if(prh != null && prh.priority <= 3)
					{
						ent.printSelf("You open the " + closeableString + " with your " + prh.ent.getName() + ".");
						ent.printRoom(Commands.sender.getName() + " opens the " + closeableString + " with their " + prh.ent.getName() + ".");
						ent.printRoom("The " + closeableString + " to " + ent.getRoom().getName() + " opens.", connectionTo.getRoom());
						
						isOpen = true;
						connectionTo.getComponent(RoomConnection.class).isOpen = true;
					}
					else
					{
						ent.printSelf("You don't have any anatomy to open the " + closeableString + " with!");
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
					ent.printSelf("The " + ent.getName() + " cannot be closed.");
				}
				else
				{
					Prehensile prh = Commands.sender.findComponentInChildrenWithPriority(Prehensile.class, 3);
					if(prh != null && prh.priority <= 3)
					{
						ent.printSelf("You close the " + closeableString + " with your " + prh.ent.getName() + ".");
						ent.printRoom(Commands.sender.getName() + " closes the " + closeableString + " with their " + prh.ent.getName() + ".");
						ent.printRoom("The " + closeableString + " to " + ent.getRoom().getName() + " closes.", connectionTo.getRoom());
						
						isOpen = false;
						connectionTo.getComponent(RoomConnection.class).isOpen = false;
					}
					else
					{
						ent.printSelf("You don't have any anatomy to close the " + closeableString + " with!");
					}
				}
			}
		});
	}
	
	Movement getMovement()
	{
		Movement mv = null;
		for(Entity o : Commands.sender.contents)
		{
			Movement m = o.getComponent(Movement.class);
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
		
		RoomConnection rc = connectionTo.getComponent(RoomConnection.class);
		rc.closeable = true;
		rc.isOpen = open;
		rc.closeableString = str;
	}
}