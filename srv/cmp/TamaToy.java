package srv.cmp;

import srv.ClientProcess;
import srv.Command;
import srv.Component;
import srv.Server;

public class TamaToy extends Component
{
	int food = 10;
	int mood = 10;
	
	ClientProcess owner;
	
	protected void Initialize()
	{
		Server.schedule(new Server.ScheduleTask() {
			public void run()
			{
				owner = ent.containedIn.getComponent(PlayerControlled.class).client;
				
				if(owner == null)
					return;
				
				food -= 1;
				mood -= 1;
				
				if(food == 2)
					Server.printToClient("Your Tama is getting hungry, mewing at you pitifully.", owner);
				if(mood == 2)
					Server.printToClient("Your Tama is getting depressed, having no satisfaction in life.", owner);
				
				if(food == 0)
				{
					Server.printToClient("Your skeletal Tama starves to death, looking up at you plaintively before keeling over with a barely noticeable thud. You are not a good person.", owner);
					return;
				}
				if(mood == 0)
				{
					Server.printToClient("Your miserable Tama decides to take its own life, swinging slowly from a noose. At least it is no longer suffering.", owner);
					return;
				}
				
				Server.schedule(this, 3.0f);
			}
		}, 3.0f);
		
		commandStrings.put("play", new Command()
		{
			public void invoke()
			{
				Server.printToClient("You play with your Tama and it makes adorable noises. It is filled with joy and happiness.", owner);
				mood = 10;
			}
		});
		
		commandStrings.put("feed", new Command()
		{
			public void invoke()
			{
				Server.printToClient("You feed with your Tama and it gobbles it down hungrily, well fed and satiated. You are a good person.", owner);
				food = 10;
			}
		});
	}
}
