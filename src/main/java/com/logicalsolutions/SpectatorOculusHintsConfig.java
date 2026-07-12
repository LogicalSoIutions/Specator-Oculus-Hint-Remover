package com.logicalsolutions;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(SpectatorOculusHintsConfig.GROUP)
public interface SpectatorOculusHintsConfig extends Config
{
	String GROUP = "spectator-oculus-hints";

	@ConfigItem(
		position = 0,
		keyName = "renderPlayerNames",
		name = "Render player names",
		description = "Render overhead player names while using spectator oculus views."
	)
	default boolean renderPlayerNames()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "playerNameColor",
		name = "Player name color",
		description = "Color of overhead player names rendered while using spectator oculus views."
	)
	default Color playerNameColor()
	{
		return Color.WHITE;
	}
}
