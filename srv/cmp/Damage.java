package srv.cmp;

import srv.Component;

public class Damage extends Component
{
	public int damage = 1;
	public float cooldown = 5.f;
	public String weaponName;
	public String attackVerb;
	public String criticalVerb;
	
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
