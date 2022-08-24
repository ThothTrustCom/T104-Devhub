package org.thothtrust.sc.t104.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.thothtrust.sc.t104.Constants;
import org.thothtrust.sc.t104.api.ManagementTokenAPI;
import org.thothtrust.sc.t104.exceptions.InvalidSizeException;

public class PinChangePanel extends JPanel {

	private JPanel currRetriesPane = null;
	private JPanel changePasswordContentPane = null;
	private JPanel changePasswordBtnPane = null;
	private JLabel currRetryLbl = null;
	private JLabel minRetryLbl = null;
	private JLabel maxRetryLbl = null;
	private JButton resetPasswordBtn = null;
	private JLabel changePwdLbl = null;
	private JLabel changePwdLbl1 = null;
	private JPasswordField changePTF = null;
	private JPasswordField changePTF1 = null;
	private GridBagConstraints c = null;

	public PinChangePanel() {
		initComponents();
	}

	public void initComponents() {
		setLayout(new BorderLayout());
		changePasswordContentPane = new JPanel(new GridBagLayout());
		changePasswordBtnPane = new JPanel(new FlowLayout());
		changePasswordContentPane.setMaximumSize(new Dimension(300, 300));
		currRetryLbl = new JLabel("Retries: ");
		minRetryLbl = new JLabel("5");
		maxRetryLbl = new JLabel("5");
		resetPasswordBtn = new JButton("Reset Password");
		changePwdLbl = new JLabel("Enter New Password:  ");
		changePwdLbl1 = new JLabel("Re-Enter Password: ");
		changePTF = new JPasswordField(15);
		changePTF1 = new JPasswordField(15);
		currRetriesPane = new JPanel(new FlowLayout(FlowLayout.LEADING));
		currRetriesPane.add(minRetryLbl);
		currRetriesPane.add(new JLabel(" / "));
		currRetriesPane.add(maxRetryLbl);
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(20, 0, 5, 0);
		changePasswordContentPane.add(currRetryLbl, c);
		c.gridx = 1;
		c.gridy = 0;
		c.anchor = GridBagConstraints.CENTER;
		changePasswordContentPane.add(currRetriesPane, c);
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 0, 5, 0);
		changePasswordContentPane.add(changePwdLbl, c);
		c.gridx = 1;
		c.gridy = 1;
		changePasswordContentPane.add(changePTF, c);
		c.gridx = 0;
		c.gridy = 2;
		changePasswordContentPane.add(changePwdLbl1, c);
		c.gridx = 1;
		c.gridy = 2;
		changePasswordContentPane.add(changePTF1, c);
		JPanel spacerPanel = new JPanel(new BorderLayout());
		c.gridx = 0;
		c.gridy = 2;
		changePasswordContentPane.add(spacerPanel, c);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		changePasswordBtnPane.add(resetPasswordBtn);
		changePasswordContentPane.add(changePasswordBtnPane, c);
		add(changePasswordContentPane, BorderLayout.NORTH);

		try {
			int tries = ManagementTokenAPI.getTriesRemaining(Constants.USER_ADMIN);
			minRetryLbl.setText("" + tries);
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | ShortBufferException
				| IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException
				| InvalidParameterSpecException | InvalidKeySpecException | CardException | InvalidSizeException e) {
			e.printStackTrace();
		}

		resetPasswordBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				char[] password = changePTF.getPassword();
				char[] password1 = changePTF1.getPassword();
				doPasswordUpdate(password, password1, Constants.USER_ADMIN);
				clearPasswordCharArray(password);
				clearPasswordCharArray(password1);
				password = null;
				password1 = null;
				changePTF.setText("");
				changePTF1.setText("");
				try {
					int tries = ManagementTokenAPI.getTriesRemaining(Constants.USER_ADMIN);
					minRetryLbl.setText("" + tries);
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | ShortBufferException
						| IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException
						| InvalidParameterSpecException | InvalidKeySpecException | CardException
						| InvalidSizeException e1) {
					e1.printStackTrace();
				}
			}

		});
	}

	private void doPasswordUpdate(char[] password, char[] password1, byte userID) {
		if (password.length == 0) {
			JOptionPane.showMessageDialog(null, "Please enter a valid password.", "Invalid Password",
					JOptionPane.WARNING_MESSAGE);
		} else {
			if (password.length == password1.length) {
				for (int i = 0; i < password.length; i++) {
					if (password[i] != password1[i]) {
						JOptionPane.showMessageDialog(null, "Passwords for update operation are not the same.",
								"Password Update Failed", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			} else {
				JOptionPane.showMessageDialog(null, "Passwords for update operation are not the same.",
						"Password Update Failed", JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				if (ManagementTokenAPI.updatePin(userID, password)) {
					JOptionPane.showMessageDialog(null, "Password update operation is successful.",
							"Password Update Successful", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(null, "Password update operation failed.", "Password Update Failed",
							JOptionPane.ERROR_MESSAGE);
				}
			} catch (CardException | InvalidKeyException | HeadlessException | NoSuchAlgorithmException
					| NoSuchPaddingException | ShortBufferException | IllegalBlockSizeException | BadPaddingException
					| InvalidAlgorithmParameterException | InvalidSizeException | InvalidParameterSpecException
					| InvalidKeySpecException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void clearPasswordCharArray(char[] pwdArray) {
		for (int i = 0; i < pwdArray.length; i++) {
			pwdArray[i] = 0;
		}
	}
}
