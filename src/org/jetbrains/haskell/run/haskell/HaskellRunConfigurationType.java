package org.jetbrains.haskell.run.haskell;

import com.intellij.execution.configuration.ConfigurationFactoryEx;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.haskell.icons.HaskellIcons;

import javax.swing.*;

public final class HaskellRunConfigurationType implements ConfigurationType {

    public static final HaskellRunConfigurationType INSTANCE = new HaskellRunConfigurationType();

    private final ConfigurationFactory myFactory;

    public HaskellRunConfigurationType() {
        this.myFactory = new ConfigurationFactoryEx(this) {
            public RunConfiguration createTemplateConfiguration(Project project) {
                return new CabalRunConfiguration(project, this);
            }
        };
    }

    public String getDisplayName() {
        return "Haskell";
    }

    public String getConfigurationTypeDescription() {
        return "Haskell application";
    }

    public Icon getIcon() {
        return HaskellIcons.APPLICATION;
    }

    @NotNull
    public String getId() {
        return "CabalRunConfiguration";
    }

    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[] {myFactory};
    }
}
