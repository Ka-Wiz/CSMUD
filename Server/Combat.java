package Server;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;

import Server.Decorators.Damage;
import Server.Decorators.PlayerControlled;

public class Combat
{
	public Server.ScheduleTask update;
	public float roundTime = 2.5f;
	
	private ArrayList<CombatState> participants = new ArrayList<CombatState>();
	
	private static Random rand = new Random();
	
	class CombatState
	{
		public CombatState(Object obj, String cmd)
		{
			this.obj = obj;
			this.cmd = cmd;
			
			lastAttack = Instant.EPOCH;
			cooldown = 0;
		}
		
		Object obj;
		String cmd;
		Instant lastAttack;
		long cooldown;
	}
	
	Combat(String cmd, Object sender, Object target)
	{
		addParticipant(target, "attack " + sender.getName());
		CombatState initialAttack = addParticipant(sender, cmd);
		
		initialAttack.cooldown = 2000;
		initialAttack.lastAttack = Instant.now();
		
		update = Server.schedule(new Server.ScheduleTask() {
			public void run()
			{
				long longest = Long.MIN_VALUE, timeSince = Long.MIN_VALUE;
				CombatState next = null;
				for(CombatState cs : participants)
				{
					timeSince = Duration.between(cs.lastAttack, Instant.now()).toMillis() - cs.cooldown;
					
					if(Server.checkDebug(Server.DBG_CBT))
						System.out.println("examining " + cs.obj.getName() + " " + timeSince);
							
					if(timeSince > longest && timeSince > 0)
					{
						longest = timeSince;
						next = cs;
					}
				}
				
				if(next != null)
				{
					if(Server.checkDebug(Server.DBG_CBT))
						System.out.println("==> " + next.obj.getName() + " " + longest);
					
					Commands.parseCommand(next.obj, next.cmd);
					
					next.cooldown = (long)(next.obj.findDecoratorInChildrenRecursive(Damage.class).cooldown * 1000.f);
					next.cooldown += rand.nextInt() % (next.cooldown * 0.65);
					next.lastAttack = Instant.now();
					
					Server.schedule(this, roundTime);
				}
				else
					Server.schedule(this, roundTime * 0.25f);
			}
		}, roundTime);
	}
	
	public CombatState addParticipant(Object o, String cmd)
	{		
		PlayerControlled pc;
		for(CombatState cs : participants)
			if((pc = cs.obj.getDecorator(PlayerControlled.class)) != null)
				Server.printToClient(o.getName() + " enters the fray!", pc.client);
		
		CombatState newState = new CombatState(o, cmd);
		participants.add(newState);
		return newState;
	}
	
	public void removeParticipant(Object o)
	{
		for(CombatState cs : participants)
			if(cs.obj == o)
			{
				participants.remove(cs);
				break;
			}
		
		PlayerControlled pc;
		for(CombatState cs : participants)
			if((pc = cs.obj.getDecorator(PlayerControlled.class)) != null)
				Server.printToClient(o.getName() + " leaves the fray!", pc.client);
	}
}
