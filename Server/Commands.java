package Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import Server.Decorators.Damage;
import Server.Decorators.Holder;
import Server.Decorators.PlayerControlled;


public class Commands
{
	static Map<String, Command> commandStrings = new LinkedHashMap<String, Command>();
	static Map<String, String> commandDescriptions = new LinkedHashMap<String, String>();
	
	public static Object sender, target;
	public static String command;
	public static String[] args;
	public static String fullCommand;
	
	private static Random rand;
	
	private static class CommandState
	{
		Object sender, target;
		String command;
		String[] args;
		String fullCommand;
	}
	
	private static Stack<CommandState> states = new Stack<>();
	private static CommandState current;
	
	static void Initialize()
	{
		rand = new Random();
		
		createCommand("look", "look at your surroundings, or examine a specific target", new Command()
		{
			public void invoke()
			{
				if(Server.checkDebug(Server.DBG_CMD))
					printDebug("inside look");
				
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
				
				Commands.parseCommand(sender, "think");
			}
		});
		
		createCommand("think", "list what you can interact with, or what you can do with a specific target", new Command()
		{
			public void invoke()
			{
				if(Server.checkDebug(Server.DBG_CMD))
					printDebug("inside think");
				
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
				
				sender.printRoomAll(str);
			}
		});
		
		createCommand("inv", "list the objects in your inventory", new Command()
		{
			public void invoke()
			{
				ArrayList<Object> holders = new ArrayList<>();
				String contents = "You have:\n";
				for(Object o : sender.contents)
					if(!o.locked)
						contents += o.getName() + "\n";
					else if(o.getDecorator(Holder.class) != null)
						holders.add(o);
				
				for(Object o : holders)
					if(o.contents.size() > 0)
					{
						contents += "\nHeld in your " + o.getName() + ":\n";
						for(Object h : o.contents)
							contents += h.getName() + "\n";
					}
				
				printSelf(contents);
			}
		});
		
		createCommand("take", "pick up an object", new Command()
		{
			public void invoke()
			{
				if(target != null && target.locked)
				{
					printSelf(target.lockMessage);
					return;
				}
				else if(target.containedIn == sender.containedIn)
				{
					printSelf("You pick up the " + target.getName() + ".");
					target.storeIn(sender);
				}
					
			}
		});
		
		createCommand("hold", "hold an object, if you are capable. for weapons, this means equipping", new Command()
		{
			public void invoke()
			{
				if(target == null)
				{
					printSelf("Could not find object to hold!");
					return;
				}
				else
				{
					if(target.containedIn != sender)
					{
						Commands.parseCommand(sender, "take " + target.getName());
						if(target.containedIn != sender)
							return;
					}
				}
				
				Holder h = sender.findDecoratorInChildren(Holder.class);
				
				if(h == null)
					printSelf("You do not have anything to hold that with!");
				else
				{
					String heldString = "You ";
					if(h.obj.contents.size() > 0)
					{
						Object held = h.obj.contents.get(0);
						heldString += "put away the " + held.getName() + " and ";
						held.storeIn(sender);
					}
						
					heldString += "hold the " + target.getName() + " with your " + h.obj.getName() + ".";
					printSelf(heldString);
					target.storeIn(h.obj);
				}
			}
		});
		
		createCommand("drop", "drop an object", new Command()
		{
			public void invoke()
			{
				if(target != null && target.containedIn == sender)
					if(!target.locked)
						target.storeIn(sender.containedIn);
					else
						printSelf(target.lockMessage);
			}
		});
		
		createCommand("attack", "attack a target with your currently held object", new Command()
		{	// attack command just covers individual attacks, overall combat timing is Combat class
			public void invoke()
			{				
				if(target != null)
				{
					if(target.isDefeated())
					{
						printSelf("You cannot attack a target that is already defeated.");
						return;
					}
					
					PlayerControlled playerSender = sender.getDecorator(PlayerControlled.class);
					PlayerControlled playerTarget = target.getDecorator(PlayerControlled.class);
					
					// COMBAT SESSION LOGIC ===================================================================
					
					Combat combat = sender.combat;
					if(combat != null)	// if the sender is in combat
					{
						if(combat != target.combat)		// if it's a different combat than the target
						{
							if(target.combat == null)	// if the target isn't in combat, add it to sender's
							{
								target.combat = combat;
								combat.addParticipant(sender, fullCommand);
							}
							else	// if target in combat, have sender escape current and join target's
							{
								combat.removeParticipant(sender, true);
								target.combat.addParticipant(sender, fullCommand);
							}
							
						}
						else if(combat.update.isScheduled())	// if both in same combat and between rounds, update sender attack command
						{
							printSelf("You decide to " + fullCommand + " next!");
							combat.changeCommand(sender, fullCommand);
							return;
						}
					}
					else
					{
						if(target.combat == null)	// if we and the target are both not in combat, create it
						{
							combat = sender.combat = new Combat(fullCommand, sender, target);
							target.combat = sender.combat;
							
							printSelf("You have the initiative and strike first!");
						}
						else	// if the target is in combat, join it
						{
							target.combat.addParticipant(sender, fullCommand);
							combat = sender.combat = target.combat;
						}
					}
					
					// DAMAGE CALCULATION ===================================================================
					
					Damage weapon = sender.findDecoratorInChildrenRecursive(Damage.class);
					String weaponName = weapon.weaponName == null ? weapon.obj.getName() : weapon.weaponName;
					int damage = Math.abs(rand.nextInt()) % weapon.damage + 1;
					float ratio = (float)damage / (float)weapon.damage;
					
					String attackStr = "", targetStr = "", roomStr = "";
					
					if(ratio < 0.25)
					{
						attackStr = "You land a glancing blow on " + target.getName() + " with your " + weaponName + " for " + damage + " damage.";
						targetStr = sender.getName() + " lands a glancing blow with their " + weaponName + " for " + damage + " damage.";
						roomStr = sender.getName() + " lands a glancing blow on " + target.getName() + " with their " + weaponName + " for " + damage + " damage.";
					}
					else if(ratio < 0.95)
					{
						attackStr = "You hit " + target.getName() + " with your " + weaponName + " for " + damage + " damage.";
						targetStr = sender.getName() + " hits you with their " + weaponName + " for " + damage + " damage.";
						roomStr = sender.getName() + " hits " + target.getName() + " with their " + weaponName + " for " + damage + " damage.";
					}
					else
					{
						attackStr = "SMASH! You slam " + target.getName() + " with your " + weaponName + " for " + damage + " damage!";
						targetStr = sender.getName() + " SLAMS you with their " + weaponName + " for " + damage + " damage!";
						roomStr = sender.getName() + " SLAMS " + target.getName() + " with their " + weaponName + " for " + damage + " damage!";
					}
					
					if(playerSender != null)
						Server.printToClient(attackStr, playerSender.client, false);
					
					if(playerTarget != null)
						Server.printToClient(targetStr, playerTarget.client, false);
					
					Server.printToRoomExcluding(roomStr, sender.getRoom(), sender, target);
					
					target.changeIntegrityBy(-damage);
					
					if(target.isDefeated())
					{
						// way to decide next target, faction system?
					}
				}
				else
				{
					printSelf("That is not a valid target for attack.");
				}
			}
		});
		
		createCommand("s", "say something out loud, used to communicate with players and NPCs alike. be careful, you never know what might hear...", new Command()
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
	
	public static void parseCommand(Object send, String input) 
	{
		// "parse" all statics for use in commands: sender, target, command, args
		// commands can nest other commands so we need an "activation record" stack
		
		if(current != null) // we are nested
			states.push(current);
		
		current = new CommandState();
		
		sender  = null; // these mostly ensure that if something is screwed up with commands it'll fail somewhere
		target  = null;
		command = null;
		args    = null;
		fullCommand = null;
		
		sender = send;
		fullCommand = input;
		
		boolean debug = Server.checkDebug(Server.DBG_CMD);
		if(debug)
			System.out.println("parsing command " + input + " from " + sender.getName());
		
		if(input.length() == 0)
		{
			printSelf("You do nothing. It is strangely unfulfilling.");
			return;
		}
		
		String[] words = input.split(" ", 2);
		
		if(words[0].equals("r"))
			input = input.replaceFirst("r", sender.getDecorator(PlayerControlled.class).lastCommand);
		
		words = input.split(" ");
		
		// == TARGET SELECTION ================================================================================
		
		int wordsInTarget = 0;
		if(words.length > 1)
		{
			String targName = words[1];
			if(targName.equals("self"))
			{
				target = sender;
				wordsInTarget = 1;
			}
			else
				for(int i = Math.min(words.length - 1, 3); i >= 1; --i)
				{
					targName = "";
					for(int j = 1; j <= i; ++j)
						targName += words[j] + " ";
					targName = targName.strip();
					
					target = getTargetFromContext(sender, targName);
					
					if(target != null)
					{
						System.out.println("target is " + target.getName() + " which is " + i + " words");
						wordsInTarget = i;
						break;
					}
					
//					if(target == null && i < words.length)
//						targName += " " + words[i];
//					else
//					{
//						if(i < words.length)
//							wordsInTarget = i - 1;
//						
//						break;
//					}
				}
		}
		
		if(debug)
			System.out.println("target name is " + wordsInTarget + " words");
		
		// == STATE SETUP ================================================================================
		
		command = words[0];
		args = Arrays.copyOfRange(words, 1 + wordsInTarget, words.length);
		
		current.sender  = sender; // we can save these things to CommandState now because they are determined
		current.target  = target;
		current.command = command;
		current.args    = args;
		current.fullCommand = fullCommand;
		
		// == COMMAND REPETITION =========================================================================
		
		PlayerControlled player = sender.getDecorator(PlayerControlled.class);
		if(player != null && !words[0].equals("r")) // we have to split up the r stuff because r depends on valid target
		{
			String last = target == null ? command : command + " " + target.getName();
			player.lastCommand = last; // eclipse breaks if we don't use last lmao
			
			if(debug)
				System.out.println("last command saved as " + last);
		}
		
		// == COMMAND OWNERSHIP ==========================================================================
		
		Command cmd;
		boolean found = false;
		
		if(target != null) // prioritize specific commands/general command overrides
		{
			cmd = target.getCommand(command);
			if((cmd = target.commandStrings.get(command)) != null)
			{
				if(debug)
					System.out.println(target.getName() + " recognized " + command + " invoking with " + args.length + " args" );
				
				found = true;
			}
			else
			{
				for(var entry : target.getDecorators().entrySet())
					if((cmd = entry.getValue().commandStrings.get(command)) != null)
					{
						if(debug)
							System.out.println(target.getName() + " decorator " + entry.getKey() + " recognized " + command + " invoking with " + args.length + " args" );
						
						found = true;
						break;
					}
			}
			
			if(found)
			{
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
				if(target == null)
					printSelf("You don't know how to " + command + ".");
				else
					printSelf("You can't think of a way to " + command + " a " + target.getName() + ".");
			}
		}
		
		// == STATE CLEANUP ==========================================================================
		
		if(!states.empty())
		{
			current = states.pop();
			
			sender  = current.sender;
			target  = current.target;
			command = current.command;
			args    = current.args;
			fullCommand = current.fullCommand;
			
			if(states.empty())
				current = null;
		}
	}
	
	private static Object getTargetFromContext(Object o, String s)
	{
		// helper function in case context scope changes later, ie "zones" where objects not in same room are visible to each other
		Object found = o.containedIn.getContained(s);
		if(found == null)
			found = sender.getContained(s, true);
		return found;
	}

	static void printSelf(String str)
	{
		if(str.length() > 0)
			Server.printToCurrentClient(str.strip() + "\n\n");
	}
	static void printDebug(String str)
	{
		if(str.length() > 0)
			System.out.println("DEBUG: " + str);
	}
}