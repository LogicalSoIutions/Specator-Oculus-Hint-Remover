package com.logicalsolutions;

import java.util.Arrays;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "Spectator Oculus Hints",
	description = "Hides top-left hover text while using spectator oculus views.",
	tags = {"spectator", "oculus", "menu", "tooltip"}
)
public class SpectatorOculusHintsPlugin extends Plugin
{
	private static final int HIDE_HINTS = 1;
	private static final int[] BLOCKED_TYPES = {
		MenuAction.CC_OP.getId(),
		MenuAction.CC_OP_LOW_PRIORITY.getId(),
		MenuAction.WIDGET_FIRST_OPTION.getId(),
		MenuAction.WIDGET_SECOND_OPTION.getId(),
		MenuAction.WIDGET_THIRD_OPTION.getId(),
		MenuAction.WIDGET_FOURTH_OPTION.getId(),
		MenuAction.WIDGET_FIFTH_OPTION.getId()
	};

	static
	{
		Arrays.sort(BLOCKED_TYPES);
	}

	@Inject
	private Client client;

	private boolean hidingHints;
	private int previousOculusHideHint;
	private int previousAllstarOculusHideHint;

	@Override
	protected void startUp()
	{
		log.debug("Spectator Oculus Hints started");
	}

	@Override
	protected void shutDown()
	{
		restoreHints();
		log.debug("Spectator Oculus Hints stopped");
	}

	@Subscribe
	public void onClientTick(ClientTick clientTick)
	{
		if (isSpectatorOculusActive())
		{
			hideHints();
		}
		else
		{
			restoreHints();
		}
	}

	@Subscribe
	public void onPostMenuSort(PostMenuSort event)
	{
		if (!isSpectatorOculusActive())
		{
			return;
		}

		MenuEntry[] menuEntries = client.getMenuEntries();
		if (menuEntries.length == 0)
		{
			return;
		}

		MenuEntry topEntry = menuEntries[menuEntries.length - 1];
		boolean hideWidgetHover = hasHiddenWidgetEntry(menuEntries);
		boolean hideWalkHere = topEntry.getType() == MenuAction.WALK && !hasPlayerEntry(menuEntries);
		if (!hideWidgetHover && !hideWalkHere)
		{
			return;
		}

		topEntry.setOption("");
		topEntry.setTarget("");
		client.setMenuEntries(new MenuEntry[] {topEntry});
	}

	private boolean isSpectatorOculusActive()
	{
		return isVisible(InterfaceID.ToplevelSpectator.VIEWPORT)
			|| isVisible(InterfaceID.DeadmanSpectator.UNIVERSE)
			|| isVisible(InterfaceID.PvpArenaSpectator.UNIVERSE)
			|| isVisible(InterfaceID.AllstarOculus.VIEWPORT);
	}

	private boolean isVisible(int componentId)
	{
		Widget widget = client.getWidget(componentId);
		return widget != null && !widget.isHidden();
	}

	private boolean shouldHide(MenuEntry menuEntry)
	{
		return Arrays.binarySearch(BLOCKED_TYPES, menuEntry.getType().getId()) >= 0
			&& menuEntry.getWidget() != null;
	}

	private boolean hasHiddenWidgetEntry(MenuEntry[] menuEntries)
	{
		for (MenuEntry menuEntry : menuEntries)
		{
			if (shouldHide(menuEntry))
			{
				return true;
			}
		}

		return false;
	}

	private boolean hasPlayerEntry(MenuEntry[] menuEntries)
	{
		for (MenuEntry menuEntry : menuEntries)
		{
			if (menuEntry.getPlayer() != null)
			{
				return true;
			}
		}

		return false;
	}

	private void hideHints()
	{
		if (!hidingHints)
		{
			previousOculusHideHint = client.getVarcIntValue(VarClientID.OCULUS_HIDEHINT);
			previousAllstarOculusHideHint = client.getVarcIntValue(VarClientID.ALLSTAR_OCULUS_HIDEHINT);
			hidingHints = true;
		}

		client.setVarcIntValue(VarClientID.OCULUS_HIDEHINT, HIDE_HINTS);
		client.setVarcIntValue(VarClientID.ALLSTAR_OCULUS_HIDEHINT, HIDE_HINTS);
	}

	private void restoreHints()
	{
		if (!hidingHints)
		{
			return;
		}

		client.setVarcIntValue(VarClientID.OCULUS_HIDEHINT, previousOculusHideHint);
		client.setVarcIntValue(VarClientID.ALLSTAR_OCULUS_HIDEHINT, previousAllstarOculusHideHint);
		hidingHints = false;
	}
}
