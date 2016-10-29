/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chungkwong.jgitgui;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javafx.application.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import javafx.util.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class Main extends Application{
	private final DirectoryChooser dirChooser=new DirectoryChooser();
	private final TreeItem<Object> navigationRoot=new TreeItem<>();
	private final StackPane content=new StackPane();
	@Override
	public void start(Stage primaryStage){
		BorderPane root=new BorderPane();
		root.setTop(createMenuBar());
		SplitPane split=new SplitPane();
		split.setOrientation(Orientation.HORIZONTAL);

		split.getItems().add(createNavigation());
		split.getItems().add(content);
		split.setDividerPosition(0,0.25);
		root.setCenter(split);

		Scene scene=new Scene(root,300,250);
		primaryStage.setTitle("JGitGUI");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	private MenuBar createMenuBar(){
		Menu fileMenu=new Menu("File");
		MenuItem openItem=new MenuItem("Open");
		openItem.setOnAction((e)->gitOpen());
		fileMenu.getItems().add(openItem);
		MenuItem initItem=new MenuItem("Init");
		initItem.setOnAction((e)->gitInit());
		fileMenu.getItems().add(initItem);
		MenuItem cloneItem=new MenuItem("Clone");
		cloneItem.setOnAction((e)->gitClone());
		fileMenu.getItems().add(cloneItem);
		return new MenuBar(fileMenu);
	}
	private TreeTableView createNavigation(){
		TreeTableView<Object> nav=new TreeTableView<>(navigationRoot);
		nav.setShowRoot(false);
		TreeTableColumn<Object,String> column=new TreeTableColumn<>("Name");
		column.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<Object, String>,ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object,String> p){
				return new ReadOnlyObjectWrapper<>(p.getValue().toString());
			}
		});
		nav.getColumns().addAll(column);
		//nav.setRoot();


		return nav;
	}
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args){
		launch(args);
	}
	private void gitOpen(){
		try{
			File dir=dirChooser.showDialog(null);
			if(dir!=null)
				navigationRoot.getChildren().add(new GitTreeItem(Git.open(dir),dir));
		}catch(IOException|GitAPIException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
	private void gitInit(){
		try{
			File dir=dirChooser.showDialog(null);
			if(dir!=null)
				navigationRoot.getChildren().add(new GitTreeItem(Git.init().setDirectory(dir).call(),dir));
		}catch(GitAPIException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
	private void gitClone(){
		try{
			File dir=dirChooser.showDialog(null);
			if(dir!=null){
				TextInputDialog urlDialog=new TextInputDialog();
				urlDialog.setTitle("Choose a repository to clone");
				urlDialog.setHeaderText("Enter the URL:");
				Optional<String> url=urlDialog.showAndWait();
				if(url.isPresent()){
					navigationRoot.getChildren().add(new GitTreeItem(Git.cloneRepository().setDirectory(dir).setURI(url.get()).call(),dir));
				}
			}
		}catch(GitAPIException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
}
