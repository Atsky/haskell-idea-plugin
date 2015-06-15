package org.jetbrains.haskell.module

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.actionSystem.PlatformDataKeys
import javax.swing.DefaultListModel

public class AddPackageAction(val packages: DefaultListModel<String>) :
                            AnAction("Add package", null, AllIcons.General.Add) {

    public override fun actionPerformed(e: AnActionEvent?): Unit {
        val dataContext = e!!.getDataContext()

        val project = PlatformDataKeys.PROJECT_CONTEXT.getData(dataContext)!!

        val packageName = Messages.showInputDialog(project, "Enter package name", "New package", Messages.getQuestionIcon())
        if (packageName != null) {
            packages.addElement(packageName);
        }
    }

    public override fun update(event: AnActionEvent?): Unit {

    }

}
