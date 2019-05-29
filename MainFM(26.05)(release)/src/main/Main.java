package main;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class Main extends Application {


    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        File[] roots = File.listRoots(); //имена дисков


        String basePath = ClassLoader.getSystemResource("").getPath();

        File file1 = new File(basePath.substring(1) + "/main/resources/disk.png");
        String localUrl1 = file1.toURI().toURL().toString();
        Image image1 = new Image(localUrl1);


        ImageView imArch = new ImageView(image1);
        imArch.setFitWidth(15);
        imArch.setFitHeight(15);

        ArrayList<String> CopyBuffer = new ArrayList<>(); //пути скопированных файлов

        ArrayList<String> Roots = new ArrayList<>();
        for (File Buf : roots) {
            Roots.add(Buf.getAbsolutePath());
        }


        File file = new File(basePath.substring(1) + "/main/resources/file.png");
        String localUrl = file.toURI().toString();
        Image image = new Image(localUrl);

        File file2 = new File(basePath.substring(1) + "/main/resources/folder.png");
        String localUrl2 = file2.toURI().toString();
        Image image2 = new Image(localUrl2);

        File file3 = new File(basePath.substring(1) + "/main/resources/archive.png");
        String localUrl3 = file3.toURI().toString();
        Image image3 = new Image(localUrl3);

        ArrayList<myTableViewTypes> RootTableView = new ArrayList<>();
        initTableViewbyRoots(RootTableView, Roots);

        ObservableList<myTableViewTypes> ObsListTable = FXCollections.observableArrayList(RootTableView);

        // определяем таблицу и устанавливаем данные
        TableView<myTableViewTypes> FilesTableView = new TableView(ObsListTable);
        FilesTableView.setMinSize(600, 400);


        TableColumn<myTableViewTypes, ImageView> imageColumn = new TableColumn("Type");
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("FileImage"));

        TableColumn<myTableViewTypes, String> fnameColumn = new TableColumn("Name");
        // определяем фабрику для столбца с привязкой к свойству name
        fnameColumn.setCellValueFactory(new PropertyValueFactory<>("FileName"));

        TableColumn<myTableViewTypes, Long> sizeColumn = new TableColumn("Size");
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("FileSize"));


        imageColumn.setMaxWidth(40);
        fnameColumn.setMinWidth(280);
        sizeColumn.setPrefWidth(60);
        //добавляем столбцы
        FilesTableView.getColumns().add(imageColumn);
        FilesTableView.getColumns().add(fnameColumn);
        FilesTableView.getColumns().add(sizeColumn);

        FilesTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        ArrayList<TableView<myTableViewTypes>> GlobalFilesTableView = new ArrayList<>();
        GlobalFilesTableView.add(FilesTableView);

        Button BtnBack = new Button("Back");
        Button CreateDirBtn = new Button("New Directory");
        Button ReNameBtn = new Button("Rename");
        Button CopyBtn = new Button("Copy");
        Button CutBtn = new Button("Cut");
        Button PasteBtn = new Button("Paste");
        Button DeleteBtn = new Button("Delete");
        Button AddTabBtn = new Button("Add Tab");
        Button SearchBtn = new Button("Search");

        MenuItem ZIParchive = new MenuItem("ZIP Compress");
        MenuItem ZIPunarchive = new MenuItem("ZIP Decompress");
        MenuButton ArchiveBtn = new MenuButton("Archive");

        MenuItem GroupRename = new MenuItem("Group Rename");
        MenuItem GroupSort = new MenuItem("Group Sort");
        MenuButton GroupBtn = new MenuButton("Group Operations");

        ArchiveBtn.getItems().addAll(ZIParchive, ZIPunarchive);
        GroupBtn.getItems().addAll(GroupRename,GroupSort);

        ArrayList<String> str = new ArrayList<>();
        ArrayList<ArrayList<String>> PathsList = new ArrayList<>(); //путь к папке, в который мы находимся
        PathsList.add(str);


        MenuItem CreateFolderItem = new MenuItem("Create folder");                        //Items для контекстного меню по правой кнопке мыши
        MenuItem ReNameItem = new MenuItem("Rename");
        MenuItem CopyItem = new MenuItem("Copy");
        MenuItem CutItem = new MenuItem("Cut");
        MenuItem PasteItem = new MenuItem("Paste");
        MenuItem DeleteItem = new MenuItem("Delete");
        MenuItem ArchiveItem = new MenuItem("Compress to ZIP");
        MenuItem UnArchiveItem = new MenuItem("Decompress from ZIP");

        ContextMenu contextMenu = new ContextMenu(CreateFolderItem, ReNameItem, CopyItem, CutItem, PasteItem, DeleteItem, ArchiveItem, UnArchiveItem);

        myTabPane TP = new myTabPane(FilesTableView, PathsList, contextMenu); //TabPane


        MenuItem smthg1 = new MenuItem("Option 1");
        MenuItem smthg2 = new MenuItem("Option 2");
        MenuItem smthg3 = new MenuItem("Option 3");
        Menu MenuFile = new Menu("File", null, smthg1, smthg2, smthg3);

        MenuItem smthg4 = new MenuItem("Option 1");
        MenuItem smthg5 = new MenuItem("Option 2");
        MenuItem smthg6 = new MenuItem("Option 3");
        Menu MenuEdit = new Menu("Edit", null, smthg4, smthg5, smthg6);

        MenuItem smthg7 = new MenuItem("Option 1");
        MenuItem smthg8 = new MenuItem("Option 2");
        MenuItem smthg9 = new MenuItem("Option 3");
        Menu MenuView = new Menu("View", null, smthg7, smthg8, smthg9);

        MenuItem smthg10 = new MenuItem("Option 1");
        MenuItem smthg11 = new MenuItem("Option 2");
        MenuItem smthg12 = new MenuItem("Option 3");
        Menu MenuAbout = new Menu("About", null, smthg10, smthg11, smthg12);

        MenuBar Menu = new MenuBar(MenuFile, MenuEdit, MenuView, MenuAbout);


        FlowPane BtnPane = new FlowPane(10, 10, BtnBack, CreateDirBtn, ReNameBtn, CopyBtn, CutBtn, PasteBtn, DeleteBtn, ArchiveBtn, AddTabBtn, SearchBtn, GroupBtn);
        FlowPane root = new FlowPane(Orientation.VERTICAL, 10, 20, Menu, BtnPane, TP.getMyTabPane());

        BtnPane.setMinWidth(700);
        root.setMinWidth(700);
        Scene scene = new Scene(root, 900, 700);

        ArrayList<String> CopyMode = new ArrayList<>(); // Copied - для копирования, Cutted - вырезать

        CreateFolderItem.setOnAction(event -> {
            for (int i = 0; i < GlobalFilesTableView.size(); i++) {
                if (TP.myTP.getTabs().get(i).isSelected()) {
                    Functions.CreateDirectory(PathsList.get(i), GlobalFilesTableView.get(i));
                    GlobalFilesTableView.get(i).scrollTo(GlobalFilesTableView.get(i).getItems().size());
                }
            }
        });

        ReNameItem.setOnAction(event -> {
            RenameFile(GlobalFilesTableView, PathsList, TP);
        });

        CopyItem.setOnAction(event -> {
            for (int i = 0; i < GlobalFilesTableView.size(); i++) {
                if (TP.myTP.getTabs().get(i).isSelected()) {
                    Functions.CopyFile(PathsList.get(i), GlobalFilesTableView.get(i), CopyBuffer);
                    CopyMode.clear();
                    CopyMode.add("Copied");
                }
            }
        });

        CutItem.setOnAction(event -> {
            for (int i = 0; i < GlobalFilesTableView.size(); i++) {
                if (TP.myTP.getTabs().get(i).isSelected()) {
                    Functions.CutFile(PathsList.get(i), GlobalFilesTableView.get(i), CopyBuffer, CopyMode);
                }
            }
        });

        PasteItem.setOnAction(event -> {
            for (int i = 0; i < GlobalFilesTableView.size(); i++) {
                if (TP.myTP.getTabs().get(i).isSelected()) {
                    Functions.PasteFile(PathsList.get(i), CopyBuffer, GlobalFilesTableView.get(i), root,CopyMode);
                }
            }
        });

        ArchiveItem.setOnAction(event -> {
            for (int i = 0; i < GlobalFilesTableView.size(); i++) {
                if (TP.myTP.getTabs().get(i).isSelected()) {
                    Functions.ArchiveZIP(PathsList.get(i), GlobalFilesTableView.get(i));
                }
            }
        });

        UnArchiveItem.setOnAction(event -> {
            for (int i = 0; i < GlobalFilesTableView.size(); i++) {
                if (TP.myTP.getTabs().get(i).isSelected()) {
                    Functions.UnArchiveZIP(PathsList.get(i), GlobalFilesTableView.get(i));
                }
            }
        });

        DeleteItem.setOnAction(event -> {
            for (int i = 0; i < GlobalFilesTableView.size(); i++) {
                if (TP.myTP.getTabs().get(i).isSelected()) {
                    Functions.DeleteFile(PathsList.get(i), GlobalFilesTableView.get(i));
                }
            }
        });


        FilesTableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {      //переход в выбранную папку/запуск файла
                for (int i = 0; i < GlobalFilesTableView.size(); i++) {
                    if (TP.myTP.getTabs().get(i).isSelected()) {
                        if (t.getButton().equals(MouseButton.SECONDARY)) {
                            contextMenu.show(FilesTableView, t.getScreenX(), t.getScreenY());
                        }

                        if (t.getButton().equals(MouseButton.PRIMARY) && t.getClickCount() == 1)
                            contextMenu.hide();

                        if (t.getButton().equals(MouseButton.PRIMARY) && t.getClickCount() == 2) {

                            if (PathsList.get(i).isEmpty()) {
                                PathsList.get(i).add(GlobalFilesTableView.get(i).getSelectionModel().getSelectedItem().getFileName());
                            } else
                                PathsList.get(i).add("\\" + GlobalFilesTableView.get(i).getSelectionModel().getSelectedItem().getFileName());
                            String newDir = new String();
                            for (String s : PathsList.get(i))
                                newDir += s;
                            File newfile = new File(newDir);
                            if (newfile.isDirectory()) {
                                String[] newfilesName = newfile.list();

                                ArrayList<myTableViewTypes> BufferTableView = new ArrayList<>();
                                initTableView(newDir, newfilesName, image, image3, image2, BufferTableView);

                                GlobalFilesTableView.get(i).getItems().clear(); //избавляемся от старых файлов
                                GlobalFilesTableView.get(i).getItems().addAll(BufferTableView);
                                TP.UpdateTab(PathsList.get(i).get(PathsList.get(i).size() - 1));
                            }
                            if (newfile.isFile()) {
                                PathsList.get(i).remove(PathsList.get(i).size() - 1);
                                try {
                                    java.awt.Desktop.getDesktop().open(newfile);
                                } catch (IOException e) {
                                    Functions.showErrorMessage("File opening error.");
                                }
                            }
                            FilesTableView.scrollTo(0);
                        }
                    }
                }
            }
        });

        BtnBack.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) { //back to previous directory
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    for (int i = 0; i < GlobalFilesTableView.size(); i++) {
                        if (TP.myTP.getTabs().get(i).isSelected() && !TP.myTP.getTabs().get(i).equals("Search Res")) {
                            try {
                                String LastFolder = PathsList.get(i).get(PathsList.get(i).size() - 1);
                                PathsList.get(i).remove(PathsList.get(i).size() - 1);
                                if (PathsList.get(i).size() > 0) {
                                    LastFolder = LastFolder.substring(1); //убираем лишнюю \
                                    String newDir = new String();
                                    for (String s : PathsList.get(i))
                                        newDir += s;
                                    File newfile = new File(newDir);
                                    String[] newfilesName = newfile.list();

                                    ArrayList<myTableViewTypes> BufferTableView = new ArrayList<>();
                                    initTableView(newDir, newfilesName, image, image3, image2, BufferTableView);
                                    GlobalFilesTableView.get(i).getItems().clear(); //избавляемся от старых файлов
                                    GlobalFilesTableView.get(i).getItems().addAll(BufferTableView);
                                    for (int j = 0; j < BufferTableView.size(); j++)
                                        if (GlobalFilesTableView.get(i).getItems().get(j).getFileName().equals(LastFolder)) //подсвечиваем папку, из которой вышли
                                            GlobalFilesTableView.get(i).getSelectionModel().select(j);
                                    TP.UpdateTab(PathsList.get(i).get(PathsList.get(i).size() - 1));
                                }
                                if (PathsList.get(i).size() == 0) {
                                    GlobalFilesTableView.get(i).getItems().removeAll();
                                    File[] roots1 = File.listRoots();
                                    ArrayList<String> Roots1 = new ArrayList<>();
                                    for (File Buf : roots1)
                                        Roots1.add(Buf.getAbsolutePath());
                                    ArrayList<myTableViewTypes> RootTableView = new ArrayList<>();
                                    initTableViewbyRoots(RootTableView, Roots);
                                    GlobalFilesTableView.get(i).getItems().setAll(RootTableView);
                                    for (int j = 0; j < RootTableView.size(); j++)
                                        if (GlobalFilesTableView.get(i).getItems().get(j).getFileName().equals(LastFolder)) //подсвечиваем папку, из которой вышли
                                            GlobalFilesTableView.get(i).getSelectionModel().select(j);
                                    TP.UpdateTab("Roots");
                                }
                                GlobalFilesTableView.get(i).scrollTo(0);
                            } catch (Exception e) {
                                Functions.showErrorMessage("Can't get last directory.");
                            }
                        }
                    }
                }
            }
        });

        CreateDirBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                for (int i = 0; i < GlobalFilesTableView.size(); i++) {
                    if (TP.myTP.getTabs().get(i).isSelected()) {
                        Functions.CreateDirectory(PathsList.get(i), GlobalFilesTableView.get(i));
                        GlobalFilesTableView.get(i).scrollTo(GlobalFilesTableView.get(i).getItems().size());
                    }
                }
            }
        });

        ReNameBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                RenameFile(GlobalFilesTableView, PathsList, TP);
            }
        });

        CopyBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                for (int i = 0; i < GlobalFilesTableView.size(); i++) {
                    if (TP.myTP.getTabs().get(i).isSelected()) {
                        Functions.CopyFile(PathsList.get(i), GlobalFilesTableView.get(i), CopyBuffer);
                        CopyMode.clear();
                        CopyMode.add("Copied");
                    }
                }
            }
        });

        CutBtn.setOnMouseClicked(event -> {
            for (int i = 0; i < GlobalFilesTableView.size(); i++) {
                if (TP.myTP.getTabs().get(i).isSelected()) {
                    Functions.CutFile(PathsList.get(i), GlobalFilesTableView.get(i), CopyBuffer, CopyMode);
                }
            }
        });

        AddTabBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                TP.AddTab(GlobalFilesTableView, root);
            }
        });


        PasteBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                for (int i = 0; i < GlobalFilesTableView.size(); i++) {
                    if (TP.myTP.getTabs().get(i).isSelected()) {
                        Functions.PasteFile(PathsList.get(i), CopyBuffer, GlobalFilesTableView.get(i), root, CopyMode);
                    }
                }
            }
        });

        DeleteBtn.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                for (int i = 0; i < GlobalFilesTableView.size(); i++) {
                    if (TP.myTP.getTabs().get(i).isSelected()) {
                        Functions.DeleteFile(PathsList.get(i), GlobalFilesTableView.get(i));
                    }
                }
            }
        });

        ArchiveBtn.getItems().get(0).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                for (int i = 0; i < GlobalFilesTableView.size(); i++) {
                    if (TP.myTP.getTabs().get(i).isSelected()) {
                        Functions.ArchiveZIP(PathsList.get(i), GlobalFilesTableView.get(i));
                    }
                }
            }
        });

        ArchiveBtn.getItems().get(1).setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                for (int i = 0; i < GlobalFilesTableView.size(); i++) {
                    if (TP.myTP.getTabs().get(i).isSelected()) {
                        Functions.UnArchiveZIP(PathsList.get(i), GlobalFilesTableView.get(i));
                    }
                }
            }
        });

        GroupBtn.getItems().get(0).setOnAction(event -> {
            Functions.GroupRename(stage,TP,PathsList,GlobalFilesTableView,root);
        });

        GroupBtn.getItems().get(1).setOnAction(event -> {
            Functions.GroupSort(stage,TP,PathsList,GlobalFilesTableView,root);
        });

        SearchBtn.setOnMouseClicked(event -> {
            Functions.SearchingPane(stage,TP,PathsList,GlobalFilesTableView,root);
        });

        stage.setMinHeight(600);
        stage.setMinWidth(700);
        stage.setScene(scene);
        stage.setTitle("ADV Manager");
        stage.show();
    }

    public static void initTableViewbyRoots(ArrayList<myTableViewTypes> rootTableView, ArrayList<String> roots) {
        for (String Buff : roots) {
            String basePath = ClassLoader.getSystemResource("").getPath();
            File file = new File(basePath.substring(1) + "/main/resources/disk.png");
            String localUrl = file.toURI().toString();
            Image image = new Image(localUrl);
            ImageView imFile = new ImageView(image);
            imFile.setFitWidth(15);
            imFile.setFitHeight(15);
            rootTableView.add(new myTableViewTypes(Buff, imFile, Functions.GetFileSize(new Integer(0).longValue())));
        }
    }

    public static void initTableView(String newDir, String[] newfilesName, Image image, Image image3, Image image2, ArrayList<myTableViewTypes> bufferTableView) {
        for (String Buff : newfilesName) {
            File currFile = new File(newDir + "//" + Buff);
            if (currFile.isFile() && !currFile.getName().contains(".zip") && !currFile.getName().contains(".rar") && !currFile.getName().contains(".7z")) {
                ImageView imFile = new ImageView(image);
                imFile.setFitWidth(15);
                imFile.setFitHeight(15);
                bufferTableView.add(new myTableViewTypes(Buff, imFile, Functions.GetFileSize(currFile.length())));
            }
            if (currFile.isDirectory()) {
                ImageView imFile1 = new ImageView(image2);
                imFile1.setFitWidth(15);
                imFile1.setFitHeight(15);
                bufferTableView.add(new myTableViewTypes(Buff, imFile1, Functions.GetFileSize(new Integer(0).longValue())));
            }
            if (currFile.getName().contains(".zip") || currFile.getName().contains(".rar") || currFile.getName().contains(".7z")) {
                ImageView imFile1 = new ImageView(image3);
                imFile1.setFitWidth(15);
                imFile1.setFitHeight(15);
                bufferTableView.add(new myTableViewTypes(Buff, imFile1, Functions.GetFileSize(currFile.length())));
            }
        }
    }

    private void RenameFile(ArrayList<TableView<myTableViewTypes>> globalFilesTableView, ArrayList<ArrayList<String>> pathsList, myTabPane TP) {
        for (int i = 0; i < globalFilesTableView.size(); i++) {
            if (TP.myTP.getTabs().get(i).isSelected()) {

                if (globalFilesTableView.get(i).getSelectionModel().getSelectedIndex() == -1) {
                    Functions.showErrorMessage("Please select the file!");
                } else
                    Functions.Rename(pathsList.get(i), globalFilesTableView.get(i).getSelectionModel().getSelectedItem().getFileName(), globalFilesTableView.get(i));
            }
        }
    }
}

