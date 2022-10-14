package Server.Decorators;

import Server.ClientProcess;
import Server.Decorator;

public class PlayerControlled extends Decorator
{
	public ClientProcess client;
	public String lastCommand = "";
	
	protected void OnStore()
	{
		
	}
}
