package org.erlide.core.internal.builder;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.erlide.engine.model.builder.BuilderConfigType;
import org.erlide.engine.model.root.ErlangProjectProperties;
import org.erlide.engine.model.root.PathSerializer;
import org.erlide.engine.model.root.ProjectConfigurationPersister;
import org.erlide.engine.model.root.ProjectConfigurator;
import org.erlide.engine.model.root.ProjectPreferencesConstants;
import org.erlide.runtime.runtimeinfo.RuntimeVersion;
import org.erlide.util.ErlLogger;
import org.osgi.service.prefs.BackingStoreException;

import com.google.common.base.Preconditions;

public class PreferencesProjectConfigurationPersister extends
        ProjectConfigurationPersister {

    private final String nodeKey;

    public PreferencesProjectConfigurationPersister(final String nodeKey) {
        Preconditions.checkNotNull(nodeKey);
        this.nodeKey = nodeKey;
    }

    public PreferencesProjectConfigurationPersister() {
        this(BuilderConfigType.INTERNAL.getConfigName());
    }

    private IEclipsePreferences getNode() {
        return new ProjectScope(getProject().getWorkspaceProject()).getNode(nodeKey);
    }

    @Override
    public ErlangProjectProperties getConfiguration() {
        final ErlangProjectProperties result = new ErlangProjectProperties();
        final IEclipsePreferences node = getNode();
        if (node == null) {
            ErlLogger.warn("Could not load project preferences for "
                    + getProject().getName());
            return null;
        }

        // TODO node.addListener(project);

        final String sourceDirsStr = node.get(ProjectPreferencesConstants.SOURCE_DIRS,
                ProjectPreferencesConstants.DEFAULT_SOURCE_DIRS);
        result.setSourceDirs(PathSerializer.unpackList(sourceDirsStr));
        final String includeDirsStr = node.get(ProjectPreferencesConstants.INCLUDE_DIRS,
                ProjectPreferencesConstants.DEFAULT_INCLUDE_DIRS);
        result.setIncludeDirs(PathSerializer.unpackList(includeDirsStr));
        final String outputDirsStr = node.get(ProjectPreferencesConstants.OUTPUT_DIR,
                ProjectPreferencesConstants.DEFAULT_OUTPUT_DIR);
        result.setOutputDir(new Path(outputDirsStr));
        result.setRequiredRuntimeVersion(new RuntimeVersion(node.get(
                ProjectPreferencesConstants.RUNTIME_VERSION, null)));
        if (!result.getRequiredRuntimeVersion().isDefined()) {
            result.setRequiredRuntimeVersion(new RuntimeVersion(
                    ProjectPreferencesConstants.DEFAULT_RUNTIME_VERSION));
        }
        result.setExternalModulesFile(node.get(
                ProjectPreferencesConstants.PROJECT_EXTERNAL_MODULES,
                ProjectPreferencesConstants.DEFAULT_EXTERNAL_MODULES));
        result.setExternalIncludesFile(node.get(
                ProjectPreferencesConstants.EXTERNAL_INCLUDES,
                ProjectPreferencesConstants.DEFAULT_EXTERNAL_INCLUDES));
        return result;
    }

    @Override
    public void setConfiguration(final ErlangProjectProperties info) {
        final IEclipsePreferences node = getNode();
        if (node == null) {
            ErlLogger.warn("Could not store project preferences for "
                    + getProject().getName());
            return;
        }

        node.put(ProjectPreferencesConstants.SOURCE_DIRS,
                PathSerializer.packList(info.getSourceDirs()));
        node.put(ProjectPreferencesConstants.INCLUDE_DIRS,
                PathSerializer.packList(info.getIncludeDirs()));
        node.put(ProjectPreferencesConstants.OUTPUT_DIR, info.getOutputDir()
                .toPortableString());
        node.put(ProjectPreferencesConstants.EXTERNAL_INCLUDES,
                info.getExternalIncludesFile());
        if (info.getRequiredRuntimeVersion().isDefined()) {
            node.put(ProjectPreferencesConstants.RUNTIME_VERSION, info
                    .getRequiredRuntimeVersion().asMinor().toString());
        } else {
            node.remove(ProjectPreferencesConstants.RUNTIME_VERSION);
        }
        node.put(ProjectPreferencesConstants.PROJECT_EXTERNAL_MODULES,
                info.getExternalModulesFile());

        try {
            node.flush();
        } catch (final BackingStoreException e) {
            ErlLogger.warn(e);
        }
    }

    @Override
    public ProjectConfigurator getConfigurator() {
        return null;
    }

}
