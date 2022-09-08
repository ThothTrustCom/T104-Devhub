package org.thothtrust.sc.t104.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.smartcardio.CardException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;

import org.thothtrust.sc.t104.api.ManagementTokenAPI;
import org.thothtrust.sc.t104.exceptions.InvalidSizeException;

public class CardSettingsPanel extends JPanel {

	private TitledBorder titledBorder = null;
	private JPanel timeSettingsPane = null;
	private JLabel timeoutTimeLbl = null;
	private JTextField timeoutTimeTf = null;
	private JButton timeoutUpdateBtn = null;
	private JToggleButton timeoutEnableBtn = null;
	private GridBagConstraints c = null;
	private short timeout = -1;

	public CardSettingsPanel() {
		initComponents();
	}

	public void initComponents() {
		setLayout(new BorderLayout());
		timeSettingsPane = new JPanel(new GridBagLayout());
		timeoutTimeLbl = new JLabel("Timeout (sec): ");
		timeoutTimeTf = new JTextField(15);
		timeoutUpdateBtn = new JButton("Update");
		timeoutEnableBtn = new JToggleButton();

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(10, 0, 20, 0);
		timeSettingsPane.add(timeoutEnableBtn, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.insets = new Insets(0, 0, 270, 0);
		timeSettingsPane.add(timeoutTimeLbl, c);
		c.gridx = 1;
		c.gridy = 1;
		c.insets = new Insets(0, 10, 270, 0);
		timeSettingsPane.add(timeoutTimeTf, c);
		c.gridx = 2;
		c.gridy = 1;
		timeSettingsPane.add(timeoutUpdateBtn, c);
		add(timeSettingsPane, BorderLayout.CENTER);

		try {
			timeout = ManagementTokenAPI.getTimeoutTime();
			timeoutTimeTf.setText("" + timeout);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | ShortBufferException
				| IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException
				| InvalidParameterSpecException | InvalidKeySpecException | CardException | InvalidSizeException e) {
			e.printStackTrace();
		}

		setTimeoutEnableToggleBtn(isTimeoutEnable());

		titledBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
				"Card Timeout Management");
		titledBorder.setTitleJustification(TitledBorder.CENTER);
		timeSettingsPane.setBorder(titledBorder);

		timeoutEnableBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (timeoutEnableBtn.isSelected()) {
					setTimeoutEnableToggleBtn(true);
				} else {
					try {
						if (ManagementTokenAPI.setTimeoutTime((short) 0xFFFF)) {
							timeout = ManagementTokenAPI.getTimeoutTime();
							setTimeoutEnableToggleBtn(isTimeoutEnable());
						} else {
							JOptionPane.showMessageDialog(null, "Failed to toggle card timeout on/off setting !",
									"Failed Timeout Update", JOptionPane.ERROR_MESSAGE);
						}
					} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
							| ShortBufferException | IllegalBlockSizeException | BadPaddingException
							| InvalidAlgorithmParameterException | InvalidParameterSpecException
							| InvalidKeySpecException | CardException | InvalidSizeException e1) {
						e1.printStackTrace();
					}
				}
				validate();
				repaint();
			}
		});

		timeoutUpdateBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateTimeoutTimer();
			}
		});

		timeoutTimeTf.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					updateTimeoutTimer();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
	}

	public void updateCardSettingsStatus() {
		timeoutTimeTf.setText("" + timeout);
		setTimeoutEnableToggleBtn(isTimeoutEnable());
	}

	private boolean isTimeoutEnable() {
		if (timeout <= 0) {
			return false;
		} else {
			return true;
		}
	}

	private void setTimeoutEnableToggleBtn(boolean isEnabled) {
		if (isEnabled) {
			timeoutEnableBtn.setText("Disable Timeout");
			timeoutEnableBtn.setForeground(Color.RED);
			timeoutEnableBtn.setToolTipText("Press to disable card timeout");
			timeoutTimeTf.setEnabled(true);
			timeoutUpdateBtn.setEnabled(true);
			timeoutEnableBtn.setSelected(true);
		} else {
			timeoutEnableBtn.setText("Enable Timeout");
			timeoutEnableBtn.setForeground(Color.GREEN);
			timeoutEnableBtn.setToolTipText("Press to enable card timeout");
			timeoutTimeTf.setEnabled(false);
			timeoutUpdateBtn.setEnabled(false);
			timeoutEnableBtn.setSelected(false);
		}
	}

	private void updateTimeoutTimer() {
		String timeInput = timeoutTimeTf.getText();
		try {
			short timeoutWanted = Short.valueOf(timeInput);
			if (timeoutWanted >= 10 && timeoutWanted <= 65534) {
				try {
					if (ManagementTokenAPI.setTimeoutTime(timeoutWanted)) {
						timeout = ManagementTokenAPI.getTimeoutTime();
						setTimeoutEnableToggleBtn(isTimeoutEnable());
						JOptionPane.showMessageDialog(null,
								"Card timeout timer has been updated to " + timeout + " seconds.",
								"Timeout Update Successful", JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(null, "Failed to update card timeout timing !",
								"Failed Timeout Update", JOptionPane.ERROR_MESSAGE);
					}
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | ShortBufferException
						| IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException
						| InvalidParameterSpecException | InvalidKeySpecException | CardException
						| InvalidSizeException e1) {
					e1.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(null, "Timeout timing must be between 10 to 65534 seconds !",
						"Invalid Input Format", JOptionPane.ERROR_MESSAGE);
			}
		} catch (NumberFormatException e2) {
			JOptionPane.showMessageDialog(null, "Timeout timing must be a positive non-decimal number !",
					"Invalid Input Format", JOptionPane.ERROR_MESSAGE);
		}
	}

}