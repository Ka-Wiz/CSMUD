package Server.Decorators;

import Server.Decorator;

public class Damage extends Decorator
{
	public int damage = 1;
	public float cooldown = 5.f;
	public String weaponName;
	
	protected void Initialize()
	{
	}
	
	protected String buildDescription()
	{
		String desc = "";
		
//		if(writing == "")
//			desc += "\nIt is currently blank.\n";
//		else
//			desc += "\nIt currently reads: \"" + writing + "\"\n";
//		
//		if(openTo != null)
//		{
//			StringProperty sp = openTo.getDecorator(StringProperty.class);
//			desc += "Underneath is a rippling surface depicting " + sp.str + ".";
//		}
		
		return desc;
	}
}
