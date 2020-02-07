import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class deletethis {

    public static void main(String[] args) throws IOException {
        Clustering c = new Clustering("datasets/birch2.txt", 100, null, new RandomSwap());
        c.runMultiple(1);
        draw("birch2.png", c.dataset, true, false);
    }


    public static void draw(String filename, Dataset dataset, boolean skipCentroids, boolean blackData) throws IOException {

        if(dataset.data[0].length != 2){
            System.out.println("draw(): only 2d dataset.data supported");
            return;
        }
        BufferedImage img = new BufferedImage(1280, 900, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = img.createGraphics();

        g.setBackground(Color.white);
        g.clearRect(0, 0, 1280, 900);

        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for(int i = 0; i < dataset.data.length; i++){
            if(dataset.data[i][0] < minX)
                minX = dataset.data[i][0];
            if(dataset.data[i][0] > maxX)
                maxX = dataset.data[i][0];

            if(dataset.data[i][1] < minY)
                minY = dataset.data[i][1];
            if(dataset.data[i][1] > maxY)
                maxY = dataset.data[i][1];
        }

        HashMap<Integer, Color> labelColors = new HashMap<>();
        for(int c = 0; c < dataset.centroids.length; c++){
            labelColors.put(c, new Color((float)Math.random(), (float)Math.random(), (float)Math.random()));
        }

        for(int i = 0; i < dataset.data.length; i++){
            double[] d = dataset.data[i];
            if(blackData)
                g.setColor(Color.black);
            else
                g.setColor(labelColors.get(dataset.partitions[i]));
            g.fillOval((int)(((d[0] - minX) / maxX) * 1280) - 2, (int)(900 - ((d[1] - minY) / maxY) * 900) - 2, 4, 4);
        }

        if(!skipCentroids) {
            for (int c = 0; c < dataset.centroids.length; c++) {
                double[] d = dataset.centroids[c];
                g.setColor(labelColors.get(c));
                g.fillOval((int) (((d[0] - minX) / maxX) * 1280) - 10, (int) (900 - ((d[1] - minY) / maxY) * 900) - 10, 20, 20);
            }
        }

//		if(realCentroids != null){
//			for(int c = 0; c < realCentroids.length; c++){
//				double[] d = realCentroids[c];
//				g.setColor(new Color(1, 0, 0, 0.3f));
//				g.fillOval((int)(((d[0] - minX) / maxX) * 1280) - 10, (int)(900 - ((d[1] - minY) / maxY) * 900) - 10, 20, 20);
//			}
//		}

        ImageIO.write(img, "png", new File(filename));

    }
}
