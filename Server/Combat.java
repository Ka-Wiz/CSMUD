package Server;

import java.util.ArrayList;
import java.util.Arrays;

import Server.Decorators.PlayerControlled;

public class Combat
{
	public Server.ScheduleTask update;
	private ArrayList<CombatState> participants;
	int position = 0;
	
	class CombatState
	{
		Object obj;
		public CombatState(Object obj, String cmd) {
			super();
			this.obj = obj;
			this.cmd = cmd;
		}
		String cmd;
	}
	
	Combat(String cmd, Object sender, Object target)
	{
		addParticipant(target, "attack " + sender.getName());
		addParticipant(sender, cmd);
		
		update = Server.schedule(new Server.ScheduleTask() {
			public void run()
			{
				setTimerTask(null);
			}
		}, 5.0f);
	}
	
	public void addParticipant(Object o, String s)
	{
		participants.add(position, new CombatState(o, s));
	}
	
	public void removeParticipant(Object o)
	{
		participants.remove(o);
	}
	
	void start(Object sender, Object target)
	{
		PlayerControlled player = sender.getDecorator(PlayerControlled.class);
		if(player != null)
		{
			Server.printToClient("You have the initiative and strike first!", player.client);
		}
		
		
	}
}
