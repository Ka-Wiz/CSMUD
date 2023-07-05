package srv.cmp;

import srv.ClientProcess;
import srv.Component;

public class PlayerControlled extends Component
{
	public ClientProcess client;
	public String lastCommand = "";
	
	protected void OnStore()
	{
		
	}
}
