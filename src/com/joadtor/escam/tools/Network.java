package com.joadtor.escam.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class Network implements IAction {
	
	
	public Layer input, output;
	public ArrayList<IAction> actionList;
	
	/// <summary>
    /// Creates a empty network
    /// </summary>
    public Network() {
        actionList = new ArrayList<IAction>();
        input = output = null;
    }

    /// <summary>
    /// Load a net from a file
    /// </summary>
    /// <param name="file">File Network route</param>
    public Network(String file) {
    	
    	this.actionList = new ArrayList<IAction>();
               
    	BufferedReader reader = null;
    	
        try {
        	
			reader = new BufferedReader(new FileReader(file));
	        this.readfromFile(reader);
		}
        catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}        
    }
    
    public Network(Network n) {
        
        this.actionList = new ArrayList<IAction>();
        for(int i = 0; i < n.actionList.size(); i++) {
        	
        	IAction ac = n.actionList.get(i);
        	
        	if(ac instanceof Layer) {
            	
                Layer acLayer = (Layer)ac;
                Layer aux = new Layer(acLayer);
                
                this.actionList.add(aux);

                if (acLayer == n.input) {
                	
                    this.input = aux;
                }
                else if (acLayer == n.output)
                {

                    this.output = aux;
                }
            }
            else {
            	
                this.actionList.add(ac);
            }

        }

    }
    
    public static Network fromDescription(String description)
    {
        Network network = new Network();

        String[] desc = description.split(" ");

        int idx = 0;
        Layer ant = null;

        while (idx < desc.length)
        {
            int nNeurons = Integer.valueOf(desc[idx++]);
            String sAct = desc[idx++];
            ActivationFunction fAct = Layer.stringToActivation(sAct);

            if (ant == null)
            {
                network.input = new Layer(nNeurons, fAct);
                ant = network.input;
                continue;
            }

            Layer newLayer = new Layer(nNeurons, fAct);

            Connections newConnection = new Connections(ant, newLayer);
            network.actionList.add(newConnection);

            network.actionList.add(newLayer);

            ant = newLayer;


        }

        network.output = ant;
        return network;

    }
    
    /// <summary>
    /// Load a net from a streamreader (file)
    /// </summary>
    /// <param name="stream">Stream file reader correctly open</param>
    public Network(BufferedReader stream)
    {
    	this.actionList = new ArrayList<IAction>();
        this.readfromFile(stream);

    }
    
    /// <summary>
    /// Add a layer from a determined vector
    /// </summary>
    /// <param name="v">Vector with the values of the layer</param>
    /// <param name="activate">The activation function</param>
    /// <returns></returns>
    public Layer addLayer(double[] v, ActivationFunction activate)
    {
        Layer layer = new Layer(v, activate);
        
        actionList.add(layer);

        return layer;
    }
    
    /// <summary>
    /// Add a layer with a given input neurons
    /// </summary>
    /// <param name="nNeurons">Number of neurons of the function</param>
    /// <param name="activate">Activation Function</param>
    /// <returns></returns>
    public Layer addLayer(int nNeurons, ActivationFunction activate)
    {
        Layer layer = new Layer(nNeurons, activate);
        
        actionList.add(layer);

        return layer;
    }
    
    /// <summary>
    /// Parser of the lua format
    /// </summary>
    /// <param name="reader">Correctly open stream file reader</param>
    public void readfromFile(BufferedReader reader) {

    	/* return {
           "256 inputs 1024 tanh 10 softmax",
           matrix.fromString[[273418
            ascii
    	 */
    	String line;

    	//Avoid comments and lua lines
    	try{
    		do {

    			line = reader.readLine().trim();

    		} while (line.startsWith("#") || line.startsWith("return"));


    		String description = line;

    		description = description.replace("\"", "");

    		String[] camps = description.split(" ");
    		int ncamps = camps.length;
    		// Read the Network info
    		// Nneurons0 tipe0 .. NneuronsN tipeN

    		Layer ant = null;

    		//Avoid lua code
//    		do {
//
//    			line = reader.readLine().trim(); 
//
//    		} while (line.startsWith("#") || line.startsWith("matrix")
//    				|| line.startsWith("[[") || line.startsWith(",")
//    				);
    		// Read two lines


    		for (int i = 0; i < ncamps; i+=2) {

    			int nNeurons = Integer.valueOf(camps[i]);
    			String type = camps[i + 1];
    			type.replace("\n","");
    			if (ant == null) {
    				// Is the first layer
    				this.input = addLayer(nNeurons, Layer.stringToActivation(type));
    				ant = this.input;
    				continue;
    			}
        		reader.readLine();
        		reader.readLine();
    			Layer newLayer = new Layer(nNeurons, Layer.stringToActivation(type));

    			Connections newConnection = new Connections(ant, newLayer);
    			this.actionList.add(newConnection);

    			this.actionList.add(newLayer);

    			newConnection.readWeightsfromStream(reader);

    			ant = newLayer;
    		}

    		if (ant != null) this.output = ant;
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    /// <summary>
    /// Returns the output layer. The computed values.
    /// </summary>
    /// <returns>Returns a double vector with the output values</returns>
    public double []getOutput()
    {

        return this.output.weights;
    }
    
    public int getMaxOutputLabel(){

        double max = Double.MIN_VALUE;
        int index = -1;
        
        for (int i = 0; i < this.getNOutputNeurons(); i++) {
            if (this.output.weights[i] > max) {
            	
                max = this.output.weights[i];
                index = i;
            }
        }

        return index;
    }
    
    /// <summary>
    /// Method that computes the feedforward action along the ActionList
    /// </summary>
	@Override
	public void doFeedForward() {
		
		for(int i = 0 ; i < this.actionList.size() ; i++) {
            IAction actionItem = this.actionList.get(i);
            actionItem.doFeedForward();
        }
	}

	/// <summary>
    /// Returns the number of the input neuron of the network
    /// </summary>
    /// <returns>Returns the number of the input neuron of the network</returns>
	@Override
	public int getNInputNeurons() {
		
		if (this.input == null) {
            return 0;
        }
        else return this.input.nNeurons;
	}

	/// <summary>
    /// Returns the number of the neurons on the output layer
    /// </summary>
    /// <returns>Returns the number of the neurons on the output layer</returns>
	@Override
	public int getNOutputNeurons() {

        if (this.output == null) {
            return 0;
        }
        else return this.output.nNeurons;
	}

	/// <summary>
    /// Specific methods that compute the feedforward on a net given a determined input
    /// </summary>
    /// <param name="v">The desired input of the network</param>
    /// <returns>The Output value of the network</returns>
    public double[] computeNetwork(double []v) {

        if (v.length != this.input.nNeurons){

            return null;

        }

        this.input.weights = v;

        this.doFeedForward();

        return this.output.weights;

    }
}
