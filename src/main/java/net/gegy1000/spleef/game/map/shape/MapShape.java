package net.gegy1000.spleef.game.map.shape;

import net.gegy1000.plasmid.game.map.template.MapTemplate;
import net.gegy1000.spleef.game.map.SpleefMapGenerator.Brush;

public interface MapShape {
    public void generate(MapTemplate template, int radius, int minY, int maxY, Brush brush);
}