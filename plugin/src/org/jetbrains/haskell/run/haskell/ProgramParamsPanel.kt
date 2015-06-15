package org.jetbrains.haskell.run.haskell

import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.module.Module
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import javax.swing.*
import java.awt.GridBagLayout
import java.awt.GridBagConstraints
import org.jetbrains.haskell.util.*
import com.intellij.ui.RawCommandLineEditor
import com.intellij.execution.configuration.EnvironmentVariablesComponent
import java.awt.Insets
import org.jetbrains.haskell.run.ModuleComboBoxRenderer
import java.awt.BorderLayout

class ProgramParamsPanel(modules: Array<Module>) : JPanel() {
    private var executableComponent: JTextField
    private var moduleComboBox: JComboBox<Module>
    private var programParametersComponent : RawCommandLineEditor
    private var workingDirectoryComponent : TextFieldWithBrowseButton
    private var environmentVariables : EnvironmentVariablesComponent


    public fun applyTo(s: CabalRunConfiguration): Unit {
        s.setModule(moduleComboBox.getSelectedItem() as Module?)
        s.setMyExecutableName(executableComponent.getText())
        s.setProgramParameters(programParametersComponent.getText())
        s.setWorkingDirectory(workingDirectoryComponent.getText())
        s.setEnvs(environmentVariables.getEnvs())
    }
    public fun reset(s: CabalRunConfiguration): Unit {
        executableComponent.setText(s.getMyExecutableName())
        programParametersComponent.setText(s.getProgramParameters())
        workingDirectoryComponent.setText(s.getWorkingDirectory())
        moduleComboBox.setSelectedItem(s.getModule())
        environmentVariables.setEnvs(s.getEnvs())
    }

    init {
        this.setLayout(GridBagLayout())
        executableComponent = JTextField();
        moduleComboBox = JComboBox(DefaultComboBoxModel(modules))
        moduleComboBox.setRenderer(ModuleComboBoxRenderer())

        programParametersComponent = RawCommandLineEditor()
        workingDirectoryComponent = TextFieldWithBrowseButton()
        environmentVariables = EnvironmentVariablesComponent()
        environmentVariables.setLabelLocation(BorderLayout.WEST)


        val base : () -> GridBagConstraints = {
            val result = GridBagConstraints()
            result.insets = Insets(3, 3, 3, 3)
            result.anchor = GridBagConstraints.LINE_START
            result
        }

        val moduleLabel = JLabel("Executable Name")
        add(moduleLabel, base().setConstraints {
            gridx = 0
            gridy = 0
            weightx = 0.1
            fill = GridBagConstraints.HORIZONTAL
        })

        add(executableComponent, base().setConstraints {
            gridx = 1
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        })

        add(JLabel("Program arguments"), base().setConstraints {
            weightx = 0.1
            gridy = 1
        })

        add(programParametersComponent, base().setConstraints {
            gridx = 1
            gridy = 1
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        })

        add(JLabel("Working directory"), base().setConstraints {
            gridx = 0
            gridy = 2
            weightx = 0.1
        })

        add(workingDirectoryComponent, base().setConstraints {
            gridx = 1
            gridy = 2
            weightx = 0.1
            fill = GridBagConstraints.HORIZONTAL
        })

        add(environmentVariables, base().setConstraints {
            gridx = 0
            gridwidth = 2
            gridy = 3
            weightx = 0.1
            fill = GridBagConstraints.HORIZONTAL
        })
        //environmentVariables.setAnchor(moduleLabel)

        add(JLabel("Module"), base().setConstraints {
            gridy = 4
            weightx = 0.1
        })

        add(moduleComboBox, base().setConstraints {
            gridx = 1
            gridy = 4
            fill = GridBagConstraints.HORIZONTAL
            weightx = 1.0
        })

    }

}
