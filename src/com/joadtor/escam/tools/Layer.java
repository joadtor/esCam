package com.joadtor.escam.tools;


enum ActivationFunction {
	
    IDENTITY,
    LOGISTIC,
    SOFTMAX,
    TANH
}

public class Layer implements IAction {
	
	public int nNeurons;
	public double[] weights;
	
	public ActivationFunction act_func;
	
    /// <summary>
    /// Gets a number of nNeurons, and creates the layer
    /// </summary>
    /// <param name="nNeurons">Number of neurons of the layer</param>
    /// <param name="act">Activation Function</param>
	public Layer(int nNeurons, ActivationFunction act) {
		
		weights = new double[nNeurons];
		this.nNeurons = nNeurons;
		this.act_func = act;
	}
	
	/// <summary>
    /// Gets a vector and generates the layer
    /// </summary>
    /// <param name="v">Vector with the values of the layer</param>
    /// <param name="act">Activation Function</param>
	public Layer(double[] v, ActivationFunction act) {
		
		this.weights = v;
		this.nNeurons = v.length;
		this.act_func = act;
	}
	
	public Layer(Layer L) {
		
		this.weights = new double[L.nNeurons];
		this.nNeurons = L.nNeurons;
		this.act_func = L.act_func;
	}
	
	
	/// <summary>
    /// An specific action that computes the activation function on the layer values
    /// </summary>
	@Override
	public void doFeedForward() {
		
		if(this.act_func == ActivationFunction.LOGISTIC) {
			
			this.activationLogistic();
		}
		else if(this.act_func == ActivationFunction.SOFTMAX) {
			
			this.activationSoftMax();
		}
		else if(this.act_func == ActivationFunction.TANH) {
			
			this.activationTanH();
		}
		// Nothing if ActivationFunction.IDENTITY
		
	}

	
    // Returns the number of neurons of the layer
	@Override
	public int getNInputNeurons() {
		
		return nNeurons;
	}
	
    // Returns the number neurons of  the layer. The same as the input
	@Override
	public int getNOutputNeurons() {
		
		return nNeurons;
	}

	@Override
	public String toString() {
		
		String s = "";
		
		for (int i = 0; i < this.nNeurons; i++) {
			
			s += this.weights[i];
			s += " ";
		}
		
		return s;
	}
	
	
	/// <summary>
    /// Map function that determines the type of the activation function. 
    /// </summary>
    /// <param name="name">The strings that contains the activation function</param>
    /// <returns>The type of the activation function</returns>
    public static ActivationFunction stringToActivation(String name) {

        name = name.replace(",", "");
        
        if(name.equals("softmax")) {
        	
        	return ActivationFunction.SOFTMAX;
        } 
        else  if(name.equals("tanh")) {
        	
        	return ActivationFunction.TANH;
        }
        else if(name.equals("output") || name.equals("logistic")) {
        	
        	return ActivationFunction.LOGISTIC;
        }
        else {

            return ActivationFunction.IDENTITY;
        }

    }
    
    /// <summary>
    /// Applies the activation soft max on the desired layer
    /// </summary>
    public void activationSoftMax() {

        double sumExp = 0;

        double maxVal = Double.MIN_VALUE;

        for (int i = 0; i < this.nNeurons; i++) {
        	
            if (weights[i] > maxVal) maxVal = weights[i];
        }
   
        for (int i = 0; i < this.nNeurons; i++) {
        	
            this.weights[i] = Math.exp(this.weights[i] - maxVal);
            sumExp += this.weights[i];
        }

        for (int i = 0; i < this.nNeurons; i++) {
        	
            this.weights[i] /= sumExp;
        }
    }
    
    /// <summary>
    /// Applies the activation sigmoid function on the desired layer
    /// </summary>
    public void activationLogistic() {

        for (int i = 0; i < this.nNeurons; i++) {

            this.weights[i] = logistic(this.weights[i]);
        }
    }
    
    /// <summary>
    /// Applies the activation TanH on the desired layer
    /// </summary>
    public void activationTanH() {
    	
        for (int i = 0; i < this.nNeurons; i++)
            this.weights[i] = tanH(this.weights[i]);
    }
    
    /// <summary>
    /// Method that computes the sigmoid
    /// </summary>
    /// <param name="t">Input value</param>
    /// <returns>The logistic value</returns>
    static double logistic(double t) {

        double aux = 1.0 / (1.0 + Math.exp(-t));
        return aux;
    }
    
    /// <summary>
    /// Computes de tanH function
    /// </summary>
    /// <param name="t">The input value</param>
    /// <returns>The TanH computed value</returns>
    static double tanH(double t)
    {
        return (2.0 / (Math.exp(-t) + 1)) - 1;
    }
}
