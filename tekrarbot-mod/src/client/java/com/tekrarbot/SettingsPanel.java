package com.tekrarbot;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/** Oyunun disinda, ayri bir pencerede acilan kucuk ayar paneli. */
public final class SettingsPanel {

	private SettingsPanel() {}

	public static void open(Follower follower) {
		SwingUtilities.invokeLater(() -> build(follower));
	}

	private static void build(Follower follower) {
		JFrame frame = new JFrame("Takip Paneli");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // pencere kapaninca mod durmasin
		frame.setAlwaysOnTop(true);

		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(4, 8, 4, 8);
		c.fill = GridBagConstraints.HORIZONTAL;

		JTextField target = new JTextField(follower.targetName, 14);
		JSpinner distance = new JSpinner(new SpinnerNumberModel(follower.followDistance, 1.0, 10.0, 0.5));
		JButton toggle = new JButton("Baslat");
		JLabel status = new JLabel("Durum: kapali");

		c.gridx = 0; c.gridy = 0; p.add(new JLabel("Takip edilecek oyuncu:"), c);
		c.gridx = 1; p.add(target, c);
		c.gridx = 0; c.gridy = 1; p.add(new JLabel("Mesafe (blok):"), c);
		c.gridx = 1; p.add(distance, c);
		c.gridx = 0; c.gridy = 2; c.gridwidth = 2; p.add(toggle, c);
		c.gridy = 3; p.add(status, c);

		Runnable applySettings = () -> {
			follower.targetName = target.getText().trim();
			follower.followDistance = ((Number) distance.getValue()).doubleValue();
		};

		// Ayarlar degisince hemen uygula
		target.addActionListener(e -> applySettings.run());
		target.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override public void focusLost(java.awt.event.FocusEvent e) { applySettings.run(); }
		});
		distance.addChangeListener(e -> applySettings.run());

		toggle.addActionListener(e -> {
			applySettings.run();
			follower.enabled = !follower.enabled;
			toggle.setText(follower.enabled ? "Durdur" : "Baslat");
		});

		// Durum etiketini periyodik guncelle
		new Timer(500, e -> {
			String s;
			if (!follower.inWorld) s = "oyunda degil";
			else if (!follower.enabled) s = "kapali";
			else if (follower.targetName.isBlank()) s = "oyuncu adi yaz";
			else if (follower.targetVisible) s = "takip ediyor";
			else s = "oyuncu yakinda degil, bekliyor";
			status.setText("Durum: " + s);
		}).start();

		frame.setContentPane(p);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}
}
