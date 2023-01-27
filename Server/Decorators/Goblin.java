package Server.Decorators;

import Server.Commands;
import Server.Decorator;
import Server.Object;
import Server.Server;

public class Goblin extends Decorator
{
	Server.ScheduleTask playerCheck;
	Server.ScheduleTask playerCombat;
	
	Object target;
	
	protected void Initialize()
	{
		playerCheck = Server.schedule(new Server.ScheduleTask() {
			public void run()
			{
				PlayerControlled pc = obj.getRoom().findDecoratorInChildren(PlayerControlled.class);
				
				if(pc == null)
					Server.schedule(this, 4.f + obj.organicRandomFloat(2.0f));
				else
				{
					target = pc.obj;
					
					Server.printToClient(obj.getName() + " notices you and lets out a shriek!", pc.client);
					Server.printToRoomExcluding(obj.getName() + " notices " + target.getName() + " and lets out a shriek!", obj.getRoom(), target);
					
					playerCombat = Server.schedule(new Server.ScheduleTask() {
						public void run()
						{
							Commands.parseCommand(obj, "attack " + target.getName());
						}
					}, 1.0f + obj.organicRandomFloat(2.0f));
				}
			}
		}, 4.0f);
	}
	
	protected String buildDescription()
	{
		String desc = "";
	
		return desc;
	}
}
