package com.tekrarbot;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * Hedef oyuncuyu canli olarak takip eder.
 * - Kafa hedefe kilitlenmez: yumusak doner, hafif sallanir, ara sira etrafa bakar.
 * - Sen fareyi oynatirsan, bir tusa (W/A/S/D/bosluk/shift...) veya fareye tiklarsan,
 *   ya da bir menu acarsan mod kontrolu birakir. Hicbir sey yapmadan 4 sn gecince
 *   takibe kendi devam eder.
 */
public final class Follower {

	// Panelden degistirilen ayarlar (panel baska thread'de oldugu icin volatile)
	public volatile String targetName = "";
	public volatile double followDistance = 2.0; // blok
	public volatile boolean enabled = false;

	// Panelin durum etiketi icin
	public volatile boolean targetVisible = false;
	public volatile boolean inWorld = false;

	private static final int IDLE_TICKS = 80; // kullanici bir sey yapinca 4 sn birak

	// Bizim son yazdigimiz tus durumlari (kullanici mu degistirdi anlamak icin)
	private final Map<KeyMapping, Boolean> expected = new IdentityHashMap<>();

	// Kafa durumu
	private boolean haveLast = false;
	private float lastYaw;
	private float lastPitch;
	private int idleTicks = 0;

	private int glanceTicks = 0;
	private float glanceYaw = 0f;
	private float glancePitch = 0f;
	private int nextGlance = 100;

	private int wobbleTicks = 0;
	private float wobbleYaw = 0f;
	private float wobblePitch = 0f;

	public void tick(Minecraft mc) {
		LocalPlayer me = mc.player;
		inWorld = me != null && mc.level != null;
		if (!inWorld) {
			resetState();
			targetVisible = false;
			return;
		}
		if (!enabled || targetName.isBlank()) {
			release();
			targetVisible = false;
			return;
		}

		Player target = find(mc, me);
		targetVisible = target != null;
		if (target == null) {
			release();
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
		ThreadLocalRandom rnd = ThreadLocalRandom.current();

		float curYaw = me.getYRot();
		float curPitch = me.getXRot();

		// 1) Kullanici bir sey yapiyor mu? (fare hareketi, tus, tiklama, menu)
		boolean headMoved = haveLast
			&& (Math.abs(Mth.wrapDegrees(curYaw - lastYaw)) > 0.05f
				|| Math.abs(curPitch - lastPitch) > 0.05f);
		boolean keyInput = mc.screen != null || userKeyInput(o);
		if (headMoved || keyInput) {
			if (idleTicks == 0) releaseOurKeys();
			idleTicks = IDLE_TICKS;
		}
		if (idleTicks > 0) {
			// Kontrol sende: hicbir tusa veya kafaya dokunma
			idleTicks--;
			expected.clear();
			glanceTicks = 0;
			haveLast = true;
			lastYaw = curYaw;
			lastPitch = curPitch;
			return;
		}

		// 2) Mesafe ve hedefe yon
		double dx = target.getX() - me.getX();
		double dy = target.getY() - me.getY();
		double dz = target.getZ() - me.getZ();
		double dist = Math.hypot(dx, dz);
		double want = followDistance;
		boolean walk = dist > want + 0.4;

		float toTargetYaw = dist > 0.01
			? (float) Math.toDegrees(Math.atan2(-dx, dz))
			: curYaw;

		// 3) Dogal kafa hareketi
		if (--wobbleTicks <= 0) {
			wobbleTicks = 15 + rnd.nextInt(30);
			wobbleYaw = (float) rnd.nextDouble(-4.0, 4.0);
			wobblePitch = (float) rnd.nextDouble(-3.0, 3.0);
		}

		if (glanceTicks > 0) {
			glanceTicks--;
		} else if (!walk && --nextGlance <= 0) {
			glanceTicks = 15 + rnd.nextInt(20);
			float side = rnd.nextBoolean() ? 1f : -1f;
			glanceYaw = side * (float) rnd.nextDouble(35.0, 70.0);
			glancePitch = (float) rnd.nextDouble(-10.0, 15.0);
			nextGlance = 80 + rnd.nextInt(140);
		}
		boolean glancing = glanceTicks > 0 && !walk;

		float desiredYaw;
		float desiredPitch;
		if (glancing) {
			desiredYaw = toTargetYaw + glanceYaw;
			desiredPitch = glancePitch;
		} else if (walk) {
			desiredYaw = toTargetYaw + wobbleYaw * 0.5f;
			desiredPitch = 6f + wobblePitch;
		} else {
			double eyeDy = target.getEyeY() - me.getEyeY();
			float eyePitch = (float) -Math.toDegrees(Math.atan2(eyeDy, Math.max(dist, 0.5)));
			desiredYaw = toTargetYaw + wobbleYaw;
			desiredPitch = eyePitch + wobblePitch * 0.5f;
		}

		float yawErr = Mth.wrapDegrees(desiredYaw - curYaw);
		float yawStep = Mth.clamp(yawErr * 0.18f, -14f, 14f);
		float pitchErr = desiredPitch - curPitch;
		float pitchStep = Mth.clamp(pitchErr * 0.15f, -6f, 6f);

		me.setYRot(curYaw + yawStep);
		me.setXRot(Mth.clamp(curPitch + pitchStep, -90f, 90f));
		haveLast = true;
		lastYaw = me.getYRot();
		lastPitch = me.getXRot();

		// 4) Yuruyus: sadece yaklasik hedefe donukken ileri git (yan yan yurume)
		float err = Math.abs(Mth.wrapDegrees(toTargetYaw - me.getYRot()));
		boolean forward = walk && err < 50f;
		press(o.keyUp, forward);
		press(o.keyDown, false);
		press(o.keyLeft, false);
		press(o.keyRight, false);
		press(o.keySprint, forward && dist > want + 4.0);
		press(o.keyShift, false);

		// Hedef daha yuksekteyse veya onumuzde engel varsa zipla
		boolean blocked = walk && me.horizontalCollision;
		press(o.keyJump, me.onGround() && (dy > 0.5 || blocked));
	}

	/** Tusu ayarla ve ne yazdigimizi hatirla (kullanici mudahalesini anlamak icin). */
	private void press(KeyMapping key, boolean down) {
		key.setDown(down);
		expected.put(key, down);
	}

	/** Kullanici bir tusa bastiysa veya tikladiysa true. */
	private boolean userKeyInput(Options o) {
		// Bizim yazdigimizdan farkli bir tus durumu = kullanici dokundu
		for (Map.Entry<KeyMapping, Boolean> e : expected.entrySet()) {
			if (e.getKey().isDown() != e.getValue()) return true;
		}
		// Fare tiklamasi (vurma/kirma, kullanma/koyma)
		if (o.keyAttack.isDown() || o.keyUse.isDown()) return true;
		// Kontrol sendeyken hareket tuslarini sen tutuyorsan suresi uzasin
		if (idleTicks > 0) {
			return o.keyUp.isDown() || o.keyDown.isDown() || o.keyLeft.isDown()
				|| o.keyRight.isDown() || o.keyJump.isDown() || o.keyShift.isDown()
				|| o.keySprint.isDown();
		}
		return false;
	}

	/** Kontrol sana gecerken, bizim basili biraktigimiz tuslari birak. */
	private void releaseOurKeys() {
		for (Map.Entry<KeyMapping, Boolean> e : expected.entrySet()) {
			if (e.getValue() && e.getKey().isDown()) e.getKey().setDown(false);
		}
		expected.clear();
	}

	/** Takip durunca (kapatildi / hedef yok) bizim bastigimiz tuslari birak. */
	private void release() {
		for (KeyMapping key : expected.keySet()) {
			key.setDown(false);
		}
		resetState();
	}

	private void resetState() {
		expected.clear();
		haveLast = false;
		idleTicks = 0;
		glanceTicks = 0;
	}
}
