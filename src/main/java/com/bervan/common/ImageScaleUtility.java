package com.bervan.common;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class ImageScaleUtility {
    public static ByteArrayOutputStream scaleImage(BufferedImage original, int maxSize, float quality) throws IOException {
        BufferedImage scaled = scaleProportional(original, maxSize);

        // Write to JPEG with compression
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();

        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(quality); // 0.0 - 1.0

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            jpgWriter.setOutput(ios);
            jpgWriter.write(null, new IIOImage(scaled, null, null), jpgWriteParam);
        }

        jpgWriter.dispose();

        return baos;
    }

    private static BufferedImage scaleProportional(BufferedImage original, int maxSize) {
        int w = original.getWidth();
        int h = original.getHeight();

        double scale = Math.min((double) maxSize / w, (double) maxSize / h);

        int newW = Math.max(1, (int) (w * scale));
        int newH = Math.max(1, (int) (h * scale));

        BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.drawImage(original, 0, 0, newW, newH, null);
        g2d.dispose();

        return scaled;
    }
}
