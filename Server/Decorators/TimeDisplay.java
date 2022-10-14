package Server.Decorators;

import Server.Decorator;
import Server.Server;

public class TimeDisplay extends Decorator
{
	protected String buildDescription()
	{
		return "\nIt says the time is currently " + Server.worldTime.toString() + ".";
	}
}
