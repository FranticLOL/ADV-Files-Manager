package main;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Functions {

    public static void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ADV FM Dialog");
        alert.setHeaderText("An error has been encountered");
        alert.setContentText(message);
        alert.showAndWait();

    }


    public static void CreateDirectory(ArrayList<String> Path, TableView<myTableViewTypes> Langs) {
        if (Path.size() > 0) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Directory Name");
            dialog.setHeaderText("Please enter directory name");

            Optional<String> result = dialog.showAndWait();

            if (result.isPresent()) {
                String newDir = new String();
                for (String s : Path)
                    newDir += s;
                File newfile = new File(newDir + "\\" + result.get());
                String basePath = ClassLoader.getSystemResource("").getPath();
                File file2 = new File(basePath.substring(1) + "/main/resources/folder.png");
                String localUrl2 = file2.toURI().toString();
                Image image2 = new Image(localUrl2);
                if (!newfile.exists()) {
                    try {
                        ImageView imFolder = new ImageView(image2);
                        imFolder.setFitWidth(15);
                        imFolder.setFitHeight(15);
                        Langs.getItems().add(new myTableViewTypes(result.get(), imFolder, Functions.GetFileSize(Long.valueOf("0"))));
                        newfile.mkdir();
                    } catch (SecurityException ex) {
                        showErrorMessage("Can't create a directory.");
                    }
                } else showErrorMessage("Can't create a directory.");
            }
        } else
            showErrorMessage("You cannot create the directory out of root.");
    }

    public static void Rename(ArrayList<String> Path, String ReFile, TableView<myTableViewTypes> Langs) {
        if (Path.size() > 0) {
            if (Langs.getSelectionModel().getSelectedItems().size() == 1) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Rename File");
                dialog.setHeaderText("Please enter new file name");
                Optional<String> result = dialog.showAndWait();

                if (result.isPresent()) {
                    String newDir = new String();
                    Langs.getSelectionModel().getSelectedItem().setFileName(result.get());
                    Langs.refresh();
                    for (String s : Path)
                        newDir += s;

                    File oldFile = new File(newDir + "\\" + ReFile);
                    if (oldFile.isDirectory()) {
                        File newFile = new File(newDir + "\\" + result.get());
                        Langs.getSelectionModel().getSelectedItem().setFileName(result.get());
                        oldFile.renameTo(newFile);
                    }
                    if (oldFile.isFile()) {
                        String extension = "";
                        int i = oldFile.getName().indexOf('.');
                        if (i > 0) {
                            extension = oldFile.getName().substring(i + 1);
                        }
                        File newFile = new File(newDir + "\\" + result.get() + '.' + extension);
                        Langs.getSelectionModel().getSelectedItem().setFileName(result.get() + '.' + extension);
                        oldFile.renameTo(newFile);
                    }
                }
                // Main.initTableView();
            } else
                showErrorMessage("You should choose only one file.");
        } else
            showErrorMessage("You cannot rename root.");

    }

    public static void CopyFile(ArrayList<String> Path, TableView<myTableViewTypes> Langs, ArrayList<String> CopiedFile) {
        if (Langs.getSelectionModel().getSelectedIndex() != -1) {
            if (Path.size() > 0) {
                CopiedFile.clear();
                String newDir = new String();
                for (String s : Path)
                    newDir += s;
                for (myTableViewTypes s : Langs.getSelectionModel().getSelectedItems())
                    CopiedFile.add(newDir + "\\" + s.getFileName());
            } else {
                showErrorMessage("You cannot copy root"); // возможно, возникнет какая-нибудь другая проблема?..
            }
        } else
            showErrorMessage("Please, select file");
    }

    public static void CutFile(ArrayList<String> Path, TableView<myTableViewTypes> Langs, ArrayList<String> CopiedFile, ArrayList<String> CopyMode) {
        if (Langs.getSelectionModel().getSelectedIndex() != -1) {
            if (Path.size() > 0) {
                Functions.CopyFile(Path, Langs, CopiedFile);
                for (myTableViewTypes buff : Langs.getSelectionModel().getSelectedItems())
                    buff.setFileName(buff.getFileName() + "  ----- cutted");
                Langs.refresh();
                CopyMode.clear();
                CopyMode.add("Cutted");
            } else {
                showErrorMessage("You cannot cut root"); // возможно, возникнет какая-нибудь другая проблема?..
            }
        } else
            showErrorMessage("Please, select file");
    }

    public static void FileProgressBar(long SizeofFiles, ArrayList<String> FilesToBar, FlowPane root) {
        ProgressBar bar = new ProgressBar();
        bar.setMaxHeight(10);

        root.getChildren().add(root.getChildren().size(), bar);

        System.out.println(SizeofFiles);
        ArrayList<File> FilesList = new ArrayList<>();
        for (String s : FilesToBar) {
            FilesList.add(new File(s));
            System.out.println(new File(s).getAbsolutePath());
        }
        Task taskBar = new Task<Void>() {
            @Override
            public Void call() {
                long NewFileSize = 0;
                long PreviosSize = -1;
                for (int i = 0; i < 10000000; i++) {
                    long FilesSize = 0;
                    try {
                        Thread.sleep(2000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    if (NewFileSize == PreviosSize) {
                        break;
                    }
                    PreviosSize = NewFileSize;
                    for (File fileinList : FilesList) {
                        if (fileinList.exists()) {
                            if (fileinList.isDirectory())
                                FilesSize += FileUtils.sizeOfDirectory(fileinList);
                            if (fileinList.isFile()) {
                                FilesSize += FileUtils.sizeOf(fileinList);
                            }
                        }
                    }
                    NewFileSize = FilesSize;
                    System.out.println(NewFileSize);
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    updateProgress(NewFileSize, SizeofFiles);
                }
                return null;
            }
        };
        taskBar.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, t -> {
            System.out.println("Bar done");
            root.getChildren().remove(bar);
        });
        bar.progressProperty().bind(taskBar.progressProperty());
        new Thread(taskBar).start();
    }

    public static void PasteFile(ArrayList<String> Path, ArrayList<String> CopiedFile, TableView<myTableViewTypes> Langs, FlowPane root, ArrayList<String> CopyMode) {

        if (Path.size() > 0) {                   // если файл называется одинаково, то либо заменить, либо переименовать
            if (CopiedFile.size() > 0) {  //проверка, что мы скопировали что-то

                String Dir = new String();
                for (String s : Path)
                    Dir += s;

                long SrcSize = 0;

                ArrayList<String> FilesToBar = new ArrayList<>();

                for (String CopiedPath : CopiedFile) //размер всех файлов
                {
                    if (new File(CopiedPath).isFile())
                        SrcSize += FileUtils.sizeOf(new File(CopiedPath));
                    if (new File(CopiedPath).isDirectory())
                        SrcSize += FileUtils.sizeOfDirectory(new File(CopiedPath));
                    FilesToBar.add(Dir + "\\" + new File(CopiedPath).getName());
                }

                Task task = new Task<Void>() {
                    @Override
                    public Void call() {
                        String Dir = new String();
                        for (String s : Path)
                            Dir += s;
                        for (String CopiedPath : CopiedFile) {
                            File source = new File(CopiedPath);
                            File dest = new File(Dir);

                            if (source.isFile()) {
                                try {
                                    FileUtils.copyFileToDirectory(source, dest);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (source.isDirectory()) {
                                try {
                                    FileUtils.copyDirectoryToDirectory(source, dest);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            String basePath = ClassLoader.getSystemResource("").getPath();

                            File file = new File(basePath.substring(1) + "/main/resources/file.png");      //заментить на взятие файлов из проектов
                            String localUrl = file.toURI().toString();
                            Image image = new Image(localUrl);

                            File file1 = new File(basePath.substring(1) + "/main/resources/archive.png");      //заментить на взятие файлов из проектов
                            String localUrl1 = file1.toURI().toString();
                            Image image1 = new Image(localUrl1);

                            File file2 = new File(basePath.substring(1) + "/main/resources/folder.png");      //заментить на взятие файлов из проектов
                            String localUrl2 = file2.toURI().toString();
                            Image image2 = new Image(localUrl2);

                            String newDir = new String();
                            for (String s : Path)
                                newDir += s;
                            File newfile = new File(newDir);
                            String[] newfilesName = newfile.list();
                            ArrayList<myTableViewTypes> BufferTableView = new ArrayList<>();
                            Main.initTableView(newDir, newfilesName, image, image1, image2, BufferTableView);
                            Langs.getItems().clear(); //избавляемся от старых файлов
                            Langs.getItems().addAll(BufferTableView);
                        }
                        return null;
                    }
                };
                new Thread(task).start();
                FileProgressBar(SrcSize, FilesToBar, root);
                ReInitTableAfCopy(Path, Langs, task, CopyMode, CopiedFile);
            }
        } else
            showErrorMessage("You cannot paste file out of root");
    }

    private static void ReInitTableAfCopy(ArrayList<String> Path, TableView<myTableViewTypes> Langs, Task task, ArrayList<String> CopyMode, ArrayList<String> CopyBuffer) {
        task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                System.out.println("Copying done");
                System.out.println(task.isDone());

                if (CopyMode.get(0) == "Cutted") {
                    Task task = new Task<Void>() {
                        @Override
                        public Void call() {                    //Task на удаление
                            for (String filepath : CopyBuffer) {
                                String CurrPath = filepath;
                                File dest = new File(CurrPath);
                                if (dest.isFile())
                                    dest.delete();
                                if (dest.isDirectory())
                                    try {
                                        FileUtils.deleteDirectory(dest);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                            }
                            return null;
                        }
                    };
                    task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent t) {
                            System.out.println("Deleting (Cutting done");
                            System.out.println(task.isDone());
                        }
                    });
                    new Thread(task).start();
                }

            }
        });
    }

    public static void DeleteFile(ArrayList<String> Path, TableView<myTableViewTypes> Langs)         //добавить ProgressBar
    {
        if (Langs.getSelectionModel().getSelectedIndex() != -1) {
            if (Path.size() > 0) {
                //Вызывается окошко, в котором нужно подтвертдить, что мы удаляем точно
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("ADV FM");
                alert.setHeaderText("Deleting File");
                alert.setContentText("Do you really want to delete choosen file?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    Task task = new Task<Void>() {
                        @Override
                        public Void call() {                    //Task на удаление
                            String FileFullPath = new String();
                            for (String s : Path)
                                FileFullPath += s;
                            String CurrPath = FileFullPath;
                            ArrayList<String> SelectedFiles = new ArrayList<>();
                            for (myTableViewTypes s : Langs.getSelectionModel().getSelectedItems())//получаем выделенные файлы
                                SelectedFiles.add(s.getFileName());
                            for (String Dir : SelectedFiles) {
                                CurrPath += "\\" + Dir;
                                File dest = new File(CurrPath);
                                if (dest.isFile())
                                    dest.delete();
                                if (dest.isDirectory())
                                    try {
                                        FileUtils.deleteDirectory(dest);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                CurrPath = FileFullPath;
                            }
                            return null;
                        }
                    };
                    task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent t) {
                            String newDir = new String();
                            for (String s : Path)
                                newDir += s;
                            File newfile = new File(newDir);
                            String basePath = ClassLoader.getSystemResource("").getPath();
                            String[] newfilesName = newfile.list();
                            File file = new File(basePath.substring(1) + "/main/resources/file.png");
                            String localUrl = file.toURI().toString();
                            Image image = new Image(localUrl);

                            File file1 = new File(basePath.substring(1) + "/main/resources/archive.png");
                            String localUrl1 = file1.toURI().toString();
                            Image image1 = new Image(localUrl1);

                            File file2 = new File(basePath.substring(1) + "/main/resources/folder.png");
                            String localUrl2 = file2.toURI().toString();
                            Image image2 = new Image(localUrl2);

                            ArrayList<myTableViewTypes> BufferTableView = new ArrayList<>();
                            Main.initTableView(newDir, newfilesName, image, image1, image2, BufferTableView);
                            Langs.getItems().clear(); //избавляемся от старых файлов
                            Langs.getItems().addAll(BufferTableView);
                            System.out.println("Deleting done");
                            System.out.println(task.isDone());
                        }
                    });
                    new Thread(task).start();
                } else {
                    alert.close();
                }
            } else
                showErrorMessage("You cannot delete roots.");
        } else
            showErrorMessage("You should choose file to delete.");
    }


    private static void addDirectory(ZipOutputStream zout, File fileSource, File ZipPath)
            throws Exception {
        File[] files = fileSource.listFiles();
        //System.out.println("Добавление директории <" + fileSource.getName() + ">");
        for (int i = 0; i < files.length; i++) {
            // Если file является директорией, то рекурсивно вызываем метод addDirectory
            if (files[i].isDirectory()) {
                addDirectory(zout, files[i], ZipPath);
                continue;
            }
            //System.out.println("Добавление файла <" + files[i].getName() + ">");

            FileInputStream fis = new FileInputStream(files[i]);
            Path zip = ZipPath.toPath();//путь к архиву
            Path file = files[i].toPath();//путь к файлу
            Path fileInArchiv = zip.relativize(file); // путь к файлу относительно архива
            zout.putNextEntry(new ZipEntry(fileInArchiv.toString().substring(3)));

            byte[] buffer = new byte[fis.available()];
            int length;
            while ((length = fis.read(buffer)) > 0)
                zout.write(buffer, 0, length);
        }
    }


    public static void ArchiveZIP(ArrayList<String> Path, TableView<myTableViewTypes> Langs) {
        if (Langs.getSelectionModel().getSelectedIndex() != -1) {
            if (Path.size() > 0) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("ZIP Archive Files");
                dialog.setHeaderText("Please enter archive name:");
                Optional<String> result = dialog.showAndWait(); /////////////////////////////Если нажать отмена, то всё равно будет создан архив!!!
                Task task = new Task<Void>() {
                    @Override
                    public Void call() {                    //Task на архивацию
                        String FileFullPath = new String();
                        for (String s : Path)
                            FileFullPath += s;
                        String CurrPath = FileFullPath;
                        ArrayList<String> SelectedFiles = new ArrayList<>();
                        for (myTableViewTypes s : Langs.getSelectionModel().getSelectedItems())//получаем выделенные файлы
                            SelectedFiles.add(s.getFileName());

                        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(FileFullPath + "\\" + result.get() + ".zip"))) {
                            for (String Dir : SelectedFiles) {
                                CurrPath += "\\" + Dir;
                                File dest = new File(CurrPath);
                                if (dest.isFile()) {
                                    FileInputStream fis = new FileInputStream(dest);
                                    ZipEntry entry = new ZipEntry(dest.getName());
                                    zout.putNextEntry(entry);
                                    // считываем содержимое файла в массив byte
                                    byte[] buffer = new byte[fis.available()];
                                    fis.read(buffer);
                                    // добавляем содержимое к архиву
                                    zout.write(buffer);
                                }
                                if (dest.isDirectory()) {
                                    addDirectory(zout, dest, new File(FileFullPath + "\\" + result.get() + ".zip"));
                                }
                                CurrPath = FileFullPath;
                            }
                            zout.closeEntry();// закрываем текущую запись для новой записи
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                        return null;
                    }

                };
                task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent t) {
                        String newDir = new String();
                        for (String s : Path)
                            newDir += s;
                        File newfile = new File(newDir);
                        String[] newfilesName = newfile.list();
                        String basePath = ClassLoader.getSystemResource("").getPath();
                        File file = new File(basePath.substring(1) + "/main/resources/file.png");      //заментить на взятие файлов из проектов
                        String localUrl = file.toURI().toString();
                        Image image = new Image(localUrl);

                        File file1 = new File(basePath.substring(1) + "/main/resources/archive.png");      //заментить на взятие файлов из проектов
                        String localUrl1 = file1.toURI().toString();
                        Image image1 = new Image(localUrl1);

                        File file2 = new File(basePath.substring(1) + "/main/resources/folder.png");      //заментить на взятие файлов из проектов
                        String localUrl2 = file2.toURI().toString();
                        Image image2 = new Image(localUrl2);

                        ArrayList<myTableViewTypes> BufferTableView = new ArrayList<>();
                        Main.initTableView(newDir, newfilesName, image, image1, image2, BufferTableView);
                        Langs.getItems().clear(); //избавляемся от старых файлов
                        Langs.getItems().addAll(BufferTableView);
                        System.out.println("ZIP Archiving done");
                        System.out.println(task.isDone());
                    }
                });
                new Thread(task).start();
            } else
                showErrorMessage("You cannot archive roots.");
        } else
            showErrorMessage("You should choose file to archive.");
    }

    public static void UnArchiveZIP(ArrayList<String> Path, TableView<myTableViewTypes> Langs) {
        if (Langs.getSelectionModel().getSelectedIndex() != -1) {
            if (Path.size() > 0) {
                Task task = new Task<Void>() {
                    @Override
                    public Void call() {                    //Task на разархивацию
                        String FileFullPath = new String();
                        for (String s : Path)
                            FileFullPath += s;
                        String CurrPath = FileFullPath;
                        ArrayList<String> SelectedFiles = new ArrayList<>();
                        for (myTableViewTypes s : Langs.getSelectionModel().getSelectedItems())//получаем выделенные файлы
                            SelectedFiles.add(s.getFileName());
                        int BUFFER_SIZE = 1024;

                        byte[] buffer = new byte[BUFFER_SIZE];
                        for (int i = 0; i < SelectedFiles.size(); ++i) {
                            if (!SelectedFiles.get(i).endsWith(".zip")) { // если это не зип файл, то ошибка
                                return null;
                            }
                            // Создаем каталог, куда будут распакованы файлы
                            String dstDirectory = CurrPath + File.separator + SelectedFiles.get(i).substring(0, SelectedFiles.get(i).lastIndexOf("."));
                            File dstDir = new File(dstDirectory);
                            if (!dstDir.exists()) {
                                dstDir.mkdir();
                            }
                            try {
                                // Получаем содержимое ZIP архива
                                final ZipInputStream zis = new ZipInputStream(new FileInputStream(dstDirectory + ".zip"));
                                ZipEntry ze = zis.getNextEntry();
                                String nextFileName;
                                while (ze != null) {
                                    nextFileName = ze.getName();
                                    File nextFile = new File(dstDirectory + File.separator + nextFileName);
                                    System.out.println("Распаковываем: " + nextFile.getAbsolutePath());
// Если мы имеем дело с каталогом - надо его создать. Если этого не сделать, то не будут созданы пустые каталоги архива
                                    if (ze.isDirectory()) {
                                        nextFile.mkdir();
                                    } else {
                                        // Создаем все родительские каталоги
                                        new File(nextFile.getParent()).mkdirs();
                                        // Записываем содержимое файла
                                        try (FileOutputStream fos = new FileOutputStream(nextFile)) {
                                            int length;
                                            while ((length = zis.read(buffer)) > 0) {
                                                fos.write(buffer, 0, length);
                                            }
                                        }
                                    }
                                    ze = zis.getNextEntry();
                                }
                                zis.closeEntry();
                                zis.close();
                            } catch (FileNotFoundException ex) {
                                ex.printStackTrace();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                        return null;

                    }

                };
                task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent t) {
                        String newDir = new String();
                        for (String s : Path)
                            newDir += s;
                        File newfile = new File(newDir);
                        String basePath = ClassLoader.getSystemResource("").getPath();
                        String[] newfilesName = newfile.list();
                        File file = new File(basePath.substring(1) + "/main/resources/file.png");
                        String localUrl = file.toURI().toString();
                        Image image = new Image(localUrl);

                        File file1 = new File(basePath.substring(1) + "/main/resources/archive.png");
                        String localUrl1 = file1.toURI().toString();
                        Image image1 = new Image(localUrl1);

                        File file2 = new File(basePath.substring(1) + "/main/resources/folder.png");
                        String localUrl2 = file2.toURI().toString();
                        Image image2 = new Image(localUrl2);

                        ArrayList<myTableViewTypes> BufferTableView = new ArrayList<>();
                        Main.initTableView(newDir, newfilesName, image, image1, image2, BufferTableView);
                        Langs.getItems().clear(); //избавляемся от старых файлов
                        Langs.getItems().addAll(BufferTableView);
                        System.out.println("ZIP UnArchiving done");
                        System.out.println(task.isDone());
                    }
                });
                new Thread(task).start();
            } else
                showErrorMessage("You cannot archive roots.");
        } else
            showErrorMessage("You should choose file to archive.");
    }

    public static String GetFileSize(Long size) {
        String Sz = "Bytes";
        if (size > 1000) {
            Sz = "Kb";
            size /= 1000;
        }
        if (size > 1000) {
            Sz = "Mb";
            size /= 1000;
        }
        if (size > 1000) {
            Sz = "Gb";
            size /= 1000;
        }
        if (size > 1000) {
            Sz = "Tb";
            size /= 1000;
        }
        return size.toString() + " " + Sz;
    }

    public static void SearchingPane(Stage stage, myTabPane myTP, ArrayList<ArrayList<String>> PathsList, ArrayList<TableView<myTableViewTypes>> GlobalFilesListView, FlowPane root) {

        Stage dialog = new Stage();
        dialog.setMaxWidth(400);
        dialog.setMaxHeight(300);

        ArrayList<CheckBox> checkBoxes = new ArrayList<>();

        for (Tab tab : myTP.getMyTabPane().getTabs())
            if (!tab.getText().equals("Search Res") && !tab.getText().equals("Roots"))
                checkBoxes.add(new CheckBox((tab.getText())));

        VBox content = new VBox();
        for (CheckBox checkBox : checkBoxes)
            content.getChildren().add(checkBox);
        TitledPane titledPane = new TitledPane("Choose searching tabs", content);
        titledPane.setExpanded(false);

        TextField textField = new TextField();
        Label textLabel = new Label("Enter file name:");

        Button cancelBtn = new Button("Cancel");
        Button searchBtn = new Button("Search");
        searchBtn.setDisable(true);

        FlowPane btnPane = new FlowPane(Orientation.HORIZONTAL, 100, 100, searchBtn, cancelBtn);

        FlowPane dialogpane = new FlowPane(Orientation.VERTICAL, 20, 20, titledPane, textLabel, textField, btnPane);
        dialogpane.setMinSize(400, 300);

        Scene dialogscene = new Scene(dialogpane);

        for (Node node : content.getChildren()) //если выбрали таб, то можно искать
            node.setOnMouseClicked(event -> {
                Boolean checker = false;
                for (CheckBox checkBox : checkBoxes)
                    if (checkBox.isSelected()) {
                        checker = true;
                        searchBtn.setDisable(false);
                    }
                if (!checker)
                    searchBtn.setDisable(true);
            });

        cancelBtn.setOnMouseClicked(event -> dialog.close());

        searchBtn.setOnMouseClicked(event -> {
            ArrayList<String> dirs = new ArrayList<>();
            ArrayList<String> badDirs = new ArrayList<>();
            for (CheckBox checkBox : checkBoxes)
                if (checkBox.isSelected()) {
                    checkBoxes.indexOf(checkBox);
                    String fullpath = new String();
                    for (String file : PathsList.get(checkBoxes.indexOf(checkBox)))
                        fullpath += file;
                    if (dirs.size() > 0) {
                        Boolean checker = true;
                        for (String prevFiles : dirs) {
                            if (fullpath.contains(prevFiles) && prevFiles.lastIndexOf("\\") != fullpath.lastIndexOf("\\"))
                                checker = false;
                            if (prevFiles.contains(fullpath) && prevFiles.lastIndexOf("\\") != fullpath.lastIndexOf("\\"))
                                badDirs.add(prevFiles);
                        }
                        if (checker)
                            dirs.add(fullpath);
                    }
                    if (dirs.size() == 0)
                        dirs.add(fullpath);                 //////////////////////пока не будем делать поиск в roots
                }

            for (int i = 0; i < dirs.size(); ++i)
                for (String str : badDirs)
                    if (dirs.get(i).equals(str)) {
                        dirs.remove(i);
                    }
            String FileName = new String(textField.getText());

            dialog.close();

            ArrayList<String> ResultList = new ArrayList<>();
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    for (String dir : dirs) {
                        ResultList.addAll(searchFileByDeepness(dir, FileName));
                    }
                    return null;
                }
            };

            task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event1 -> {

                ObservableList<String> langs = FXCollections.observableArrayList(ResultList);
                ListView<String> SearchListView = new ListView<>(langs);
                Tab SearchTab = new Tab("Search Res");
                SearchTab.setContent(SearchListView);
                if (myTP.getMyTabPane().getTabs().get(myTP.getMyTabPane().getTabs().size() - 1).getText().equals("Search Res")) { //Search лист пока будет 1
                    myTP.getMyTabPane().getTabs().remove(myTP.getMyTabPane().getTabs().size() - 1);
                    myTP.getMyTabPane().getTabs().add(SearchTab);
                } else
                    myTP.getMyTabPane().getTabs().add(SearchTab);
                myTP.getMyTabPane().getSelectionModel().selectLast();

                SearchListView.setOnMouseClicked(event2 -> {
                    if (event2.getButton().equals(MouseButton.PRIMARY) && event2.getClickCount() == 2) {
                        File selectedFile = new File(SearchListView.getSelectionModel().getSelectedItem());
                        if (selectedFile.isFile())
                            try {
                                java.awt.Desktop.getDesktop().open(selectedFile);
                            } catch (IOException e) {
                                Functions.showErrorMessage("File opening error.");
                            }
                        if (selectedFile.isDirectory()) {
                            myTP.getMyTabPane().getSelectionModel().selectPrevious();
                            myTP.AddTab(GlobalFilesListView, root);
                            myTP.getMyTabPane().getSelectionModel().select(PathsList.size() - 1);
                            PathsList.get(PathsList.size() - 1).clear();
                            File buffFile = new File(selectedFile.getAbsolutePath());
                            while (!(buffFile.getParent() == null)) {
                                PathsList.get(PathsList.size() - 1).add(0, "\\" + buffFile.getName());
                                if (!(buffFile.getParent() == null))
                                    buffFile = new File(buffFile.getParent());
                            }
                            PathsList.get(PathsList.size() - 1).add(0, buffFile.getAbsolutePath());
                            String[] newfilesName = selectedFile.list();
                            ArrayList<myTableViewTypes> BufferTableView = new ArrayList<>();
                            Main.initTableView(selectedFile.getAbsolutePath(), newfilesName, myTP.image, myTP.image1, myTP.image2, BufferTableView);
                            myTP.TabTableView.get(PathsList.size() - 1).getItems().clear(); //избавляемся от старых файлов
                            myTP.TabTableView.get(PathsList.size() - 1).getItems().addAll(BufferTableView);
                            myTP.UpdateTab(selectedFile.getName());
                            myTP.TabTableView.get(PathsList.size() - 1).scrollTo(0);
                        }
                    }
                });

            });

            new Thread(task).start();

        });

        dialog.initOwner(stage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setScene(dialogscene);
        dialog.showAndWait();
    }

    private static ArrayList<String> searchFileByDeepness(String directoryName, String fileName) {
        ArrayList<File> target = new ArrayList<>();
        if (directoryName != null && fileName != null) {
            File directory = new File(directoryName);
            if (directory.isDirectory()) {
                for (File neededFile : directory.listFiles()) {
                    if (neededFile.getName().contains(fileName))
                        target.add(neededFile);
                }

                List<File> subDirectories = getSubDirectories(directory);
                do {
                    List<File> subSubDirectories = new ArrayList<>();
                    for (File subDirectory : subDirectories) {
                        if (!(subDirectory.listFiles() == null)) {
                            for (File neededFile : subDirectory.listFiles()) {
                                if (neededFile.getName().contains(fileName))
                                    target.add(neededFile);
                            }
                            subSubDirectories.addAll(getSubDirectories(subDirectory));
                        }
                    }
                    subDirectories = subSubDirectories;
                } while (subDirectories != null && !subDirectories.isEmpty());
            }
        }
        ArrayList<String> PathsList = new ArrayList<>();
        if (target.size() != 0)
            for (File file : target)
                PathsList.add(file.getAbsolutePath());
        return PathsList;
    }

    private static List<File> getSubDirectories(File directory) {
        File[] subDirectories = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        return Arrays.asList(subDirectories);
    }

    public static void GroupRename(Stage stage, myTabPane myTP, ArrayList<ArrayList<String>> PathsList, ArrayList<TableView<myTableViewTypes>> GlobalFilesListView, FlowPane root) {
        {
            Stage dialog = new Stage();
            dialog.setMaxWidth(400);
            dialog.setMaxHeight(300);

            ArrayList<CheckBox> checkBoxes = new ArrayList<>();

            for (Tab tab : myTP.getMyTabPane().getTabs())
                if (!tab.getText().equals("Search Res") && !tab.getText().equals("Roots"))
                    checkBoxes.add(new CheckBox((tab.getText())));

            VBox content = new VBox();
            for (CheckBox checkBox : checkBoxes)
                content.getChildren().add(checkBox);
            TitledPane titledPane = new TitledPane("Choose tabs for the group rename", content);
            titledPane.setExpanded(false);

            RadioButton RNAll = new RadioButton("ReName all files");
            RadioButton RNSel = new RadioButton("ReName selected files");
            RadioButton AddAll = new RadioButton("Add name to all files");
            RadioButton AddSel = new RadioButton("Add name to selected files");

            ToggleGroup group = new ToggleGroup();
            // установка группы
            RNAll.setToggleGroup(group);
            RNSel.setToggleGroup(group);
            AddAll.setToggleGroup(group);
            AddSel.setToggleGroup(group);

            RadioButton countType = new RadioButton("1,2,3...");
            RadioButton ImSizeType = new RadioButton("Pixels size of image");
            RadioButton VidSizeType = new RadioButton("Pixels size of video");
            RadioButton VidLenType = new RadioButton("Lenght of video");
            RadioButton VidFPSType = new RadioButton("FPS of video");
            RadioButton AudLenType = new RadioButton("Lenght of audio");
            RadioButton AudBitrateType = new RadioButton("Bitrate of audio");

            ToggleGroup groupType = new ToggleGroup();
            countType.setToggleGroup(groupType);
            ImSizeType.setToggleGroup(groupType);
            VidSizeType.setToggleGroup(groupType);
            VidLenType.setToggleGroup(groupType);
            VidFPSType.setToggleGroup(groupType);
            AudLenType.setToggleGroup(groupType);
            AudBitrateType.setToggleGroup(groupType);

            VBox reNameType = new VBox();
            reNameType.getChildren().addAll(countType, ImSizeType, VidSizeType, VidLenType, VidFPSType, AudLenType, AudBitrateType);
            TitledPane titledPane1 = new TitledPane("Choose type of renaming", reNameType);
            titledPane1.setExpanded(false);

            countType.setOnMouseClicked(event -> titledPane1.setExpanded(false));
            ImSizeType.setOnMouseClicked(event -> titledPane1.setExpanded(false));
            VidSizeType.setOnMouseClicked(event -> titledPane1.setExpanded(false));
            VidLenType.setOnMouseClicked(event -> titledPane1.setExpanded(false));
            VidFPSType.setOnMouseClicked(event -> titledPane1.setExpanded(false));
            AudLenType.setOnMouseClicked(event -> titledPane1.setExpanded(false));
            AudBitrateType.setOnMouseClicked(event -> titledPane1.setExpanded(false));

            VBox content2 = new VBox(RNAll, RNSel, AddAll, AddSel);
            TitledPane titledPane2 = new TitledPane("Choose one of this:", content2);
            titledPane2.setExpanded(false);

            RNAll.setOnMouseClicked(event -> titledPane2.setExpanded(false));
            RNSel.setOnMouseClicked(event -> titledPane2.setExpanded(false));
            AddAll.setOnMouseClicked(event -> titledPane2.setExpanded(false));
            AddSel.setOnMouseClicked(event -> titledPane2.setExpanded(false));

            Button cancelBtn = new Button("Cancel");
            Button renameBtn = new Button("ReName");

            cancelBtn.setOnMouseClicked(event -> dialog.close());

            renameBtn.setDisable(true);

            FlowPane btnPane = new FlowPane(Orientation.HORIZONTAL, 100, 100, renameBtn, cancelBtn);

            FlowPane dialogpane = new FlowPane(Orientation.VERTICAL, 20, 20, titledPane, titledPane2, titledPane1, btnPane);
            dialogpane.setMinSize(400, 300);

            Scene dialogscene = new Scene(dialogpane);

            for (Node node : content.getChildren()) //если выбрали таб, то можно искать
                node.setOnMouseClicked(event -> {
                    Boolean checker = false;
                    for (CheckBox checkBox : checkBoxes)
                        if (checkBox.isSelected()) {
                            checker = true;
                            renameBtn.setDisable(false);
                        }
                    if (!checker)
                        renameBtn.setDisable(true);
                });

            //по выбранным вкладкам получаем либо все, либо выбранные файлы
            //К этим листам файлов применяем выбранное переименование

            renameBtn.setOnMouseClicked(event -> {
                for (CheckBox check : checkBoxes) {
                    if (check.isSelected()) {
                        int index = checkBoxes.indexOf(check);
                        if (RNAll.isSelected() || AddAll.isSelected()) {
                            String CurrPath = "";
                            for (String str : PathsList.get(index))
                                CurrPath += str;
                            if (AddAll.isSelected()) {
                                Integer i = 0;
                                for (File file : new File(CurrPath).listFiles()) {
                                    //добавляем имя каждому файлу
                                    if (countType.isSelected()) {
                                        ++i;
                                        if (file.isDirectory()) {
                                            File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + i.toString());
                                            file.renameTo(newFile);
                                        }
                                        if (file.isFile()) {
                                            String extension = "";
                                            int j = file.getName().lastIndexOf('.');
                                            if (j > 0) {
                                                extension = file.getName().substring(j + 1);
                                            }
                                            File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + i.toString() + '.' + extension);
                                            file.renameTo(newFile);
                                        }
                                    }
                                    if (AudLenType.isSelected()) {
                                        if (file.isFile() && file.getName().contains(".mp3")) {
                                            try {
                                                Mp3File mp3file = new Mp3File(file.getAbsolutePath());
                                                for (File file2 : new File(CurrPath).listFiles())  //проверка на совпадение имён
                                                    if (file2.getName().equals(file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + new Long(mp3file.getLengthInSeconds()).toString() + "s.mp3") && !file.equals(file2)) {
                                                        ++i;
                                                        File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + new Long(mp3file.getLengthInSeconds()).toString() + "s" + "(" + i.toString() + ")" + ".mp3");
                                                        file.renameTo(newFile);
                                                    }
                                                File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + new Long(mp3file.getLengthInSeconds()).toString() + "s.mp3");
                                                file.renameTo(newFile);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (InvalidDataException e) {
                                                e.printStackTrace();
                                            } catch (UnsupportedTagException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    if (AudBitrateType.isSelected()) {
                                        if (file.isFile() && file.getName().contains(".mp3")) {
                                            try {
                                                Mp3File mp3file = new Mp3File(file.getAbsolutePath());
                                                for (File file2 : new File(CurrPath).listFiles())  //проверка на совпадение имён
                                                    if (file2.getName().equals(file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + new Long(mp3file.getBitrate()).toString() + "kbps.mp3") && !file.equals(file2)) {
                                                        ++i;
                                                        File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + new Long(mp3file.getBitrate()).toString() + "kbps" + "(" + i.toString() + ")" + ".mp3");
                                                        file.renameTo(newFile);
                                                    }
                                                File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + new Long(mp3file.getBitrate()).toString() + "kbps.mp3");
                                                file.renameTo(newFile);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (InvalidDataException e) {
                                                e.printStackTrace();
                                            } catch (UnsupportedTagException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    if (VidFPSType.isSelected()) {
                                        if (file.isFile() && (file.getName().contains(".mp4") || file.getName().contains(".avi")) || file.getName().contains(".mov")) {
                                            try {
                                                Metadata metadata = ImageMetadataReader.readMetadata(file);
                                                for (Directory directory : metadata.getDirectories()) {
                                                    for (Tag tag : directory.getTags()) {
                                                        if (tag.getTagName().contains("Frame Rate")) {
                                                            String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                                            for (File file2 : new File(CurrPath).listFiles()) {  //проверка на совпадение имён
                                                                if (file2.getName().equals(file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + tag.toString().substring(tag.toString().lastIndexOf(" ") + 1) + "fps" + ext) && !file.equals(file2)) {
                                                                    ++i;
                                                                    File newFile1 = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + tag.toString().substring(tag.toString().lastIndexOf(" ") + 1) + "(" + i.toString() + ")" + "fps" + ext);
                                                                    file.renameTo(newFile1);
                                                                }
                                                            }
                                                            File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + tag.toString().substring(tag.toString().lastIndexOf(" ") + 1) + "fps" + ext);
                                                            file.renameTo(newFile);
                                                        }
                                                    }
                                                }
                                            } catch (ImageProcessingException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    }
                                    if (VidLenType.isSelected()) {
                                        if (file.isFile() && (file.getName().contains(".mp4") || file.getName().contains(".avi")) || file.getName().contains(".mov")) {
                                            try {
                                                Metadata metadata = ImageMetadataReader.readMetadata(file);
                                                for (Directory directory : metadata.getDirectories()) {
                                                    for (Tag tag : directory.getTags()) {
                                                        if (tag.getTagName().contains("Duration")) {
                                                            String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                                            String TagName = tag.toString().substring(tag.toString().lastIndexOf(" ") + 1);
                                                            TagName = TagName.replaceAll(":", ";");
                                                            for (File file2 : new File(CurrPath).listFiles()) {  //проверка на совпадение имён
                                                                if (file2.getName().equals(file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + TagName + ext) && !file.equals(file2)) {
                                                                    ++i;
                                                                    File newFile1 = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + TagName + "(" + i.toString() + ")" + ext);
                                                                    file.renameTo(newFile1);
                                                                }
                                                            }
                                                            File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + TagName + ext);
                                                            file.renameTo(newFile);
                                                        }
                                                    }
                                                }
                                            } catch (ImageProcessingException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    if (VidSizeType.isSelected()) {
                                        if (file.isFile() && (file.getName().contains(".mp4") || file.getName().contains(".avi")) || file.getName().contains(".mov")) {
                                            try {
                                                String str = "";
                                                Metadata metadata = ImageMetadataReader.readMetadata(file);
                                                for (Directory directory : metadata.getDirectories()) {
                                                    for (Tag tag : directory.getTags()) {
                                                        if (tag.getTagName().contains("Width")) {
                                                            str = tag.toString().substring(tag.toString().indexOf("-") + 2);
                                                            str = str.substring(0, str.lastIndexOf(" "));
                                                        }
                                                        if (tag.getTagName().contains("Height")) {
                                                            String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                                            str += "x" + tag.toString().substring(tag.toString().indexOf("-") + 2);
                                                            str = str.substring(0, str.lastIndexOf(" "));
                                                            for (File file2 : new File(CurrPath).listFiles()) {  //проверка на совпадение имён
                                                                if (file2.getName().equals(file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + str + ext) && !file.equals(file2)) {
                                                                    ++i;
                                                                    File newFile1 = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + str + "(" + i.toString() + ")" + ext);
                                                                    file.renameTo(newFile1);
                                                                }
                                                            }
                                                            File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + str + ext);
                                                            file.renameTo(newFile);
                                                        }
                                                    }
                                                }
                                            } catch (ImageProcessingException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    }
                                    if (ImSizeType.isSelected()) {
                                        if (file.isFile() && (file.getName().contains(".jpg") || file.getName().contains(".png")) || file.getName().contains(".bmp") || file.getName().contains(".tiff")) {
                                            {
                                                String str = "";
                                                String localUrl = file.toURI().toString();
                                                Image image = new Image(localUrl);
                                                str += (int) image.getWidth() + "x" + (int) image.getHeight();
                                                String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                                for (File file2 : new File(CurrPath).listFiles()) {  //проверка на совпадение имён
                                                    if (file2.getName().equals(file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + str + ext) && !file.equals(file2)) {
                                                        ++i;
                                                        File newFile1 = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + str + "(" + i.toString() + ")" + ext);
                                                        file.renameTo(newFile1);
                                                    }
                                                }
                                                File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + str + ext);
                                                file.renameTo(newFile);
                                            }
                                        }
                                    }
                                }
                            }
                            if (RNAll.isSelected()) {
                                Integer i = 0;
                                for (File file : new File(CurrPath).listFiles()) {
                                    //меняем имя каждому файлу
                                    if (countType.isSelected()) {
                                        ++i;
                                        if (file.isDirectory()) {
                                            File newFile = new File(CurrPath + "\\" + i.toString());
                                            file.renameTo(newFile);
                                        }
                                        if (file.isFile()) {
                                            String extension = "";
                                            int j = file.getName().lastIndexOf('.');
                                            if (j > 0) {
                                                extension = file.getName().substring(j + 1);
                                            }
                                            File newFile = new File(CurrPath + "\\" + i.toString() + '.' + extension);
                                            file.renameTo(newFile);
                                        }
                                    }
                                    if (AudLenType.isSelected()) {
                                        if (file.isFile() && file.getName().contains(".mp3")) {
                                            try {
                                                Mp3File mp3file = new Mp3File(file.getAbsolutePath());
                                                for (File file2 : new File(CurrPath).listFiles())  //проверка на совпадение имён
                                                    if (file2.getName().equals(new Long(mp3file.getLengthInSeconds()).toString() + "s.mp3") && !file.equals(file2)) {
                                                        ++i;
                                                        File newFile = new File(CurrPath + "\\" + new Long(mp3file.getLengthInSeconds()).toString() + "s" + "(" + i.toString() + ")" + ".mp3");
                                                        file.renameTo(newFile);
                                                    }
                                                File newFile = new File(CurrPath + "\\" + new Long(mp3file.getLengthInSeconds()).toString() + "s.mp3");
                                                file.renameTo(newFile);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (InvalidDataException e) {
                                                e.printStackTrace();
                                            } catch (UnsupportedTagException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    if (AudBitrateType.isSelected()) {
                                        if (file.isFile() && file.getName().contains(".mp3")) {
                                            try {
                                                Mp3File mp3file = new Mp3File(file.getAbsolutePath());
                                                for (File file2 : new File(CurrPath).listFiles())  //проверка на совпадение имён
                                                    if (file2.getName().equals(new Long(mp3file.getBitrate()).toString() + "kbps.mp3") && !file.equals(file2)) {
                                                        ++i;
                                                        File newFile = new File(CurrPath + "\\" + new Long(mp3file.getBitrate()).toString() + "kbps" + "(" + i.toString() + ")" + ".mp3");
                                                        file.renameTo(newFile);
                                                    }
                                                File newFile = new File(CurrPath + "\\" + new Long(mp3file.getBitrate()).toString() + "kbps.mp3");
                                                file.renameTo(newFile);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (InvalidDataException e) {
                                                e.printStackTrace();
                                            } catch (UnsupportedTagException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    if (VidFPSType.isSelected()) {
                                        if (file.isFile() && (file.getName().contains(".mp4") || file.getName().contains(".avi")) || file.getName().contains(".mov")) {
                                            try {
                                                Metadata metadata = ImageMetadataReader.readMetadata(file);
                                                for (Directory directory : metadata.getDirectories()) {
                                                    for (Tag tag : directory.getTags()) {
                                                        if (tag.getTagName().contains("Frame Rate")) {
                                                            String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                                            for (File file2 : new File(CurrPath).listFiles()) {  //проверка на совпадение имён
                                                                if (file2.getName().equals(tag.toString().substring(tag.toString().lastIndexOf(" ") + 1) + "fps" + ext) && !file.equals(file2)) {
                                                                    ++i;
                                                                    File newFile1 = new File(CurrPath + "\\" + tag.toString().substring(tag.toString().lastIndexOf(" ") + 1) + "(" + i.toString() + ")" + "fps" + ext);
                                                                    file.renameTo(newFile1);
                                                                }
                                                            }
                                                            File newFile = new File(CurrPath + "\\" + tag.toString().substring(tag.toString().lastIndexOf(" ") + 1) + "fps" + ext);
                                                            file.renameTo(newFile);
                                                        }
                                                    }
                                                }
                                            } catch (ImageProcessingException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    }
                                    if (VidLenType.isSelected()) {
                                        if (file.isFile() && (file.getName().contains(".mp4") || file.getName().contains(".avi")) || file.getName().contains(".mov")) {
                                            try {
                                                Metadata metadata = ImageMetadataReader.readMetadata(file);
                                                for (Directory directory : metadata.getDirectories()) {
                                                    for (Tag tag : directory.getTags()) {
                                                        if (tag.getTagName().contains("Duration")) {
                                                            String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                                            String TagName = tag.toString().substring(tag.toString().lastIndexOf(" ") + 1);
                                                            TagName = TagName.replaceAll(":", ";");
                                                            for (File file2 : new File(CurrPath).listFiles()) {  //проверка на совпадение имён
                                                                if (file2.getName().equals(TagName + ext) && !file.equals(file2)) {
                                                                    ++i;
                                                                    File newFile1 = new File(CurrPath + "\\" + TagName + "(" + i.toString() + ")" + ext);
                                                                    file.renameTo(newFile1);
                                                                }
                                                            }
                                                            File newFile = new File(CurrPath + "\\" + TagName + ext);
                                                            file.renameTo(newFile);
                                                        }
                                                    }
                                                }
                                            } catch (ImageProcessingException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    if (VidSizeType.isSelected()) {
                                        if (file.isFile() && (file.getName().contains(".mp4") || file.getName().contains(".avi")) || file.getName().contains(".mov")) {
                                            try {
                                                String str = "";
                                                Metadata metadata = ImageMetadataReader.readMetadata(file);
                                                for (Directory directory : metadata.getDirectories()) {
                                                    for (Tag tag : directory.getTags()) {
                                                        if (tag.getTagName().contains("Width")) {
                                                            str = tag.toString().substring(tag.toString().indexOf("-") + 2);
                                                            str = str.substring(0, str.lastIndexOf(" "));
                                                        }
                                                        if (tag.getTagName().contains("Height")) {
                                                            String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                                            str += "x" + tag.toString().substring(tag.toString().indexOf("-") + 2);
                                                            str = str.substring(0, str.lastIndexOf(" "));
                                                            for (File file2 : new File(CurrPath).listFiles()) {  //проверка на совпадение имён
                                                                if (file2.getName().equals(str + ext) && !file.equals(file2)) {
                                                                    ++i;
                                                                    File newFile1 = new File(CurrPath + "\\" + str + "(" + i.toString() + ")" + ext);
                                                                    file.renameTo(newFile1);
                                                                }
                                                            }
                                                            File newFile = new File(CurrPath + "\\" + str + ext);
                                                            file.renameTo(newFile);
                                                        }
                                                    }
                                                }
                                            } catch (ImageProcessingException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    }
                                    if (ImSizeType.isSelected()) {
                                        if (file.isFile() && (file.getName().contains(".jpg") || file.getName().contains(".png")) || file.getName().contains(".bmp") || file.getName().contains(".tiff")) {
                                            {
                                                String str = "";
                                                String localUrl = file.toURI().toString();
                                                Image image = new Image(localUrl);
                                                str += (int) image.getWidth() + "x" + (int) image.getHeight();
                                                String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                                for (File file2 : new File(CurrPath).listFiles()) {  //проверка на совпадение имён
                                                    if (file2.getName().equals(str + ext) && !file.equals(file2)) {
                                                        ++i;
                                                        File newFile1 = new File(CurrPath + "\\" + str + "(" + i.toString() + ")" + ext);
                                                        file.renameTo(newFile1);
                                                    }
                                                }
                                                File newFile = new File(CurrPath + "\\" + str + ext);
                                                file.renameTo(newFile);
                                            }
                                        }
                                    }
                                }
                            }

                        }
                        if (RNSel.isSelected() || AddSel.isSelected()) {
                            //выбранные файлы во вкладке
                            String CurrPath = "";
                            for (String str : PathsList.get(index))
                                CurrPath += str;
                            ArrayList<File> currFiles = new ArrayList<>();
                            for (myTableViewTypes line : GlobalFilesListView.get(index).getSelectionModel().getSelectedItems())
                                currFiles.add(new File(CurrPath + "\\" + line.getFileName()));
                            if (AddSel.isSelected()) {
                                Integer i = 0;
                                for (File file : currFiles) {
                                    //добавляем имя выбранным файлам
                                    if (countType.isSelected()) {
                                        ++i;
                                        if (file.isDirectory()) {
                                            File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + i.toString());
                                            file.renameTo(newFile);
                                        }
                                        if (file.isFile()) {
                                            String extension = "";
                                            int j = file.getName().lastIndexOf('.');
                                            if (j > 0) {
                                                extension = file.getName().substring(j + 1);
                                            }
                                            File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + i.toString() + '.' + extension);
                                            file.renameTo(newFile);
                                        }
                                    }
                                    if (AudLenType.isSelected()) {
                                        if (file.isFile() && file.getName().contains(".mp3")) {
                                            try {
                                                Mp3File mp3file = new Mp3File(file.getAbsolutePath());
                                                for (File file2 : new File(CurrPath).listFiles())  //проверка на совпадение имён
                                                    if (file2.getName().equals(file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + new Long(mp3file.getLengthInSeconds()).toString() + "s.mp3") && !file.equals(file2)) {
                                                        ++i;
                                                        File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + new Long(mp3file.getLengthInSeconds()).toString() + "s" + "(" + i.toString() + ")" + ".mp3");
                                                        file.renameTo(newFile);
                                                    }
                                                File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + new Long(mp3file.getLengthInSeconds()).toString() + "s.mp3");
                                                file.renameTo(newFile);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (InvalidDataException e) {
                                                e.printStackTrace();
                                            } catch (UnsupportedTagException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    if (AudBitrateType.isSelected()) {
                                        if (file.isFile() && file.getName().contains(".mp3")) {
                                            try {
                                                Mp3File mp3file = new Mp3File(file.getAbsolutePath());
                                                for (File file2 : new File(CurrPath).listFiles())  //проверка на совпадение имён
                                                    if (file2.getName().equals(file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + new Long(mp3file.getBitrate()).toString() + "kbps.mp3") && !file.equals(file2)) {
                                                        ++i;
                                                        File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + new Long(mp3file.getBitrate()).toString() + "kbps" + "(" + i.toString() + ")" + ".mp3");
                                                        file.renameTo(newFile);
                                                    }
                                                File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + new Long(mp3file.getBitrate()).toString() + "kbps.mp3");
                                                file.renameTo(newFile);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (InvalidDataException e) {
                                                e.printStackTrace();
                                            } catch (UnsupportedTagException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    if (VidFPSType.isSelected()) {
                                        if (file.isFile() && (file.getName().contains(".mp4") || file.getName().contains(".avi")) || file.getName().contains(".mov")) {
                                            try {
                                                Metadata metadata = ImageMetadataReader.readMetadata(file);
                                                for (Directory directory : metadata.getDirectories()) {
                                                    for (Tag tag : directory.getTags()) {
                                                        if (tag.getTagName().contains("Frame Rate")) {
                                                            String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                                            for (File file2 : new File(CurrPath).listFiles()) {  //проверка на совпадение имён
                                                                if (file2.getName().equals(file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + tag.toString().substring(tag.toString().lastIndexOf(" ") + 1) + "fps" + ext) && !file.equals(file2)) {
                                                                    ++i;
                                                                    File newFile1 = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + tag.toString().substring(tag.toString().lastIndexOf(" ") + 1) + "(" + i.toString() + ")" + "fps" + ext);
                                                                    file.renameTo(newFile1);
                                                                }
                                                            }
                                                            File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + tag.toString().substring(tag.toString().lastIndexOf(" ") + 1) + "fps" + ext);
                                                            file.renameTo(newFile);
                                                        }
                                                    }
                                                }
                                            } catch (ImageProcessingException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    }
                                    if (VidLenType.isSelected()) {
                                        if (file.isFile() && (file.getName().contains(".mp4") || file.getName().contains(".avi")) || file.getName().contains(".mov")) {
                                            try {
                                                Metadata metadata = ImageMetadataReader.readMetadata(file);
                                                for (Directory directory : metadata.getDirectories()) {
                                                    for (Tag tag : directory.getTags()) {
                                                        if (tag.getTagName().contains("Duration")) {
                                                            String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                                            String TagName = tag.toString().substring(tag.toString().lastIndexOf(" ") + 1);
                                                            TagName = TagName.replaceAll(":", ";");
                                                            for (File file2 : new File(CurrPath).listFiles()) {  //проверка на совпадение имён
                                                                if (file2.getName().equals(file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + TagName + ext) && !file.equals(file2)) {
                                                                    ++i;
                                                                    File newFile1 = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + TagName + "(" + i.toString() + ")" + ext);
                                                                    file.renameTo(newFile1);
                                                                }
                                                            }
                                                            File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + TagName + ext);
                                                            file.renameTo(newFile);
                                                        }
                                                    }
                                                }
                                            } catch (ImageProcessingException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    if (VidSizeType.isSelected()) {
                                        if (file.isFile() && (file.getName().contains(".mp4") || file.getName().contains(".avi")) || file.getName().contains(".mov")) {
                                            try {
                                                String str = "";
                                                Metadata metadata = ImageMetadataReader.readMetadata(file);
                                                for (Directory directory : metadata.getDirectories()) {
                                                    for (Tag tag : directory.getTags()) {
                                                        if (tag.getTagName().contains("Width")) {
                                                            str = tag.toString().substring(tag.toString().indexOf("-") + 2);
                                                            str = str.substring(0, str.lastIndexOf(" "));
                                                        }
                                                        if (tag.getTagName().contains("Height")) {
                                                            String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                                            str += "x" + tag.toString().substring(tag.toString().indexOf("-") + 2);
                                                            str = str.substring(0, str.lastIndexOf(" "));
                                                            for (File file2 : new File(CurrPath).listFiles()) {  //проверка на совпадение имён
                                                                if (file2.getName().equals(file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + str + ext) && !file.equals(file2)) {
                                                                    ++i;
                                                                    File newFile1 = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + str + "(" + i.toString() + ")" + ext);
                                                                    file.renameTo(newFile1);
                                                                }
                                                            }
                                                            File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + str + ext);
                                                            file.renameTo(newFile);
                                                        }
                                                    }
                                                }
                                            } catch (ImageProcessingException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    if (ImSizeType.isSelected()) {
                                        if (file.isFile() && (file.getName().contains(".jpg") || file.getName().contains(".png")) || file.getName().contains(".bmp") || file.getName().contains(".tiff")) {
                                            {
                                                String str = "";
                                                String localUrl = file.toURI().toString();
                                                Image image = new Image(localUrl);
                                                str += (int) image.getWidth() + "x" + (int) image.getHeight();
                                                String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                                for (File file2 : new File(CurrPath).listFiles()) {  //проверка на совпадение имён
                                                    if (file2.getName().equals(file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + str + ext) && !file.equals(file2)) {
                                                        ++i;
                                                        File newFile1 = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + str + "(" + i.toString() + ")" + ext);
                                                        file.renameTo(newFile1);
                                                    }
                                                }
                                                File newFile = new File(CurrPath + "\\" + file.getName().substring(0, file.getName().lastIndexOf(".")) + " " + str + ext);
                                                file.renameTo(newFile);
                                            }
                                        }
                                    }
                                }
                            }
                            if (RNSel.isSelected()) {
                                Integer i = 0;
                                for (File file : currFiles) {
                                    //меняем имя выбранным файлам
                                    if (countType.isSelected()) {
                                        ++i;
                                        if (file.isDirectory()) {
                                            File newFile = new File(CurrPath + "\\" + i.toString());
                                            file.renameTo(newFile);
                                        }
                                        if (file.isFile()) {
                                            String extension = "";
                                            int j = file.getName().lastIndexOf('.');
                                            if (j > 0) {
                                                extension = file.getName().substring(j + 1);
                                            }
                                            File newFile = new File(CurrPath + "\\" + i.toString() + '.' + extension);
                                            file.renameTo(newFile);
                                        }
                                    }
                                    if (AudLenType.isSelected()) {
                                        if (file.isFile() && file.getName().contains(".mp3")) {
                                            try {
                                                Mp3File mp3file = new Mp3File(file.getAbsolutePath());
                                                for (File file2 : new File(CurrPath).listFiles())  //проверка на совпадение имён
                                                    if (file2.getName().equals(new Long(mp3file.getLengthInSeconds()).toString() + "s.mp3") && !file.equals(file2)) {
                                                        ++i;
                                                        File newFile = new File(CurrPath + "\\" + new Long(mp3file.getLengthInSeconds()).toString() + "s" + "(" + i.toString() + ")" + ".mp3");
                                                        file.renameTo(newFile);
                                                    }
                                                File newFile = new File(CurrPath + "\\" + new Long(mp3file.getLengthInSeconds()).toString() + "s.mp3");
                                                file.renameTo(newFile);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (InvalidDataException e) {
                                                e.printStackTrace();
                                            } catch (UnsupportedTagException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    if (AudBitrateType.isSelected()) {
                                        if (file.isFile() && file.getName().contains(".mp3")) {
                                            try {
                                                Mp3File mp3file = new Mp3File(file.getAbsolutePath());
                                                for (File file2 : new File(CurrPath).listFiles())  //проверка на совпадение имён
                                                    if (file2.getName().equals(new Long(mp3file.getBitrate()).toString() + "kbps.mp3") && !file.equals(file2)) {
                                                        ++i;
                                                        File newFile = new File(CurrPath + "\\" + new Long(mp3file.getBitrate()).toString() + "kbps" + "(" + i.toString() + ")" + ".mp3");
                                                        file.renameTo(newFile);
                                                    }
                                                File newFile = new File(CurrPath + "\\" + new Long(mp3file.getBitrate()).toString() + "kbps.mp3");
                                                file.renameTo(newFile);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            } catch (InvalidDataException e) {
                                                e.printStackTrace();
                                            } catch (UnsupportedTagException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    if (VidFPSType.isSelected()) {
                                        if (file.isFile() && (file.getName().contains(".mp4") || file.getName().contains(".avi")) || file.getName().contains(".mov")) {
                                            try {
                                                Metadata metadata = ImageMetadataReader.readMetadata(file);
                                                for (Directory directory : metadata.getDirectories()) {
                                                    for (Tag tag : directory.getTags()) {
                                                        if (tag.getTagName().contains("Frame Rate")) {
                                                            String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                                            for (File file2 : new File(CurrPath).listFiles()) {  //проверка на совпадение имён
                                                                if (file2.getName().equals(tag.toString().substring(tag.toString().lastIndexOf(" ") + 1) + "fps" + ext) && !file.equals(file2)) {
                                                                    ++i;
                                                                    File newFile1 = new File(CurrPath + "\\" + tag.toString().substring(tag.toString().lastIndexOf(" ") + 1) + "(" + i.toString() + ")" + "fps" + ext);
                                                                    file.renameTo(newFile1);
                                                                }
                                                            }
                                                            File newFile = new File(CurrPath + "\\" + tag.toString().substring(tag.toString().lastIndexOf(" ") + 1) + "fps" + ext);
                                                            file.renameTo(newFile);
                                                        }
                                                    }
                                                }
                                            } catch (ImageProcessingException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    }
                                    if (VidLenType.isSelected()) {
                                        if (file.isFile() && (file.getName().contains(".mp4") || file.getName().contains(".avi")) || file.getName().contains(".mov")) {
                                            try {
                                                Metadata metadata = ImageMetadataReader.readMetadata(file);
                                                for (Directory directory : metadata.getDirectories()) {
                                                    for (Tag tag : directory.getTags()) {
                                                        if (tag.getTagName().contains("Duration")) {
                                                            String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                                            String TagName = tag.toString().substring(tag.toString().lastIndexOf(" ") + 1);
                                                            TagName = TagName.replaceAll(":", ";");
                                                            for (File file2 : new File(CurrPath).listFiles()) {  //проверка на совпадение имён
                                                                if (file2.getName().equals(TagName + ext) && !file.equals(file2)) {
                                                                    ++i;
                                                                    File newFile1 = new File(CurrPath + "\\" + TagName + "(" + i.toString() + ")" + ext);
                                                                    file.renameTo(newFile1);
                                                                }
                                                            }
                                                            File newFile = new File(CurrPath + "\\" + TagName + ext);
                                                            file.renameTo(newFile);
                                                        }
                                                    }
                                                }
                                            } catch (ImageProcessingException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                    if (VidSizeType.isSelected()) {
                                        if (file.isFile() && (file.getName().contains(".mp4") || file.getName().contains(".avi")) || file.getName().contains(".mov")) {
                                            try {
                                                String str = "";
                                                Metadata metadata = ImageMetadataReader.readMetadata(file);
                                                for (Directory directory : metadata.getDirectories()) {
                                                    for (Tag tag : directory.getTags()) {
                                                        if (tag.getTagName().contains("Width")) {
                                                            str = tag.toString().substring(tag.toString().indexOf("-") + 2);
                                                            str = str.substring(0, str.lastIndexOf(" "));
                                                        }
                                                        if (tag.getTagName().contains("Height")) {
                                                            String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                                            str += "x" + tag.toString().substring(tag.toString().indexOf("-") + 2);
                                                            str = str.substring(0, str.lastIndexOf(" "));
                                                            for (File file2 : new File(CurrPath).listFiles()) {  //проверка на совпадение имён
                                                                if (file2.getName().equals(str + ext) && !file.equals(file2)) {
                                                                    ++i;
                                                                    File newFile1 = new File(CurrPath + "\\" + str + "(" + i.toString() + ")" + ext);
                                                                    file.renameTo(newFile1);
                                                                }
                                                            }
                                                            File newFile = new File(CurrPath + "\\" + str + ext);
                                                            file.renameTo(newFile);
                                                        }
                                                    }
                                                }
                                            } catch (ImageProcessingException e) {
                                                e.printStackTrace();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    }
                                    if (ImSizeType.isSelected()) {
                                        if (file.isFile() && (file.getName().contains(".jpg") || file.getName().contains(".png")) || file.getName().contains(".bmp") || file.getName().contains(".tiff")) {
                                            {
                                                String str = "";
                                                String localUrl = file.toURI().toString();
                                                Image image = new Image(localUrl);
                                                str += (int) image.getWidth() + "x" + (int) image.getHeight();
                                                String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                                for (File file2 : new File(CurrPath).listFiles()) {  //проверка на совпадение имён
                                                    if (file2.getName().equals(str + ext) && !file.equals(file2)) {
                                                        ++i;
                                                        File newFile1 = new File(CurrPath + "\\" + str + "(" + i.toString() + ")" + ext);
                                                        file.renameTo(newFile1);
                                                    }
                                                }
                                                File newFile = new File(CurrPath + "\\" + str + ext);
                                                file.renameTo(newFile);
                                            }
                                        }
                                    }
                                }
                            }
                        }


                        String newDir = new String();
                        for (String s : PathsList.get(index))
                            newDir += s;

                        String basePath = ClassLoader.getSystemResource("").getPath();

                        File newfile = new File(newDir);
                        String[] newfilesName = newfile.list();
                        File file = new File(basePath.substring(1) + "/main/resources/file.png");      //заментить на взятие файлов из проектов
                        String localUrl = file.toURI().toString();
                        Image image = new Image(localUrl);

                        File file1 = new File(basePath.substring(1) + "/main/resources/archive.png");      //заментить на взятие файлов из проектов
                        String localUrl1 = file1.toURI().toString();
                        Image image1 = new Image(localUrl1);

                        File file2 = new File(basePath.substring(1) + "/main/resources/folder.png");      //заментить на взятие файлов из проектов
                        String localUrl2 = file2.toURI().toString();
                        Image image2 = new Image(localUrl2);

                        ArrayList<myTableViewTypes> BufferTableView = new ArrayList<>();
                        Main.initTableView(newDir, newfilesName, image, image1, image2, BufferTableView);
                        GlobalFilesListView.get(index).getItems().clear(); //избавляемся от старых файлов
                        GlobalFilesListView.get(index).getItems().addAll(BufferTableView);
                    }
                }
                dialog.close();
            });

            dialog.initOwner(stage);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(dialogscene);
            dialog.showAndWait();
        }
    }

    public static void GroupSort(Stage stage, myTabPane myTP, ArrayList<ArrayList<String>> PathsList, ArrayList<TableView<myTableViewTypes>> GlobalFilesListView, FlowPane root) {

        Stage dialog = new Stage();
        dialog.setMaxWidth(400);
        dialog.setMaxHeight(300);

        ArrayList<CheckBox> checkBoxes = new ArrayList<>();

        for (Tab tab : myTP.getMyTabPane().getTabs())
            if (!tab.getText().equals("Search Res") && !tab.getText().equals("Roots"))
                checkBoxes.add(new CheckBox((tab.getText())));

        VBox content = new VBox();
        for (CheckBox checkBox : checkBoxes)
            content.getChildren().add(checkBox);
        TitledPane titledPane = new TitledPane("Choose tabs for the group sort", content);
        titledPane.setExpanded(false);

        CheckBox ImSizeType = new CheckBox("Pixels size of image");
        CheckBox VidSizeType = new CheckBox("Pixels size of video");
        //CheckBox VidLenType = new CheckBox("Lenght of video");
        CheckBox VidFPSType = new CheckBox("FPS of video");
        CheckBox AudLenType = new CheckBox("Lenght of audio");
        CheckBox AudBitrateType = new CheckBox("Bitrate of audio");

        VBox SortType = new VBox();
        SortType.getChildren().addAll(ImSizeType, VidSizeType/*, VidLenType*/, VidFPSType, AudLenType, AudBitrateType);
        TitledPane titledPane1 = new TitledPane("Choose type of renaming", SortType);
        titledPane1.setExpanded(false);


        Button cancelBtn = new Button("Cancel");
        Button sortBtn = new Button("Sort");

        cancelBtn.setOnMouseClicked(event -> dialog.close());

        sortBtn.setDisable(true);

        FlowPane btnPane = new FlowPane(Orientation.HORIZONTAL, 100, 100, sortBtn, cancelBtn);

        FlowPane dialogpane = new FlowPane(Orientation.VERTICAL, 20, 20, titledPane, titledPane1, btnPane);
        dialogpane.setMinSize(400, 300);

        Scene dialogscene = new Scene(dialogpane);

        for (Node node : content.getChildren()) //если выбрали таб, то можно искать
            node.setOnMouseClicked(event -> {
                Boolean checker = false;
                for (CheckBox checkBox : checkBoxes)
                    if (checkBox.isSelected()) {
                        checker = true;
                        sortBtn.setDisable(false);
                    }
                if (!checker)
                    sortBtn.setDisable(true);
            });

        sortBtn.setOnMouseClicked(event -> {
            for (CheckBox check : checkBoxes) {
                if (check.isSelected()) {
                    int index = checkBoxes.indexOf(check);
                    String CurrPath = "";
                    for (String str : PathsList.get(index))
                        CurrPath += str;

                    ArrayList<String> SortTypes = new ArrayList<>();
                    SortTypes.add("");
                    if (ImSizeType.isSelected())
                        SortTypes.add("ImSize");
                    if (VidFPSType.isSelected())
                        SortTypes.add("VidFPS");
                    if (VidSizeType.isSelected())
                        SortTypes.add("VidSize");
                    if (AudBitrateType.isSelected())
                        SortTypes.add("AudBit");
                    if (AudLenType.isSelected())
                        SortTypes.add("AudLen");
                    for (String SortList : SortTypes)
                        Sort(CurrPath, SortList, index, GlobalFilesListView);
                    dialog.close();
                }
            }
        });

        dialog.initOwner(stage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setScene(dialogscene);
        dialog.showAndWait();
    }

    private static void Sort(String CurrPath, String SortType, int index, ArrayList<TableView<myTableViewTypes>> GlobalFilesListView) {
        if (SortType.equals(""))
            return;

        //берем файл, смотрим, есть ли у него папка с его характеристикой, если нет, то создаем и перемещаем его в эту папку
        File CurrDir = new File(CurrPath);
        for (File file : CurrDir.listFiles()) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    if (SortType.equals("ImSize")) {
                        if (file.isFile() && (file.getName().contains(".jpg") || file.getName().contains(".png"))) {
                            {
                                String str = "";
                                String localUrl = file.toURI().toString();
                                Image image = new Image(localUrl);
                                str += (int) image.getWidth() + "x" + (int) image.getHeight();
                                File NewDir = CrDir(CurrDir, str);
                                CopyPasteDelete(file, NewDir);
                            }
                        }
                    }
                    if (SortType.equals("VidFPS")) {
                        if (file.isFile() && (file.getName().contains(".mp4") || file.getName().contains(".avi")) || file.getName().contains(".mov")) {
                            try {
                                String str = "";
                                Metadata metadata = ImageMetadataReader.readMetadata(file);
                                for (Directory directory : metadata.getDirectories()) {
                                    for (Tag tag : directory.getTags()) {
                                        if (tag.getTagName().contains("Frame Rate")) {
                                            str += tag.toString().substring(tag.toString().lastIndexOf(" ") + 1) + "fps";
                                            File NewDir = CrDir(CurrDir, str);
                                            CopyPasteDelete(file, NewDir);
                                        }
                                    }
                                }
                            } catch (ImageProcessingException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if (SortType.equals("VidSize")) {
                        if (file.isFile() && (file.getName().contains(".mp4") || file.getName().contains(".avi")) || file.getName().contains(".mov")) {
                            try {
                                String str = "";
                                Metadata metadata = ImageMetadataReader.readMetadata(file);
                                for (Directory directory : metadata.getDirectories()) {
                                    for (Tag tag : directory.getTags()) {
                                        if (tag.getTagName().contains("Width")) {
                                            str = tag.toString().substring(tag.toString().indexOf("-") + 2);
                                            str = str.substring(0, str.lastIndexOf(" "));
                                        }
                                        if (tag.getTagName().contains("Height")) {
                                            String ext = file.getName().substring(file.getName().lastIndexOf("."));
                                            str += "x" + tag.toString().substring(tag.toString().indexOf("-") + 2);
                                            str = str.substring(0, str.lastIndexOf(" "));
                                            File NewDir = CrDir(CurrDir, str);
                                            CopyPasteDelete(file, NewDir);
                                        }
                                    }
                                }

                            } catch (ImageProcessingException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if(SortType.equals("AudBit"))
                    {
                        if (file.isFile() && file.getName().contains(".mp3")) {
                            try {
                                Mp3File mp3file = new Mp3File(file.getAbsolutePath());
                                File NewDir = CrDir(CurrDir, new Long(mp3file.getBitrate()).toString() + "kbps");
                                CopyPasteDelete(file, NewDir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InvalidDataException e) {
                                e.printStackTrace();
                            } catch (UnsupportedTagException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    if(SortType.equals("AudLen")){
                        if (file.isFile() && file.getName().contains(".mp3")) {
                            try {
                                Mp3File mp3file = new Mp3File(file.getAbsolutePath());
                                if(new Long(mp3file.getLengthInSeconds()) < 60)
                                {
                                    File NewDir = CrDir(CurrDir,  "less than 60sec");
                                    CopyPasteDelete(file, NewDir);
                                }
                                if(60<=new Long(mp3file.getLengthInSeconds()) && new Long(mp3file.getLengthInSeconds()) < 120)
                                {
                                    File NewDir = CrDir(CurrDir,  "from 60 to 120 sec");
                                    CopyPasteDelete(file, NewDir);
                                }
                                if(120<=new Long(mp3file.getLengthInSeconds()) && new Long(mp3file.getLengthInSeconds()) < 180)
                                {
                                    File NewDir = CrDir(CurrDir,  "from 120 to 180 sec");
                                    CopyPasteDelete(file, NewDir);
                                }
                                if(180<=new Long(mp3file.getLengthInSeconds()))
                                {
                                    File NewDir = CrDir(CurrDir,  "over 180 sec");
                                    CopyPasteDelete(file, NewDir);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InvalidDataException e) {
                                e.printStackTrace();
                            } catch (UnsupportedTagException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    return null;
                }
            };

            task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> {
                String[] newfilesName = CurrDir.list();
                String basePath = ClassLoader.getSystemResource("").getPath();

                File file3 = new File(basePath.substring(1) + "/main/resources/file.png");      //заментить на взятие файлов из проектов
                String localUrl = file3.toURI().toString();
                Image image = new Image(localUrl);

                File file1 = new File(basePath.substring(1) + "/main/resources/archive.png");      //заментить на взятие файлов из проектов
                String localUrl1 = file1.toURI().toString();
                Image image1 = new Image(localUrl1);

                File file2 = new File(basePath.substring(1) + "/main/resources/folder.png");      //заментить на взятие файлов из проектов
                String localUrl2 = file2.toURI().toString();
                Image image2 = new Image(localUrl2);

                ArrayList<myTableViewTypes> BufferTableView = new ArrayList<>();
                Main.initTableView(CurrDir.getAbsolutePath(), newfilesName, image, image1, image2, BufferTableView);
                GlobalFilesListView.get(index).getItems().clear(); //избавляемся от старых файлов
                GlobalFilesListView.get(index).getItems().addAll(BufferTableView);
            });

            new Thread(task).start();
        }
    }

    private static File CrDir(File CurrDir, String NewDir) {
        File DirType = new File(CurrDir.getAbsolutePath() + "\\" + NewDir);
        if (!DirType.exists())
            DirType.mkdir();
        return DirType;
    }

    private static void CopyPasteDelete(File file, File NewDir) {
        try {
            FileUtils.copyFileToDirectory(file, NewDir);
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
