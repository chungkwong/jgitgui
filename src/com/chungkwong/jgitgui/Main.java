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
import org.eclipse.jgit.lib.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class Main extends Application{
	private final DirectoryChooser dirChooser=new DirectoryChooser();
	private final TreeItem<Object> navigationRoot=new TreeItem<>();
	private ProgressBar progressBar=new ProgressBar(1.0);
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

		root.setBottom(progressBar);

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
		ContextMenu menu=new ContextMenu();
		MenuItem checkout=new MenuItem("Checkout");
		checkout.setOnAction((e)->gitCheckout((BranchTreeItem)nav.getSelectionModel().getSelectedItem()));
		menu.getItems().add(checkout);
		MenuItem branch=new MenuItem("New branch");
		branch.setOnAction((e)->gitBranchNew((GitTreeItem)nav.getSelectionModel().getSelectedItem()));
		menu.getItems().add(branch);
		MenuItem rmBranch=new MenuItem("Remove branch");
		rmBranch.setOnAction((e)->gitBranchRemove((BranchTreeItem)nav.getSelectionModel().getSelectedItem()));
		menu.getItems().add(rmBranch);
		MenuItem renameBranch=new MenuItem("Rename branch");
		renameBranch.setOnAction((e)->gitBranchRename((BranchTreeItem)nav.getSelectionModel().getSelectedItem()));
		menu.getItems().add(renameBranch);
		menu.getItems().add(new SeparatorMenuItem());
		nav.setContextMenu(menu);
		nav.setOnContextMenuRequested((e)->{
			TreeItem item=nav.getSelectionModel().getSelectedItem();
			if(item instanceof GitTreeItem){
				checkout.setDisable(true);
				branch.setDisable(false);
				rmBranch.setDisable(true);
				renameBranch.setDisable(true);
			}else if(item instanceof BranchTreeItem){
				checkout.setDisable(false);
				branch.setDisable(true);
				rmBranch.setDisable(false);
				renameBranch.setDisable(false);
			}else{
				checkout.setDisable(true);
				branch.setDisable(true);
				rmBranch.setDisable(true);
				renameBranch.setDisable(true);
			}
		});
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
					navigationRoot.getChildren().add(new GitTreeItem(
							Git.cloneRepository().setDirectory(dir).setURI(url.get()).call(),dir));
				}
			}
		}catch(GitAPIException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
	private void gitCheckout(BranchTreeItem item){
		try{
			((Git)item.getParent().getValue()).checkout().setName(((Ref)item.getValue()).getName()).call();
		}catch(GitAPIException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
	private void gitBranchNew(GitTreeItem item){
		TextInputDialog branchDialog=new TextInputDialog();
		branchDialog.setTitle("Choose a name for the new branch");
		branchDialog.setHeaderText("Enter the name of the new branch:");
		Optional<String> name=branchDialog.showAndWait();
		if(name.isPresent())
			try{
				item.getChildren().add(new BranchTreeItem(((Git)item.getValue()).branchCreate().setName(name.get()).call()));
			}catch(GitAPIException ex){
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
			}
	}
	private void gitBranchRename(BranchTreeItem item){
		TextInputDialog branchDialog=new TextInputDialog();
		branchDialog.setTitle("Choose a new name for the branch");
		branchDialog.setHeaderText("Enter the new name of the branch:");
		Optional<String> name=branchDialog.showAndWait();
		if(name.isPresent())
			try{
				item.setValue(((Git)item.getParent().getValue()).branchRename().setOldName(((Ref)item.getValue()).getName()).setNewName(name.get()).call());
			}catch(GitAPIException ex){
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
			}
	}
	private void gitBranchRemove(BranchTreeItem item){
		try{
			((Git)item.getParent().getValue()).branchDelete().setBranchNames(((Ref)item.getValue()).getName()).call();
			item.getParent().getChildren().remove(item);
		}catch(GitAPIException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
}
