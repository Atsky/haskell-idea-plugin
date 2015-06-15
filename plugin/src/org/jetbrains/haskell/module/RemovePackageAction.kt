package org.jetbrains.haskell.module

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import javax.swing.DefaultListModel
import javax.swing.ListSelectionModel
import com.intellij.ui.components.JBList

public class RemovePackageAction(val packages: DefaultListModel<String>,
                                 val list: JBList) : AnAction("Remove package", null, AllIcons.General.Remove) {

    public override fun actionPerformed(e: AnActionEvent?): Unit {
        packages.removeElement(list.getSelectedValue())
    }

    public override fun update(event: AnActionEvent?): Unit {

    }

}
