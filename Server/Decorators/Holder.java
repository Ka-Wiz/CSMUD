package Server.Decorators;

import Server.Decorator;

public class Holder extends Decorator
{
	protected void Initialize()
	{
		
	}
	
	void Damage(int dam)
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
