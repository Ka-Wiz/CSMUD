package srv.cmp;

import srv.Component;
import srv.Server;

public class TimeDisplay extends Component
{
	protected String buildDescription()
	{
		return "\nIt says the time is currently " + Server.worldTime.toString() + ".";
	}
}
