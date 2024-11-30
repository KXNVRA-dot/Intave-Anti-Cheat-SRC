// 
// Decompiled by Procyon v0.5.36
// 

package de.jpx3.intave.graph;

import java.io.IOException;
import java.awt.image.RenderedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.Iterator;
import java.awt.Graphics2D;
import java.util.Map;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

public final class iGraph
{
    private List<iGraphLine> iGraphLines;
    private List<iGraphTextComponent> textComponents;
    private BufferedImage finalImage;
    private Color backgroundColor;
    private Color renderColor;
    private final Dimension dimension;
    
    iGraph() {
        this.iGraphLines = new ArrayList<iGraphLine>();
        this.textComponents = new ArrayList<iGraphTextComponent>();
        this.finalImage = null;
        this.backgroundColor = Color.black;
        this.renderColor = Color.cyan;
        this.dimension = new Dimension(1280, 720);
    }
    
    private BufferedImage renderImage() {
        final BufferedImage bufferedImage = new BufferedImage(this.dimension.width, this.dimension.height, 2);
        final Graphics2D graphics = bufferedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(this.backgroundColor);
        graphics.fillRect(0, 0, this.dimension.width, this.dimension.height);
        graphics.setColor(this.renderColor);
        for (final iGraphTextComponent textComponent : this.textComponents) {
            graphics.setColor(textComponent.getColor());
            graphics.drawString(textComponent.getText(), textComponent.getX(), textComponent.getY());
        }
        for (final iGraphLine line : this.iGraphLines) {
            final double n = this.dimension.width / line.getHighestIndex();
            final double h = this.dimension.height / line.getHighestValue();
            graphics.setColor(line.getColor());
            int lastIndex = 0;
            double lastIndexValue = 0.0;
            for (final Map.Entry<Integer, Double> f : line.getMap().entrySet()) {
                graphics.drawLine((int)(lastIndex * n), (int)(-(lastIndexValue * h)) + this.dimension.height, (int)(f.getKey() * n), (int)(-(f.getValue() * h)) + this.dimension.height);
                lastIndex = f.getKey();
                lastIndexValue = f.getValue();
            }
        }
        graphics.dispose();
        return bufferedImage;
    }
    
    public boolean imageIsLoaded() {
        return this.finalImage != null;
    }
    
    public BufferedImage getFinalImage() {
        if (!this.imageIsLoaded()) {
            this.finalImage = this.renderImage();
        }
        return this.finalImage;
    }
    
    public void loadAndSaveTo(final File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        ImageIO.write(this.getFinalImage(), "JPEG", file);
    }
    
    private void clearCache() {
        this.finalImage = null;
    }
    
    public Color getBackgroundColor() {
        return this.backgroundColor;
    }
    
    public Color getRenderColor() {
        return this.renderColor;
    }
    
    public List<iGraphLine> getGraphLines() {
        return this.iGraphLines;
    }
    
    public List<iGraphTextComponent> getTextComponents() {
        return this.textComponents;
    }
    
    public int getHeight() {
        return this.dimension.height;
    }
    
    public int getWidth() {
        return this.dimension.width;
    }
    
    public void setBackgroundColor(final Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.clearCache();
    }
    
    public void setRenderColor(final Color renderColor) {
        this.renderColor = renderColor;
        this.clearCache();
    }
    
    public void setGraphLines(final List<iGraphLine> iGraphLines) {
        this.iGraphLines = iGraphLines;
        this.clearCache();
    }
    
    public void setTextComponents(final List<iGraphTextComponent> textComponents) {
        this.textComponents = textComponents;
        this.clearCache();
    }
    
    public void setHeight(final int height) {
        this.dimension.height = height;
        this.clearCache();
    }
    
    public void setWidth(final int width) {
        this.dimension.width = width;
        this.clearCache();
    }
}
