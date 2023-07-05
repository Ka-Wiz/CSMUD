package srv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import srv.cmp.Damage;
import srv.cmp.PlayerControlled;
import srv.cmp.Prehensile;


public class Commands
{
	static Map<String, Command> commandStrings = new LinkedHashMap<String, Command>();
	static Map<String, String> commandDescriptions = new LinkedHashMap<String, String>();
	
	public static Entity sender, target;
	public static String command;
	public static String[] args;
	public static String fullCommand;
	
	private static Random rand;
	
	private static class CommandState
	{
		Entity sender, target;
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
				if(Server.checkDebug(Server.DBG.CMD))
					Server.printDebug("LOOK", "inside look");
				
				if(Server.checkDebug(Server.DBG.CMD))
					Server.printDebug("LOOK", "got target " + target);
				
				String str = "";
				if(target == null)
				{
					if(Server.checkDebug(Server.DBG.CMD))
						Server.printDebug("LOOK", "target was null");
					
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
					if(Server.checkDebug(Server.DBG.CMD))
						Server.printDebug("LOOK", "target wasnt null");
				
				printSelf(str + target.buildDescription());
			}
		});
		
		createCommand("think", "list what you can interact with, or what you can do with a specific target", new Command()
		{
			public void invoke()
			{
				if(Server.checkDebug(Server.DBG.CMD))
					Server.printDebug("THINK", "inside think");
				
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
				
				sender.printRoomAll(sender.getName() +  " " + str);
			}
		});
		
		createCommand("inv", "list the objects in your inventory", new Command()
		{
			public void invoke()
			{
				ArrayList<Entity> holders = new ArrayList<>();
				String contents = "You have:\n";
				for(Entity o : sender.contents)
					if(!o.locked)
						contents += o.getName() + "\n";
					else if(o.getComponent(Prehensile.class) != null)
						holders.add(o);
				
				for(Entity o : holders)
					if(o.contents.size() > 0)
					{
						contents += "\nHeld in your " + o.getName() + ":\n";
						for(Entity h : o.contents)
							contents += h.getName() + "\n";
					}
				
				printSelf(contents);
			}
		});
		
		createCommand("take", "pick up an object", new Command()
		{
			public void invoke()
			{
				Prehensile prh = sender.findComponentInChildrenWithPriority(Prehensile.class, 1);
				
				if(prh == null || prh.priority > 1)
				{
					printSelf("You do not have any anatomy to take that with!");
					return;
				}
				
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
					printSelf("You do not see a " + args[0] + " to hold!");
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
				
				Prehensile prh = sender.findComponentInChildrenWithPriority(Prehensile.class, 1);
				
				if(prh == null || prh.priority > 1)
					printSelf("You do not have any anatomy to hold that with!");
				else
				{
					String heldString = "You ";
					if(prh.ent.contents.size() > 0)
					{
						Entity held = prh.ent.contents.get(0);
						heldString += "put away the " + held.getName() + " and ";
						held.storeIn(sender);
					}
						
					heldString += "hold the " + target.getName() + " with your " + prh.ent.getName() + ".";
					printSelf(heldString);
					target.storeIn(prh.ent);
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
					
					PlayerControlled playerSender = sender.getComponent(PlayerControlled.class);
					PlayerControlled playerTarget = target.getComponent(PlayerControlled.class);
					
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
					
					Damage weapon = sender.findComponentInChildrenRecursive(Damage.class);
					String weaponName = weapon.weaponName == null ? weapon.ent.getName() : weapon.weaponName;
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
						attackStr = "You " + weapon.attackVerb + " " + target.getName() + " with your " + weaponName + " for " + damage + " damage.";
						targetStr = sender.getName() + " " + weapon.attackVerb + "s you with their " + weaponName + " for " + damage + " damage.";
						roomStr = sender.getName() + " " + weapon.attackVerb + "s " + target.getName() + " with their " + weaponName + " for " + damage + " damage.";
					}
					else
					{
						attackStr = "SMASH! You " + weapon.criticalVerb + " " + target.getName() + " with your " + weaponName + " for " + damage + " damage!";
						targetStr = sender.getName() + " " + weapon.criticalVerb + "S you with their " + weaponName + " for " + damage + " damage!";
						roomStr = sender.getName() + " " + weapon.criticalVerb + "S " + target.getName() + " with their " + weaponName + " for " + damage + " damage!";
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
				Commands.parseCommand(sender, sender.getComponent(PlayerControlled.class).lastCommand);
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
	
	public static void parseCommand(Entity send, String input) 
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
		
		if(Server.checkDebug(Server.DBG.CMD))
			Server.printDebug("PARSE", "parsing command " + input + " from " + sender.getName());
		
		if(input.length() == 0)
		{
			printSelf("You do nothing. It is strangely unfulfilling.");
			return;
		}
		
		String[] words = input.split(" ", 2);
		
		if(words[0].equals("r"))
			input = input.replaceFirst("r", sender.getComponent(PlayerControlled.class).lastCommand);
		
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
						if(Server.checkDebug(Server.DBG.CMD))
							Server.printDebug("TARGET", "target is " + target.getName() + " which is " + i + " words");
						
						wordsInTarget = i;
						break;
					}
				}
		}
		
		// == STATE SETUP ================================================================================
		
		command = words[0];
		args = Arrays.copyOfRange(words, 1 + wordsInTarget, words.length);
		
		current.sender  = sender; // we can save these things to CommandState now because they are determined
		current.target  = target;
		current.command = command;
		current.args    = args;
		current.fullCommand = fullCommand;
		
		// == COMMAND REPETITION =========================================================================
		
		PlayerControlled player = sender.getComponent(PlayerControlled.class);
		if(player != null && !words[0].equals("r")) // we have to split up the r stuff because r depends on valid target
		{
			String last = target == null ? command : command + " " + target.getName();
			player.lastCommand = last; // eclipse breaks if we don't use last lmao
			
			if(Server.checkDebug(Server.DBG.CMD))
				Server.printDebug("PARSE", "last command saved as " + last);
		}
		
		// == COMMAND OWNERSHIP ==========================================================================
		
		Command cmd;
		boolean found = false;
		
		if(target != null) // prioritize specific commands/general command overrides
		{
			cmd = target.getCommand(command);
			if((cmd = target.commandStrings.get(command)) != null)
			{
				if(Server.checkDebug(Server.DBG.CMD))
					Server.printDebug("PARSE", target.getName() + " recognized " + command + " invoking with " + args.length + " args" );
				
				found = true;
			}
			else
			{
				for(var entry : target.getComponents().entrySet())
					if((cmd = entry.getValue().commandStrings.get(command)) != null)
					{
						if(Server.checkDebug(Server.DBG.CMD))
							Server.printDebug("PARSE", target.getName() + " decorator " + entry.getKey() + " recognized " + command + " invoking with " + args.length + " args" );
						
						found = true;
						break;
					}
			}
			
			if(found)
			{
				cmd.invoke();
				
				if(Server.checkDebug(Server.DBG.CMD))
					Server.printDebug("PARSE", "specific cmd " + command + " complete");
			}
		}
		
		if(!found)
		{
			cmd = commandStrings.get(command);
			
			if(cmd != null)
			{
				if(Server.checkDebug(Server.DBG.CMD))
					Server.printDebug("PARSE", "generic cmd " + command + ", invoking with " + args.length + " args" );
				
				cmd.invoke();
				
				if(Server.checkDebug(Server.DBG.CMD))
					Server.printDebug("PARSE", "cmd " + command + " complete");
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
	
	private static Entity getTargetFromContext(Entity o, String s)
	{
		// helper function in case context scope changes later, ie "zones" where objects not in same room are visible to each other
		Entity found = o.containedIn.getContained(s);
		if(found == null)
			found = sender.getContained(s, true);
		return found;
	}

	static void printSelf(String str)
	{
		if(str.length() > 0)
			Server.printToCurrentClient(str.strip() + "\n\n");
	}
}