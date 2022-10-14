package Server;

import Server.Decorators.*;

public class Creation
{
	public static void createWorld()
	{
		Object uhb = Server.world;
		
		Object s = Server.startingRoom = new Object(uhb, "the computer lab", "the computer lab at University of Illinois Springfield.\n\nIt's filled with a soft fluorescent "
				+ "glow from technical-looking light fixtures suspended from the ceiling. There are three "
				+ "long rows of computer desks filled with quietly whirring terminals, each capped at the far end "
				+ "by a rack of networking equipment. There are multiple whiteboards, ostensibly for complicated "
				+ "computery diagrams. Miscellaneous hardware components are strewn about haphazardly in the perennial "
				+ "tradition of such labs, as is the presence of several bookshelves and a black enigmatic monolith of a "
				+ "cabinet. There is also a teacher's podium.");
		
		new Object(s, "JoeNPC", "Just the average working class citizen.");
		
		Object board = new Object(s, "whiteboard", "A dry erase board commonly seen in modern classrooms. A "
				+ "chemical successor to the simple chalkboard, which was too dusty and screechy for some tastes.");
		board.setLocked("The whiteboard is secured tightly to the wall. You cannot remove it.");
		Writeable w = board.addDecorator(Writeable.class);
		w.writingTool = "marker";
		
		createClock(s);
		
		Object java2 = new Object(uhb, "the Java 2 room", "the bridge of a spaceship. Out the viewports across from you, you see a magnificent starfield, unhindered by any trace "
				+ "of atmosphere. Near the bottom, you can see the majestic glowing arc of a greenish-blue planet. You hear the gentle whirring and beeps of the bridge "
				+ "terminals around you, as well as the rumble of the ship's reactor.");
		java2.containmentPreposition = "on";
		
		Object cj = createHuman(java2);
		cj.setName("Captain Josh");
		cj.setDescription("An imposing figure dressed in a slick black uniform with silver trim and golden shoulder pads with dangling tassels. He "
				+ "glances at you with a stern expression, as if contemplating ordering you to organize the cargo hold. The large captain's hat atop his finely cropped "
				+ "and slicked hair leaves his authority on this vessel in no doubt.");
		
		Dialog d = cj.addDecorator(Dialog.class);
		d.createNode("hello", "Welcome onboard the UIS Enterprise, cadet. We're on a 4-year mission to [explore] the galaxy, and we could use your [help] to do it.");
		d.createNode("explore", "Yes, discover new worlds, new life, all that jazz. In fact you've come at an opportune time, as you can see we've just arrived in orbit "
									+ "around a new [world].");
		d.createNode("world", "That little ball of spinach you see below us is the world [Antaeon]. Mysterious place, never been charted before. Until now, of course.");
		d.createNode("antaeon", "We've received some strange stories from ships passing through this sector. Anomalous events, time skips, disappearances, you name it. We've "
									+ "been sent to investigate, and that's where your [help] comes in.");
//		d.createNode("help", "You voulenteer as part of a new scouting regiment on Antaeon. You land on the planet after a month's journey. When you step foot on the planet "
//				+ "you see small yellow bugs swarming the ground. You start to feel dizzy and ill. You are suddenly back on the ship, but the hourglass bugs have now covered every inch of your flesh."
//				+ "Suddenly, you are back in orbit, the bugs are chewing through your eyes. Time itself is ending. There is nothing you can do but call for [help].");
		d.createNode("help", "Yes, I've put together an away team that is currently on the planet's surface. You're a promising young cadet and they could use your help.");
		
		Object bob = new Object(board, "bob-omb battlefield", "the middle of some sort of brightly-colored warzone. Nearby on a concrete platform two pink bomb-people are "
				+ "firing large bubbles out of a cannon haphazardly. There is a path leading to an angled wooden bridge "
				+ "going upward, and a very red canyon beside it. In the distance you can see a huge mountain and... a floating "
				+ "island?! Maybe you just slammed your head into the board...\n\nOne of the pink bombs approaches you...");
		bob.addDecorator(StringProperty.class).str = "a curious scene of 3 marching bombs against a cloudy blue sky";
		
		Object uhbeh = new Object(uhb, "the UHB east hallway", "the UHB east hallway. At the north end you can see the open space of the staircase, while at the other "
				+ "the hallway ends in a few computery rooms and a right turn, heading west.");
		
		connectRooms(s, uhbeh, "doorway", "lab doorway", "out", "through");
		connectRooms(java2, uhbeh, "doorway", "java 2 doorway", "out", "through");
		
		Object uhbstair = new Object(uhb, "the UHB staircase", "the UHB staircase area, second floor. The blue carpeting makes your footsteps feel soft. There is a large window "
				+ "with a grand view of the UIS campus hub, with PAC, the library, and the biology building surrounding the "
				+ "lovely pillared fountain that is currently shooting water jets in intricate patterns.\n\n"
				+ "Inside, there are connections to two hallways, and stairs leading both up to the third floor and down to "
				+ "the first. There are also some couches and tables near the window.");
		
		connectRooms(uhbeh, uhbstair, "hall north", "hall south", "down");
		
		Object uhbsh = new Object(uhb, "the UHB south hallway", "the UHB south hallway, second floor.");
		
		connectRooms(uhbeh, uhbsh, "hall west", "hallway", "down");
	}
	
	public static Object createPlayer(Object storeIn, String name, ClientProcess cp)
	{
		Object player = createHuman(storeIn);
		player.setName(name != "" ? name : "Eric");
		player.setDescription("You handsome devil, you.");
		player.addDecorator(PlayerControlled.class).client = cp;
		
		new Object(player, "deathnote", "A mysterious notebook from another dimension. Its rules are not entirely clear.").addDecorator(Writeable.class);
		
		return player;
	}
	public static Object createHuman(Object storeIn)
	{
		Object body = new Object(storeIn);
		createArms(body);
		createLegs(body);
		
		return body;
	}
	public static Object createArms(Object storeIn)
	{
		Object arms = new Object(storeIn, "arms");
		arms.setLocked("They are attached.");
		Movement mv = arms.addDecorator(Movement.class);
		mv.setMovePriority(Movement.MovePriority.TERTIARY);
		mv.moveString = "drag yourself";
		
		return arms;
	}
	public static Object createLegs(Object storeIn)
	{
		Object legs = new Object(storeIn, "legs");
		legs.setLocked("They are attached.");
		Movement mv = legs.addDecorator(Movement.class);
		mv.setMovePriority(Movement.MovePriority.PRIMARY);
		mv.moveString = "walk";
		
		return legs;
	}
	
	public static Object createClock(Object storeIn)
	{
		Object clock = new Object(storeIn, "clock", "A wall-mounted time-keeping device.");
		clock.addDecorator(TimeDisplay.class);
		
		return clock;
	}
	
 	public static void connectRooms(Object r1, Object r2, String name1, String type1)
	{
		connectRooms(r1, r2, name1, name1, type1, type1);
	}
	public static void connectRooms(Object r1, Object r2, String name1, String name2, String type1)
	{
		connectRooms(r1, r2, name1, name2, type1, type1);
	}
	public static void connectRooms(Object r1, Object r2, String name1, String name2, String type1, String type2)
	{
		Object c1 = new Object(r1, name1);
		RoomConnection rc1 = c1.addDecorator(RoomConnection.class);
		rc1.moveType = type1;
		
		Object c2 = new Object(r2, name2 == "" ? name1 : name2);
		RoomConnection rc2 = c2.addDecorator(RoomConnection.class);
		rc2.moveType = type2;
		
		rc1.connectionTo = c2;
		rc2.connectionTo = c1;
	}
}
