package Server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

enum DecType
{
	MOVEMENT,
	MAX
}

abstract public class Decorator
{
	protected Decorator() {Initialize(); }
	
	protected void Initialize() {}
	public Object obj;
	
	protected HashSet<DecType> types;
	protected enum DecType
	{
		MOVEMENT,
		MAX
	}
	
	protected void OnStore() {}
	protected String buildDescription() { return ""; }
	
	protected Map<String, Command> commandStrings = new HashMap<String, Command>();
	String GetCommands()
	{
		String cmd = "";
		
		if(commandStrings.keySet().size() > 0)
		{
			for(String s : commandStrings.keySet())
				cmd += s + ", ";
		}
		
		return cmd;
	}
}
