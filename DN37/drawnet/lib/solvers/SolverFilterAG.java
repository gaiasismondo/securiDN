package drawnet.lib.solvers;

import drawnet.lib.ddl.ElementInstance;
import drawnet.lib.ddl.ElementType;
import drawnet.lib.ddl.propertyvalues.FloatPropertyValue;

import java.util.Enumeration;

import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;


public class SolverFilterAG extends SolverFilter
{

	private final String DESCR = "AG ---> JSON";

	private ElementInstance ag = null;

	/**
	 * Constructor.
	 */
	public SolverFilterAG(){
		super();
		setDescription( DESCR );
	}

	/**
	 * Constructor.
	 *
	 * @param listener the listener to assign to this solver
	 */
	public SolverFilterAG( SolverListener listener )
	{
		super( listener );
		setDescription( DESCR );
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see solvers.SolverLauncher#execute()
	 */

	private void print(String messaggio)
	{
		System.out.print(messaggio);
	}

	private void mainVisit()
	{
		Enumeration<ElementInstance> enumeration;
		ElementInstance elementInstance;
	  	ElementType elementType;

		enumeration = model_.getMainElement().subElementsEnum();
      		while (enumeration.hasMoreElements())
      		{
			elementInstance = enumeration.nextElement();
			elementType = elementInstance.getElementType();
			
			print("\nmainVisit " + elementInstance.getId() + ": " + elementType.getId());
			
			if (elementType.getId().equals("AG"))
			{
				ag = elementInstance;
				print(" ---> e' un AG");
			}
		}
	}

	private void agVisit()
	{
		Enumeration<ElementInstance> enumeration;
		ElementInstance elementInstance;
	  	ElementType elementType;
		FloatPropertyValue prob;

		enumeration = ag.subElementsEnum();
      		while (enumeration.hasMoreElements())
      		{
			elementInstance = enumeration.nextElement();
			elementType = elementInstance.getElementType();
			
			print("\nagVisit " + elementInstance.getId() + ": " + elementType.getId());
			
			if (elementType.getId().equals("Node"))
			{
				print(" ---> e' una tecnica ");

				prob = (FloatPropertyValue)elementInstance.getPropertyValue("prob");	
				if (prob != null)
					print("con probabilita' " + prob.toString());
			}
		}
	}

	public boolean execute()
	{
		this.mainVisit();
		this.agVisit();
		print("\nAG ---> JSON eseguito\n");

		return true;
	}
}
