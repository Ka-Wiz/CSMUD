package Server;

import Server.Decorators.*;

public class Creation
{
	public static void createWorld()
	{
		Object hub = Server.world;
		
		Object s = Server.startingRoom = new Object(hub, "the white room", "a sterile white room of moderate size, bare of almost any detail. The walls and "
				+ "floor are a familiar yet unidentifiable matte white material that nevertheless glows with a pleasant softness from an unseen "
				+ "light source. On one of the walls there is a whiteboard, and next to it a clock, as well as a doorway.");
		
		Object board = new Object(s, "whiteboard", "A dry erase board commonly seen in modern classrooms. A "
				+ "chemical successor to the simple chalkboard, which was too dusty and screechy for some tastes.");
		board.setLocked("The whiteboard is secured tightly to the wall. You cannot remove it.");
		Writeable w = board.addDecorator(Writeable.class);
		w.writingTool = "marker";
		
		createClock(s);
		
		Object bri = createHuman(s);
		bri.setName("Brian");
		bri.setDescription("A pleasant-looking fellow with light brown hair and blue eyes.");
		
		Dialog d = bri.addDecorator(Dialog.class);
		d.createNode("hello", "Hi there! I'm Brian, and this is my virtual world [CSMUD]. I hope you're enjoying it so far!");
		d.createNode("csmud", "Yes, it's short for Computer Science Multi User Dungeon. It's not a very creative name but it's made specifically to give java [students] "
				+ "an easy framework for applying what they're learning to a fun, interactive virtual environment they can share with their friends.");
		d.createNode("students", "I'm a student myself (we all are, I would hope!) and I felt like the CS curriculum was sorely lacking in ways to spark engagement and "
				+ "creativity. So I did something about it! It's technically a [reimplementation] of my main project.");
		d.createNode("reimplementation", "Hehe, well, for several years I've been working on a full 3D virtual world called Antaeon. It's meant to be a fun sci-fi romp on "
				+ "a little simulated planet, inspired by mid-00s Star Wars RPGs like Galaxies and Knights of the Old Republic. CSMUD is a side project that borrows many of the "
				+ "[design] concepts I came up with for it, like the dialog system you are interacting with right now!");
		d.createNode("design", "Well, trying to make games is how I got into computer science. I learned that it's hard, I suck at making assets, and that I love programming. "
				+ "Uh, hence the whole [CS] student thing. Anyway, I'm tired of writing this dialog. Go out and have fun in the world!");
		d.createNode("cs", "UGH, I thought I told you I was tired of writing this. Just check out the code in the github, it's cool. Creation.java is the best place "
				+ "to start, as it is where this dialog and the rest of the world is defined and the main interface to the engine. I guess one of the central things "
				+ "to know about the world it that it's just a big tree, where child-parent relationships represent 'containment'. The game makes very little distinction "
				+ "between players in a room and the items in their inventories; you could say the players are items in the room's inventory. It's simple but effective, "
				+ "like all good CS should be! I hope you enjoy :)");
		
		Object meadow = new Object(hub, "outside", "a bright green meadow. The grass sways softly in the breeze as a sparse scattering of cumulus clouds drift by in the sky. "
				+ "You feel like you could stand here forever, breathing the fresh air and feeling your mind fill with peace and positivity. Maybe you will. Or maybe you'll "
				+ "go back through the doorway standing straight up out of the grass, or down the clean-looking dirt path to the east. Your call. No rush~");
		
		connectRooms(s, meadow, "doorway", "doorway", "out", "in");
		
		Object copse = new Object(hub, "copse", "the edge of a copse, which the search results for 'small group of trees term' tell me is a... well, small group of trees. "
				+ "I think the difference between a copse and a grove is that a copse can have undergrowth? Not really sure. Anyway, you're on the edge of one. There's really "
				+ "no reason for this to be here other than to build some tension and create a small sense of distance, or, to put it cynically, pad out this example world a bit. "
				+ "Proceed inside for some excitement or back down the path for some relaxation.");
		copse.containmentPreposition = "on";
		
		connectRooms(meadow, copse, "path east", "path west", "down", "back");
		
		Object woods = new Object(hub, "woods", "a thick collection of trees that is eerily silent compared to the pleasant meadow outside. Even the sun has difficulty penetrating "
				+ "this foreboding place. There is a rough stone stairway that leads into what looks like an abandoned mine.");
		
		bri = createHuman(woods);
		bri.setName("Brian");
		bri.setDescription("A pleasant-looking fellow with light brown hair and blue eyes.");
		
		d = bri.addDecorator(Dialog.class);
		d.createNode("hello", "Hi there! I'm Brian, and this is my virtu... (He seems to notice his surroundings.) Oh wait, I'm the woods Brian. Welcome to the "
				+ "[combat] demonstration!");
		d.createNode("combat", "Yes indeed! Down those spooky steps is a [goblin] cave. They're nasty little guys, but you can [fight] to defend yourself!");
		d.createNode("goblin", "Goblins are short green dudes that like to raid human settlements. They're also great generic video game baddies. Though... I "
				+ "did see them acting [weird] once.");
		d.createNode("fight", "They'll attack you on sight, but you can defend yourself with your fists (ineffective) or, ideally, a [weapon].");
		d.createNode("weapon", "placeholder for demonstrating dialog running code to examine players weapon");
		d.createNode("weird", "They were getting along with this other tribe I'd seen them fighting before. I don't know much about the specifics but "
				+ "I heard the words 'krplach' and 'smootu'. Do with that what you will.");
		
		connectRooms(copse, woods, "copse", "exit", "into", "out");
		
		Object m = new Object(hub, "mine entrance", "the entrance of a disused mine. Spiderwebs and concerningly splintered beams cover the ceiling, but underfoot you notice "
				+ "tons of tiny little prints. The tunnel forks ahead of you.");
		
		connectRooms(woods, m, "stone steps", "steps", "down", "up");
		
		Object mn = new Object(hub, "mine north", "the north tunnel of the mine slash goblin caves.");
		Object ms = new Object(hub, "mine south", "the south tunnel of the mine slash goblin caves.");
		
		connectRooms(m, mn, "branch north", "exit tunnel", "down", "up");
		connectRooms(m, ms, "branch south", "exit tunnel", "down", "up");
		
//		Object cj = createHuman(java2);
//		cj.setName("Captain Josh");
//		cj.setDescription("An imposing figure dressed in a slick black uniform with silver trim and golden shoulder pads with dangling tassels. He "
//				+ "glances at you with a stern expression, as if contemplating ordering you to organize the cargo hold. The large captain's hat atop his finely cropped "
//				+ "and slicked hair leaves his authority on this vessel in no doubt.");
//		
//		Dialog d = cj.addDecorator(Dialog.class);
//		d.createNode("hello", "Welcome onboard the UIS Enterprise, cadet. We're on a 4-year mission to [explore] the galaxy, and we could use your [help] to do it.");
//		d.createNode("explore", "Yes, discover new worlds, new life, all that jazz. In fact you've come at an opportune time, as you can see we've just arrived in orbit "
//									+ "around a new [world].");
//		d.createNode("world", "That little ball of spinach you see below us is the world [Antaeon]. Mysterious place, never been charted before. Until now, of course.");
//		d.createNode("antaeon", "We've received some strange stories from ships passing through this sector. Anomalous events, time skips, disappearances, you name it. We've "
//									+ "been sent to investigate, and that's where your [help] comes in.");
////		d.createNode("help", "You voulenteer as part of a new scouting regiment on Antaeon. You land on the planet after a month's journey. When you step foot on the planet "
////				+ "you see small yellow bugs swarming the ground. You start to feel dizzy and ill. You are suddenly back on the ship, but the hourglass bugs have now covered every inch of your flesh."
////				+ "Suddenly, you are back in orbit, the bugs are chewing through your eyes. Time itself is ending. There is nothing you can do but call for [help].");
//		d.createNode("help", "Yes, I've put together an away team that is currently on the planet's surface. You're a promising young cadet and they could use your help.");
//		
//		Object bob = new Object(board, "bob-omb battlefield", "the middle of some sort of brightly-colored warzone. Nearby on a concrete platform two pink bomb-people are "
//				+ "firing large bubbles out of a cannon haphazardly. There is a path leading to an angled wooden bridge "
//				+ "going upward, and a very red canyon beside it. In the distance you can see a huge mountain and... a floating "
//				+ "island?! Maybe you just slammed your head into the board...\n\nOne of the pink bombs approaches you...");
//		bob.addDecorator(StringProperty.class).str = "a curious scene of 3 marching bombs against a cloudy blue sky";
//		
//		Object uhbeh = new Object(hub, "the UHB east hallway", "the UHB east hallway. At the north end you can see the open space of the staircase, while at the other "
//				+ "the hallway ends in a few computery rooms and a right turn, heading west.");
//		
//		connectRooms(s, uhbeh, "doorway", "lab doorway", "out", "through");
//		connectRooms(java2, uhbeh, "doorway", "java 2 doorway", "out", "through");
//		
//		Object uhbstair = new Object(hub, "the UHB staircase", "the UHB staircase area, second floor. The blue carpeting makes your footsteps feel soft. There is a large window "
//				+ "with a grand view of the UIS campus hub, with PAC, the library, and the biology building surrounding the "
//				+ "lovely pillared fountain that is currently shooting water jets in intricate patterns.\n\n"
//				+ "Inside, there are connections to two hallways, and stairs leading both up to the third floor and down to "
//				+ "the first. There are also some couches and tables near the window.");
//		
//		connectRooms(uhbeh, uhbstair, "hall north", "hall south", "down");
//		
//		Object uhbsh = new Object(hub, "the UHB south hallway", "the UHB south hallway, second floor.");
//		
//		connectRooms(uhbeh, uhbsh, "hall west", "hallway", "down");
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
