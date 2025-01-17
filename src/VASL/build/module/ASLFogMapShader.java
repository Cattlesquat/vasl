package VASL.build.module;

import VASL.LOS.Map.Hex;
import VASL.environment.FogLevel;
import VASSAL.build.GameModule;
import VASSAL.build.module.Map;
import VASSAL.build.module.map.MapShader;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.properties.GlobalProperty;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.Collection;

import static VASL.environment.FogLevel.NONE;

public class ASLFogMapShader extends MapShader {
  private final GlobalProperty globalFogLevel = new GlobalProperty();
  private FogLevel fogLevel = NONE;
  public ASLFogMapShader() {
    super();
    globalFogLevel.setPropertyName("fog_level");
    globalFogLevel.setAttribute("initialValue", fogLevel.name());
    GameModule gm = GameModule.getGameModule();
    gm.addMutableProperty("fog_level", globalFogLevel);
  }

  @Override
  protected void toggleShading() {
    Object[] possibilities = FogLevel.values();
    FogLevel tempFogLevel = (FogLevel)JOptionPane.showInputDialog(
        getLaunchButton().getParent(),
        "Select Fog level and intensity:",
        "Fog Level",
        JOptionPane.PLAIN_MESSAGE,
        getLaunchButton().getIcon(),
        possibilities,
        fogLevel.toString());
    if(tempFogLevel != null) {
      fogLevel = tempFogLevel;
    }
    GameModule.getGameModule().getChatter().send(fogLevel.toString() + " is in effect.");
    this.setShadingVisibility(setFogLevelAndOpacity());
  }

  private boolean setFogLevelAndOpacity() {
    switch (fogLevel) {
      case NONE:
        opacity = 0;
        break;
      case LIGHT_FOGM1:
      case LIGHT_FOGL0:
      case LIGHT_FOGL1:
      case LIGHT_FOGL2:
      case LIGHT_FOGL3:
      case LIGHT_FOGL4:
        opacity = 35;
        break;
      case MODERATE_FOGM1:
      case MODERATE_FOGL0:
      case MODERATE_FOGL1:
      case MODERATE_FOGL2:
      case MODERATE_FOGL3:
      case MODERATE_FOGL4:
        opacity = 40;
        break;
      case HEAVY_FOGM1:
      case HEAVY_FOGL0:
      case HEAVY_FOGL1:
      case HEAVY_FOGL2:
      case HEAVY_FOGL3:
      case HEAVY_FOGL4:
        opacity = 45;
        break;
    }

    globalFogLevel.setAttribute("initialValue", fogLevel.name());
    buildComposite();
    return fogLevel != NONE;
  }
  @Override
  public void draw(Graphics g, Map map) {
    if (!shadingVisible) {
      return;
    }

    ASLMap aslMap = (ASLMap) map;
    // if there is no hex grid for the map, we can't shade hex by hex.
    if(aslMap.isLegacyMode()) {
      super.draw(g, map);
      return;
    }

    Hex[][] hexes = aslMap.getVASLMap().getHexGrid();
    this.boardClip=null;
    Area boardArea = getBoardClip();

    for (Hex[] row:hexes) {
      for (Hex hex :row) {
        if( hex.getBaseHeight() <= fogLevel.fogHeight()) {
          Polygon finalHexPolygon = new Polygon(hex.getHexBorder().xpoints,  hex.getHexBorder().ypoints, hex.getHexBorder().npoints);

          // offset to the actual board rather than draw from 0,0 on the Map.
          for(int n = 0; n < 6; ++n) {
            finalHexPolygon.xpoints[n] += aslMap.getEdgeBuffer().width;
            finalHexPolygon.ypoints[n] += aslMap.getEdgeBuffer().height;
          }
          Area area = new Area(finalHexPolygon);
          if (area.isEmpty()) {
            return;
          }

          final Graphics2D g2d = (Graphics2D) g;
          final double os_scale = g2d.getDeviceConfiguration().getDefaultTransform().getScaleX();
          final double zoom = map.getZoom() * os_scale;

          if (zoom != 1.0) {
            area = new Area(AffineTransform.getScaleInstance(zoom, zoom)
                .createTransformedShape(area));
            boardArea = new Area(AffineTransform.getScaleInstance(zoom, zoom)
                .createTransformedShape(getBoardClip()));

          }
          // remove the bits of the hex that aren't on the board
          area.intersect(boardArea);

          final Composite oldComposite = g2d.getComposite();
          final Color oldColor = g2d.getColor();
          final Paint oldPaint = g2d.getPaint();

          g2d.setComposite(getComposite());
          g2d.setColor(getColor());
          g2d.setPaint(getTexture(zoom));
          g2d.fill(area);

          if (border) {
            final Stroke oldStroke = g2d.getStroke();
            g2d.setComposite(getBorderComposite());
            g2d.setStroke(getStroke(zoom));
            g2d.setColor(getBorderColor());
            g2d.draw(area);

            g2d.setStroke(oldStroke);
          }

          g2d.setComposite(oldComposite);
          g2d.setColor(oldColor);
          g2d.setPaint(oldPaint);

        }
      }
    }
  }
}
