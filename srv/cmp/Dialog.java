package srv.cmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import srv.Command;
import srv.Commands;
import srv.Component;
import srv.Entity;
import srv.Server;

// example:
// player: "hello"
// NPC: "Hi, welcome to the [party]! We have [cake]!"
// player: "party"
// NPC: "Yes, it's Harry's Birthday!"
// player: "cake"
// NPC: "It's so delicious and moist!"

public class Dialog extends Component
{
	public static class DialogNode
	{
		public String keyword, message;
		HashMap<String, DialogNode> children = new HashMap<>();
		DialogCode code;
		
		DialogNode(String key, String msg, DialogCode cod) { keyword = key; message = msg; code = cod;}
		DialogNode(String key, String msg) { keyword = key; message = msg; }
	}
	
	public class DialogCode
	{
		public DialogNode thisNode;
		public void enter() {}
		public void exit() {}
		public void afterSpeaking() {}
	}

	// data for tree in general
	DialogNode root;
	HashMap<String, ArrayList<DialogNode>> keywordToParents = new HashMap<>();
	HashMap<String, DialogNode> keywordToNode = new HashMap<>();
	
	// state data for conversation in progress
	public Entity talkingTo;
	DialogNode curNode, lastNode;
	HashMap<String, DialogNode> observedKeywords;
	
	public void createNode(String keyword, String msg, DialogCode code)
	{
		if(keywordToNode.containsKey(keyword))
		{
			System.out.println("Keyword " + keyword + " already exists in this tree!");
			return;
		}
			
		DialogNode newNode = new DialogNode(keyword, msg, code);
		keywordToNode.put(keyword, newNode);
		
		Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(msg);
		while(m.find())
		{
			String k = m.group(1).toLowerCase();
			ArrayList<DialogNode> l;
			
			if(Server.checkDebug(Server.DBG.DLG))
				Server.printDebug("adding keyword [" + k + "] to parent " + keyword);
			
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
		
		if(code != null)
			code.thisNode = newNode;
	}
	public void createNode(String keyword, String msg)
	{
		createNode(keyword, msg, null);
	}
	public void attachCode(String keyword, DialogCode code)
	{
		DialogNode n = keywordToNode.get(keyword);
		if(n == null || code == null)
		{
			System.out.println("Keyword " + keyword + " does not exist in this tree, or code was null!");
			return;
		}
		
		n.code = code;
		code.thisNode = n;
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
						if(curNode.code != null)
							curNode.code.exit();
						
						if(next.code != null)
							next.code.enter();
						
						say(next.message);
						if(next.code != null)
							next.code.afterSpeaking();
						
						lastNode = curNode;
						curNode = next;
					}
					else
						noResponseBehavior();
				}
				else if(talkingTo == null || ent.containedIn.getContained(talkingTo.getName()) == null)
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
	
	public void noResponseBehavior()
	{
		Commands.parseCommand(ent, "emote looks at you with confusion.");
	}
	
	void say(String msg)
	{
		ent.printRoomAll(ent.getName() + " says, \"" + msg.strip() + "\"");
	}
}

