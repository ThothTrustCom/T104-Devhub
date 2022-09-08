package org.thothtrust.sc.t104.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.smartcardio.CardException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.thothtrust.sc.t104.AOCContainerObject;
import org.thothtrust.sc.t104.Constants;
import org.thothtrust.sc.t104.api.ManagementTokenAPI;
import org.thothtrust.sc.t104.exceptions.InvalidSizeException;
import org.thothtrust.sc.t104.util.BinUtils;

public class EWalletManagementPanel extends JPanel {

	private JPanel tablePane = null;
	private JPanel ctrlPane = null;
	private JPanel accessCtrlPane = null;
	private TitledBorder titledBorder = null;
	private String[] colHeaders = { "Applet AID", "Has E-Wallet Access" };
	private JScrollPane tableSP = null;
	private static JTable appletTable = null;
	private static DefaultTableModel tableModel = null;
	private static JButton toggleEWalletAccessBtn = null;
	private static JButton clearAOCContainersBtn = null;
	private GridBagConstraints c = null;
	private static volatile boolean allowTableAccess = true;
	private static final String STR_ENABLE_EWALLET_TEXT = "Enable E-Wallet Access";
	private static final String STR_DISABLE_EWALLET_TEXT = "Disable E-Wallet Access";

	public EWalletManagementPanel() {
		initComponents();
	}

	public void initComponents() {
		setLayout(new BorderLayout());
		toggleEWalletAccessBtn = new JButton(STR_ENABLE_EWALLET_TEXT);
		toggleEWalletAccessBtn.setToolTipText("Toggle to enable or disable access to E-Wallet feature");
		clearAOCContainersBtn = new JButton("Cleanup AOC Containers");
		clearAOCContainersBtn.setForeground(Color.WHITE);
		clearAOCContainersBtn.setBackground(new Color(190, 35, 35));
		clearAOCContainersBtn.setOpaque(true);
		clearAOCContainersBtn.setBorderPainted(false);
		clearAOCContainersBtn.setToolTipText(
				"Clicking this button will automatically delete all AOC containers without existing applets in the card");
		tablePane = new JPanel(new BorderLayout());
		tableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}

			@Override
			public Class<?> getColumnClass(int column) {

				switch (column) {
				case 1:
					return Boolean.class;
				default:
					return String.class;
				}
			}
		};
		appletTable = new JTable(tableModel);
		appletTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for (String header : colHeaders) {
			tableModel.addColumn(header);
		}
		tableSP = new JScrollPane(appletTable);
		tableSP.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tableSP.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		tablePane.add(tableSP, BorderLayout.CENTER);
		appletTable.getColumnModel().getColumn(0).setPreferredWidth(200);

		accessCtrlPane = new JPanel(new FlowLayout());
		accessCtrlPane.add(toggleEWalletAccessBtn);
		tablePane.add(accessCtrlPane, BorderLayout.SOUTH);

		titledBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),
				"E-Wallet Access Control");
		titledBorder.setTitleJustification(TitledBorder.CENTER);
		tablePane.setBorder(titledBorder);

		ctrlPane = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(10, 0, 10, 0);
		c.fill = GridBagConstraints.BOTH;
		ctrlPane.add(clearAOCContainersBtn, c);

		add(tablePane, BorderLayout.CENTER);
		add(ctrlPane, BorderLayout.SOUTH);

		appletTable.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				System.out.println("List selection event triggered ...");
				if (allowTableAccess == true) {
					System.out.println("Reading selected row info ...");
					readSelectedRowInfo();
					revalidate();
					repaint();
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

		});

		clearAOCContainersBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					clearAOCContainerAction();
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | ShortBufferException
						| IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException
						| InvalidParameterSpecException | InvalidKeySpecException | CardException
						| InvalidSizeException e1) {
					e1.printStackTrace();
				}
			}

		});

		toggleEWalletAccessBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					enableDisableAOCContainerEWalletAccess();
				} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | ShortBufferException
						| IllegalBlockSizeException | BadPaddingException | InvalidAlgorithmParameterException
						| InvalidParameterSpecException | InvalidKeySpecException | CardException
						| InvalidSizeException e1) {
					e1.printStackTrace();
				}
			}

		});
	}

	public static void populateTable() throws CardException, InvalidKeyException, NoSuchAlgorithmException,
			NoSuchPaddingException, ShortBufferException, IllegalBlockSizeException, BadPaddingException,
			InvalidAlgorithmParameterException, org.thothtrust.sc.t104.exceptions.InvalidSizeException,
			InvalidParameterSpecException, InvalidKeySpecException {
		AOCContainerObject[] aocList = ManagementTokenAPI.getAOCContainerSlotList();
		int row = 0;
		int totalRows = tableModel.getRowCount();
		System.out.println("Total Table Rows: " + totalRows);
		allowTableAccess = false;
		for (int i = 0; i < totalRows; i++) {
			System.out.println("Deleting row: " + i);
			tableModel.removeRow(0);
		}
		if (aocList != null) {
			for (AOCContainerObject aocItem : aocList) {
				if (aocItem != null) {
					boolean hasEWalletAccess = false;
					if (aocItem.getAuxData() != null) {
						byte[] auxData = aocItem.getAuxData();
						if (auxData.length == 1) {
							if (aocItem.getAuxData()[0] == (byte) 0x31) {
								hasEWalletAccess = true;
							}
						}
					}
					tableModel.addRow(new Object[] { BinUtils.toHexString(aocItem.getCredName()), hasEWalletAccess });
				}
			}
		}
		if (tableModel.getRowCount() > 0) {
			appletTable.changeSelection(0, 0, false, false);
		}
		allowTableAccess = true;
		if (tableModel.getRowCount() > 0) {
			appletTable.setRowSelectionInterval(0, 0);
		}
	}

	public static void depopulateTable() {
		allowTableAccess = false;
		int totalRows = tableModel.getRowCount();
		for (int i = 0; i < totalRows; i++) {
			tableModel.removeRow(0);
		}
		allowTableAccess = true;
	}

	private void clearAOCContainerAction() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
			ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidParameterSpecException, InvalidKeySpecException, CardException, InvalidSizeException {
		if (ManagementTokenAPI.clearAOCContainers()) {
			populateTable();
		} else {
			JOptionPane.showMessageDialog(null, "An error occurred while trying to cleanup the AOC containers !",
					"AOC Container Cleanup Failed", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void enableDisableAOCContainerEWalletAccess()
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, ShortBufferException,
			IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException,
			InvalidParameterSpecException, InvalidKeySpecException, CardException, InvalidSizeException {
		int selectedRow = appletTable.getSelectedRow();
		if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
			byte[] credID = BinUtils.hexStringToByteArray((String) tableModel.getValueAt(selectedRow, 0));
			boolean hasEWalletAccess = (boolean) tableModel.getValueAt(selectedRow, 1);

			byte[] tlvData = new byte[5 + credID.length];
			int pos = 0;

			// Set TLV_TAG_AOC_CRED_ID
			tlvData[pos] = Constants.TLV_TAG_AOC_CRED_ID;
			pos++;

			// Set length
			tlvData[pos] = (byte) (credID.length & 0xFF);
			pos++;

			// Set value
			System.arraycopy(credID, 0, tlvData, pos, credID.length);
			pos += credID.length;

			// Set TLV_TAG_AOC_AUXDATA
			tlvData[pos] = Constants.TLV_TAG_AOC_AUXDATA;
			pos++;

			// Set length
			tlvData[pos] = (byte) (0x01);
			pos++;

			// Set value
			if (hasEWalletAccess) {
				// Disable access for those that have access
				tlvData[pos] = (byte) (0x30);
			} else {
				// Enable access for those that have access
				tlvData[pos] = (byte) (0x31);
			}
			pos++;
			System.out.println("Editing AOC Container with values: " + BinUtils.toHexString(tlvData));

			if (ManagementTokenAPI.editAOCCOntainerSetting(tlvData)) {
				populateTable();
				readSelectedRowInfo();
				revalidate();
				repaint();
			} else {
				JOptionPane.showMessageDialog(null,
						"An error occurred while trying to edit E-Wallet acces for the AOC container !",
						"AOC Container Update Failed", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private static void readSelectedRowInfo() {
		System.out.println("Reading selected row ...");
		int selectedRow = appletTable.getSelectedRow();
		if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
			boolean hasEWalletAccess = (boolean) tableModel.getValueAt(selectedRow, 1);
			if (hasEWalletAccess) {
				System.out.println("Has E-Wallet access ...");
				toggleEWalletAccessBtn.setText(STR_DISABLE_EWALLET_TEXT);
			} else {
				System.out.println("Has NOT E-Wallet access ...");
				toggleEWalletAccessBtn.setText(STR_ENABLE_EWALLET_TEXT);
			}
		}
	}

}
