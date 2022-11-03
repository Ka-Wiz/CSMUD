package Server;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import Server.Decorators.*;


public class Commands
{
	static Map<String, Command> commandStrings = new LinkedHashMap<String, Command>();
	static Map<String, String> commandDescriptions = new LinkedHashMap<String, String>();
	
	public static Object sender;
	public static String command;
	public static String[] args;
	
	static void Initialize()
	{
		createCommand("look", "look at your surroundings, or examine a specific target", new Command()
		{
			public void invoke()
			{
				if(Server.checkDebug(Server.DBG_CMD))
					printDebug("inside look");

				Object target = getTarget();
				
				if(Server.checkDebug(Server.DBG_CMD))
					printDebug("got target " + target);
				
				String str = "";
				if(target == null)
				{
					if(Server.checkDebug(Server.DBG_CMD))
						printDebug("target was null");
					
					if(args.length == 0)
					{
						target = sender.containedIn;
						str += "You are " + target.containmentPreposition + " ";
					}
					else
					{
						printSelf("You don't see a " + args[0] + ".");
						return;
					}
				}
				else
					if(Server.checkDebug(Server.DBG_CMD))
						printDebug("target wasnt null");
				
				printSelf(str + target.buildDescription());
			}
		});
		
		createCommand("think", "list what you can interact with, or what you can do with a specific target", new Command()
		{
			public void invoke()
			{
				if(Server.checkDebug(Server.DBG_CMD))
					printDebug("inside think");
				
				Object target = getTarget();
				
				if(target == null)
					printSelf(sender.containedIn.getInteractables());
				else
					printSelf(target.getCommands());
			}
		});
		
		createCommand("emote", "narrate your character performing an action. immersive, but no gameplay effect", new Command()
		{
			public void invoke()
			{
				String str = "";
				for(String s : Commands.args)
					str += s + " ";
				str = str.strip();
				
				printSelf("You " + str + ".");
			}
		});
		
		createCommand("inv", "list the objects in your inventory", new Command()
		{
			public void invoke()
			{
				String contents = "You have:\n";
				for(Object o : sender.contents)
					if(!o.locked)
						contents += o.getName() + "\n";
				
				printSelf(contents);
			}
		});
		
		createCommand("take", "pick up an object", new Command()
		{
			public void invoke()
			{
				Object target = getTarget();
				
				if(target.locked)
				{
					printSelf(target.lockMessage);
					return;
				}
				
				if(target.containedIn == sender.containedIn)
					target.storeIn(sender);
			}
		});
		
		createCommand("drop", "drop an object", new Command()
		{
			public void invoke()
			{
				Object target = getTarget();
				if(target.containedIn == sender)
					if(!target.locked)
						target.storeIn(sender.containedIn);
					else
						printSelf(target.lockMessage);
			}
		});
		
		createCommand("s", "say something out loud, used to communicate with players and NPCs alike. be careful, you never know what might hear..", new Command()
		{
			public void invoke()
			{
				String str = "";
				for(String s : args)
					str += s + " ";
				
				Server.printToRoom(sender.getName() + " says, \"" + str.strip() + "\"", sender.containedIn);
			}
		});
		
		createCommand("r", "repeat your last command", new Command()
		{
			public void invoke()
			{
				Commands.parseCommand(sender, sender.getDecorator(PlayerControlled.class).lastCommand);
			}
		});
		
		createCommand("help", "i think you get the idea by now", new Command()
		{
			public void invoke()
			{
				String cmds = "Universal commands are:";
				for(String c : commandStrings.keySet())
					cmds += "\n" + c + " - " + commandDescriptions.get(c) + ".";
				
				printSelf("Welcome to the world of CSMUD, traveller! Here you will find many things familiar, and many more new and exciting. "
						+ "Interact with the world by typing commands, like \"look\" to see the room you are in, and \"think\" to list interactions.\n\nCommands are always in "
						+ "the form \"<command> <target> <parameters>\", so \"look self\" will examine yourself, \"think whiteboard\" will show what you can do with a whiteboard, and "
						+ "\"write whiteboard hello world!\" will write something on it. Always make sure to specify your target like that!\n\nAnyway, that's the tutorial. Have fun "
						+ "exploring and experimenting! The world is at your fingertips.\n\n" + cmds);
			}
		});
	}
	private static void createCommand(String text, String desc, Command cmd)
	{
		commandStrings.put(text, cmd);
		commandDescriptions.put(text, desc);
	}
	
	public static void parseCommand(Object sender, String input)
	{
		boolean debug = Server.checkDebug(Server.DBG_CMD);
		
		if(input.length() == 0)
		{
			printSelf("You do nothing. It is strangely unfulfilling.");
			return;
		}
		
		if(debug)
			System.out.println("parsing command " + input);
		
		String[] words = input.split(" ", 2);
		
		if(words[0].equals("r"))
			input = input.replaceFirst("r", sender.getDecorator(PlayerControlled.class).lastCommand);
		
		words = input.split(" ");
		Commands.command = words[0];
		
		Commands.sender = sender;
		Commands.args = Arrays.copyOfRange(words, 1, words.length);
		
		Object targ = getTarget();
		
		if(!words[0].equals("r")) // we have to split up the r stuff because r depends on valid target
		{
			String last = targ != null ? String.join(" ", command, words[1]) : command;
			sender.getDecorator(PlayerControlled.class).lastCommand = last; // eclipse breaks if we don't use last lmao
		}
		
		Command cmd;
		boolean found = false;
		
		if(targ != null) // prioritize specific commands/general command overrides
		{
			if((cmd = targ.commandStrings.get(command)) != null)
			{
				if(debug)
					System.out.println(targ.getName() + " recognized " + command + " invoking with " + (args.length-1) + " args" );
				
				found = true;
			}
			else
			{
				for(var entry : targ.getDecorators().entrySet())
					if((cmd = entry.getValue().commandStrings.get(command)) != null)
					{
						if(debug)
							System.out.println(targ.getName() + " decorator " + entry.getKey() + " recognized " + command + " invoking with " + (args.length-1) + " args" );
						
						found = true;
						break;
					}
			}
			
			if(found)
			{
				Commands.args = Arrays.copyOfRange(words, 2, words.length);
				cmd.invoke();
				
				if(debug)
					System.out.println("specific cmd " + command + " complete");
			}
		}
		
		if(!found)
		{
			cmd = commandStrings.get(command);
			
			if(cmd != null)
			{
				if(debug)
					System.out.println("generic cmd " + command + ", invoking with " + args.length + " args" );
				
				cmd.invoke();
				
				if(debug)
					System.out.println("cmd " + command + " complete");
			}
			else
			{
				if(targ == null)
					printSelf("You don't know how to " + command + ".");
				else
					printSelf("You can't think of a way to " + command + " a " + targ.getName() + ".");
			}
		}
		
		Commands.sender = null;
		Commands.command = null;
		Commands.args = null;
		
//		Command cmd = commandStrings.get(words[0]);
//		if(cmd != null)
//		{
//			if(targ != null)
//			{
//				if(debug)
//					System.out.println("specific cmd " + words[0] + " on " + words[1] + ", invoking with " + args.length + " args" );
//				
//				Command scmd = targ.getCommand(words[0]);
//				if(scmd != null)
//				{
//					scmd.invoke();
//					return;
//				}
//			}
//			
//			if(debug)
//				System.out.println("generic cmd " + words[0] + ", invoking with " + args.length + " args" );
//			
//			cmd.invoke();
//			
//			if(debug)
//				System.out.println("cmd " + words[0] + " complete");
//		}
//		else
//		{
//			if(debug)
//				System.out.println("checking for specific");
//			
//			if(targ != null)
//			{
//				cmd = targ.commandStrings.get(words[0]);
//				
//				if(cmd != null)
//				{
//					if(debug)
//						System.out.println(targ.getName() + " recognized " + words[0] + " invoking with " + (args.length-1) + " args" );
//					
//					Commands.args = Arrays.copyOfRange(words, 2, words.length);
//					cmd.invoke();
//				}
//				else
//				{
//					boolean invoked = false;
//					for(var entry : targ.getDecorators().entrySet())
//					{
//						cmd = entry.getValue().commandStrings.get(words[0]);
//						if(cmd != null)
//						{
//							if(debug)
//								System.out.println(targ.getName() + " decorator " + entry.getKey() + " recognized " + words[0] + " invoking with " + (args.length-1) + " args" );
//							
//							Commands.args = Arrays.copyOfRange(words, 2, words.length);
//							cmd.invoke();
//							invoked = true;
//							break;
//						}
//					}
//					
//					if(!invoked)
//						printSelf("You can't think of a way to " + words[0] + " a " + targ.getName() + ".");
//				}
//			}
//			else
//				printSelf("You don't know how to " + words[0] + ".");
//		}
	}
	
	private static Object getTarget()
	{
		if(args.length > 0)
		{
			if(args[0].equals("self"))
				return sender;
			else
			{
				Object res = null;
				String name = args[0];
				for(int i = 1; i < 4; ++i)
				{
					res = getTargetFromContext(sender, name);
					
					if(res == null && i < args.length)
						name += " " + args[i];
					else
						return res;
				}
			}
		}

		return null;
	}
	private static Object getTargetFromContext(Object o, String s)
	{
		// helper function in case context scope changes later, ie "zones" where objects not in same room are visible to each other
		System.out.println("checking for object " + s);
		Object found = o.containedIn.getContainedObjectFromString(s);
		if(found == null)
		{
			found = sender.getContainedObjectFromString(s);
		}
		return found;
	}

	static void printSelf(String str)
	{
		if(str.length() > 0)
			Server.printToClient(str.strip() + "\n\n");
	}
	static void printDebug(String str)
	{
		if(str.length() > 0)
			System.out.println("DEBUG: " + str);
	}
}