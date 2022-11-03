package Server.Decorators;

import Server.Command;
import Server.Commands;
import Server.Decorator;
import Server.Object;

public class RoomConnection extends Decorator
{
	public Object connectionTo = null; // should be another RoomConnection 99% of the time
	public String moveType; // adverb used when traversing. "You walk out/down/through/across" etc
	
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
				
				obj.printSelf("You " + mv.moveString + " " + moveType + " the " + obj.getName() + " with your " + mv.obj.getName() + ".");
				obj.printRoom(sender.getName() + " " + mv.moveString + "s " + moveType + " the " + obj.getName() + " to " + dest.getName() + ".");
				
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
				obj.printSelf("a " + obj.getName() + " to " + c.getName());
			}
		});
	}
}