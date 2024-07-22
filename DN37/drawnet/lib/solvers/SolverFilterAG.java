package drawnet.lib.solvers;

import drawnet.lib.ddl.ElementInstance;
import drawnet.lib.ddl.ElementType;
import drawnet.lib.ddl.propertyvalues.FloatPropertyValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Stack;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.text.ElementIterator;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;

import com.google.gson.Gson;


public class SolverFilterAG extends SolverFilter
{

	private final String DESCR = "AG ---> JSON";

	private ElementInstance ag = null;
	private ArrayList<String> edges = new ArrayList<String>();
	private ArrayList<String> nodes = new ArrayList<String>();

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


	//scorre il grado e inserisce tutti i nodi nell'ArrayList nodes e tutti gli archi nell'arrayList edges
	private void getEdgesAndNodes(){

		Enumeration<ElementInstance> enumeration;
		ElementInstance elementInstance;
	  	ElementType elementType;

		enumeration = ag.subElementsEnum();

		while (enumeration.hasMoreElements()){

			elementInstance = enumeration.nextElement();
			elementType = elementInstance.getElementType();

			if(elementType.getId().equals("Arc")){
				edges.add(elementInstance.getId());
			}
			else if(elementType.getId().equals("Node")||elementType.getId().equals("NodeOR")||elementType.getId().equals("NodeAND")){
				nodes.add(elementInstance.getId());
			}
		}
	}


	//Vengono catalogati come "nodi con genitori" tutti i nodi che hanno almeno un arco entrante
	//Facendo la differenza tra l'insieme di tutti i nodi e dei "nodi con genitori" si trovano quelli senza genitori
	private ArrayList<String> getNodesWithoutParents(ArrayList<String> edges , ArrayList<String> nodes){
		
		ArrayList<String> nodesWithParents = new ArrayList<String>();

		for(String e: edges){
			ElementInstance edge = ag.getSubElement(e);
			String destination_node = edge.getPropertyValue("to").toString();
			if(!nodesWithParents.contains(destination_node)){
				nodesWithParents.add(destination_node);
			}
		}

		nodes.removeAll(nodesWithParents);
		return nodes;
	}

	//restituisce true se il nodo passato in input è un nodo di tipo AND, false altrimenti
	private boolean isANDnode(String node){
		ElementInstance n = ag.getSubElement(node);
		if (n.getElementType().getId().equals("NodeAND")){
			return true;
		}
		return false;
	}

	//restituisce true se il nodo passato in input è un nodo di tipo OR, false altrimenti
	private boolean isORnode(String node){
		ElementInstance n = ag.getSubElement(node);
		if (n.getElementType().getId().equals("NodeOR")){
			return true;
		}
		return false;
	}

	//prende in input un nodo e una lista di archi e restituisce un array contenente gli archi uscenti da quel nodo
	private ArrayList<String> getOutgoingEdges(String node, ArrayList<String> edges){

		ArrayList<String> outgoingEdges = new ArrayList<String>();

		for(String e: edges){
			ElementInstance edge = ag.getSubElement(e);
			String starting_node = edge.getPropertyValue("from").toString();

			if(starting_node.equals(node)){
				outgoingEdges.add(e);
			}
		}

		return outgoingEdges;
	}


	//prende in input un nodo e una lista di archi e restituisce un array contenente gli archi entranti in quel nodo
	private ArrayList<String> getIncomingEdges(String node, ArrayList<String> edges){

		ArrayList<String> incomingEdges = new ArrayList<String>();

		for(String e: edges){
			ElementInstance edge = ag.getSubElement(e);
			String destination_node = edge.getPropertyValue("to").toString();

			if(destination_node.equals(node)){
				incomingEdges.add(e);
			}
		}

		return incomingEdges;
	}


	//Preso l'elenco degli archi uscenti da un nodo restituisce l'elenco dei figli
	private ArrayList<String> getChildrenNodes(ArrayList<String> outgoingEdges){

		ArrayList<String> childrenNodes = new ArrayList<String>();

			for(String e: outgoingEdges){
				ElementInstance edge = ag.getSubElement(e);
				childrenNodes.add(edge.getPropertyValue("to").toString());
			}

		return childrenNodes;
	}


	//Preso l'elenco degli archi entanti in un nodo restituisce l'elenco dei genitori
	private ArrayList<String> getParentsNodes(ArrayList<String> incomingEdges){

		ArrayList<String> parentsNodes = new ArrayList<String>();

			for(String e: incomingEdges){
				ElementInstance edge = ag.getSubElement(e);
				parentsNodes.add(edge.getPropertyValue("from").toString());
			}

		return parentsNodes;
	}
	

//restotuisce l'ordine topologico dell'attack graph
private ArrayList<String> getTopologicalOrder() {
    this.getEdgesAndNodes();

    @SuppressWarnings("unchecked")
    ArrayList<String> edges = (ArrayList<String>) this.edges.clone();
    @SuppressWarnings("unchecked")
    ArrayList<String> nodes = (ArrayList<String>) this.nodes.clone();

    String start = getNodesWithoutParents(edges, nodes).get(0);

    ArrayList<String> visited = new ArrayList<String>();
    Stack<String> stack = new Stack<String>();
    ArrayList<String> topologicalOrder = new ArrayList<String>();

    stack.add(start);

    while (!stack.isEmpty()) {
        String x = stack.peek(); 

        if (!visited.contains(x)) {
            visited.add(x);
            ArrayList<String> childrenNodes = getChildrenNodes(getOutgoingEdges(x, edges));
            for (String node : childrenNodes) {
                if (!visited.contains(node))  stack.add(node);
            }
        } else {
            stack.pop(); // Rimuove l'elemento dopo aver visitato tutti i figli
            if (!topologicalOrder.contains(x)) {
                topologicalOrder.add(0,x); // Aggiunge alla pila dell'ordine topologico
            }
        }
    }

    return topologicalOrder;
}
	
	
	
	
	
	
  



	private void createJson()
	{
		try(FileWriter writer = new FileWriter("drawnet/lib/json/Attacks_flow.json")){
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
	
		ArrayList<String> topologicalOrder = this.getTopologicalOrder();

		System.out.println("\n\nTopological Order: "+topologicalOrder);
		System.out.println(topologicalOrder.size());
		
		print("\nAG ---> JSON eseguito\n");
		this.createJson();


		return true;
	}
}
