package srv.cmp;

import srv.Commands;
import srv.Component;
import srv.Entity;
import srv.Server;

public class Goblin extends Component
{
	Server.ScheduleTask playerCheck;
	Server.ScheduleTask playerCombat;
	
	Entity target;
	
	protected void Initialize()
	{
		playerCheck = Server.schedule(new Server.ScheduleTask() {
			public void run()
			{
				PlayerControlled pc = ent.getRoom().findComponentInChildren(PlayerControlled.class);
				
				if(pc == null)
					Server.schedule(this, 4.f + ent.organicRandomFloat(2.0f));
				else
				{
					target = pc.ent;
					
					Server.printToClient(ent.getName() + " notices you and lets out a shriek!", pc.client);
					Server.printToRoomExcluding(ent.getName() + " notices " + target.getName() + " and lets out a shriek!", ent.getRoom(), target);
					
					playerCombat = Server.schedule(new Server.ScheduleTask() {
						public void run()
						{
							Commands.parseCommand(ent, "attack " + target.getName());
						}
					}, 1.0f + ent.organicRandomFloat(2.0f));
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
