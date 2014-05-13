package com.joadtor.escam.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Random;

public class Connections implements IAction {
	
	public Layer source, dest;
	
	// Size of the matrix
	protected int n1, n2;
	
	// Weight matrix
	public double[][] weights;

		/// <summary>
    /// Gets the number of the connection
    /// </summary>
    /// <param name="n1">Number of neurons of the input layer</param>
    /// <param name="n2">Number of neurons of the output layer</param>
	public Connections(int n1, int n2) {
		
		this.n1 = n1;
		this.n2 = n2;
		
		this.weights = new double[n2][n1];
		this.source = null;
		this.dest = null;
	}
	
	/// <summary>
    /// Gets two layers and generate the all_to_all weigth matrix
    /// </summary>
    /// <param name="input">The source layer</param>
    /// <param name="output">The otuput layer</param>
	public Connections(Layer input, Layer output) {
		
		// A network more for the bias
		this.n1 = input.nNeurons + 1;
		this.n2 = output.nNeurons;
		
		this.weights = new double[n2][n1];
		this.source = input;
		this.dest = output;
	}
	
    /// <summary>
    /// Forward action that computes the output values as input*weight
    /// </summary>
	@Override
	public void doFeedForward() {
		
		// output = input*m
		for (int i = 0; i < this.n2; i++){
			
			// Add bias
			this.dest.weights[i] = this.weights[i][0];
			
			for (int j = 1; j < this.n1; j++) {
				
				this.dest.weights[i] += this.source.weights[j-1] * this.weights[i][j];
			}
		}
	}

	/// <summary>
	/// Returns the number of neurons the input layer
	/// </summary>
	/// <returns></returns>
	@Override
	public int getNInputNeurons() {
		
		return n1-1;
	}

    /// <summary>
    /// Returns the number of neurons of the output layer
    /// </summary>
    /// <returns></returns>
	@Override
	public int getNOutputNeurons() {
		
		return n2;
	}

	/// <summary>
    /// Randomize the weights. Good for inizialiation.
    /// </summary>
    /// <param name="min_weight"></param>
    /// <param name="max_weight"></param>
    public void randomizeWeights(double min_weight, double max_weight) {
    	
        Random rnd = new Random();

        for (int i = 0; i < this.n1; i++) {
            for (int j = 0; j < this.n2; ++j) {
            	
                this.weights[i][j] = min_weight + (rnd.nextDouble()/(max_weight-min_weight));
            }
        }
    }
    
    /// <summary>
    /// Take a stream and read the data
    /// </summary>
    /// <param name="file">Corrected opened stream</param>
    public void readWeightsfromStream(BufferedReader file)
    {
    	try {
    		for (int i = 0; i < this.n2; i++) {
    			for (int j = 0; j < this.n1; j++) {

    				char aux;
    				String sWeight = "";
    				do {


    					aux = (char)file.read();


    				} while(!Character.isLetterOrDigit(aux) && aux != '-' && aux != '.');

    				sWeight += aux;

    				do {
    					aux = (char)file.read();
    					sWeight += aux;

    				} while (Character.isLetterOrDigit(aux) || aux == '-' || aux == '.');

    				this.weights[i][j] = Double.valueOf(sWeight.replace(',','.'));
    			}
    		}
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
    }
    
    @Override
	public String toString() {
    	
		String s = "";
		
		for (int i = 0; i < this.n2; i++) {
			for (int j = 0; j < this.n1; j++){
				
				s += this.weights[i][j];
				
			}
			
			s += "\n";
		}
		
		return s;
	}
}
