package com.tekrarbot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Hedef oyuncuyu canli olarak takip eder: yaklasik "followDistance" blok
 * mesafede durur, uzaklasirsa yuruyup yetisir.
 */
public final class Follower {

	// Panelden degistirilen ayarlar (panel baska thread'de oldugu icin volatile)
	public volatile String targetName = "";
	public volatile double followDistance = 2.0; // blok
	public volatile boolean enabled = false;

	// Panelin durum etiketi icin
	public volatile boolean targetVisible = false;
	public volatile boolean inWorld = false;

	private boolean driving = false;

	public void tick(Minecraft mc) {
		LocalPlayer me = mc.player;
		inWorld = me != null && mc.level != null;
		if (!inWorld) {
			driving = false;
			targetVisible = false;
			return;
		}
		if (!enabled || targetName.isBlank()) {
			release(mc);
			targetVisible = false;
			return;
		}

		Player target = find(mc, me);
		targetVisible = target != null;
		if (target == null) {
			release(mc);
			return;
		}
		follow(mc, me, target);
	}

	private Player find(Minecraft mc, LocalPlayer me) {
		String wanted = targetName.trim();
		for (Player p : mc.level.players()) {
			if (p == me) continue;
			if (p.getName().getString().equalsIgnoreCase(wanted)) return p;
		}
		return null; // hedef yakinda degil / yuklenmemis
	}

	private void follow(Minecraft mc, LocalPlayer me, Player target) {
		Options o = mc.options;

		double dx = target.getX() - me.getX();
		double dy = target.getY() - me.getY();
		double dz = target.getZ() - me.getZ();
		double dist = Math.hypot(dx, dz);
		double want = followDistance;

		// Hedefe dogru bak
		if (dist > 0.01) {
			me.setYRot((float) Math.toDegrees(Math.atan2(-dx, dz)));
			me.setXRot(0.0f);
		}

		// Mesafe ayarlanan degerden buyukse yuru, yakinsa dur
		boolean walk = dist > want + 0.4;
		o.keyUp.setDown(walk);
		o.keyDown.setDown(false);
		o.keyLeft.setDown(false);
		o.keyRight.setDown(false);
		o.keySprint.setDown(walk && dist > want + 4.0);
		o.keyShift.setDown(false);

		// Hedef daha yuksekteyse veya onumuzde engel varsa zipla
		boolean blocked = walk && me.horizontalCollision;
		o.keyJump.setDown(me.onGround() && (dy > 0.5 || blocked));
		driving = true;
	}

	/** Bizim bastigimiz tuslari birak (sadece tuslari biz surduysek). */
	private void release(Minecraft mc) {
		if (!driving) return;
		Options o = mc.options;
		o.keyUp.setDown(false);
		o.keyDown.setDown(false);
		o.keyLeft.setDown(false);
		o.keyRight.setDown(false);
		o.keySprint.setDown(false);
		o.keyShift.setDown(false);
		o.keyJump.setDown(false);
		driving = false;
	}
}
