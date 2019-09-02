package thedemitrius.androidif.vm;

public class VMThread extends Thread
{
	private VM vm;
	
	public VMThread(VM vm)
	{
		this.vm=vm;
	}

	public void run()
	{
		vm.run();
	}
	
	public void finalize()
	{
		if (vm!=null){
			vm.cleanExit();
			vm=null;
		}
	}
}
