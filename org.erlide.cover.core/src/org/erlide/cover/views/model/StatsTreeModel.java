package org.erlide.cover.views.model;

import java.io.Serializable;
import java.util.Calendar;

/**
 * 
 * Data model for statatistics viewer
 * 
 * @author Aleksandra Lipiec <aleksandra.lipiec@erlang-solutions.com>
 * 
 */
public class StatsTreeModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private static StatsTreeModel model;

    private ICoverageObject root;
    private String timestamp;

    private StatsTreeModel() {
        initialize();
        timestamp = "";
    }

    public static StatsTreeModel getInstance() {
        if (model == null) {
            model = new StatsTreeModel();
        }
        return model;
    }

    public static void changeInstance(StatsTreeModel mod) {
        model = mod;
    }
    
    public ICoverageObject getRoot() {
        return root;
    }

    public void clear() {
        root.removeAllChildren();
        root.setLiniesCount(0);
        root.setCoverCount(0);

        StringBuilder timeTmp = new StringBuilder();
        timeTmp.append(Calendar.getInstance().get(Calendar.YEAR))
                .append(String.format("%02d",
                        Calendar.getInstance().get(Calendar.MONTH) + 1))
                .append(String.format("%02d",
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)))
                .append(String.format("%02d",
                        Calendar.getInstance().get(Calendar.HOUR_OF_DAY)))
                .append(String.format("%02d",
                        Calendar.getInstance().get(Calendar.MINUTE)))
                .append(String.format("%02d",
                        Calendar.getInstance().get(Calendar.SECOND)));
        
        timestamp = timeTmp.toString();

        ModuleSet.clear();
    }

    public void addTotal(final int allLines, final int coveredLines) {
        final int all = root.getLinesCount() + allLines;
        final int cov = root.getCoverCount() + coveredLines;
        root.setLiniesCount(all);
        root.setCoverCount(cov);
    }

    public void setIndex(final String path) {
        root.setHtmlPath(path);
    }

    public void setRootLabel(final String name) {
        root.setLabel(name);
    }
    
    public String getTimestamp() {
        return timestamp;
    }

    private void initialize() {
        root = new StatsTreeObject("total", 0, 0, ObjectType.PROJECT);
    }

}
