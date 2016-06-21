package ru.etu.astamir.gui;

import sun.swing.ImageIconUIResource;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.IconUIResource;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IconFactory {
    public static IconUIResource makeImageIconResource(Class base_class, String gif_file) {
        return new IconUIResource(makeImageIcon(base_class, gif_file));
    }

	public static ImageIcon makeImageIcon(Class base_class, String gif_file) {
		byte[] buffer = null;
        BufferedInputStream in = null;
        ByteArrayOutputStream out = null;
		try {
			InputStream resource = base_class.getResourceAsStream(gif_file);
			if (resource == null)
				return null;
			in = new BufferedInputStream(resource);
			out = new ByteArrayOutputStream(1024);
			buffer = new byte[1024];
			int n;
			while ((n = in.read(buffer)) > 0)
				out.write(buffer, 0, n);
			out.flush();
			buffer = out.toByteArray();
		} catch (IOException ioe) {
			System.err.println(ioe.toString());
			return null;
		} finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

		if (buffer == null) {
			System.err.println(base_class.getName() + "/" + gif_file + " not found.");
			return null;
		}
		if (buffer.length == 0) {
			System.err.println("warning: " + gif_file + " is zero-length");
			return null;
		}

		return new ImageIcon(buffer);
	}

    public static ImageIconUIResource createResourceIcon(String resource) {
        InputStream stream = IconFactory.class.getResourceAsStream(resource);
        if(stream == null){
            System.err.println("warning: " + resource + " does not exist ");
            return null;
        }
        try {
            try {
                BufferedImage img = ImageIO.read(stream);
                return new ImageIconUIResource(img);
            } finally {
                stream.close();
            }
        } catch (IOException e) {
            System.err.println("warning: can't read " + resource + " caused by " + e);
            return null;
        }
    }
}
