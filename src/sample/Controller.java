package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;

class Position {
    int X;
    int Y;

    Position(int x, int y) {
        X = x;
        Y = y;
    }
}

public class Controller {
    @FXML
    private ImageView imagePreview;

    private String imagePath = "/home/samba/kupidurap/biometrics-christmas/digit.png";

    @FXML
    public void initialize() {
        try {
            BufferedImage sourceImage = ImageIO.read(new File(imagePath));
            BufferedImage processed = skeletonized(sourceImage);

            if (processed != null)
                imagePreview.setImage(SwingFXUtils.toFXImage(processed, null));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage skeletonized(BufferedImage source) {
        int width = source.getWidth(), height = source.getHeight();
        int[][] image = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = new Color(source.getRGB(x, y));
                int grey = (color.getBlue() + color.getGreen() + color.getRed()) / 3;
                image[x][y] = grey < 127 ? 1 : 0;
            }
        }

        while (true) {
            ArrayList<Position> borderPixels = findBorderPixels(image, width, height);
            boolean change = false;

            for (HashSet<Integer> weightsSet : weightsSets)
                change |= phase(image, borderPixels, weightsSet);

            if (!change)
                break;
        }

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                int weight = pixelWeight(x, y, image);

                if (a1pix.contains(weight))
                    image[x][y] = 0;
            }
        }

        BufferedImage result = new BufferedImage(width, height, source.getType());

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                result.setRGB(x, y, Color.WHITE.getRGB());
                if (image[x][y] == 1)
                    result.setRGB(x, y, Color.BLACK.getRGB());
            }
        }

        return result;
    }

    private ArrayList<Position> findBorderPixels(int[][] image, int width, int height) {
        ArrayList<Position> borderPixels = new ArrayList<>();

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                int weight = pixelWeight(x, y, image);

                if (a0.contains(weight))
                    borderPixels.add(new Position(x, y));
            }
        }

        return borderPixels;
    }

    private boolean phase(int[][] image, ArrayList<Position> borderPixels, HashSet<Integer> weightsSet) {
        boolean change = false;

        ArrayList<Position> borderPixelsToRemove = new ArrayList<>();

        for (Position borderPixel : borderPixels) {
            int weight = pixelWeight(borderPixel.X, borderPixel.Y, image);

            if (weightsSet.contains(weight)) {
                image[borderPixel.X][borderPixel.Y] = 0;
                borderPixelsToRemove.add(borderPixel);
                change = true;
            }
        }

        borderPixelsToRemove.forEach(borderPixels::remove);

        return change;
    }

    private int pixelWeight(int x, int y, int[][] image) {
        int weight = 0;

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                weight += weightMatrix[i + 1][j + 1] * image[x + i][y + j];
            }
        }

        return weight;
    }

    private int[][] weightMatrix = new int[][]{
            {128, 1, 2},
            {64, 0, 4},
            {32, 16, 8}
    };

    private HashSet<Integer> a0 = new HashSet<>(Arrays.asList(
            3, 6, 7, 12, 14, 15, 24, 28, 30, 31, 48, 56, 60,
            62, 63, 96, 112, 120, 124, 126, 127, 129, 131, 135,
            143, 159, 191, 192, 193, 195, 199, 207, 223, 224,
            225, 227, 231, 239, 240, 241, 243, 247, 248, 249,
            251, 252, 253, 254
    ));

    private HashSet<Integer> a1 = new HashSet<>(Arrays.asList(
            7, 14, 28, 56, 112, 131, 193, 224
    ));

    private HashSet<Integer> a2 = new HashSet<>(Arrays.asList(
            7, 14, 15, 28, 30, 56, 60, 112, 120, 131, 135,
            193, 195, 224, 225, 240
    ));

    private HashSet<Integer> a3 = new HashSet<>(Arrays.asList(
            7, 14, 15, 28, 30, 31, 56, 60, 62, 112, 120,
            124, 131, 135, 143, 193, 195, 199, 224, 225, 227,
            240, 241, 248
    ));

    private HashSet<Integer> a4 = new HashSet<>(Arrays.asList(
            7, 14, 15, 28, 30, 31, 56, 60, 62, 63, 112, 120,
            124, 126, 131, 135, 143, 159, 193, 195, 199, 207,
            224, 225, 227, 231, 240, 241, 243, 248, 249, 252
    ));

    private HashSet<Integer> a5 = new HashSet<>(Arrays.asList(
            7, 14, 15, 28, 30, 31, 56, 60, 62, 63, 112, 120,
            124, 126, 131, 135, 143, 159, 191, 193, 195, 199,
            207, 224, 225, 227, 231, 239, 240, 241, 243, 248,
            249, 251, 252, 254
    ));

    private ArrayList<HashSet<Integer>> weightsSets = new ArrayList<>(Arrays.asList(a1, a2, a3, a4, a5));

    private HashSet<Integer> a1pix = new HashSet<>(Arrays.asList(
            3, 6, 7, 12, 14, 15, 24, 28, 30, 31, 48, 56,
            60, 62, 63, 96, 112, 120, 124, 126, 127, 129, 131,
            135, 143, 159, 191, 192, 193, 195, 199, 207, 223,
            224, 225, 227, 231, 239, 240, 241, 243, 247, 248,
            249, 251, 252, 253, 254
    ));
}
