package com.joadtor.escam.tools;

public interface IAction {
    int getNInputNeurons();
    int getNOutputNeurons();

    void doFeedForward();
}
