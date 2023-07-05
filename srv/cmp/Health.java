package srv.cmp;

import srv.Command;
import srv.Component;
import srv.Server;

public class Health extends Component
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
				ent.printSelf("You have " + hp + " health.");
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
		
		return desc;
	}
}
