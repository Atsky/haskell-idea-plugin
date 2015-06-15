package org.jetbrains.haskell.module

import com.intellij.openapi.roots.ui.configuration.BuildElementsEditor
import com.intellij.openapi.roots.ui.configuration.ModuleConfigurationState
import com.intellij.openapi.roots.ui.configuration.ModuleElementsEditor
import org.jetbrains.annotations.Nls
import javax.swing.*
import com.intellij.ui.components.JBList
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import java.awt.BorderLayout
import com.intellij.openapi.project.Project
import java.util.Collections
import javax.swing.event.ListDataListener
import javax.swing.event.ListDataEvent
import java.util.Enumeration

public class PackagesEditor(state: ModuleConfigurationState, val project : Project) : ModuleElementsEditor(state) {
    val packages = DefaultListModel<String>()

    protected override fun createComponentImpl(): JComponent {
        val resultPanel = JPanel(BorderLayout())

        for (pkg in CabalPackagesContainer.getInstance(project).getState()!!.myPackages!!) {
            packages.addElement(pkg)
        }
        val list = JBList(packages)

        val actionGroup = DefaultActionGroup()
        actionGroup.add(AddPackageAction(packages))
        actionGroup.add(RemovePackageAction(packages, list))

        val toolbar = ActionManager.getInstance()!!.createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, true)!!

        resultPanel.add(toolbar.getComponent()!!, BorderLayout.PAGE_START)
        resultPanel.add(list, BorderLayout.CENTER)

        return resultPanel
    }


    override fun isModified(): Boolean {
        return true;
    }
    public override fun saveData(): Unit {
        CabalPackagesContainer.getInstance(project).getState()!!.myPackages =
                Collections.list(packages.elements()) as List<String>;
    }

    public override fun getDisplayName(): String {
        return "Packages"
    }

    public override fun getHelpTopic(): String {
        return ""
    }

}
