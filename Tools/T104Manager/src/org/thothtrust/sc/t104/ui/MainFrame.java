package org.thothtrust.sc.t104.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.RoundingMode;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.text.DecimalFormat;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.thothtrust.sc.t104.Constants;
import org.thothtrust.sc.t104.api.ManagementTokenAPI;
import org.thothtrust.sc.t104.exceptions.InvalidSizeException;
import org.thothtrust.sc.t104.sc.APDUResult;
import org.thothtrust.sc.t104.sc.Device;
import org.thothtrust.sc.t104.sc.DeviceManager;
import org.thothtrust.sc.t104.util.BinUtils;

public class MainFrame extends JFrame {

	private JPanel loginPane = null;
	private JPanel passPane = null;
	private JPanel loginBtnPane = null;
	private JLabel loginLbl = null;
	private JPasswordField pwdTf = null;
	private JButton loginBtn = null;
	private JPanel mainPane = null;
	private JTabbedPane mainTabPane = null;
	private GridBagConstraints c = null;
	private Device dev = null;
	private PinChangePanel tp1 = null;
	private EWalletManagementPanel tp2  = null;
	private CardSettingsPanel tp3  = null;		
	public static ImageIcon logoIcon = null;

	public MainFrame(Device dev) {
		setDevice(dev);
		initComponents();
	}

	public void initComponents() {
		this.setTitle("T104 Manager");
		this.setSize(400, 150);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setLayout(new BorderLayout());
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		logoIcon = new ImageIcon(BinUtils.hexStringToByteArray(Constants.LOGO_BYTES_HEXSTR));
		this.setIconImage(logoIcon.getImage());
		tp1 = new PinChangePanel();
		tp2 = new EWalletManagementPanel();
		tp3 = new CardSettingsPanel();
		mainPane = new JPanel(new BorderLayout());
		mainTabPane = new JTabbedPane();
		mainTabPane.addTab("Change Admin PIN", null, tp1, "Change administrative pin");
		mainTabPane.addTab("Manage E-Wallet", null, tp2, "Manage access rights of E-Wallet");
		mainTabPane.addTab("Manage Card", null, tp3, "Manage generic card settings");
		mainPane.add(mainTabPane, BorderLayout.CENTER);

		loginPane = new JPanel(new GridBagLayout());
		passPane = new JPanel(new FlowLayout());
		loginBtnPane = new JPanel(new FlowLayout());
		loginLbl = new JLabel("Admin PIN: ");
		pwdTf = new JPasswordField(20);
		loginBtn = new JButton("Login as Admin");
		passPane.add(loginLbl);
		passPane.add(pwdTf);
		loginBtnPane.add(loginBtn);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(0, 0, 5, 0);
		loginPane.add(passPane, c);
		c.gridx = 0;
		c.gridy = 1;
		loginPane.add(loginBtnPane, c);

		// Actions
		pwdTf.addKeyListener((KeyListener) new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					doLogin();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});

		loginBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doLogin();
			}
		});

		this.getContentPane().add(loginPane, BorderLayout.CENTER);

		this.addWindowListener(new WindowAdapter() {
			public void windowOpened(WindowEvent e) {
				pwdTf.requestFocus();
			}
		});
		
		mainTabPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				tp3.updateCardSettingsStatus();
			}
		});

		this.setVisible(true);
	}

	private void doLogin() {
		if (pwdTf.getPassword().length > 0) {
			try {
				if (ManagementTokenAPI.authenticateUser(Constants.USER_ADMIN, pwdTf.getPassword())) {
					showMainTabPane();
					EWalletManagementPanel.populateTable();
				} else {
					pwdTf.setText("");
					JOptionPane.showMessageDialog(null, "Login failed with "
							+ ManagementTokenAPI.getTriesRemaining(Constants.USER_ADMIN) + " retries left !",
							"Login Failed", JOptionPane.ERROR_MESSAGE);
				}
			} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | ShortBufferException
					| IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException
					| InvalidParameterSpecException | InvalidKeySpecException | CardException
					| InvalidSizeException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void showMainTabPane() {
		this.getContentPane().removeAll();
		this.getContentPane().add(mainPane, BorderLayout.CENTER);
		this.setSize(400, 450);
		this.validate();
		this.repaint();
	}

	public void setDevice(Device dev) {
		this.dev = dev;
	}

	private APDUResult rawGetAmount(byte type) throws CardException {
		if (dev != null) {
			byte p1 = type;
			byte p2 = (byte) 0x00;
			return new APDUResult(dev.send(new CommandAPDU((byte) 0x00, (byte) 0x02, p1, p2)));
		} else {
			throw new CardException("Device not found");
		}
	}

	private APDUResult rawSetAmount(byte type, double amt) throws CardException {
		if (dev != null) {
			byte p1 = type;
			byte p2 = (byte) 0x00;
			DecimalFormat df = new DecimalFormat("0.00");
			df.setRoundingMode(RoundingMode.DOWN);
			String textStr = df.format(amt);
			return new APDUResult(dev.send(new CommandAPDU((byte) 0x00, (byte) 0x01, p1, p2, textStr.getBytes())));
		} else {
			throw new CardException("Device not found");
		}
	}

	private APDUResult rawOtpGen() throws CardException {
		byte p1 = (byte) 0x00;
		byte p2 = (byte) 0x00;
		SecureRandom rand = new SecureRandom();
		String otpText = "";
		for (int i = 0; i < 6; i++) {
			otpText += rand.nextInt(10);
		}
		System.out.println("OTP Text: " + otpText);
		byte[] otpBytes = otpText.getBytes();
		System.out.println("OTP Bytes: " + BinUtils.toHexString(otpBytes));
		if (dev != null) {
			return new APDUResult(dev.send(new CommandAPDU((byte) 0x00, (byte) 0x00, p1, p2, otpBytes)));
		} else {
			throw new CardException("Device not found");
		}
	}
}