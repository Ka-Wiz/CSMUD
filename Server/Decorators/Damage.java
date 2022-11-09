package Server.Decorators;

import Server.Decorator;
import Server.Server;

public class Damage extends Decorator
{
	public int damage = 1;
	public float cooldown = 5.f;
	Server.ScheduleTask attackTask = new Server.ScheduleTask()
							{
								public void run()
								{
									Server.schedule(this, cooldown);
								}
							};
	
	protected void Initialize()
	{
	}
	
	protected String buildDescription()
	{
		String desc = "";
		
//		if(writing == "")
//			desc += "\nIt is currently blank.\n";
//		else
//			desc += "\nIt currently reads: \"" + writing + "\"\n";
//		
//		if(openTo != null)
//		{
//			StringProperty sp = openTo.getDecorator(StringProperty.class);
//			desc += "Underneath is a rippling surface depicting " + sp.str + ".";
//		}
		
		return desc;
	}
}
