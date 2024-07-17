package drawnet.lib.solvers;

import drawnet.lib.ddl.ElementInstance;
import drawnet.lib.ddl.ElementType;
import drawnet.lib.ddl.propertyvalues.FloatPropertyValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import javax.swing.text.ElementIterator;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;

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
		print("\n");
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

	//vengono eliminati dal modello gli archi entranti in nodi di tipo analytics
	//vengono eliminati dal modello i nodi di tipo analytics 
	//vengono eliminati dal modello i nodi di tipo G_Layer e G_iconDef
	//(vengono salvati in un array e poi vengono eliminati alla fine per preservare puntatori)
	private void removeAnalytics()
	{
		Enumeration<ElementInstance> enumeration;
		ElementInstance elementInstance;
	  	ElementType elementType;
		ArrayList<String> analyticsNodes = new ArrayList<String>();

		enumeration = ag.subElementsEnum();

		while (enumeration.hasMoreElements()){

			elementInstance = enumeration.nextElement();
			elementType = elementInstance.getElementType();
		
			if(elementType.getId().equals("Arc")){
				String destination_node_string = elementInstance.getPropertyValue("to").toString();
				ElementInstance destination_node = ag.getSubElement(destination_node_string);
				if(destination_node.getElementType().getId().equals("Analytics")){
					ag.removeSubElement(elementInstance.getId());
				}		
			}

			if(elementType.getId().equals("Analytics")||elementType.getId().equals("G_Layer")||elementType.getId().equals("G_iconDef")){
				analyticsNodes.add(elementInstance.getId());
			}
		
		}

		for (String s : analyticsNodes) {
			ag.removeSubElement(s);
		}
	}

	//Vengono catalogati come "non radice" tutti i nodi che hanno almeno un arco entrante
	//I nodi rimanenti sono le radici del grafo e i loro Id vengono restituiti all'interno di un arrayList
	private ArrayList<String> getRootNodes(){

		Enumeration<ElementInstance> enumeration;
		ElementInstance elementInstance;
	  	ElementType elementType;
		ArrayList<String> nonRootNodes = new ArrayList<String>();
		ArrayList<String> allNodes = new ArrayList<String>();

		enumeration = ag.subElementsEnum();

		while (enumeration.hasMoreElements()){

			elementInstance = enumeration.nextElement();
			elementType = elementInstance.getElementType();

			print("\n");
			print("\n"+elementType.getId()+"   "+elementInstance.getId());


			if(elementType.getId().equals("Arc")){
				String destination_node_string = elementInstance.getPropertyValue("to").toString();
				if(!nonRootNodes.contains(destination_node_string)){
					nonRootNodes.add(destination_node_string);
				}
			}
			
			if(elementType.getId().equals("Node")||elementType.getId().equals("NodeOR")||elementType.getId().equals("NodeAND")){
				if(!allNodes.contains(elementInstance.getId())){
					allNodes.add(elementInstance.getId());
				}
			}

		}

		allNodes.removeAll(nonRootNodes);

		return allNodes;
	}


	private void createJson(String filePath)
	{
		try(FileWriter writer = new FileWriter(filePath)){
			writer.write("{}");
		}
		catch (IOException e){
			e.printStackTrace();
		}
	}

	
	public boolean execute()
	{
		this.mainVisit();
		this.removeAnalytics();
		this.agVisit();
		
		print("\nAG ---> JSON eseguito\n");
		this.createJson("drawnet/lib/json/esempio.json");


		return true;
	}
}
