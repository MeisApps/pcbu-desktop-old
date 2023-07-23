package com.meisapps.pcbiounlock.ui.panels.pairing

import com.formdev.flatlaf.util.ScaledImageIcon
import com.meisapps.pcbiounlock.natives.NativeUtils
import com.meisapps.pcbiounlock.natives.WinUtils
import com.meisapps.pcbiounlock.ui.UIGlobals
import com.meisapps.pcbiounlock.ui.panels.RoundedPanel
import com.meisapps.pcbiounlock.utils.extensions.withFontSize
import com.meisapps.pcbiounlock.utils.host.OperatingSystem
import com.meisapps.pcbiounlock.utils.io.ResourceHelper
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.*


class UserGroup {
    var selectedPanel: UserPanel? = null
    private val panelList = ArrayList<UserPanel>()

    fun add(panel: UserPanel) {
        panel.group = this
        panelList.add(panel)

        if(panel.isLoggedIn)
            selectPanel(panel)
    }

    fun selectPanel(panel: UserPanel) {
        panelList.stream().filter { it != panel }.forEach {
            it.setSelected(false)
        }

        selectedPanel = panel
        selectedPanel?.setSelected(isSelected = true, notify = false)
    }
}

class UserPanel(displayName: String, val userName: String, val isLoggedIn: Boolean)
    : RoundedPanel(25, UIManager.getColor("List.background")) {
    var isSelectedItem = false
    var group: UserGroup? = null

    init {
        layout = GridBagLayout()
        var infoText = if(isLoggedIn) I18n.get("ui_current_user") else ""
        if(OperatingSystem.isWindows && userName.startsWith("MicrosoftAccount")) {
            infoText = if(infoText.isBlank())
                "Microsoft Account"
            else
                "$infoText, Microsoft Account"
        }

        val resImg = ImageIcon(ResourceHelper.getFileBytes("assets/user.png"))
        val userIcon = ScaledImageIcon(resImg, 48, 48)
        val userIconLbl = JLabel(userIcon)

        val userNameLbl = JLabel(displayName).withFontSize(UIGlobals.DefaultFontSize)
        userNameLbl.foreground = Color.WHITE
        val infoLbl = JLabel(infoText)
            .withFontSize(12F)

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.LINE_START
        gbc.weightx = 0.0
        gbc.weighty = 0.0

        gbc.insets = Insets(10, 10, 10, 10)
        gbc.gridheight = 2
        gbc.gridx = 0
        gbc.gridy = 0
        add(userIconLbl, gbc)

        gbc.gridheight = 1
        gbc.weightx = 1.0
        gbc.weighty = 0.0

        gbc.insets = Insets(15, 5, 5, 10)
        gbc.gridx = 1
        gbc.gridy = 0
        add(userNameLbl, gbc)
        gbc.insets = Insets(0, 5, 5, 5)
        gbc.gridx = 1
        gbc.gridy = 1
        add(infoLbl, gbc)

        addMouseListener(object : MouseListener {
            override fun mouseClicked(p0: MouseEvent?) {
                setSelected(true)
            }
            override fun mousePressed(p0: MouseEvent?) {}
            override fun mouseReleased(p0: MouseEvent?) {}
            override fun mouseEntered(p0: MouseEvent?) {}
            override fun mouseExited(p0: MouseEvent?) {}
        })
    }

    fun setSelected(isSelected: Boolean, notify: Boolean = true) {
        if(isSelected) {
            foreground = UIManager.getColor("List.selectionForeground")
            backgroundColor = UIManager.getColor("List.selectionBackground")
            if(notify)
                group?.selectPanel(this)
        } else {
            backgroundColor = UIManager.getColor("List.background")
        }

        isSelectedItem = isSelected
        repaint()
    }
}

class PairingUserSelectPanel(form: IPairingForm) : PairingPanel(form) {
    private val userGroup = UserGroup()
    private val rootPanel = JPanel()

    init {
        rootPanel.layout = GridBagLayout()

        val allUsers = NativeUtils.getForPlatform().getAllUsers()
        val currentUser = NativeUtils.getForPlatform().getCurrentUserName()

        val userPanels = ArrayList<UserPanel>(allUsers.size)
        allUsers.stream().forEach {
            var displayName = it
            if(OperatingSystem.isWindows) {
                val split = displayName.split('\\')
                displayName = split[1]
            }
            userPanels.add(UserPanel(displayName, it, it == currentUser))
        }

        val usersPanel = JPanel()
        usersPanel.layout = GridBagLayout()

        val scrollPane = JScrollPane(usersPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        scrollPane.border = BorderFactory.createEmptyBorder()

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.LINE_START
        gbc.insets = Insets(10, 25, 10, 25)
        gbc.weightx = 1.0
        gbc.weighty = 0.0

        for((count, userPanel) in userPanels.withIndex()) {
            gbc.gridx = 0
            gbc.gridy = count
            usersPanel.add(userPanel, gbc)
            userGroup.add(userPanel)
        }

        gbc.insets = Insets(0, 0, 0, 0)
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        rootPanel.add(scrollPane, gbc)
    }

    override fun initialize() {
        form.setDescription(I18n.get("ui_pairing_select_user"))
        //EventQueue.invokeLater { form.getNextButton().doClick() }
    }

    override fun onNextClicked(): PairingPanel? {
        if(userGroup.selectedPanel == null)
            return null
        if(OperatingSystem.isWindows && !WinUtils.hasUserPassword(userGroup.selectedPanel!!.userName)) {
            JOptionPane.showMessageDialog(form.getFrame(), I18n.get("ui_win_no_password"), I18n.get("error"), JOptionPane.ERROR_MESSAGE)
            return null
        }

        return PairingUserPasswordPanel(form, userGroup.selectedPanel!!.userName)
    }

    override fun onBackClicked() {
    }

    override fun getRootPanel(): JPanel {
        return rootPanel
    }
}