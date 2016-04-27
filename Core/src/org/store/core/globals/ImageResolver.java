package org.store.core.globals;

import org.store.core.beans.Product;

import java.io.File;

public interface ImageResolver {


    public String getImageForProduct(Product p, String folder);

    public String[] getImagesForProduct(Product p, String folder);

    public String[] getImagesForProduct(Product p, String c1, String c2, String c3, String folder);

    public boolean validExtension(String ext);

    public String getNewValidName(String extension, Product p );
    
    public String getNewValidName(String extension, Product p , String c1, String c2, String c3);

    public boolean processImage(Product product, File image, String ext);

    public void deleteImages(Product product, String deleteImg);

    public void deleteImages(Product product);

    public String getLastError();

}
