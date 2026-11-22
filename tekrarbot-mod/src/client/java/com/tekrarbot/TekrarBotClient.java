package com.tekrarbot;

import java.awt.GraphicsEnvironment;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class TekrarBotClient implements ClientModInitializer {

	private static final Follower FOLLOWER = new Follower();

	@Override
	public void onInitializeClient() {
		// Her istemci tikinin basinda hareketi uygula
		ClientTickEvents.START_CLIENT_TICK.register(FOLLOWER::tick);

		// Ayarlar oyun icinden degil, ayri bir pencerede (panel) yapilir
		System.setProperty("java.awt.headless", "false");
		if (GraphicsEnvironment.isHeadless()) {
			System.err.println("[Tekrar] Bu sistemde pencere acilamiyor (headless).");
			return;
		}
		SettingsPanel.open(FOLLOWER);
	}
}
