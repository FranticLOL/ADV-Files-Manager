package main;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class myTableViewTypes {

    private SimpleStringProperty FileName;
    private ImageView FileImage;
    private String FileSize;

    public myTableViewTypes(String FileName, ImageView FileImage, String FileSize) {
        this.FileName = new SimpleStringProperty(FileName);
        this.FileImage = FileImage;
        this.FileSize = FileSize;
    }

    public String getFileName() {
        return FileName.get();
    }

    public void setFileName(String name) {
        FileName.set(name);
    }

    public ImageView getFileImage() {
        return FileImage;
    }

    public void setFileImage(Image ImageType) {
        FileImage.setImage(ImageType);
    }

    public String getFileSize() {
        return FileSize;
    }

    public void setFileSize(String size) {
        FileSize = size;
    }

}
