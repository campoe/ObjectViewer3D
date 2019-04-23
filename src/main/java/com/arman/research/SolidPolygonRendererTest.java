package com.arman.research;

import com.arman.research.core.Core3D;
import com.arman.research.geom.polygons.Polygon3f;
import com.arman.research.geom.polygons.PolygonGroup;
import com.arman.research.geom.transforms.Transform3f;
import com.arman.research.loader.ObjectLoader;
import com.arman.research.render.SolidPolygonRenderer;
import com.arman.research.render.View;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;

public class SolidPolygonRendererTest extends Core3D {

    private File file;

    public static void main(String[] args) {
        SolidPolygonRendererTest test = new SolidPolygonRendererTest();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Object file", "obj"));
        int val = fileChooser.showOpenDialog(null);
        if (val == JFileChooser.APPROVE_OPTION) {
            test.setFile(fileChooser.getSelectedFile());
            test.run();
        }
    }

    private void setFile(File file) {
        this.file = file;
    }

    public void createPolygons() {
        PolygonGroup pg = null;
        ObjectLoader loader = new ObjectLoader();
        try {
            pg = loader.loadObject(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (pg != null) {
            while (pg.hasNext()) {
                Polygon3f p = pg.nextPolygon();
                addPolygon(p);
            }
        }
    }

    public void createPolygonRenderer() {
        setView(new View(0, 0, getScreen().getWidth(), getScreen().getHeight(), (float) Math.toRadians(75)));
        Transform3f camera = new Transform3f(200, 200, 1000);
        setPolygonRenderer(new SolidPolygonRenderer(camera, getView()));
    }

}
