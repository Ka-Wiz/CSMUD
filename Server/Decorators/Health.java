package Server.Decorators;

import Server.Command;
import Server.Decorator;
import Server.Server;

public class Health extends Decorator
{
	int hp = 100;
	int regenAmount = 1;
	float regenTime = 30.f;
	Server.ScheduleTask regenTask = new Server.ScheduleTask()
							{
								public void run()
								{
									hp += regenAmount;
									if(hp < 100)
										Server.schedule(this, regenTime);
								}
							};
	
	protected void Initialize()
	{
		commandStrings.put("status", new Command()
		{
			public void invoke()
			{
				obj.printSelf("You have " + hp + " health.");
			}
		});
	}
	
	void Damage(int dam)
	{
		hp -= dam;
		
		if(regenTask.isScheduled())
			regenTask.cancel();
			
		Server.schedule(regenTask, 60.f);
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
