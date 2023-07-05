package srv.cmp;

import java.time.LocalTime;

import srv.Command;
import srv.Commands;
import srv.Component;
import srv.Entity;
import srv.Server;

public class Writeable extends Component
{
	String writing = "";
	public String writingTool = "pen";
	Entity openTo;
	
	protected void Initialize()
	{
		commandStrings.put("write", new Command()
		{
			public void invoke()
			{
				Prehensile prh = Commands.sender.findComponentInChildrenWithPriority(Prehensile.class, 1);
				if(prh == null || prh.priority > 1)
				{
					ent.printSelf("You don't have any anatomy to write with!");
					return;
				}
						
				if(Commands.args.length == 0)
					ent.printSelf("You didn't specify what to write!");
				else
				{
					String str = "";
					for(String s : Commands.args)
						str += s + " ";
					str = str.strip();
					
					writing = str;
					ent.printSelf("You grab a " + writingTool + " with your " + prh.ent.getName() + " and scribble with a poetic flourish, \'" + str + "\'");
					ent.printRoom(Commands.sender.getName() + " scribbles something on the " + ent.getName());
					
					if(ent.getName().equals("death notebook")) // just some fun temporary code for testing
					{
						Server.schedule(new Server.ScheduleTask() {
									public void run()
									{
										Server.broadcast("JoeNPC shits himself horrendously.");
										Server.broadcast("JoeNPC falls over and dies.");
									}
								}, LocalTime.parse(Commands.args[Commands.args.length - 1]));
					}
					
					boolean opened = false; // can't just set openTo to null, original state will be lost, needed for close message
					for(Entity c : ent.contents)
					{
						System.out.println("checking item " + c.getName());
						if(c.getName().equals(str))
						{
							openTo = c;
							opened = true;
							
							ent.printRoomAll("As the writing is finished, the surface underneath starts to shimmer and ripple "
									+ "in a whirling kaleidoscope of color. It soon settles into " + openTo.getComponent(StringProperty.class).str
									+ ", the surface of the board continuing to wave as if liquid.");
						}
					}
					
					if(openTo != null && !opened) // if the portal is currently open and we haven't changed it to a valid destination, close it
					{
						ent.printRoomAll("As the writing is finished, " + openTo.getComponent(StringProperty.class).str
								+ " is quickly washed away by a swirl of white as the board surface solidifies and is normal once again.");
						
						openTo = null;
					}
				}
			}
		});
		
		commandStrings.put("erase", new Command()
		{
			public void invoke()
			{
				writing = "";
				if(openTo == null)
				{
					ent.printSelf("Grabbing an eraser, you wipe the board clean in a single marvelous sweep.");
				}
				else
				{
					ent.printSelf("As you grab an eraser and erase the text at the top of the board, " + openTo.getComponent(StringProperty.class).str
							+ " is quickly consumed by splotches of white erupting from beneath it, hardening the board once again. It has returned to normal.");
					openTo = null;
				}
			}
		});
		
		commandStrings.put("jump", new Command()
		{
			public void invoke()
			{
				if(openTo == null)
					ent.printSelf("Why would you jump at a whiteboard? Haha that's pretty goofy. This command must be a mistake.");
				else
				{
					ent.printSelf("You take a running leap into the seemingly liquid whiteboard. You must be insane. Surprisingly, you "
							+ "feel yourself pass through the surface and start to... tumble through the air? Your feet connect with "
							+ "something solid and...");
					
					Commands.sender.storeIn(ent.contents.get(0));
					Commands.parseCommand(Commands.sender, "look");
				}
			}
		});
	}
	
	protected String buildDescription()
	{
		String desc = "";
		
		if(writing == "")
			desc += "\nIt is currently blank.\n";
		else
			desc += "\nIt currently reads: \"" + writing + "\"\n";
		
		if(openTo != null)
		{
			StringProperty sp = openTo.getComponent(StringProperty.class);
			desc += "Underneath is a rippling surface depicting " + sp.str + ".";
		}
		
		return desc;
	}
}
