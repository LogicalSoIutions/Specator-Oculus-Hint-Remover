package com.logicalsolutions;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SpectatorOculusHintsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SpectatorOculusHintsPlugin.class);
		RuneLite.main(args);
	}
}
