package srv;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;

import srv.cmp.Damage;
import srv.cmp.PlayerControlled;

public class Combat
{
	public Server.ScheduleTask update;
	public float roundTime = 2.25f;
	private ArrayList<CombatState> participants = new ArrayList<CombatState>();
	
	private static Random rand = new Random();
	
	class CombatState
	{
		public CombatState(Entity obj, String cmd)
		{
			this.obj = obj;
			this.cmd = cmd;
			
			lastAttack = Instant.EPOCH;
			cooldown = 0;
		}
		
		Entity obj;
		String cmd;
		Instant lastAttack;
		long cooldown;
	}
	
	Combat(String cmd, Entity sender, Entity target)
	{
		addParticipant(target, "attack " + sender.getName());
		CombatState initialAttack = addParticipant(sender, cmd);
		
		initialAttack.cooldown = 2000;
		initialAttack.lastAttack = Instant.now();
		
		update = Server.schedule(new Server.ScheduleTask() {
			public void run()
			{
				if(participants.size() == 1)
					return;
				
				long longest = Long.MIN_VALUE, timeSince = Long.MIN_VALUE;
				CombatState next = null;
				for(CombatState cs : participants)
				{
					timeSince = Duration.between(cs.lastAttack, Instant.now()).toMillis();
//					timeSince -= (cs.cooldown + rand.nextInt() % (cs.cooldown * 0.1));
					timeSince -= cs.cooldown + cs.cooldown * rand.nextGaussian() * 0.25d;
					timeSince += timeSince * rand.nextGaussian() * 0.3d;
					
					if(Server.checkDebug(Server.DBG.CBT))
						Server.printDebug("UPDATE", "examining " + cs.obj.getName() + " " + timeSince);
							
					if(timeSince > longest && timeSince > 0)
					{
						longest = timeSince;
						next = cs;
					}
				}
				
				if(next != null)
				{
					if(Server.checkDebug(Server.DBG.CBT))
						Server.printDebug("UPDATE", "==> " + next.obj.getName() + " " + longest);
					
					Commands.parseCommand(next.obj, next.cmd);
					
					next.cooldown = (long)(next.obj.findComponentInChildrenRecursive(Damage.class).cooldown * 1000.f);
					next.lastAttack = Instant.now();
					
					Server.schedule(this, roundTime);
				}
				else
					Server.schedule(this, roundTime * 0.25f);
			}
		}, roundTime);
	}
	
	public CombatState addParticipant(Entity o, String cmd)
	{		
		PlayerControlled pc;
		for(CombatState cs : participants)
			if((pc = cs.obj.getComponent(PlayerControlled.class)) != null)
				Server.printToClient(o.getName() + " engages in battle!", pc.client);
		
		CombatState newState = new CombatState(o, cmd);
		participants.add(newState);
		return newState;
	}
	
	public void removeParticipant(Entity o, boolean displayMessage)
	{
		for(CombatState cs : participants)
			if(cs.obj == o)
			{
				participants.remove(cs);
				break;
			}
		
		if(displayMessage)
		{
			PlayerControlled pc;
			for(CombatState cs : participants)
				if((pc = cs.obj.getComponent(PlayerControlled.class)) != null)
					Server.printToClient(o.getName() + " leaves the fray!", pc.client);
		}
	}
	
	public void changeCommand(Entity o, String cmd)
	{
		for(CombatState cs : participants)
			if(cs.obj == o)
			{
				cs.cmd = cmd;
				return;
			}
	}
}
