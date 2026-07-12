package com.logicalsolutions;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.util.Text;

@Singleton
class SpectatorOculusHintsOverlay extends Overlay
{
	private static final int ACTOR_OVERHEAD_TEXT_MARGIN = 40;

	private final Client client;
	private final SpectatorOculusHintsConfig config;
	private final SpectatorOculusHintsPlugin plugin;

	@Inject
	private SpectatorOculusHintsOverlay(
		Client client,
		SpectatorOculusHintsConfig config,
		SpectatorOculusHintsPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(PRIORITY_MED);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.renderPlayerNames() || !plugin.isSpectatorOculusActive())
		{
			return null;
		}

		for (Player player : client.getPlayers())
		{
			renderPlayerName(graphics, player);
		}

		return null;
	}

	private void renderPlayerName(Graphics2D graphics, Player player)
	{
		String name = player.getName();
		if (name == null)
		{
			return;
		}

		name = Text.sanitize(name);
		Point textLocation = player.getCanvasTextLocation(
			graphics,
			name,
			player.getLogicalHeight() + ACTOR_OVERHEAD_TEXT_MARGIN);

		if (textLocation != null)
		{
			OverlayUtil.renderTextLocation(graphics, textLocation, name, config.playerNameColor());
		}
	}
}
