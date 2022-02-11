/*
 * $Id$
 *
 * Copyright (c) 2013-2113 by Federico Corso (FredKors)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */
package VASL.build.module;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import VASL.environment.*;
import VASSAL.build.Buildable;
import VASSAL.build.GameModule;
import VASSAL.build.module.GlobalOptions;
import VASSAL.command.CommandEncoder;
import VASSAL.configure.*;
import VASSAL.i18n.Resources;
import VASSAL.preferences.Prefs;
import VASSAL.tools.KeyStrokeListener;
import VASSAL.tools.KeyStrokeSource;
import VASSAL.tools.ScrollPane;
import VASSAL.tools.imageop.Op;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.InputEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.*;

import static VASSAL.build.GameModule.getGameModule;

/**
 * The chat window component.  Displays text messages and
 * accepts i.  Also acts as a {@link CommandEncoder},
 * encoding/decoding commands that display message in the text area
 */
public class ASLChatter extends VASSAL.build.module.Chatter
{
  private ArrayList<ChatterListener> chatter_listeners = new ArrayList<ChatterListener>();

  public static final String BEFORE_CATEGORY = "   ";
  private static final String CHAT_FONT = "ChatFont";
  private static final String BUTTON_FONT = "ButtonFont";
  private static final String USE_DICE_IMAGES = "useDiceImages"; //$NON-NLS-1$
  private static final String SHOW_DICE_STATS = "showDiceStats"; //$NON-NLS-1$
  private static final String CHAT_BACKGROUND_COLOR = "chatBackgroundColor"; //$NON-NLS-1$
  private static final String COLORED_DICE_COLOR = "coloredDiceColor"; //$NON-NLS-1$
  private static final String SINGLE_DIE_COLOR = "singleDieColor"; //$NON-NLS-1$
  private static final String THIRD_DIE_COLOR = "thirdDieColor"; //$NON-NLS-1$
  private static final String NOTIFICATION_LEVEL = "notificationLevel"; //$NON-NLS-1$
  private final static String m_strFileNameFormat = "chatter/DC%s_%s.png";

  private final static int DR_NOTIFY_NONE = 0;
  private final static int DR_NOTIFY_SNIPERS = 1;
  private final static int DR_NOTIFY_STARTER_KIT = 2;
  private final static int DR_NOTIFY_ALL = 3;

  private static final String preferenceTabName = "VASL"; // alwaysontop preference

  private enum DiceType
  {
      WHITE,
      COLORED,
      OTHER_DUST
  }

  private Color m_clrBackground;
  private Color m_clrGameMsg;
  private Color m_clrSystemMsg;
  private Color m_crlMyChatMsg;
  private Color m_clrOtherChatMsg;
  private Color m_clrColoredDiceColor;
  private Color m_clrDustColoredDiceColor;
  private Color m_clrSingleDieColor;

  private JButton m_btnStats;
  private JButton m_btnDR;
  private JButton m_btnIFT;
  private JButton m_btnTH;
  private JButton m_btnTK;
  private JButton m_btnMC;
  private JButton m_btnRally;
  private JButton m_btnCC;
  private JButton m_btnTC;
  private JButton m_btndr;
  private JButton m_btnSA;
  private JButton m_btnRS;

  //private JTextPane m_objChatPanel;
  //private StyledDocument m_objDocument;
  //private StyleContext m_objStyleContext;

  private Font m_objChatterFont;

  private Style m_objMainStyle;
  private Style m_objIconStyle;
  private boolean m_bUseDiceImages;
  private boolean m_bShowDiceStats;

  private final Icon [] mar_objWhiteDCIcon = new Icon[6];
  private final Icon [] mar_objColoredDCIcon = new Icon[6];
  private final Icon [] mar_objOtherColoredDCIcon = new Icon[6];
  private final Icon [] mar_objSingleDieIcon = new Icon[6];

  private Environment environment = new Environment();

  private JTextField m_edtInputText;
  private final JScrollPane m_objScrollPane = new ScrollPane(
       JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
       JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    private int m_DRNotificationLevel;

    public void RemoveActionForKeyStroke(KeyStroke aKeyStroke)
    {
        ActionMap am = conversationPane.getActionMap();

        if (am == null) {
            return;
        }

        for (int counter = 0; counter < 3; counter++) {
            InputMap inputMap = conversationPane.getInputMap(counter);

            if (inputMap != null) {
                Object actionBinding = inputMap.get(aKeyStroke);

                if (actionBinding != null) {
                    Action action = am.get(actionBinding);

                    if (action != null) {
                        action.setEnabled(false);
                    }
                }
            }
        }
    }

    public ASLChatter() {
        super();

        m_clrBackground = Color.white;
        m_clrGameMsg = Color.magenta;
        m_clrSystemMsg = new Color(160, 160, 160);
        m_crlMyChatMsg = Color.gray;
        m_clrOtherChatMsg =  Color.black;
        m_clrColoredDiceColor = Color.YELLOW;
        m_clrDustColoredDiceColor = Color.magenta;
        m_clrSingleDieColor = Color.RED;

        Style l_objDefaultStyle = style.getStyle(StyleContext.DEFAULT_STYLE);

        m_objMainStyle = style.addStyle("MainStyle", l_objDefaultStyle);
        m_objIconStyle = style.addStyle("IconStyle", l_objDefaultStyle);

        try
        {
            for (int l_i = 0; l_i < 6; l_i++)
            {
                mar_objWhiteDCIcon[l_i] = new ImageIcon(Op.load(String.format(m_strFileNameFormat, String.valueOf(l_i + 1), "W")).getImage(null));
                mar_objColoredDCIcon[l_i] = null;
                mar_objOtherColoredDCIcon[l_i] = null;
                mar_objSingleDieIcon[l_i] = null;
            }

            RebuildColoredDiceFaces();
            RebuildSingleDieFaces();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        
        conversationPane.setEditable(false);

        conversationPane.addKeyListener(new KeyListener()
        {
            public void keyTyped(KeyEvent e) {
                if (!e.isConsumed()) {
                    keyCommand(KeyStroke.getKeyStrokeForEvent(e));
                }
            }

            public void keyPressed(KeyEvent e) {
                if (!e.isConsumed()) {
                    keyCommand(KeyStroke.getKeyStrokeForEvent(e));
                }
            }

            public void keyReleased(KeyEvent e) {
                if (!e.isConsumed()) {
                    keyCommand(KeyStroke.getKeyStrokeForEvent(e));
                }
            }
        }
        );

        RemoveActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK));
        RemoveActionForKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));

        conversationPane.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                m_objScrollPane.getVerticalScrollBar().setValue(m_objScrollPane.getVerticalScrollBar().getMaximum());
            }
        });

        m_btnStats = CreateStatsDiceButton("stat.png", "", "Dice rolls stats", KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.CTRL_DOWN_MASK));
        m_btnDR = CreateChatterDiceButton("DRs.gif", "DR", "DR", KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), true, ASLDiceBot.OTHER_CATEGORY);
        m_btnIFT = CreateChatterDiceButton("", "IFT", "IFT attack DR", KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK), true, "IFT");
        m_btnTH = CreateChatterDiceButton("", "TH", "To Hit DR", KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK), true, "TH");
        m_btnTK = CreateChatterDiceButton("", "TK", "To Kill DR", KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK), true, "TK");
        m_btnMC = CreateChatterDiceButton("", "MC", "Morale Check DR", KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK), true, "MC");
        m_btnRally = CreateChatterDiceButton("", "Rally", "Rally DR", KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK), true, "Rally");
        m_btnCC = CreateChatterDiceButton("", "CC", "Close Combat DR", KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK), true, "CC");
        m_btnTC = CreateChatterDiceButton("", "TC", "Task Check DR", KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK), true, "TC");
        m_btndr = CreateChatterDiceButton("dr.gif", "dr", "dr", KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), false, ASLDiceBot.OTHER_CATEGORY);
        m_btnSA = CreateChatterDiceButton("", "SA", "Sniper Activation dr", KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK), false, "SA");
        m_btnRS = CreateChatterDiceButton("", "RS", "Random Selection dr", KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_DOWN_MASK), false, "RS");

        JPanel l_objPanelContainer = new JPanel();
        l_objPanelContainer.setLayout(new BoxLayout(l_objPanelContainer, BoxLayout.LINE_AXIS));
        l_objPanelContainer.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        JPanel l_objButtonPanel = new JPanel();
        l_objButtonPanel.setLayout(new GridBagLayout());
        l_objButtonPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 2, 1));
        l_objButtonPanel.setMaximumSize(new Dimension(800, 800));

        GridBagConstraints l_objGridBagConstraints = new GridBagConstraints();
        l_objGridBagConstraints.fill = GridBagConstraints.BOTH;
        l_objGridBagConstraints.weightx = 0.5;
        l_objGridBagConstraints.weighty = 0.5;
        l_objGridBagConstraints.insets = new Insets(0, 1, 0, 1);

        l_objButtonPanel.add(m_btnStats, l_objGridBagConstraints);
        l_objButtonPanel.add(m_btnDR, l_objGridBagConstraints);
        l_objButtonPanel.add(m_btnIFT, l_objGridBagConstraints);
        l_objButtonPanel.add(m_btnTH, l_objGridBagConstraints);
        l_objButtonPanel.add(m_btnTK, l_objGridBagConstraints);
        l_objButtonPanel.add(m_btnMC, l_objGridBagConstraints);
        l_objButtonPanel.add(m_btnTC, l_objGridBagConstraints);
        l_objButtonPanel.add(m_btnRally, l_objGridBagConstraints);
        l_objButtonPanel.add(m_btnCC, l_objGridBagConstraints);
        l_objButtonPanel.add(m_btndr, l_objGridBagConstraints);
        l_objButtonPanel.add(m_btnSA, l_objGridBagConstraints);
        l_objButtonPanel.add(m_btnRS, l_objGridBagConstraints);

        m_edtInputText = new JTextField(60);
        m_edtInputText.setFocusTraversalKeysEnabled(false);
        m_edtInputText.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                send(formatChat(e.getActionCommand()), e.getActionCommand()); //BR// This overload is the preferred routine to call now
                m_edtInputText.setText(""); //$NON-NLS-1$
            }
        });

        m_edtInputText.setMaximumSize(new Dimension(m_edtInputText.getMaximumSize().width,
        m_edtInputText.getPreferredSize().height));

        m_objScrollPane.setViewportView(conversationPane);

        l_objPanelContainer.add(l_objButtonPanel);

        GroupLayout l_objGroupLayout = new GroupLayout(this);
        setLayout(l_objGroupLayout);
        l_objGroupLayout.setHorizontalGroup(
            l_objGroupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(m_objScrollPane)
            .addComponent(l_objPanelContainer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(m_edtInputText)
        );
        l_objGroupLayout.setVerticalGroup(
            l_objGroupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(l_objGroupLayout.createSequentialGroup()
                    .addComponent(m_objScrollPane, GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                    .addGap(0, 0, 0)
                    .addComponent(l_objPanelContainer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, 0)
                    .addComponent(m_edtInputText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );

    }

    private void SetButtonsFonts(Font objFont)
    {
        m_btnStats.setFont(objFont);
        m_btnDR.setFont(objFont);
        m_btnIFT.setFont(objFont);
        m_btnTH.setFont(objFont);
        m_btnTK.setFont(objFont);
        m_btnMC.setFont(objFont);
        m_btnRally.setFont(objFont);
        m_btnCC.setFont(objFont);
        m_btnTC.setFont(objFont);
        m_btndr.setFont(objFont);
        m_btnSA.setFont(objFont);
        m_btnRS.setFont(objFont);
    }


    private JButton CreateStatsDiceButton(String strImage, String strCaption, String strTooltip, KeyStroke keyStroke)
    {
        JButton l_btn = new JButton(strCaption);

        l_btn.setMinimumSize(new Dimension(5, 30));
        l_btn.setMargin(new Insets(0, 0, 0, -1));

        try
        {
            if (!strImage.isEmpty())
                l_btn.setIcon(new ImageIcon(Op.load(strImage).getImage(null)));
        }
        catch (Exception ex)
        {
        }

        ActionListener l_objAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    ASLDiceBot l_objDice = GameModule.getGameModule().getComponentsOf(ASLDiceBot.class).iterator().next();

                    if (l_objDice != null)
                        l_objDice.statsToday();
                }
                catch (Exception ex)
                {
                }
            }
        };

        l_btn.addActionListener(l_objAL);
        KeyStrokeListener l_Listener = new KeyStrokeListener(l_objAL);
        l_Listener.setKeyStroke(keyStroke);
        AddHotKeyToTooltip(l_btn, l_Listener, strTooltip);
        l_btn.setFocusable(false);
        GameModule.getGameModule().addKeyStrokeListener(l_Listener);

        return l_btn;
    }

    private JButton CreateChatterDiceButton(String strImage, String strCaption, String strTooltip, KeyStroke keyStroke, final boolean bDice, final String strCat)
    {
        JButton l_btn = new JButton(strCaption);

        l_btn.setMinimumSize(new Dimension(5, 30));
        l_btn.setMargin(new Insets(0, 0, 0, -1));

        try
        {
            if (!strImage.isEmpty())
                l_btn.setIcon(new ImageIcon(Op.load(strImage).getImage(null)));
        }
        catch (Exception ex)
        {
        }

        ActionListener l_objAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    ASLDiceBot l_objDice = GameModule.getGameModule().getComponentsOf(ASLDiceBot.class).iterator().next();

                    if (l_objDice != null)
                    {
                        if (bDice)
                            l_objDice.DR(strCat);
                        else
                            l_objDice.dr(strCat);
                    }
                }
                catch (Exception ex)
                {
                }
            }
        };

        l_btn.addActionListener(l_objAL);
        KeyStrokeListener l_Listener = new KeyStrokeListener(l_objAL);
        l_Listener.setKeyStroke(keyStroke);
        AddHotKeyToTooltip(l_btn, l_Listener, strTooltip);
        l_btn.setFocusable(false);
        GameModule.getGameModule().addKeyStrokeListener(l_Listener);

        return l_btn;
    }

    private void AddHotKeyToTooltip(JButton objButton, KeyStrokeListener objListener, String strTooltipText)
    {
        if (objListener.getKeyStroke() != null)
            objButton.setToolTipText(strTooltipText + " [" + HotKeyConfigurer.getString(objListener.getKeyStroke()) + "]");
    }

    /*
    @Override
    protected String formatChat(String text)
    {
        final String id = GlobalOptions.getInstance().getPlayerId();

        return "<" + (id.length() == 0 ? "(" + getAnonymousUserName() + ")" : id) + "> - " + text; //$NON-NLS-1$ //$NON-NLS-2$
    }
    */

    /*
    @Override
    public JTextField getInputField()
    {
        return m_edtInputText;
    }
    */

    String [] FindUser (String strVal)
    {
        String [] lar_strRetValue = new String[] {strVal,"",""};

        //BR// I can't find where the "<" literals are getting in here, but I know that "<" literals in a string will cause HTML to go crazy
        strVal = strVal.replaceAll("<", "&lt;").replaceAll(">", "&gt;");

        int l_iUserStart = strVal.indexOf("&lt;");
        int l_iUserEnd = strVal.indexOf("&gt;");

        if ((l_iUserStart != -1) && (l_iUserEnd != -1))
        {
            lar_strRetValue[0] = strVal.substring(0, l_iUserStart + 1);
            lar_strRetValue[1] = strVal.substring(l_iUserStart + 1, l_iUserEnd);
            lar_strRetValue[2] = strVal.substring(l_iUserEnd);
        }

        return lar_strRetValue;
    }

    @Override
    public void show(String strMsg)
    {
        try
        {
            if (strMsg.length() > 0)
            {
                if (strMsg.startsWith("<html>"))
                {
                    try
                    {
                        //BR// This table is already HTML so let's just insert it into the normal editor kit?
                        kit.insertHTML(doc, doc.getLength(), strMsg, 0, 0, null);
                        /*
                        JLabel l_objLabel = new JLabel(strMsg);
                        l_objLabel.setAlignmentY(0.7f);

                        StyleConstants.setComponent(m_objIconStyle, l_objLabel);
                        doc.insertString(doc.getLength(), "\n", m_objMainStyle);
                        doc.insertString(doc.getLength(), "\n", m_objMainStyle);
                        doc.insertString(doc.getLength(), "Ignored", m_objIconStyle);
                        doc.insertString(doc.getLength(), "\n", m_objMainStyle);
                        */
                    }
                    catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                else if (strMsg.startsWith("*** DR = "))
                {
                    //BR// I think this may be legacy anyway? but you could comment super.show() out and fix the other one if needed
                    super.show(strMsg);
                    //ParseOldDR(strMsg);
                }
                else if (strMsg.startsWith("*** dr = "))
                {
                    //BR// I think this may be legacy anyway? but you could comment super.show() out and fix the other one if needed
                    super.show(strMsg);
                    //ParseOlddr(strMsg);
                }
                else if (strMsg.startsWith("*** 3d6 = "))
                {
                    //BR// I think this may be legacy anyway? but you could comment super.show() out and fix the other one if needed
                    super.show(strMsg);
                    //Parse3d6(strMsg);
                }
                else if (strMsg.startsWith("*** ("))
                {
                    //BR// I think this may be legacy anyway? But you could comment super.show() out and fix the other one if needed
                    ParseNewDiceRoll(strMsg);
                }
                else {
                  //BR// Use the Vassal parser for everything that isn't one of our super-special-VASL things
                  //BR// That way all the HTML/QuickColors stuff will work right
                  super.show(strMsg);
                }

                /*
                else if (strMsg.startsWith("<"))
                {
                    ParseUserMsg(strMsg);
                }
                else if (strMsg.startsWith("-"))
                {
                    ParseSystemMsg(strMsg);
                }
                else if (strMsg.startsWith("*"))
                {
                    ParseMoveMsg(strMsg);
                }
                else
                {
                    ParseDefaultMsg(strMsg);
                }
                */
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void ParseDefaultMsg(String strMsg) {
        try
        {
            StyleConstants.setForeground(m_objMainStyle, Color.black);
            doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void ParseMoveMsg(String strMsg) {
        try
        {
            StyleConstants.setForeground(m_objMainStyle, m_clrGameMsg);
            doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void ParseSystemMsg(String strMsg) {
        try
        {
            StyleConstants.setForeground(m_objMainStyle, m_clrSystemMsg);
            doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void ParseUserMsg(String strMsg) {
        try
        {
            String[] lar_strParts = FindUser(strMsg);

            if (strMsg.startsWith(formatChat(""))) { //$NON-NLS-1$
                StyleConstants.setForeground(m_objMainStyle, m_crlMyChatMsg);
            } else {
                StyleConstants.setForeground(m_objMainStyle, m_clrOtherChatMsg);
            }

            if ((!lar_strParts[1].isEmpty()) && (!lar_strParts[2].isEmpty()))
            {
                doc.insertString(doc.getLength(), "\n" + lar_strParts[0], m_objMainStyle);
                StyleConstants.setBold(m_objMainStyle, true);
                doc.insertString(doc.getLength(), lar_strParts[1], m_objMainStyle);
                StyleConstants.setBold(m_objMainStyle, false);
                doc.insertString(doc.getLength(), lar_strParts[2], m_objMainStyle);
            }
            else
                doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void ParseOlddr(String strMsg) {
        try
        { // *** dr = 2 *** <FredKors>
            String l_strRestOfMsg = strMsg.substring("*** dr = ".length());
            int l_iPos = l_strRestOfMsg.indexOf(" ***");
            String l_strUser = "";

            StyleConstants.setForeground(m_objMainStyle, m_clrGameMsg);

            if (l_iPos != -1)
            {
                String l_strLast = l_strRestOfMsg.substring(l_iPos);
                String l_strDice = l_strRestOfMsg.substring(0, l_iPos);

                if (l_strDice.length() == 1)
                {
                    int l_iDice = Integer.parseInt(l_strDice);

                    if ((l_iDice > 0)
                        && (l_iDice < 7))
                    {
                        String[] lar_strParts = FindUser(l_strLast);

                        if ((!lar_strParts[1].isEmpty()) && (!lar_strParts[2].isEmpty()))
                        {
                            l_strUser = lar_strParts[1];

                            doc.insertString(doc.getLength(), "\n*** dr = ", m_objMainStyle);
                            if (m_bUseDiceImages)
                            {
                                PaintIcon(l_iDice, DiceType.COLORED,true, "");
                            }
                            else
                            {
                                StyleConstants.setBold(m_objMainStyle, true);
                                doc.insertString(doc.getLength(), l_strDice, m_objMainStyle);
                                StyleConstants.setBold(m_objMainStyle, false);
                            }
                            doc.insertString(doc.getLength(), lar_strParts[0], m_objMainStyle);
                            StyleConstants.setBold(m_objMainStyle, true);
                            doc.insertString(doc.getLength(), l_strUser, m_objMainStyle); // user
                            StyleConstants.setBold(m_objMainStyle, false);
                            doc.insertString(doc.getLength(), lar_strParts[2], m_objMainStyle);

                            FireDiceRoll("", l_strUser, "", l_iDice, -1);
                        }
                        else
                        {
                            doc.insertString(doc.getLength(), "\n*** dr = ", m_objMainStyle);
                            PaintIcon(l_iDice, DiceType.COLORED,true, "");
                            doc.insertString(doc.getLength(), l_strLast, m_objMainStyle);

                            FireDiceRoll("", "?", "", l_iDice, -1);
                        }
                    }
                    else
                        doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
                }
                else
                    doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
            }
            else
                doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void ParseNewDiceRoll(String strMsg)
    {
        // *** (Other DR) 4,2 ***   <FredKors>      Allied SAN    [1 / 8   avg   6,62 (6,62)]    (01.51 - by random.org)
        String l_strCategory = "", l_strDice = "", l_strUser = "", l_strSAN = "";
        int l_iFirstDice, l_iSecondDice;

        Map<DiceType, Integer> otherDice = new HashMap<>();
        DustLevel dustLevel = environment.getCurrentDustLevel();

        StringBuilder string = new StringBuilder("<html>");

        try
        {
            String l_strRestOfMsg = strMsg.substring("*** (".length()); // Other DR) 4,2 ***   <FredKors>      Allied SAN    [1 / 8   avg   6,62 (6,62)]    (01.51 - by random.org)        

            int l_iPos = l_strRestOfMsg.indexOf(" DR) ");

            if (l_iPos != -1)
            {
                l_strCategory = l_strRestOfMsg.substring(0, l_iPos);
                l_strRestOfMsg = l_strRestOfMsg.substring(l_iPos + " DR) ".length()); //4,2 ***   <FredKors>      Allied SAN    [1 / 8   avg   6,62 (6,62)]    (01.51 - by random.org)        

                l_iPos = l_strRestOfMsg.indexOf(" ***");

                if (l_iPos != -1)
                {
                    l_strDice = l_strRestOfMsg.substring(0, l_iPos);
                    l_strRestOfMsg = l_strRestOfMsg.substring(l_iPos + " ***".length());//   <FredKors>      Allied SAN    [1 / 8   avg   6,62 (6,62)]    (01.51 - by random.org)

                    if (l_strDice.length() == 3 || l_strDice.length() == 5)
                    {
                        String [] lar_strDice = l_strDice.split(",");

                        if (lar_strDice.length == 2 || (lar_strDice.length == 3 && environment.dustInEffect()))
                        {
                            l_iFirstDice = Integer.parseInt(lar_strDice[0]);
                            l_iSecondDice = Integer.parseInt(lar_strDice[1]);
                            if(environment.dustInEffect() && lar_strDice.length == 3)
                            {
                                otherDice.put(DiceType.OTHER_DUST, Integer.parseInt(lar_strDice[2]));
                            }

                            if ((l_iFirstDice > 0)
                                && (l_iFirstDice < 7)
                                && (l_iSecondDice > 0)
                                && (l_iSecondDice < 7))
                            {
                                String[] lar_strParts = FindUser(l_strRestOfMsg);

                                ArrayList<String> specialMessages = new ArrayList<String>();

                                if ((!lar_strParts[1].isEmpty()) && (!lar_strParts[2].isEmpty()))
                                {
                                    l_strUser = lar_strParts[1];
                                    l_strRestOfMsg = lar_strParts[2]; // >      Allied SAN    [1 / 8   avg   6,62 (6,62)]    (01.51 - by random.org)        

                                    l_strRestOfMsg = l_strRestOfMsg.replace(">", " ").trim(); //Allied SAN    [1 / 8   avg   6,62 (6,62)]    (01.51 - by random.org)        


                                    // Add special event hints, if necessary
                                    // First, SAN, which should not trigger for Rally, TK and CC rolls
                                    // and should happen on "Only Sniper" setting and in Full ASL mode
                                    if(m_DRNotificationLevel == 3 || m_DRNotificationLevel == 1)
                                    {
                                        if ((!l_strCategory.equals("TK")) && (!l_strCategory.equals("CC"))
                                                && (!l_strCategory.equals(("Rally"))))
                                        {
                                            if (l_strRestOfMsg.startsWith("Axis SAN"))
                                            {
                                                l_strSAN = "Axis SAN";
                                                specialMessages.add("Axis SAN");
                                                l_strRestOfMsg = l_strRestOfMsg.substring("Axis SAN".length());
                                            } else if (l_strRestOfMsg.startsWith("Allied SAN"))
                                            {
                                                l_strSAN = "Allied SAN";
                                                specialMessages.add("Allied SAN");
                                                l_strRestOfMsg = l_strRestOfMsg.substring("Allied SAN".length());
                                            } else if (l_strRestOfMsg.startsWith("Axis/Allied SAN"))
                                            {
                                                l_strSAN = "Axis/Allied SAN";
                                                specialMessages.add("Axis/Allied SAN");
                                                l_strRestOfMsg = l_strRestOfMsg.substring("Axis/Allied SAN".length());
                                            }
                                        }

                                        if (l_strCategory.equals("TC"))
                                        {
                                            if (l_strRestOfMsg.startsWith("Axis Booby Trap"))
                                            {
                                                l_strSAN = "Axis Booby Trap";
                                                specialMessages.add("Axis Booby Trap");
                                                l_strRestOfMsg = l_strRestOfMsg.substring("Axis Booby Trap".length());
                                            } else if (l_strRestOfMsg.startsWith("Allied Booby Trap"))
                                            {
                                                l_strSAN = "Allied Booby Trap";
                                                specialMessages.add("Allied Booby Trap");
                                                l_strRestOfMsg = l_strRestOfMsg.substring("Allied Booby Trap".length());
                                            } else if (l_strRestOfMsg.startsWith("Axis/Allied Booby Trap"))
                                            {
                                                l_strSAN = "Axis/Allied Booby Trap";
                                                specialMessages.add("Axis/Allied Booby Trap");
                                                l_strRestOfMsg = l_strRestOfMsg.substring("Axis/Allied Booby Trap".length());
                                            }
                                        }
                                    }
                                    // ALL of these happen only in Starter Kit mode or Full ASL mode
                                    if(m_DRNotificationLevel >= 2)
                                    {
                                        // For TH rolls only, show possible hit location, Unlikely hit and multiple hit
                                        if (l_strCategory.equals("TH"))
                                        {
                                            if (l_iFirstDice == l_iSecondDice)
                                            {
                                                // Starter Kit + Full ASL
                                                if (l_iFirstDice == 1)
                                                {
                                                    specialMessages.add("Unlikely Hit (C3.6)");
                                                }
                                                // Full ASL only
                                                if (m_DRNotificationLevel == 3)
                                                {
                                                    specialMessages.add("Multiple Hits 15..40mm (C3.8)");
                                                }
                                            }
                                            if (l_iFirstDice < l_iSecondDice)
                                            {
                                                specialMessages.add("Turret");
                                            } else
                                            {
                                                specialMessages.add("Hull");
                                            }
                                            HandleSpecialMessagesForOtherDice(l_strCategory, specialMessages, l_iFirstDice,l_iSecondDice, otherDice);

                                        } else if (l_strCategory.equals("TK"))
                                        {
                                            if (l_iFirstDice == l_iSecondDice)
                                            {
                                                if (l_iFirstDice == 6)
                                                {
                                                    specialMessages.add("Dud (C7.35)");
                                                }
                                            }
                                        } else if (l_strCategory.equals("MC"))
                                        {
                                            // Full ASL only
                                            if (l_iFirstDice == 1 && l_iSecondDice == 1 && m_DRNotificationLevel == 3)
                                            {
                                                specialMessages.add("Heat of Battle (A15.1)");
                                            }
                                            // Starter Kit & Full ASL
                                            else if (l_iFirstDice == 6 && l_iSecondDice == 6)
                                            {
                                                specialMessages.add("Casualty MC (A10.31)");
                                            }
                                            HandleSpecialMessagesForOtherDice(l_strCategory, specialMessages, l_iFirstDice,l_iSecondDice, otherDice);
                                        } else if (l_strCategory.equals("TC"))
                                        {

                                        } else if (l_strCategory.equals("Rally"))
                                        {
                                            // Full ASL only
                                            if (l_iFirstDice == 1 && l_iSecondDice == 1 && m_DRNotificationLevel == 3)
                                            {
                                                specialMessages.add("Heat of Battle (A15.1) or Field Promotion (A18.11)");
                                            }
                                            // Starter Kit + Full ASL
                                            else if (l_iFirstDice == 6 && l_iSecondDice == 6)
                                            {
                                                specialMessages.add("Fate -> Casualty Reduction (A10.64)");
                                            }

                                        } else if (l_strCategory.equals("IFT"))
                                        {
                                            // check for cowering
                                            if (l_iFirstDice == l_iSecondDice)
                                            {
                                                // Full ASL only
                                                if (l_iFirstDice == 1 && m_DRNotificationLevel == 3)
                                                {
                                                    specialMessages.add("Unlikely Kill vs * (A7.309)");
                                                }
                                                // Starter Kit + Full ASL
                                                specialMessages.add("Cower if MMC w/o LDR");
                                            }
                                            HandleSpecialMessagesForOtherDice(l_strCategory, specialMessages, l_iFirstDice,l_iSecondDice, otherDice);                                        } else if (l_strCategory.equals("CC"))
                                        {
                                            // Full ASL only
                                            if (l_iFirstDice == 1 && l_iSecondDice == 1 && m_DRNotificationLevel == 3)
                                            {
                                                specialMessages.add("Infiltration (A11.22), Field Promotion (A18.12), Unlikely Kill (A11.501)");
                                            } else if (l_iFirstDice == 6 && l_iSecondDice == 6 && m_DRNotificationLevel == 3)
                                            {
                                                specialMessages.add("Infiltration (A11.22)");
                                            }
                                        }
                                    }



                                    // Construct Special Message string
                                    String l_strSpecialMessages = "";
                                    for (int i = 0; i < specialMessages.size(); ++i)
                                    {
                                        l_strSpecialMessages += specialMessages.get(i);
                                        if (i < specialMessages.size() - 1)
                                        {
                                            l_strSpecialMessages += ", ";
                                        }
                                    }

                                    StyleConstants.setForeground(m_objMainStyle, Color.BLACK);
                                    StyleConstants.setBold(m_objMainStyle, true);

                                    //doc.insertString(doc.getLength(), "\n" + BEFORE_CATEGORY + l_strCategory + "\t", m_objMainStyle);
                                    string.append("\n" + BEFORE_CATEGORY + l_strCategory + "\t");

                                    StyleConstants.setForeground(m_objMainStyle, m_clrGameMsg);
                                    StyleConstants.setBold(m_objMainStyle, false);

                                    if (m_bUseDiceImages)
                                    {
                                        PaintIcon(l_iFirstDice, DiceType.COLORED,false, (m_bShowDiceStats ? "" : l_strRestOfMsg));
                                        //doc.insertString(doc.getLength(), " ", m_objMainStyle);
                                        string.append(' ');

                                        PaintIcon(l_iSecondDice, DiceType.WHITE,false, (m_bShowDiceStats ? "" : l_strRestOfMsg));
                                        //Add any other dice required
                                        for ( Map.Entry<DiceType, Integer> entry : otherDice.entrySet())
                                        {
                                            string.append(' ');
                                            //doc.insertString(doc.getLength(), " ", m_objMainStyle);
                                            PaintIcon(entry.getValue(), entry.getKey() ,false, (m_bShowDiceStats ? "" : l_strRestOfMsg));
                                        }
                                    }
                                    else
                                    {
                                        StyleConstants.setBold(m_objMainStyle, true);
                                        string.append(l_strDice);
                                        //doc.insertString(doc.getLength(), l_strDice, m_objMainStyle);
                                        StyleConstants.setBold(m_objMainStyle, false);
                                    }
                                    string.append("  ...  ");
                                    //doc.insertString(doc.getLength(), "  ...  ", m_objMainStyle);

                                    StyleConstants.setBold(m_objMainStyle, true);
                                    string.append(l_strUser);
                                    //doc.insertString(doc.getLength(), l_strUser, m_objMainStyle);

                                    StyleConstants.setBold(m_objMainStyle, false);
                                    string.append("   ");
                                    //doc.insertString(doc.getLength(), "   ", m_objMainStyle);

                                    StyleConstants.setBold(m_objMainStyle, true);
                                    StyleConstants.setUnderline(m_objMainStyle, true);
                                    string.append(l_strSpecialMessages);
                                    //doc.insertString(doc.getLength(), l_strSpecialMessages, m_objMainStyle);

                                    StyleConstants.setBold(m_objMainStyle, false);
                                    StyleConstants.setUnderline(m_objMainStyle, false);

                                    if (m_bShowDiceStats)
                                        string.append(l_strRestOfMsg);
                                        //doc.insertString(doc.getLength(), l_strRestOfMsg, m_objMainStyle);
                                    else
                                        string.append(" ");
                                        //doc.insertString(doc.getLength(), " ", m_objMainStyle);

                                    FireDiceRoll(l_strCategory, l_strUser, l_strSAN, l_iFirstDice, l_iSecondDice);
                                }
                                else
                                    string.append("\n" + strMsg);
                                    //doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
                            }
                            else
                                string.append("\n" + strMsg);
                                //doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
                        }
                        else
                          string.append("\n" + strMsg);
                          //doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
                    }
                    else
                      string.append("\n" + strMsg);
                      //doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
                }
                else
                  string.append("\n" + strMsg);
                  //doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
            }
            else // *** (Other dr) 3 ***   <FredKors>      [1 / 1   avg   3,00 (3,00)]    (01.84)
            {
                l_iPos = l_strRestOfMsg.indexOf(" dr) ");

                if (l_iPos != -1) {
                    l_strCategory = l_strRestOfMsg.substring(0, l_iPos);
                    l_strRestOfMsg = l_strRestOfMsg.substring(l_iPos + " dr) ".length()); //3 ***   <FredKors>      [1 / 1   avg   3,00 (3,00)]    (01.84)

                    l_iPos = l_strRestOfMsg.indexOf(" ***");

                    if (l_iPos != -1)
                    {
                        l_strDice = l_strRestOfMsg.substring(0, l_iPos);
                        l_strRestOfMsg = l_strRestOfMsg.substring(l_iPos + " ***".length());//   <FredKors>      [1 / 8   avg   6,62 (6,62)]    (01.51 - by random.org)        

                        if (l_strDice.length() == 1)
                        {
                            int l_iDice = Integer.parseInt(l_strDice);

                            if ((l_iDice > 0)
                                && (l_iDice < 7))
                            {
                                String[] lar_strParts = FindUser(l_strRestOfMsg);

                                if ((!lar_strParts[1].isEmpty()) && (!lar_strParts[2].isEmpty()))
                                {
                                    l_strUser = lar_strParts[1];
                                    l_strRestOfMsg = lar_strParts[2]; // >      [1 / 8   avg   6,62 (6,62)]    (01.51 - by random.org)        

                                    l_strRestOfMsg = l_strRestOfMsg.replace(">", " ").trim();

                                    StyleConstants.setForeground(m_objMainStyle, Color.BLACK);
                                    StyleConstants.setBold(m_objMainStyle, true);

                                    string.append("\n" + BEFORE_CATEGORY + l_strCategory + "\t");
                                    //doc.insertString(doc.getLength(), "\n" + BEFORE_CATEGORY + l_strCategory + "\t", m_objMainStyle);

                                    StyleConstants.setForeground(m_objMainStyle, m_clrGameMsg);
                                    StyleConstants.setBold(m_objMainStyle, false);

                                    if (m_bUseDiceImages)
                                    {
                                        PaintIcon(l_iDice, DiceType.COLORED,true, (m_bShowDiceStats ? "" : l_strRestOfMsg));
                                    }
                                    else
                                    {
                                        StyleConstants.setBold(m_objMainStyle, true);
                                        string.append(l_strDice);

                                        //doc.insertString(doc.getLength(), l_strDice, m_objMainStyle);
                                        StyleConstants.setBold(m_objMainStyle, false);
                                    }
                                    string.append("  ...  ");
                                    //doc.insertString(doc.getLength(), "  ...  ", m_objMainStyle);

                                    StyleConstants.setBold(m_objMainStyle, true);
                                    string.append(l_strUser);
                                    //doc.insertString(doc.getLength(), l_strUser, m_objMainStyle);

                                    StyleConstants.setBold(m_objMainStyle, false);

                                    if (m_bShowDiceStats)
                                        string.append("   " + l_strRestOfMsg);
                                        //doc.insertString(doc.getLength(), "   " + l_strRestOfMsg, m_objMainStyle);
                                    else
                                        string.append(" ");
                                        //doc.insertString(doc.getLength(), " ", m_objMainStyle);

                                    // added by DR 2018 to add chatter text on Sniper Activation dr
                                    if (l_strCategory.equals("SA")) {
                                        StyleConstants.setBold(m_objMainStyle, true);
                                        String sniperstring="";
                                        if (l_iDice == 1) {
                                            sniperstring ="Eliminates SMC, Dummy stack, Sniper; Stuns & Recalls CE crew; breaks MMC & Inherent crew of certain vehicles; immobilizes unarmored vehicle (A14.3)" ;
                                            string.append("   " + sniperstring);
                                            //doc.insertString(doc.getLength(), "   " + sniperstring, m_objMainStyle);
                                        } else if (l_iDice == 2) {
                                            sniperstring ="Eliminates Dummy stack; Wounds SMC; Stuns CE crew; pins MMC, Inherent crew of certain vehicles, Sniper (A14.3)" ;
                                            string.append("   " + sniperstring);
                                            //doc.insertString(doc.getLength(), "   " + sniperstring, m_objMainStyle);
                                        }
                                        StyleConstants.setBold(m_objMainStyle, false);
                                    }
                                    FireDiceRoll(l_strCategory, l_strUser, "", l_iDice, -1);
                                }
                                else
                                    string.append("\n" + strMsg);
                                    //doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
                            }
                            else
                                string.append("\n" + strMsg);
                                //doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
                        }
                        else
                            string.append("\n" + strMsg);
                            //doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
                    }
                    else
                        string.append("\n" + strMsg);
                        //doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
                }
                else
                    string.append("\n" + strMsg);
                    //doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
            }

            string.append("</html>");

            //BR// I wonder if those /n being inserted up there should all be <br> ?

            //BR// Now insert our combined HTML string into the kit/doc
            kit.insertHTML(doc, doc.getLength(), string.toString(), 0, 0, null);

            //System.out.println(string.toString());

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void HandleSpecialMessagesForOtherDice(final String l_strCategory, final ArrayList<String> specialMessages,
                                                   final int l_iFirstDice, final int l_iSecondDice, final Map<DiceType, Integer> otherDice)
    {
      int total = l_iFirstDice + l_iSecondDice;
      int unmodifiedTotal = total;
      final String SPACE = " ";
      // Dust
      if(environment.dustInEffect() && m_DRNotificationLevel == 3)
      {
        switch (l_strCategory)
        {
          case "TH":
          case "IFT": {
            if (environment.isSpecialDust()) {
              total += environment.getSpecialDust(otherDice.get(DiceType.OTHER_DUST));
            } else {
              if (environment.isLightDust()) {
                total += Environment.getLightDust(otherDice.get(DiceType.OTHER_DUST));
              } else {
                total += Environment.getModerateDust(otherDice.get(DiceType.OTHER_DUST));
              }
            }
            specialMessages.add(environment.getCurrentDustLevel().toString() + SPACE);
            break;
          }

          case "MC": {
            if (environment.isSpecialDust()) {
              total -= environment.getSpecialDust(otherDice.get(DiceType.OTHER_DUST));
            } else {
              if (environment.isLightDust()) {
                total -= Environment.getLightDust(otherDice.get(DiceType.OTHER_DUST));
              } else {
                total -= Environment.getModerateDust(otherDice.get(DiceType.OTHER_DUST));
              }
            }
            specialMessages.add(environment.getCurrentDustLevel().toString() +" - if interdicted, MC is " + total);
            break;
          }
        }
      }
      // Night
      if(environment.isNight()  && m_DRNotificationLevel == 3)
      {
        switch (l_strCategory)
        {

          case "TH":
          case "IFT":
          {
            total += 1;
            specialMessages.add( "+1 Night LV"  + SPACE);
            break;
          }
        }
      }
      // LV
      if(environment.isLV())
      {
        LVLevel lvLevel = environment.getCurrentLVLevel();
        switch (l_strCategory) {
          case "TH":
          case "IFT":
          {
            switch (lvLevel)
            {
              case DAWN_DUSK: {
                total += 1;
                break;
              }

            }
            specialMessages.add(lvLevel.toString() + SPACE);
            break;
          }
        }
      }
      // Fog
      if(environment.isFog())
      {
        switch (l_strCategory)
        {

          case "TH":
          case "IFT":
          {
            FogLevel fogLevel = environment.getCurrentFogLevel();
            specialMessages.add(fogLevel.toString() + SPACE);
            break;
          }
        }
      }
      //Heat Haze
      if(environment.isHeatHaze())
      {
        switch (l_strCategory)
        {
          case "TH":
          case "IFT":
          {
            HeatHazeLevel heatHazeLevel = environment.getCurrentHeatHazeLevel();
            specialMessages.add(heatHazeLevel.toString() + SPACE);
            break;
          }
        }
      }
      //Sun Blindness
      if(environment.isSunBlindness())
      {
        switch (l_strCategory)
        {
          case "TH":
          case "IFT":
          {
            SunBlindnessLevel sunBlindnessLevel = environment.getCurrentSunBlindnessLevel();
            specialMessages.add(sunBlindnessLevel.toString() + SPACE);
            break;
          }
        }
      }

      if( unmodifiedTotal < total || environment.dustInEffect()) {
        specialMessages.add("Total: " + total);
      }
    }

    private void Parse3d6(String strMsg)
    {
        try
        {// *** 3d6 = 5,4,6 *** <FredKors>
            String l_strRestOfMsg = strMsg.substring("*** 3d6 = ".length());
            int l_iPos = l_strRestOfMsg.indexOf(" ***");
            String l_strUser = "";

            StyleConstants.setForeground(m_objMainStyle, m_clrGameMsg);

            if (l_iPos != -1)
            {
                String l_strLast = l_strRestOfMsg.substring(l_iPos);
                String l_strDice = l_strRestOfMsg.substring(0, l_iPos);

                if (l_strDice.length() == 5)
                {
                    String [] lar_strDice = l_strDice.split(",");

                    if (lar_strDice.length == 3)
                    {
                        int l_iFirstDice = Integer.parseInt(lar_strDice[0]);
                        int l_iSecondDice = Integer.parseInt(lar_strDice[1]);
                        int l_iThirdDice = Integer.parseInt(lar_strDice[2]);

                        if ((l_iFirstDice > 0)
                            && (l_iFirstDice < 7)
                            && (l_iSecondDice > 0)
                            && (l_iSecondDice < 7)
                            && (l_iThirdDice > 0)
                            && (l_iThirdDice < 7))
                        {
                            String[] lar_strParts = FindUser(l_strLast);

                            if ((!lar_strParts[1].isEmpty()) && (!lar_strParts[2].isEmpty()))
                            {
                                l_strUser = lar_strParts[1];

                                doc.insertString(doc.getLength(), "\n*** 3d6 = ", m_objMainStyle);
                                if (m_bUseDiceImages)
                                {
                                    PaintIcon(l_iFirstDice, DiceType.COLORED,false, "");
                                    doc.insertString(doc.getLength(), " ", m_objMainStyle);
                                    PaintIcon(l_iSecondDice, DiceType.WHITE,false, "");
                                    doc.insertString(doc.getLength(), " ", m_objMainStyle);
                                    PaintIcon(l_iThirdDice,DiceType.COLORED, true, "");
                                }
                                else
                                {
                                    StyleConstants.setBold(m_objMainStyle, true);
                                    doc.insertString(doc.getLength(), l_strDice, m_objMainStyle);
                                    StyleConstants.setBold(m_objMainStyle, false);
                                }
                                doc.insertString(doc.getLength(), lar_strParts[0], m_objMainStyle);
                                StyleConstants.setBold(m_objMainStyle, true);
                                doc.insertString(doc.getLength(), lar_strParts[1], m_objMainStyle);
                                StyleConstants.setBold(m_objMainStyle, false);
                                doc.insertString(doc.getLength(), lar_strParts[2], m_objMainStyle);

                                FireDiceRoll("", l_strUser, "", l_iFirstDice, l_iSecondDice);
                                FireDiceRoll("", l_strUser, "", l_iThirdDice, -1);
                            }
                            else
                            {
                                doc.insertString(doc.getLength(), "\n*** 3d6 = ", m_objMainStyle);
                                PaintIcon(l_iFirstDice, DiceType.COLORED,false, "");
                                doc.insertString(doc.getLength(), " ", m_objMainStyle);
                                PaintIcon(l_iSecondDice, DiceType.WHITE,false, "");
                                doc.insertString(doc.getLength(), " ", m_objMainStyle);
                                PaintIcon(l_iThirdDice, DiceType.COLORED,true, "");
                                doc.insertString(doc.getLength(), l_strLast, m_objMainStyle);

                                FireDiceRoll("", "?", "", l_iFirstDice, l_iSecondDice);
                                FireDiceRoll("", "?", "", l_iThirdDice, -1);
                            }
                        }
                        else
                            doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
                    }
                    else
                        doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
                }
                else
                    doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
            }
            else
                doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void ParseOldDR(String strMsg)
    {
        try
        {   //*** DR = 1,6 *** <FredKors> Axis/Allied SAN
            // *** DR = 1,3 *** <FredKors> Allied SAN
            // *** DR = 5,2 *** <FredKors> Axis SAN
            String l_strRestOfMsg = strMsg.substring("*** DR = ".length());
            int l_iPos = l_strRestOfMsg.indexOf(" ***");
            String l_strUser = "", l_strSAN = "";

            StyleConstants.setForeground(m_objMainStyle, m_clrGameMsg);

            if (l_iPos != -1)
            {
                String l_strLast = l_strRestOfMsg.substring(l_iPos);
                String l_strDice = l_strRestOfMsg.substring(0, l_iPos);

                if (l_strDice.length() == 3)
                {
                    String [] lar_strDice = l_strDice.split(",");

                    if (lar_strDice.length == 2)
                    {
                        int l_iFirstDice = Integer.parseInt(lar_strDice[0]);
                        int l_iSecondDice = Integer.parseInt(lar_strDice[1]);

                        if ((l_iFirstDice > 0)
                            && (l_iFirstDice < 7)
                            && (l_iSecondDice > 0)
                            && (l_iSecondDice < 7))
                        {
                            String[] lar_strParts = FindUser(l_strLast);

                            if ((!lar_strParts[1].isEmpty()) && (!lar_strParts[2].isEmpty()))
                            {
                                l_strUser = lar_strParts[1];

                                doc.insertString(doc.getLength(), "\n*** DR = ", m_objMainStyle);
                                if (m_bUseDiceImages)
                                {
                                    PaintIcon(l_iFirstDice, DiceType.COLORED,false, "");
                                    doc.insertString(doc.getLength(), " ", m_objMainStyle);
                                    PaintIcon(l_iSecondDice, DiceType.WHITE,false, "");
                                }
                                else
                                {
                                    StyleConstants.setBold(m_objMainStyle, true);
                                    doc.insertString(doc.getLength(), l_strDice, m_objMainStyle);
                                    StyleConstants.setBold(m_objMainStyle, false);
                                }
                                doc.insertString(doc.getLength(), lar_strParts[0], m_objMainStyle);
                                StyleConstants.setBold(m_objMainStyle, true);
                                doc.insertString(doc.getLength(), lar_strParts[1], m_objMainStyle);
                                StyleConstants.setBold(m_objMainStyle, false);
                                doc.insertString(doc.getLength(), lar_strParts[2], m_objMainStyle);

                                if (lar_strParts[2].contains("Axis SAN"))
                                {
                                    l_strSAN = "Axis SAN";
                                }
                                else if (lar_strParts[2].contains("Allied SAN"))
                                {
                                    l_strSAN = "Allied SAN";
                                }
                                else if (lar_strParts[2].contains("Axis/Allied SAN"))
                                {
                                    l_strSAN = "Axis/Allied SAN";
                                }

                                FireDiceRoll("", l_strUser, l_strSAN, l_iFirstDice, l_iSecondDice);
                            }
                            else
                            {
                                doc.insertString(doc.getLength(), "\n*** DR = ", m_objMainStyle);
                                PaintIcon(l_iFirstDice, DiceType.COLORED, false, "");
                                doc.insertString(doc.getLength(), " ", m_objMainStyle);
                                PaintIcon(l_iSecondDice, DiceType.WHITE, false, "");
                                doc.insertString(doc.getLength(), l_strLast, m_objMainStyle);

                                if (l_strLast.contains("Axis SAN"))
                                {
                                    l_strSAN = "Axis SAN";
                                }
                                else if (l_strLast.contains("Allied SAN"))
                                {
                                    l_strSAN = "Allied SAN";
                                }
                                else if (l_strLast.contains("Axis/Allied SAN"))
                                {
                                    l_strSAN = "Axis/Allied SAN";
                                }

                                FireDiceRoll("", "?", l_strSAN, l_iFirstDice, l_iSecondDice);
                            }
                        }
                        else
                            doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
                    }
                    else
                        doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
                }
                else
                    doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
            }
            else
                doc.insertString(doc.getLength(), "\n" + strMsg, m_objMainStyle);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void PaintIcon(int l_iDice, DiceType diceType, boolean bSingle, String strTooltip) throws BadLocationException, IOException
    {
        //BR// Will need to insert an <img= > tag here. Example:
        //BR// <img src="d6-1-grey.png" width="14" height="14">
        //BR// Any image you have in your "images" subdirectory can be referenced by filename in the tag, and you can change the height/width if you need to.
        //BR// I do see your code also *recolors* these on the fly as well -- that may require fancier footwork than I'm aware of how to do, or more likely put images of dice in the colors you want to support individually into the module

        //BR// For now I just put in this placeholder string insertion
        String temp = "<html>" +  l_iDice + " (" + diceType.toString() + ") " + (bSingle ? "(Single)" : "(NotSingle)") + " " + strTooltip + "\n" + "</html>";
        kit.insertHTML(doc, doc.getLength(), temp, 0, 0, null);

        /*
        JLabel l_objLabel = null;
        try
        {
            if(diceType == DiceType.OTHER_DUST)
            {
                l_objLabel = new JLabel(mar_objOtherColoredDCIcon[l_iDice - 1]);
            }
            else
            {
                l_objLabel = new JLabel((bSingle ? mar_objSingleDieIcon[l_iDice - 1] : (diceType == DiceType.COLORED ? mar_objColoredDCIcon[l_iDice - 1] : mar_objWhiteDCIcon[l_iDice - 1])));
            }
            l_objLabel.setAlignmentY(0.7f);

            if (!strTooltip.isEmpty())
                l_objLabel.setToolTipText(strTooltip.trim());

            StyleConstants.setComponent(m_objIconStyle, l_objLabel);
            doc.insertString(doc.getLength(), "Ignored", m_objIconStyle);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        */
    }

    private void RebuildStyles()
    {
        SimpleAttributeSet attribs = new SimpleAttributeSet();

        StyleConstants.setAlignment(attribs, StyleConstants.ALIGN_LEFT);
        StyleConstants.setFontFamily(attribs, m_objChatterFont.getFamily());
        StyleConstants.setFontSize(attribs, m_objChatterFont.getSize());
        StyleConstants.setSpaceAbove(attribs, 2);
        StyleConstants.setSpaceBelow(attribs, 2);

        send("- ");
        send("- Chatter font changed");
        send("- ");

        FontMetrics l_objFM = conversationPane.getFontMetrics(m_objChatterFont);

        //float l_f = (float)l_objFM.stringWidth(BEFORE_CATEGORY + ASLDiceBot.OTHER_CATEGORY + "XXX");
        //TabStop[] lar_objTabs = new TabStop[10]; // this sucks

        //for(int l_i = 0; l_i < lar_objTabs.length; l_i++)
        //{
        //     lar_objTabs[l_i] = new TabStop(l_f * (l_i + 1), TabStop.ALIGN_LEFT, TabStop.LEAD_NONE);
        //}

        //TabSet l_objTabset = new TabSet(lar_objTabs);

        //StyleConstants.setTabSet(attribs, new TabSet(new TabStop[0]));
        //StyleConstants.setTabSet(attribs, l_objTabset);

        //conversationPane.setParagraphAttributes(attribs, true);
    }

    @Override
    public void setFont(Font f)
    {
        if (m_edtInputText != null)
        {
            if (m_edtInputText.getText().length() == 0)
            {
                m_edtInputText.setText("XXX"); //$NON-NLS-1$
                m_edtInputText.setFont(f);
                m_edtInputText.setText(""); //$NON-NLS-1$

            }
            else
            {
                m_edtInputText.setFont(f);
            }
        }

        m_objChatterFont = f;

        super.setFont(f); //BR// Tell VASSAL about the font and let it make its stylesheet and stuff
    }

    @Override
    public void build(org.w3c.dom.Element e)
    {
    }

    @Override
    public org.w3c.dom.Element getBuildElement(org.w3c.dom.Document doc)
    {
        return doc.createElement(getClass().getName());
    }

    private void RebuildColoredDiceFaces()
    {
        BufferedImage l_objImage = null;

        try
        {
            for (int l_i = 0; l_i < 6; l_i++)
            {
                l_objImage = Op.load(String.format(m_strFileNameFormat, String.valueOf(l_i + 1), "W")).getImage(null);
                mar_objColoredDCIcon[l_i] = new ImageIcon(ColorChanger.changeColor(l_objImage, Color.white, m_clrColoredDiceColor));
                mar_objOtherColoredDCIcon[l_i] = new ImageIcon(ColorChanger.changeColor(l_objImage, Color.white, m_clrDustColoredDiceColor));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private void RebuildSingleDieFaces()
    {
        BufferedImage l_objImage = null;

        try
        {
            for (int l_i = 0; l_i < 6; l_i++)
            {
                l_objImage = Op.load(String.format(m_strFileNameFormat, String.valueOf(l_i + 1), "W")).getImage(null);
                mar_objSingleDieIcon[l_i] = new ImageIcon(ColorChanger.changeColor(l_objImage, Color.white, m_clrSingleDieColor));
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

   /**
   * Expects to be added to a GameModule.  Adds itself to the
   * controls window and registers itself as a
   * {@link CommandEncoder} */
    @Override
    public void addTo(Buildable b)
    {
        //BR// Let the VASSAL chatter attempt to initialize itself (so its stylesheets have some hope of being right)
        //BR// This probably duplicates a couple objects (or makes and then destroys some), but it gets us off the ground
        //BR// with the HTML & "Quick Colors" stylings working right
        super.addTo(b);

        GameModule l_objGameModule = (GameModule) b;

        if (l_objGameModule.getChatter() != null)
        {
            try
            {
                l_objGameModule.getControlPanel().remove(l_objGameModule.getChatter());
                l_objGameModule.removeCommandEncoder(l_objGameModule.getChatter());
                l_objGameModule.remove(l_objGameModule.getChatter());
            }
            catch(Exception ex)
            { }
        }

        l_objGameModule.setChatter(this);
        l_objGameModule.addCommandEncoder(this);
        l_objGameModule.addKeyStrokeSource(new KeyStrokeSource(this, WHEN_ANCESTOR_OF_FOCUSED_COMPONENT));
        l_objGameModule.getControlPanel().add(this, BorderLayout.CENTER);

        final Prefs l_objModulePrefs = l_objGameModule.getPrefs();

        FontConfigurer l_objChatFontConfigurer = null;
        FontConfigurer l_objChatFontConfigurer_Exist = (FontConfigurer)l_objModulePrefs.getOption("ChatFont");

        if (l_objChatFontConfigurer_Exist == null)
        {
            l_objChatFontConfigurer = new FontConfigurer(CHAT_FONT, Resources.getString("Chatter.chat_font_preference")); //$NON-NLS-1$ //$NON-NLS-2$
            l_objModulePrefs.addOption(Resources.getString("Chatter.chat_window"), l_objChatFontConfigurer); //$NON-NLS-1$
        }
        else
            l_objChatFontConfigurer = l_objChatFontConfigurer_Exist;

        l_objChatFontConfigurer.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                setFont((Font) evt.getNewValue());
                RebuildStyles();
            }
        });

        makeStyleSheet((Font)l_objChatFontConfigurer.getValue());
        l_objChatFontConfigurer.fireUpdate();

        FontConfigurer l_objButtonsFontConfigurer = null;
        FontConfigurer l_objButtonsFontConfigurer_Exist = (FontConfigurer)l_objModulePrefs.getOption("ButtonFont");

        if (l_objButtonsFontConfigurer_Exist == null)
        {
            l_objButtonsFontConfigurer = new FontConfigurer(BUTTON_FONT, "Chatter's dice buttons font: "); //$NON-NLS-1$ //$NON-NLS-2$
            l_objModulePrefs.addOption(Resources.getString("Chatter.chat_window"), l_objButtonsFontConfigurer); //$NON-NLS-1$
        }
        else
            l_objButtonsFontConfigurer = l_objButtonsFontConfigurer_Exist;

        l_objButtonsFontConfigurer.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                SetButtonsFonts((Font) evt.getNewValue());
            }
        });

        l_objButtonsFontConfigurer.fireUpdate();


        ColorConfigurer l_objBackgroundColor = null;
        ColorConfigurer l_objBackgroundColor_Exist = (ColorConfigurer)l_objModulePrefs.getOption(CHAT_BACKGROUND_COLOR);

        if (l_objBackgroundColor_Exist == null)
        {
            l_objBackgroundColor = new ColorConfigurer(CHAT_BACKGROUND_COLOR, "Background color: ", Color.white); //$NON-NLS-1$
            l_objModulePrefs.addOption(Resources.getString("Chatter.chat_window"), l_objBackgroundColor); //$NON-NLS-1$
        }
        else
            l_objBackgroundColor = l_objBackgroundColor_Exist;

        m_clrBackground = (Color) l_objModulePrefs.getValue(CHAT_BACKGROUND_COLOR);

        l_objBackgroundColor.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent e)
            {
                m_clrBackground = (Color) e.getNewValue();
                conversationPane.setBackground(m_clrBackground);
            }
        });

        l_objBackgroundColor.fireUpdate();


        ColorConfigurer l_objGameMsgColor = null;
        ColorConfigurer l_objGameMsgColor_Exist = (ColorConfigurer)l_objModulePrefs.getOption(GAME_MSG1_COLOR);

        if (l_objGameMsgColor_Exist == null)
        {
            l_objGameMsgColor = new ColorConfigurer(GAME_MSG1_COLOR, Resources.getString("Chatter.game_messages_preference"), Color.magenta); //$NON-NLS-1$
            l_objModulePrefs.addOption(Resources.getString("Chatter.chat_window"), l_objGameMsgColor); //$NON-NLS-1$
        }
        else
            l_objGameMsgColor = l_objGameMsgColor_Exist;

        m_clrGameMsg = (Color) l_objModulePrefs.getValue(GAME_MSG1_COLOR);

        l_objGameMsgColor.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent e)
            {
                m_clrGameMsg = (Color) e.getNewValue();
            }
        });

        ColorConfigurer l_objSystemMsgColor = null;
        ColorConfigurer l_objSystemMsgColor_Exist = (ColorConfigurer)l_objModulePrefs.getOption(SYS_MSG_COLOR);

        if (l_objSystemMsgColor_Exist == null)
        {
            l_objSystemMsgColor = new ColorConfigurer(SYS_MSG_COLOR, Resources.getString("Chatter.system_message_preference"), new Color(160, 160, 160)); //$NON-NLS-1$
            l_objModulePrefs.addOption(Resources.getString("Chatter.chat_window"), l_objSystemMsgColor); //$NON-NLS-1$
        }
        else
            l_objSystemMsgColor = l_objSystemMsgColor_Exist;

        m_clrSystemMsg = (Color) l_objModulePrefs.getValue(SYS_MSG_COLOR);

        l_objSystemMsgColor.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent e)
            {
                m_clrSystemMsg = (Color) e.getNewValue();
            }
        });

        ColorConfigurer l_objMyChatColor = null;
        ColorConfigurer l_objMyChatColor_Exist = (ColorConfigurer)l_objModulePrefs.getOption(MY_CHAT_COLOR);

        if (l_objMyChatColor_Exist == null)
        {
            l_objMyChatColor = new ColorConfigurer(MY_CHAT_COLOR, Resources.getString("Chatter.my_text_preference"), Color.gray); //$NON-NLS-1$
            l_objModulePrefs.addOption(Resources.getString("Chatter.chat_window"), l_objMyChatColor); //$NON-NLS-1$
        }
        else
            l_objMyChatColor = l_objMyChatColor_Exist;

        m_crlMyChatMsg = (Color) l_objModulePrefs.getValue(MY_CHAT_COLOR);

        l_objMyChatColor.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent e)
            {
                m_crlMyChatMsg = (Color) e.getNewValue();
            }
        });

        ColorConfigurer l_objOtherChatColor = null;
        ColorConfigurer l_objOtherChatColor_Exist = (ColorConfigurer)l_objModulePrefs.getOption(OTHER_CHAT_COLOR);

        if (l_objOtherChatColor_Exist == null)
        {
            l_objOtherChatColor = new ColorConfigurer(OTHER_CHAT_COLOR, Resources.getString("Chatter.other_text_preference"), Color.black); //$NON-NLS-1$
            l_objModulePrefs.addOption(Resources.getString("Chatter.chat_window"), l_objOtherChatColor); //$NON-NLS-1$
        }
        else
            l_objOtherChatColor = l_objOtherChatColor_Exist;

        m_clrOtherChatMsg = (Color) l_objModulePrefs.getValue(OTHER_CHAT_COLOR);

        l_objOtherChatColor.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent e)
            {
                m_clrOtherChatMsg = (Color) e.getNewValue();
            }
        });

        BooleanConfigurer l_objUseDiceImagesOption = null;
        BooleanConfigurer l_objUseDiceImagesOption_Exist = (BooleanConfigurer)l_objModulePrefs.getOption(USE_DICE_IMAGES);

        if (l_objUseDiceImagesOption_Exist == null)
        {
            l_objUseDiceImagesOption = new BooleanConfigurer(USE_DICE_IMAGES, "Use images for dice rolls", Boolean.TRUE);  //$NON-NLS-1$
            l_objModulePrefs.addOption(Resources.getString("Chatter.chat_window"), l_objUseDiceImagesOption); //$NON-NLS-1$
        }
        else
            l_objUseDiceImagesOption = l_objUseDiceImagesOption_Exist;

        m_bUseDiceImages = (Boolean) (l_objModulePrefs.getValue(USE_DICE_IMAGES));

        l_objUseDiceImagesOption.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent e)
            {
                m_bUseDiceImages = (Boolean) e.getNewValue();
            }
        });

        BooleanConfigurer l_objShowDiceStatsOption = null;
        BooleanConfigurer l_objShowDiceStatsOption_Exist = (BooleanConfigurer)l_objModulePrefs.getOption(SHOW_DICE_STATS);

        if (l_objShowDiceStatsOption_Exist == null)
        {
            l_objShowDiceStatsOption = new BooleanConfigurer(SHOW_DICE_STATS, "Show dice stats after each dice rolls", Boolean.FALSE);  //$NON-NLS-1$
            l_objModulePrefs.addOption(Resources.getString("Chatter.chat_window"), l_objShowDiceStatsOption); //$NON-NLS-1$
        }
        else
            l_objShowDiceStatsOption = l_objShowDiceStatsOption_Exist;

        m_bShowDiceStats = (Boolean) (l_objModulePrefs.getValue(SHOW_DICE_STATS));

        l_objShowDiceStatsOption.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent e)
            {
                m_bShowDiceStats = (Boolean) e.getNewValue();
            }
        });

        // TODO Resources ???
        //final ColorConfigurer l_objColoredDiceColor = new ColorConfigurer(COLORED_DICE_COLOR, Resources.getString("Chatter.colored_dice_color"), Color.YELLOW); //$NON-NLS-1$
        ColorConfigurer l_objColoredDiceColor = null;
        ColorConfigurer l_objColoredDiceColor_Exist = (ColorConfigurer)l_objModulePrefs.getOption(COLORED_DICE_COLOR);

        if (l_objColoredDiceColor_Exist == null)
        {
            l_objColoredDiceColor = new ColorConfigurer(COLORED_DICE_COLOR, "Colored die color:  ", Color.YELLOW); //$NON-NLS-1$
            l_objModulePrefs.addOption(Resources.getString("Chatter.chat_window"), l_objColoredDiceColor); //$NON-NLS-1$
        }
        else
            l_objColoredDiceColor = l_objColoredDiceColor_Exist;

        l_objColoredDiceColor.addPropertyChangeListener(new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent e)
            {
                m_clrColoredDiceColor = (Color) e.getNewValue();
                RebuildColoredDiceFaces();
            }
        });

        l_objColoredDiceColor.fireUpdate();

        // TODO Resources ???
        //final ColorConfigurer l_objColoredDiceColor = new ColorConfigurer(SINGLE_DIE_COLOR, Resources.getString("Chatter.single_die_color"), Color.RED); //$NON-NLS-1$
        ColorConfigurer l_objColoredDieColor = null;
        ColorConfigurer l_objColoredDieColor_Exist = (ColorConfigurer)l_objModulePrefs.getOption(SINGLE_DIE_COLOR);

        if (l_objColoredDieColor_Exist == null)
        {
            l_objColoredDieColor = new ColorConfigurer(SINGLE_DIE_COLOR, "Single die color:  ", Color.RED); //$NON-NLS-1$
            l_objModulePrefs.addOption(Resources.getString("Chatter.chat_window"), l_objColoredDieColor); //$NON-NLS-1$
        }
        else
            l_objColoredDieColor = l_objColoredDieColor_Exist;

        l_objColoredDieColor.addPropertyChangeListener(new PropertyChangeListener()
        {
          public void propertyChange(PropertyChangeEvent e)
          {
            m_clrSingleDieColor = (Color) e.getNewValue();
            RebuildSingleDieFaces();
          }
        });

        ColorConfigurer l_objThirdDieColor = null;
        ColorConfigurer l_objThirdDieColor_Exist = (ColorConfigurer)l_objModulePrefs.getOption(THIRD_DIE_COLOR);

        if (l_objThirdDieColor == null)
        {
          l_objThirdDieColor = new ColorConfigurer(THIRD_DIE_COLOR, "Third die color:  ", Color.GRAY); //$NON-NLS-1$
          l_objModulePrefs.addOption(Resources.getString("Chatter.chat_window"), l_objThirdDieColor); //$NON-NLS-1$
        } else {
          l_objThirdDieColor = l_objThirdDieColor_Exist;
        }
        l_objThirdDieColor.addPropertyChangeListener(new PropertyChangeListener()
        {
          public void propertyChange(PropertyChangeEvent e)
          {
            m_clrDustColoredDiceColor = (Color) e.getNewValue();
            RebuildColoredDiceFaces();
          }
        });
        l_objThirdDieColor.fireUpdate();

        StringEnumConfigurer l_objSpecialDiceRollNotificationLevel = (StringEnumConfigurer)l_objModulePrefs.getOption(NOTIFICATION_LEVEL);

        final String[] l_DROptions = {
                "None",
                "Snipers only",
                "Starter Kit",
                "Full ASL"
        };
        if(l_objSpecialDiceRollNotificationLevel == null)
        {
            l_objSpecialDiceRollNotificationLevel = new StringEnumConfigurer(NOTIFICATION_LEVEL,
                    "Notify about special DRs: ", l_DROptions);
            l_objSpecialDiceRollNotificationLevel.setValue("Full ASL");
            l_objModulePrefs.addOption(Resources.getString("Chatter.chat_window"), l_objSpecialDiceRollNotificationLevel);
        }

        for(int i = 0; i < l_DROptions.length; ++i)
        {
            if (l_DROptions[i].equals(l_objSpecialDiceRollNotificationLevel.getValueString()))
            {
                m_DRNotificationLevel = i;
                break;
            }
        }

        // just for access from inside the event handler
        final StringEnumConfigurer __cfg = l_objSpecialDiceRollNotificationLevel;
        l_objSpecialDiceRollNotificationLevel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e)
            {
                for(int i = 0; i < l_DROptions.length; ++i){
                    if(l_DROptions[i].equals(__cfg.getValueString()))
                    {
                        m_DRNotificationLevel = i;
                        return;
                    }
                }
                m_DRNotificationLevel = 3;
            }
        });

        l_objColoredDieColor.fireUpdate();
        final BooleanConfigurer AlwaysOnTop = new BooleanConfigurer("PWAlwaysOnTop", "Player Window (menus, toolbar, chat) is always on top in uncombined application mode (requires a VASSAL restart)", false);
        getGameModule().getPrefs().addOption(preferenceTabName, AlwaysOnTop);
    }

    @Override
    public void add(Buildable b)
    {
    }

    @Override
    public void keyCommand(KeyStroke e)
    {
        if ((e.getKeyCode() == 0 || e.getKeyCode() == KeyEvent.CHAR_UNDEFINED)
            && !Character.isISOControl(e.getKeyChar()))
        {
            m_edtInputText.setText(m_edtInputText.getText() + e.getKeyChar());
        }
        else if (e.isOnKeyRelease())
        {
            switch (e.getKeyCode())
            {
                case KeyEvent.VK_ENTER:
                    if (m_edtInputText.getText().length() > 0)
                    {
                        send(formatChat(m_edtInputText.getText()));
                    }

                    m_edtInputText.setText(""); //$NON-NLS-1$
                    break;

                case KeyEvent.VK_BACK_SPACE:
                case KeyEvent.VK_DELETE:
                    String s = m_edtInputText.getText();
                    if (s.length() > 0)
                    {
                        m_edtInputText.setText(s.substring(0, s.length() - 1));
                    }
                    break;
            }
        }
    }

  /*public static void main(String[] args)
  {
    ASLChatter chat = new ASLChatter();
    JFrame f = new JFrame();
    f.add(chat);
    f.pack();
    f.setVisible(true);
  }*/

  private void FireDiceRoll(String strCategory, String strUser, String strSAN, int iFirstDice, int iSecondDice) {
        for (ChatterListener objListener : chatter_listeners)
            objListener.DiceRoll(strCategory, strUser, strSAN, iFirstDice, iSecondDice);
  }

  public void addListener(ChatterListener toAdd) {
        chatter_listeners.add(toAdd);
  }

  public void removeListener(ChatterListener toRemove) {
        chatter_listeners.remove(toRemove);
  }

  public interface ChatterListener {
        public void DiceRoll(String strCategory, String strUser, String strSAN, int iFirstDice, int iSecondDice);
  }
}

