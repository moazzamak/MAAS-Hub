package com.maas.agents;

import com.maas.ui.OutwardQueGUI;

import jade.core.Agent;

public class OrderCollectorAgent extends Agent {
	protected void setup(){
		OutwardQueGUI.getInstance().setVisible();
	}
}
