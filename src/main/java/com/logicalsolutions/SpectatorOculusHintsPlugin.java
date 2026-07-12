package com.logicalsolutions;

import com.google.inject.Provides;
import java.util.Arrays;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Spectator Oculus Hints",
	description = "Hides top-left hover text and restores overhead player names while using spectator oculus views.",
	tags = {"spectator", "oculus", "menu", "tooltip", "players", "names"}
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

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private SpectatorOculusHintsOverlay overlay;

	private boolean hidingHints;
	private int previousOculusHideHint;
	private int previousAllstarOculusHideHint;
	private MenuEntry[] savedMenuEntries;
	private String[] savedOptions;
	private String[] savedTargets;

	@Provides
	SpectatorOculusHintsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(SpectatorOculusHintsConfig.class);
	}

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		log.debug("Spectator Oculus Hints started");
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		clearSavedMenu();
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
			clearSavedMenu();
			return;
		}

		MenuEntry[] menuEntries = client.getMenuEntries();
		if (menuEntries.length == 0)
		{
			clearSavedMenu();
			return;
		}

		MenuEntry topEntry = menuEntries[menuEntries.length - 1];
		boolean hideWidgetHover = hasHiddenWidgetEntry(menuEntries);
		boolean hideWalkHere = topEntry.getType() == MenuAction.WALK && !hasPlayerEntry(menuEntries);
		if (!hideWidgetHover && !hideWalkHere)
		{
			clearSavedMenu();
			return;
		}

		saveMenu(menuEntries);
		topEntry.setOption("");
		topEntry.setTarget("");
		client.setMenuEntries(new MenuEntry[] {topEntry});
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		if (!isSpectatorOculusActive() || savedMenuEntries == null)
		{
			return;
		}

		restoreSavedMenuText();
		event.setMenuEntries(savedMenuEntries);
		client.setMenuEntries(savedMenuEntries);
		clearSavedMenu();
	}

	boolean isSpectatorOculusActive()
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

	private void saveMenu(MenuEntry[] menuEntries)
	{
		savedMenuEntries = Arrays.copyOf(menuEntries, menuEntries.length);
		savedOptions = new String[menuEntries.length];
		savedTargets = new String[menuEntries.length];

		for (int i = 0; i < menuEntries.length; i++)
		{
			savedOptions[i] = menuEntries[i].getOption();
			savedTargets[i] = menuEntries[i].getTarget();
		}
	}

	private void restoreSavedMenuText()
	{
		for (int i = 0; i < savedMenuEntries.length; i++)
		{
			savedMenuEntries[i].setOption(savedOptions[i]);
			savedMenuEntries[i].setTarget(savedTargets[i]);
		}
	}

	private void clearSavedMenu()
	{
		savedMenuEntries = null;
		savedOptions = null;
		savedTargets = null;
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
