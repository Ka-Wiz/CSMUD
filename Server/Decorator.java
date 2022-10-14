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
	protected Decorator() { Initialize(); }
	
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
//			cmd = "\nYou know you can:\n";
			for(String s : commandStrings.keySet())
				cmd += s + ", ";
			
//			cmd = cmd.substring(0, cmd.length() - 2);
			
//			cmd += "\n";
		}
		
		return cmd;
	}
}

class PlayerInfo extends Decorator
{
	
}

class M64Stats extends Decorator
{
	int hp = 6, lives = 3;
	Object respawnRoom;
	
	M64Stats(Object spawn)
	{
		respawnRoom = spawn;
	}
	
	void Damage(int dmg)
	{
		hp -= dmg;
		
		if(hp <= 0)
		{
			
		}
	}
}

class CombatStats extends Decorator
{
	int hp;
}
