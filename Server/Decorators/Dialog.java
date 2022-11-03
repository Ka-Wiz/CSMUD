package Server.Decorators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Server.Command;
import Server.Commands;
import Server.Decorator;
import Server.Object;

// example:
// player: "hello"
// NPC: "Hi, welcome to the [party]! We have [cake]!"
// player: "party"
// NPC: "Yes, it's Harry's Birthday!"
// player: "cake"
// NPC: "It's so delicious and moist!"

public class Dialog extends Decorator
{
	// data for tree in general
	DialogNode root;
	HashMap<String, ArrayList<DialogNode>> keywordToParents = new HashMap<>();
	HashMap<String, DialogNode> keywordToNode = new HashMap<>();
	
	// state data for conversation in progress
	Object talkingTo;
	DialogNode curNode, lastNode;
	HashMap<String, DialogNode> observedKeywords;
	
	public void createNode(String keyword, String msg)
	{
		if(keywordToNode.containsKey(keyword))
		{
			System.out.println("Keyword " + keyword + " already exists in this tree!");
			return;
		}
			
		DialogNode newNode = new DialogNode(keyword, msg);
		keywordToNode.put(keyword, newNode);
		
		Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(msg);
		while(m.find())
		{
			String k = m.group(1).toLowerCase();
			ArrayList<DialogNode> l;
			
			if((l = keywordToParents.get(k)) == null)
				keywordToParents.put(k, l = new ArrayList<DialogNode>());
				
			l.add(newNode);								// add all the [names] this dialog adds so we can build those nodes in future
		}
		
		if(root == null)
		{
			root = newNode;
			curNode = root;
			newNode.keyword = "hello";
		}
		else if(!keywordToParents.containsKey(keyword))	// if none of the previous dialogs had a hyperlink for this name
			System.out.println("DIALOG WARNING: NO LINKED NAME FOR " + keyword + ", HOPE YOU KNOW WHAT YOU'RE DOING!");
		else
		{
			for(var p : keywordToParents.get(keyword))
				p.children.put(keyword, newNode);		// add this new node as child to its appropriate parents
		}  
	}
	public void attachCode(String keyword, DialogCode code)
	{
		DialogNode n = keywordToNode.get(keyword);
		if(n == null)
			System.out.println("Keyword " + keyword + " does not exist in this tree!");
		
		n.code = code;
	}
	
	protected void Initialize()
	{
		commandStrings.put("s", new Command()
		{
			public void invoke()
			{
				if(talkingTo != null && talkingTo.equals(Commands.sender))
				{
					String s = Commands.args[0].toLowerCase();
					DialogNode next = curNode.children.get(s);
					if(next != null)
					{
						say(next.message);
						lastNode = curNode;
						curNode = next;
					}
					else
						System.out.println("next dialog was null");
				}
				else if(talkingTo == null || obj.containedIn.getContainedObjectFromString(talkingTo.getName()) == null)
				{
					talkingTo = Commands.sender;
					curNode = root;
					
					say(root.message);
				}
				else
				{
					say("Sorry, " + Commands.sender + ", I'm speaking with " + talkingTo.getName() + " right now.");
				}
			}
		});
	}
	
	void say(String msg)
	{
		obj.printRoomAll(obj.getName() + " says, \"" + msg.strip() + "\"");
	}
}

class DialogNode
{
	String keyword, message;
	HashMap<String, DialogNode> children = new HashMap<>();
	DialogCode code;
	
	DialogNode(String key, String msg) { keyword = key; message = msg;}
}

interface DialogCode
{
	void enter();
	void exit();
}