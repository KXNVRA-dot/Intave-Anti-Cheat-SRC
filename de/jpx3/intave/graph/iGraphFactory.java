// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.graph;

import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

public final class iGraphFactory
{
    public static iGraph createGraph() {
        return new iGraph();
    }
    
    public static iGraph createSimpleGraph(final iGraphLine line) {
        final iGraph graph = new iGraph();
        final List<iGraphLine> lines = new ArrayList<iGraphLine>();
        lines.add(line);
        graph.setGraphLines(lines);
        return graph;
    }
    
    public static iGraph createSimpleGraphWithText(final iGraphLine line, final iGraphTextComponent textComponent) {
        final iGraph graph = new iGraph();
        final List<iGraphLine> lines = new ArrayList<iGraphLine>();
        lines.add(line);
        graph.setGraphLines(lines);
        final List<iGraphTextComponent> componentList = new ArrayList<iGraphTextComponent>();
        componentList.add(textComponent);
        graph.setTextComponents(componentList);
        return graph;
    }
    
    public static iGraph createGraphWithMulipleLines(final List<iGraphLine> lines) {
        final iGraph graph = new iGraph();
        graph.setGraphLines(lines);
        return graph;
    }
    
    public static iGraph createGraphWithMulipleLinesAndText(final List<iGraphLine> lines, final List<iGraphTextComponent> textComponents) {
        final iGraph graph = new iGraph();
        graph.setGraphLines(lines);
        graph.setTextComponents(textComponents);
        return graph;
    }
    
    public static void safeToFile(final iGraph graph, final String filePatch) throws IOException {
        graph.loadAndSaveTo(new File(filePatch));
    }
    
    public static void safeToFile(final iGraph graph, final File file) throws IOException {
        graph.loadAndSaveTo(file);
    }
}
