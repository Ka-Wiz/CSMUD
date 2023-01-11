package Server;

import Server.Decorators.*;

public class Creation
{
	public static void createWorld()
	{
		Object hub = Server.world;
		hub.setName("World");
		
		Object s = Server.startingRoom = new Object(hub, "the white room", "a sterile white room of moderate size, bare of almost any detail. The walls and "
				+ "floor are a familiar yet unidentifiable matte white material that nevertheless glows with a pleasant softness from an unseen "
				+ "light source. On one of the walls there is a whiteboard, and next to it a clock, as well as a doorway.");
		
		Object board = new Object(s, "whiteboard", "A dry erase board commonly seen in modern classrooms. A "
				+ "chemical successor to the simple chalkboard, which was too dusty and screechy for some tastes.");
		board.setLocked("The whiteboard is secured tightly to the wall. You cannot remove it.");
		Writeable w = board.addDecorator(Writeable.class);
		w.writingTool = "marker";
		
		createClock(s);
		
		createSword(s);
		
		for(int i = 0; i < 4; ++i)
		{
			Object gob = createGoblin(s);
			gob.setName("Goblin " + (i+1));
		}
		
		Object bri = createHumanoid(s);
		bri.setName("Brian");
		bri.setDescription("A pleasant-looking fellow with light brown hair and blue eyes.");
		
		final Dialog d1 = bri.addDecorator(Dialog.class);
		d1.createNode("hello", "Hi there! I'm Brian, and this is my virtual world [CSMUD]. I hope you're enjoying it so far!");
		d1.createNode("csmud", "Yes, it's short for Computer Science Multi User Dungeon. It's not a very creative name but it's made specifically to give java [students] "
				+ "an easy framework for applying what they're learning to a fun, interactive virtual environment they can share with their friends.");
		d1.createNode("students", "I'm a student myself (we all are, I would hope!) and I felt like the CS curriculum was sorely lacking in ways to spark engagement and "
				+ "creativity. So I did something about it! It's technically a [reimplementation] of my main project.");
		d1.createNode("reimplementation", "Hehe, well, for several years I've been working on a full 3D virtual world called Antaeon. It's meant to be a fun sci-fi romp on "
				+ "a little simulated planet, inspired by mid-00s Star Wars RPGs like Galaxies and Knights of the Old Republic. CSMUD is a side project that borrows many of the "
				+ "[design] concepts I came up with for it, like the dialog system you are interacting with right now!");
		d1.createNode("design", "Well, trying to make games is how I got into computer science. I learned that it's hard, I suck at making assets, and that I love programming. "
				+ "Uh, hence the whole [CS] student thing. Anyway, I'm tired of writing this dialog. Go out and have fun in the world!");
		d1.createNode("cs", "UGH, I thought I told you I was tired of writing this. Just check out the code in the github, it's cool. Creation.java is the best place "
				+ "to start, as it is where this dialog and the rest of the world is defined and the main interface to the engine. I guess one of the central things "
				+ "to know about the world it that it's just a big tree, where child-parent relationships represent 'containment'. The game makes very little distinction "
				+ "between players in a room and the items in their inventories; you could say the players are items in the room's inventory. It's simple but effective, "
				+ "like all good CS should be! I hope you enjoy :)");
		
		Object meadow = new Object(hub, "outside", "a bright green meadow. The grass sways softly in the breeze as a sparse scattering of cumulus clouds drift by in the sky. "
				+ "You feel like you could stand here forever, breathing the fresh air and feeling your mind fill with peace and positivity. Maybe you will. Or maybe you'll "
				+ "go back through the doorway standing straight up out of the grass, or down the clean-looking dirt path to the east. Your call. No rush~");
		
		RoomConnection doorway = connectRooms(s, meadow, "doorway", "doorway", "out", "in");
		doorway.makeCloseable("door", false);
		
		Object copse = new Object(hub, "copse", "the edge of a copse, which the search results for 'small group of trees term' tell me is a... well, small group of trees. "
				+ "I think the difference between a copse and a grove is that a copse can have undergrowth? Not really sure. Anyway, you're on the edge of one. There's really "
				+ "no reason for this to be here other than to build some tension and create a small sense of distance, or, to put it cynically, pad out this example world a bit. "
				+ "Proceed inside for some excitement or back down the path for some relaxation.");
		copse.containmentPreposition = "on";
		
		connectRooms(meadow, copse, "path east", "path west", "down", "back");
		
		Object woods = new Object(hub, "woods", "a thick collection of trees that is eerily silent compared to the pleasant meadow outside. Even the sun has difficulty penetrating "
				+ "this foreboding place. There is a rough stone stairway that leads into what looks like an abandoned mine.");
		
		bri = createHumanoid(woods);
		bri.setName("Brian");
		bri.setDescription("A pleasant-looking fellow with light brown hair and blue eyes.");
		
		Dialog dialog = bri.addDecorator(Dialog.class);
		dialog.createNode("hello", "Hi there! I'm Brian, and this is my virtu... (He seems to notice his "
									+ "surroundings.) Oh wait, I'm the woods Brian. Welcome to the [combat] demonstration!");
		
		dialog.createNode("combat", "Yes indeed! Down those spooky steps is a [goblin] cave. They're nasty "
									+ "little guys, but you can [fight] to defend yourself!");
		
		dialog.createNode("goblin", "Goblins are short green dudes that like to raid human settlements. They're "
									+ "also great generic video game baddies. Though... I did see them acting [weird] once.");
		
		dialog.createNode("fight", "They'll attack you on sight, but you can defend yourself with your fists or, ideally, a [weapon].");
		
		dialog.createNode("weapon", "Yup, it's generally a good idea to equip yourself with something. Here, "
									+ "would you like a [dagger] or a [sword]? Daggers are faster, but swords are stronger!");
		
		dialog.createNode("dagger", "Here you go, hope you enjoy slicing and dicing!", 
										dialog.new DialogCode() { public void enter() {
																						Server.printToCurrentClient("He lightly tosses you a dagger.");
																						createDagger(dialog.talkingTo);
																				  	  } } );
		
		dialog.createNode("sword", "Here you go, hope you enjoy hacking and slashing!",
										dialog.new DialogCode() { public void enter() {
																						Server.printToCurrentClient("He gently hands you a sword.");
																						createSword(dialog.talkingTo);
																					  } } );
		dialog.createNode("weird", "They were getting along with this other tribe I'd seen them fighting before. I don't know much about the specifics but "
				+ "I heard the words 'krplach' and 'smootu'. Do with that what you will.");
		
		connectRooms(copse, woods, "copse", "exit", "into", "out");
		
		Object m = new Object(hub, "mine entrance", "the entrance of a disused mine. Spiderwebs and concerningly splintered beams cover the ceiling, but underfoot you notice "
				+ "tons of tiny little prints. The tunnel forks ahead of you.");
		
		connectRooms(woods, m, "stone steps", "steps", "down", "up");
		
		Object mn = new Object(hub, "mine north", "the north tunnel of the mine slash goblin caves.");
		Object ms = new Object(hub, "mine south", "the south tunnel of the mine slash goblin caves.");
		
		connectRooms(m, mn, "branch north", "exit tunnel", "down", "up");
		connectRooms(m, ms, "branch south", "exit tunnel", "down", "up");
	}
	
	public static Object createPlayer(Object storeIn, String name, ClientProcess cp)
	{
		Object player = createHumanoid(storeIn);
		player.setName(name != "" ? name : "Eric");
		player.setDescription("You handsome devil, you.");
		player.addDecorator(PlayerControlled.class).client = cp;
		
		new Object(player, "deathnote", "A mysterious notebook from another dimension. Its rules are not entirely clear.").addDecorator(Writeable.class);
		
		return player;
	}
	public static Object createHumanoid(Object storeIn)
	{
		Object body = new Object(storeIn);
		createHands(body);
		createLegs(body);
		
		return body;
	}
	public static Object createHands(Object storeIn)
	{
		Object hands = new Object(storeIn, "hands");
		hands.setLocked("They are attached.");
		hands.addDecorator(Holder.class);
		Movement mv = hands.addDecorator(Movement.class);
		mv.setMovePriority(Movement.MovePriority.TERTIARY);
		mv.moveString = "drag yourself";
		
		Damage dmg = hands.addDecorator(Damage.class);
		dmg.damage = 2;
		dmg.cooldown = 4.f;
		dmg.weaponName = "fists";
		
		return hands;
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
	
	public static Object createGoblin(Object storeIn)
	{
		Object gobbo = createHumanoid(storeIn);
		gobbo.setName("Goblin");
		gobbo.setDescription("Little guy. Warty green skin. Pointy ears.");
		
		gobbo.addDecorator(Goblin.class);
		
		createDagger(gobbo);
		Commands.parseCommand(gobbo, "hold dagger");
		
		return gobbo;
	}
	
	public static Object createClock(Object storeIn)
	{
		Object clock = new Object(storeIn, "clock", "A wall-mounted time-keeping device.");
		clock.addDecorator(TimeDisplay.class);
		
		return clock;
	}
	
	public static Object createDagger(Object storeIn)
	{
		Object dag = new Object(storeIn, "dagger", "A rather short but deadly sharp piece of metal.");
		Damage d = dag.addDecorator(Damage.class);
		d.damage = 4;
		d.cooldown = 2.f;
		return dag;
	}
	
	public static Object createSword(Object storeIn)
	{
		Object sword = new Object(storeIn, "sword", "A moderately long, sharp piece of metal.");
		Damage d = sword.addDecorator(Damage.class);
		d.damage = 6;
		d.cooldown = 5.f;
		return sword;
	}
	
 	public static RoomConnection connectRooms(Object r1, Object r2, String name1, String type1)
	{
		return connectRooms(r1, r2, name1, name1, type1, type1);
	}
	public static RoomConnection connectRooms(Object r1, Object r2, String name1, String name2, String type1)
	{
		return connectRooms(r1, r2, name1, name2, type1, type1);
	}
	public static RoomConnection connectRooms(Object r1, Object r2, String name1, String name2, String type1, String type2)
	{
		Object c1 = new Object(r1, name1);
		RoomConnection rc1 = c1.addDecorator(RoomConnection.class);
		rc1.moveAdverb = type1;
		
		Object c2 = new Object(r2, name2 == "" ? name1 : name2);
		RoomConnection rc2 = c2.addDecorator(RoomConnection.class);
		rc2.moveAdverb = type2;
		
		rc1.connectionTo = c2;
		rc2.connectionTo = c1;
		
		return rc1;
	}
}
