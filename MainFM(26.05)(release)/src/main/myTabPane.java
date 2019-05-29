package main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.FlowPane;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

public class myTabPane {

    ArrayList<Tab> myTab = new ArrayList<>();
    TabPane myTP = new TabPane();
    ArrayList<TableView<myTableViewTypes>> TabTableView = new ArrayList<>();
    ArrayList<ArrayList<String>> PathsList = new ArrayList<>();
    Integer TabsCounts = 1;

    String basePath = ClassLoader.getSystemResource("").getPath();

    File file = new File(basePath.substring(1) + "/main/resources/file.png");      //заментить на взятие файлов из проектов
    String localUrl = file.toURI().toString();
    Image image = new Image(localUrl);

    File file1 = new File(basePath.substring(1) + "/main/resources/archive.png");      //заментить на взятие файлов из проектов
    String localUrl1= file1.toURI().toString();
    Image image1 = new Image(localUrl1);

    File file2 = new File(basePath.substring(1) + "/main/resources/folder.png");      //заментить на взятие файлов из проектов
    String localUrl2 = file2.toURI().toString();
    Image image2 = new Image(localUrl2);


    ContextMenu contextMenu;

    myTabPane(TableView<myTableViewTypes> Langs, ArrayList<ArrayList<String>> str, ContextMenu contextMenu) {

        TabTableView.add(Langs);
        Tab FirstTab = new Tab();
        FirstTab.setId("0");
        myTP.setTabMaxWidth(70);
        myTP.setTabMinWidth(70);
        PathsList = str;
        myTab.add(FirstTab);
        myTP.getTabs().add(myTab.get(0));
        myTab.get(0).setText("Roots");
        myTab.get(0).setContent(Langs);
        myTab.get(0).setClosable(false);

        this.contextMenu = contextMenu;
    }

    public void UpdateTab(String nameDir) {
        for (Tab Buf : myTab)
            if (Buf.isSelected())
                Buf.setText(nameDir);
    }


    public void AddTab(ArrayList<TableView<myTableViewTypes>> GlobalFilesListView, FlowPane root) {
        File[] roots = File.listRoots();                          //инициализируем добавляемую вкладку
        ArrayList<String> Roots = new ArrayList<>();
        for (File Buf : roots)
            Roots.add(Buf.getAbsolutePath());
        ArrayList<myTableViewTypes> RootTableView = new ArrayList<>();
        for (String Buff : Roots) {
            File file = new File(basePath.substring(1) + "/main/resources/disk.png");      //заментить на взятие файлов из проектов
            String localUrl = file.toURI().toString();
            Image image = new Image(localUrl);
            ImageView imFile = new ImageView(image);
            imFile.setFitWidth(15);
            imFile.setFitHeight(15);
            RootTableView.add(new myTableViewTypes(Buff, imFile, Functions.GetFileSize(new Long(0))));
        }
        ObservableList<myTableViewTypes> Langs = FXCollections.observableArrayList(RootTableView);
        TableView<myTableViewTypes> NewTableViewFiles = new TableView<>(Langs);

        NewTableViewFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        NewTableViewFiles.setMinSize(600, 400);

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
        NewTableViewFiles.getColumns().add(imageColumn);
        NewTableViewFiles.getColumns().add(fnameColumn);
        NewTableViewFiles.getColumns().add(sizeColumn);

        TabsCounts++;
        ArrayList<String> str = new ArrayList<>();
        TabTableView.add(NewTableViewFiles);
        PathsList.add(str);
        Tab NewTab = new Tab();
        NewTab.setId(TabsCounts.toString());

        NewTab.setText("Roots");
        NewTab.setContent(NewTableViewFiles);
        myTP.getSelectionModel().select(NewTab);
        myTab.add(NewTab);
        if(myTP.getTabs().get(myTP.getTabs().size()-1).getText().equals("Search Res")) {
            myTP.getTabs().add(myTP.getTabs().size()-1,NewTab);
        }
        else
            myTP.getTabs().add(NewTab);
        NewTab.setOnClosed(event -> {
            TabsCounts--;
            myTab.remove(NewTab);
            PathsList.remove(str);
            TabTableView.remove(NewTableViewFiles);
            GlobalFilesListView.remove(NewTableViewFiles);
        });


        GlobalFilesListView.add(NewTableViewFiles);

        NewTableViewFiles.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {      //переход в выбранную папку/запуск файла
                if (t.getButton().equals(MouseButton.SECONDARY)) {
                    contextMenu.show(NewTableViewFiles, t.getScreenX(), t.getScreenY());
                }

                if (t.getButton().equals(MouseButton.PRIMARY) && t.getClickCount() == 2) {
                    if (PathsList.get(myTP.getTabs().indexOf(NewTab)).isEmpty()) {
                        PathsList.get(myTP.getTabs().indexOf(NewTab)).add(NewTableViewFiles.getSelectionModel().getSelectedItem().getFileName());
                    } else
                        PathsList.get(myTP.getTabs().indexOf(NewTab)).add("\\" + NewTableViewFiles.getSelectionModel().getSelectedItem().getFileName());
                    String newDir = new String();
                    for (String s : PathsList.get(myTP.getTabs().indexOf(NewTab)))
                        newDir += s;
                    File newfile = new File(newDir);
                    if (newfile.isDirectory()) {
                        String[] newfilesName = newfile.list();
                        ArrayList<myTableViewTypes> BufferTableView = new ArrayList<>();
                        Main.initTableView(newDir, newfilesName, image, image1, image2, BufferTableView);
                        NewTableViewFiles.getItems().clear(); //избавляемся от старых файлов
                        NewTableViewFiles.getItems().addAll(BufferTableView);
                        UpdateTab(PathsList.get(myTP.getTabs().indexOf(NewTab)).get(PathsList.get(myTP.getTabs().indexOf(NewTab)).size() - 1));
                        NewTableViewFiles.scrollTo(0);
                    }
                    if (newfile.isFile()) {
                        PathsList.get(myTP.getTabs().indexOf(NewTab)).remove(PathsList.get(myTP.getTabs().indexOf(NewTab)).size() - 1);
                        try {
                            java.awt.Desktop.getDesktop().open(newfile);
                        } catch (IOException e) {
                            Functions.showErrorMessage("File opening error.");
                        }
                    }
                }
            }

        });


        // Add mouse event handlers for the source
        NewTableViewFiles.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1) {
                    NewTableViewFiles.setMouseTransparent(true);
                    //    System.out.println("Event on Source: mouse pressed");
                    event.setDragDetect(true);
                    ((Node) event.getSource()).setCursor(Cursor.CLOSED_HAND);
                }
            }
        });

        NewTableViewFiles.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                ((Node) event.getSource()).setCursor(Cursor.DEFAULT);
                NewTableViewFiles.setMouseTransparent(false);
                //  System.out.println("Event on Source: mouse released");
            }
        });

        NewTableViewFiles.setOnMouseDragged(event -> {
                    event.setDragDetect(false);
            //System.out.println("Event on Source: mouse dragged");
                }
        );

        NewTableViewFiles.setOnDragDetected(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                NewTableViewFiles.startFullDrag();
                // System.out.println("Event on Source: drag detected");
            }
        });


        NewTab.getContent().setOnMouseDragEntered(new EventHandler<MouseDragEvent>() {
            public void handle(MouseDragEvent event) {
            }
        });
        myTP.setOnMouseDragOver(new EventHandler<MouseDragEvent>() {
            public void handle(MouseDragEvent event) {
                // System.out.println("Event on Target: mouse drag over");
            }
        });

        myTP.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() //Сделать копирование перетаскиванием для первой вкладки
        {
            public void handle(MouseDragEvent event) {
                ((Node) event.getSource()).setCursor(Cursor.DEFAULT);

                int CurrTab = 0;
                for (Tab t : myTP.getTabs())
                    if (t.isSelected())
                        break;
                    else CurrTab++;

                System.out.println("Event on Target: mouse drag released");

                Set<Node> tabs = myTP.lookupAll(".tab");                                    //Сверяем id табов и нодов, копируем туда, где отпустили
                System.out.println(myTP.getLayoutX() + "   " + myTP.getLayoutY()); //Y = myTPY + 5
                for (Node node : tabs) {
                    if (event.getSceneX() < node.getLayoutX() + 82 && event.getSceneX() > node.getLayoutX() && event.getSceneY() > myTP.getLayoutY() + 5 && event.getSceneY() < myTP.getLayoutY() + 30)
                        for (Tab t : myTP.getTabs())
                            if (t.getId() == node.getId()) {  //проверка на совпадение таба и нода, где отжали мышку
                                System.out.println(t.getText() + "       " + TabsCounts);
                                ArrayList<String> CopiedFile = new ArrayList<>();
                                ArrayList<String> CopyMode = new ArrayList<>();
                                CopyMode.clear();
                                CopyMode.add("Copied");
                                Functions.CopyFile(PathsList.get(CurrTab), TabTableView.get(CurrTab), CopiedFile); //может быть проблема с индексами
                                int TabinCopy = 0; //номер таба куда копируем
                                for (int i = 0; i < myTP.getTabs().size(); i++)
                                    if (t.equals(myTP.getTabs().get(i)))
                                        TabinCopy = i;
                                Functions.PasteFile(PathsList.get(TabinCopy), CopiedFile, (TableView<myTableViewTypes>) t.getContent(),root,CopyMode);
                            }
                }
                System.out.println(event.getSceneX() + "   " + event.getSceneY());
            }
        });


    }

    public TabPane getMyTabPane() {
        myTP.setPrefWidth(600);
        return myTP;
    }
}
