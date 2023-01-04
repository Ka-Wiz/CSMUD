package Server.Decorators;

import java.time.LocalTime;

import Server.Command;
import Server.Commands;
import Server.Decorator;
import Server.Object;
import Server.Server;

public class Goblin extends Decorator
{
	Server.ScheduleTask playerCheck;
	Server.ScheduleTask playerCombat;
	
	Object target;
	
	protected void Initialize()
	{
		playerCheck = Server.schedule(new Server.ScheduleTask() {
			public void run()
			{
				PlayerControlled pc = obj.getRoom().findDecoratorInChildren(PlayerControlled.class);
				
				if(pc == null)
					Server.schedule(this, 7.0f);
				else
				{
					target = pc.obj;
					
					Server.printToClient(obj.getName() + " notices you and lets out a shriek!", pc.client);
					obj.printRoom(obj.getName() + " notices " + target.getName() + " and lets out a shriek!");
					
					playerCombat = Server.schedule(new Server.ScheduleTask() {
						public void run()
						{
							
						}
					}, 2.0f);
				}
			}
		}, 5.0f);
		
//		commandStrings.put("write", new Command()
//		{
//			public void invoke()
//			{
//				if(Commands.args.length == 0)
//					obj.printSelf("You didn't specify what to write!");
//				else
//				{
//					String str = "";
//					for(String s : Commands.args)
//						str += s + " ";
//					str = str.strip();
//					
//					writing = str;
//					obj.printSelf("You grab a " + writingTool + " and scribble with a poetic flourish, \'" + str + "\'");
//					obj.printRoom(Commands.sender.getName() + " scribbles something on the " + obj.getName());
//					
//					if(obj.getName().equals("death notebook")) // just some fun temporary code for testing
//					{
//						Server.schedule(new Server.ScheduleTask() {
//									public void run()
//									{
//										Server.broadcast("JoeNPC shits himself horrendously.");
//										Server.broadcast("JoeNPC falls over and dies.");
//									}
//								}, LocalTime.parse(Commands.args[Commands.args.length - 1]));
//					}
//					
//					boolean opened = false; // can't just set openTo to null, original state will be lost, needed for close message
//					for(Object c : obj.contents)
//					{
//						System.out.println("checking item " + c.getName());
//						if(c.getName().equals(str))
//						{
//							openTo = c;
//							opened = true;
//							
//							obj.printRoomAll("As the writing is finished, the surface underneath starts to shimmer and ripple "
//									+ "in a whirling kaleidoscope of color. It soon settles into " + openTo.getDecorator(StringProperty.class).str
//									+ ", the surface of the board continuing to wave as if liquid.");
//						}
//					}
//					
//					if(openTo != null && !opened) // if the portal is currently open and we haven't changed it to a valid destination, close it
//					{
//						obj.printRoomAll("As the writing is finished, " + openTo.getDecorator(StringProperty.class).str
//								+ " is quickly washed away by a swirl of white as the board surface solidifies and is normal once again.");
//						
//						openTo = null;
//					}
//				}
//			}
//		});
//		
//		commandStrings.put("erase", new Command()
//		{
//			public void invoke()
//			{
//				writing = "";
//				if(openTo == null)
//				{
//					obj.printSelf("Grabbing an eraser, you wipe the board clean in a single marvelous sweep.");
//				}
//				else
//				{
//					obj.printSelf("As you grab an eraser and erase the text at the top of the board, " + openTo.getDecorator(StringProperty.class).str
//							+ " is quickly consumed by splotches of white erupting from beneath it, hardening the board once again. It has returned to normal.");
//					openTo = null;
//				}
//			}
//		});
//		
//		commandStrings.put("jump", new Command()
//		{
//			public void invoke()
//			{
//				if(openTo == null)
//					obj.printSelf("Why would you jump at a whiteboard? Haha that's pretty goofy. This command must be a mistake.");
//				else
//				{
//					obj.printSelf("You take a running leap into the seemingly liquid whiteboard. You must be insane. Surprisingly, you "
//							+ "feel yourself pass through the surface and start to... tumble through the air? Your feet connect with "
//							+ "something solid and...");
//					
//					Commands.sender.storeIn(obj.contents.get(0));
//					Commands.parseCommand(Commands.sender, "look");
//				}
//			}
//		});
	}
	
	protected String buildDescription()
	{
		String desc = "";
	
		return desc;
	}
}
