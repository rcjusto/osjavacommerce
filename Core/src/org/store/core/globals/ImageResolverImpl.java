package org.store.core.globals;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.store.core.beans.Product;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.ServletContext;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;

public class ImageResolverImpl implements ImageResolver {

    public static Logger log = Logger.getLogger(ImageResolverImpl.class);
    private String basePath;
    private String storeCode;
    private Point sizeZoom;
    private Point sizeDetail;
    private Point sizeList;
    private Integer maxWidth;
    private Integer maxHeight;
    public static final String IMAGE_PRODUCT_PATH = File.separator + "images" + File.separator + "products" + File.separator;
    public static final String PATH_ZOOM = "zoom" + File.separator;
    public static final String PATH_LIST = "list" + File.separator;
    private Float qualityZoom = 0.75f;
    private Float qualityDetail = 0.70f;
    private Float qualityList = 0.65f;
    private String lastError;

    public ImageResolverImpl(ServletContext ctx, String stCode, String propZoom, String propDetail, String propList, Integer maxWidth, Integer maxHeight) {
        this.basePath = ctx.getRealPath("/");
        if (!basePath.endsWith(File.separator)) basePath += File.separator;
        this.storeCode = (StringUtils.isNotEmpty(stCode)) ? stCode : "";
        this.sizeZoom = propToSize(propZoom);
        this.sizeDetail = propToSize(propDetail);
        this.sizeList = propToSize(propList);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public ImageResolverImpl(String basePath, String stCode, String propZoom, String propDetail, String propList, Integer maxWidth, Integer maxHeight) {
        this.basePath = basePath;
        if (!basePath.endsWith(File.separator)) basePath += File.separator;
        this.storeCode = (StringUtils.isNotEmpty(stCode)) ? stCode : "";
        this.sizeZoom = propToSize(propZoom);
        this.sizeDetail = propToSize(propDetail);
        this.sizeList = propToSize(propList);
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    private Point propToSize(String prop) {
        String[] arr = (StringUtils.isNotEmpty(prop)) ? prop.split("x") : null;
        if (arr != null && arr.length == 2) {
            Integer width = SomeUtils.strToInteger(arr[0].trim());
            Integer height = SomeUtils.strToInteger(arr[1].trim());
            return (width != null && height != null && width > 1 && height > 1) ? new Point(width, height) : null;
        }
        return null;
    }

    public String getImageForProduct(Product p, String folder) {
        String[] images = getImagesForProduct(p, folder);
        if (images != null && StringUtils.isNotEmpty(p.getMainImage()) && ArrayUtils.contains(images, p.getMainImage())) {
            return p.getMainImage();
        } else if (images != null && images.length > 0) {
            return images[0];
        }
        return "";
    }

    public String[] getImagesForProduct(Product p, String folder) {
        return (p != null) ? getImagesForPrefix(p.getPartNumber(), folder) : null;
    }

    public String[] getImagesForProduct(Product p, String c1, String c2, String c3, String folder) {
        StringBuilder prefix = new StringBuilder();
        prefix.append(p.getPartNumber()).append("_");
        if (StringUtils.isNotEmpty(c1)) prefix.append(c1);
        if (StringUtils.isNotEmpty(c2)) prefix.append(c2);
        if (StringUtils.isNotEmpty(c3)) prefix.append(c3);
        return getImagesForPrefix(prefix.toString(), folder);
    }

    public boolean validExtension(String name) {
        return (name.toLowerCase().endsWith(".gif") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg") || name.toLowerCase().endsWith(".png"));
    }

    public String[] getImagesForPrefix(String prefix, String folder) {
        File path = new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + folder);
        ImagePrefixFilenameFilter filter = new ImagePrefixFilenameFilter(prefix);
        return path.list(filter);
    }


    public String getNewValidName(String extension, Product p) {
        return getNewValidName(extension, p.getPartNumber());
    }

    public String getNewValidName(String extension, Product p, String c1, String c2, String c3) {
        StringBuffer prefix = new StringBuffer();
        prefix.append(p.getPartNumber()).append("_");
        if (StringUtils.isNotEmpty(c1)) prefix.append(c1);
        if (StringUtils.isNotEmpty(c2)) prefix.append(c2);
        if (StringUtils.isNotEmpty(c3)) prefix.append(c3);
        return getNewValidName(extension, prefix.toString());
    }

    public boolean processImage(Product product, File image, String ext) {
        if (ext != null) ext = ext.toLowerCase();
        BufferedImage inImage = null;
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(image);
            Iterator<ImageReader> i = ImageIO.getImageReaders(iis);
            while (i.hasNext()) {
                ImageReader ir = i.next();
                ir.setInput(iis);
                int width = ir.getWidth(0);
                int height = ir.getHeight(0);
                if (width == 0 || height == 0) throw new Exception("Incorrect Format");
                if (dimensionsAccepted(width, height)) {
                    inImage = ir.read(ir.getMinIndex());
                    width = inImage.getWidth();
                    height = inImage.getHeight();

                    String outFilename = getNewValidName(ext, product);
                    String outFilenameJPG = ("jpg".equalsIgnoreCase(ext)) ? outFilename : getNewValidName("jpg", product);

                    // Procesar zoom
                    if (sizeZoom != null) {
                        if (width >= sizeZoom.x || height >= sizeZoom.y) {
                            File outFile = new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + PATH_ZOOM + outFilenameJPG);
                            if (outFile.exists()) FileUtils.forceDelete(outFile);
                            FileUtils.forceMkdir(new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + PATH_ZOOM));
                            BufferedImage img = resize(inImage, sizeZoom.x, sizeZoom.y, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                            writeJPEG(img, basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + PATH_ZOOM + outFilename, qualityZoom);
                        } else {
                            File outFile = new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + PATH_ZOOM + outFilename);
                            if (outFile.exists()) FileUtils.forceDelete(outFile);
                            FileUtils.copyFile(image, outFile);
                        }
                    }

                    // Procesar detalle
                    if (sizeDetail != null) {
                        if (width >= sizeDetail.x || height >= sizeDetail.y) {
                            File outFile = new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + outFilenameJPG);
                            if (outFile.exists()) FileUtils.forceDelete(outFile);
                            FileUtils.forceMkdir(new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH));
                            BufferedImage img = resize(inImage, sizeDetail.x, sizeDetail.y, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                            writeJPEG(img, basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + outFilename, qualityDetail);
                        } else {
                            File outFile = new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + outFilename);
                            if (outFile.exists()) FileUtils.forceDelete(outFile);
                            FileUtils.copyFile(image, outFile);
                        }
                    }

                    // Procesar listado
                    if (sizeList != null) {
                        if (width >= sizeList.x || height >= sizeList.y) {
                            File outFile = new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + PATH_LIST + outFilenameJPG);
                            if (outFile.exists()) FileUtils.forceDelete(outFile);
                            FileUtils.forceMkdir(new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + PATH_LIST));
                            BufferedImage img = resize(inImage, sizeList.x, sizeList.y, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                            writeJPEG(img, basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + PATH_LIST + outFilename, qualityList);
                        } else {
                            File outFile = new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + PATH_LIST + outFilename);
                            if (outFile.exists()) FileUtils.forceDelete(outFile);
                            FileUtils.copyFile(image, outFile);
                        }
                    }
                    lastError = null;
                } else {
                    log.error("Image too big.  - IMAGE: " + image.getName());
                    lastError = "Image too big";
                }
            }
            iis.close();
            return lastError==null;
        } catch (Exception e) {
            log.error(e.getMessage() + " - IMAGE: " + image.getName());
            lastError = e.getMessage();
            return false;
        }
    }

    private boolean dimensionsAccepted(int width, int height) {
        return !(maxWidth != null && maxWidth > 0 && width > maxWidth) && !(maxHeight != null && maxHeight > 0 && height > maxHeight);
    }

    public void deleteImages(Product product) {
        String[] listN = getImagesForProduct(product, "");
        if (listN != null) {
            for (String fn : listN) {
                File f = new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + fn);
                if (f.exists()) try {
                    FileUtils.forceDelete(f);
                } catch (IOException ignored) {
                }
            }
        }
        String[] listL = getImagesForProduct(product, PATH_LIST);
        if (listL != null) {
            for (String fn : listL) {
                File f = new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + PATH_ZOOM + fn);
                if (f.exists()) try {
                    FileUtils.forceDelete(f);
                } catch (IOException ignored) {
                }
            }
        }
        String[] listZ = getImagesForProduct(product, PATH_ZOOM);
        if (listZ != null) {
            for (String fn : listZ) {
                File f = new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + PATH_ZOOM + fn);
                if (f.exists()) try {
                    FileUtils.forceDelete(f);
                } catch (IOException ignored) {
                }
            }
        }
    }

    public String getLastError() {
        return lastError;
    }

    public void deleteImages(Product product, String deleteImg) {
        try {
            File f1 = new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + PATH_LIST + deleteImg);
            if (f1.exists()) FileUtils.forceDelete(f1);
            File f2 = new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + deleteImg);
            if (f2.exists()) FileUtils.forceDelete(f2);
            File f3 = new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + PATH_ZOOM + deleteImg);
            if (f3.exists()) FileUtils.forceDelete(f3);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public String getNewValidName(String extension, String prefix) {
        int index = 1;
        String res = prefix + "_" + index + "." + extension;
        while (new File(basePath + "stores/" + storeCode + IMAGE_PRODUCT_PATH + res).exists()) {
            index++;
            res = prefix + "_" + index + "." + extension;
        }
        return res;
    }

    public class ImagePrefixFilenameFilter implements FilenameFilter {
        private String prefix;

        public ImagePrefixFilenameFilter(String pref) {
            this.prefix = (pref!=null) ? pref.toLowerCase() : "";
        }

        public boolean accept(File dir, String name) {
            name = name.toLowerCase();
            return (name.startsWith(prefix + "_") || name.startsWith(prefix + ".")) && (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif"));
        }

    }

    public static BufferedImage resize(BufferedImage source, int destWidth, int destHeight, Object interpolation) {
        if (source == null) throw new NullPointerException("source image is NULL!");
        if (destWidth <= 0 && destHeight <= 0) throw new IllegalArgumentException("destination width & height are both <=0!");
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();
        double xScale = ((double) destWidth) / (double) sourceWidth;
        double yScale = ((double) destHeight) / (double) sourceHeight;
        double rScale = Math.min(xScale, yScale);
        BufferedImage scaled = getScaledInstance2(source, rScale);

        BufferedImage out2 = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = out2.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.fillRect(0, 0, destWidth, destHeight);
        int x = (destWidth - scaled.getWidth()) / 2;
        int y = (destHeight - scaled.getHeight()) / 2;
        g2.drawImage(scaled, x, y, scaled.getWidth(), scaled.getHeight(), null);
        g2.dispose();
        return out2;
    }


    public static BufferedImage getScaledInstance2(BufferedImage image,
                                                   double scale) {

// works, but I need an extra buffer and draw operation - UGLY
// and UNNECESSARILY COMPLICATED (in my view)
        AffineTransform xform = new AffineTransform();
        xform.scale(scale, scale);
        AffineTransformOp op = new AffineTransformOp(xform, AffineTransformOp.TYPE_BICUBIC);
        BufferedImage out = op.filter(image, null);

// create yet another buffer with the proper RGB pixel structure
// (no alpha), draw transformed image 'out' into this buffer 'out2'
        BufferedImage out2 = new BufferedImage(out.getWidth(), out.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out2.createGraphics();
        g.setBackground(Color.WHITE);
        g.fillRect(0, 0, out.getWidth(), out.getHeight());
        g.drawRenderedImage(out, null);
        return out2;
    }

    public static BufferedImage getScaledInstance(BufferedImage img,
                                                  int targetWidth,
                                                  int targetHeight,
                                                  Object hint,
                                                  boolean higherQuality) {
        int type = (img.getTransparency() == Transparency.OPAQUE) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
        BufferedImage ret = img;
        int w, h;
        if (higherQuality) {
            // Use multi-step technique: start with original size, then
            // scale down in multiple passes with drawImage()
            // until the target size is reached
            w = img.getWidth();
            h = img.getHeight();
        } else {
            // Use one-step technique: scale directly from original
            // size to target size with a single drawImage() call
            w = targetWidth;
            h = targetHeight;
        }

        do {
            if (higherQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }

            if (higherQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();
            g2.setBackground(Color.WHITE);
            g2.fillRect(0, 0, w, h);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        return ret;
    }


    public static void writeJPEG(BufferedImage input, String name, Float quality) throws IOException {
        ImageIO.write(input, "jpg", new File(name));
        /*
        Iterator iter = ImageIO.getImageWritersByFormatName("JPG");
        if (iter.hasNext()) {
            ImageWriter writer = (ImageWriter) iter.next();
            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(quality);
            File outFile = new File(name);
            FileImageOutputStream output = new FileImageOutputStream(outFile);
            writer.setOutput(output);
            IIOImage image = new IIOImage(input, null, null);
            writer.write(null, image, iwp);
            output.close();
        }
        */
    }

    public Float getQualityZoom() {
        return qualityZoom;
    }

    public void setQualityZoom(Float qualityZoom) {
        this.qualityZoom = qualityZoom;
    }

    public Float getQualityDetail() {
        return qualityDetail;
    }

    public void setQualityDetail(Float qualityDetail) {
        this.qualityDetail = qualityDetail;
    }

    public Float getQualityList() {
        return qualityList;
    }

    public void setQualityList(Float qualityList) {
        this.qualityList = qualityList;
    }

    /*
    public byte[] resizeImageAsJPG(byte[] pImageData, int pMaxWidth) throws IOException {
        InputStream imageInputStream = new ByteArrayInputStream(pImageData);
// read in the original image from an input stream
        SeekableStream seekableImageStream = SeekableStream.wrapInputStream(imageInputStream, true);
        RenderedOp originalImage = JAI.create(JAI_STREAM_ACTION, seekableImageStream);
        ((OpImage) originalImage.getRendering()).setTileCache(null);
        int origImageWidth = originalImage.getWidth();
// now resize the image
        double scale = 1.0;
        if (pMaxWidth > 0 && origImageWidth > pMaxWidth) {
            scale = (double) pMaxWidth / originalImage.getWidth();
        }
        ParameterBlock paramBlock = new ParameterBlock();
        paramBlock.addSource(originalImage); // The source image
        paramBlock.add(scale); // The xScale
        paramBlock.add(scale); // The yScale
        paramBlock.add(0.0); // The x translation
        paramBlock.add(0.0); // The y translation

        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        RenderedOp resizedImage = JAI.create(JAI_SUBSAMPLE_AVERAGE_ACTION, paramBlock, qualityHints);

// lastly, write the newly-resized image to an output stream, in a specific encoding
        ByteArrayOutputStream encoderOutputStream = new ByteArrayOutputStream();
        JAI.create(JAI_ENCODE_ACTION, resizedImage, encoderOutputStream, JAI_ENCODE_FORMAT_JPEG, null);
// Export to Byte Array
        byte[] resizedImageByteArray = encoderOutputStream.toByteArray();
        return resizedImageByteArray;
    }
    */

}
