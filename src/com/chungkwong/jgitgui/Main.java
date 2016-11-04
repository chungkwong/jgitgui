/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chungkwong.jgitgui;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
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
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.transport.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class Main extends Application{
	private final DirectoryChooser dirChooser=new DirectoryChooser();
	private final TreeItem<Object> navigationRoot=new TreeItem<>();
	private final BorderPane content=new BorderPane();
	@Override
	public void start(Stage primaryStage){
		BorderPane root=new BorderPane();
		root.setTop(createMenuBar());
		SplitPane split=new SplitPane();
		split.setOrientation(Orientation.HORIZONTAL);
		split.getItems().add(createNavigation());
		split.getItems().add(content);
		split.setDividerPosition(0,0.3);
		root.setCenter(split);

		Scene scene=new Scene(root);
		primaryStage.setTitle("JGitGUI");
		primaryStage.setScene(scene);
		primaryStage.setMaximized(true);
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
	private BorderPane createNavigation(){
		BorderPane view=new BorderPane();
		TreeTableView<Object> nav=new TreeTableView<>(navigationRoot);
		nav.setShowRoot(false);
		ContextMenu contextMenu=new ContextMenu();
		nav.setContextMenu(contextMenu);
		nav.setOnContextMenuRequested((e)->contextMenu.getItems().setAll(((NavigationTreeItem)nav.getSelectionModel().getSelectedItem()).getContextMenuItems()));
		nav.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Object>>(){
			@Override
			public void changed(ObservableValue<? extends TreeItem<Object>> ov,TreeItem<Object> t,TreeItem<Object> t1){
				if(t1!=null)
					content.setCenter(((NavigationTreeItem)t1).getContentPage());
				else
					content.setCenter(null);
			}
		});
		view.setCenter(nav);
		view.setBottom(createColumnsChooser(nav));
		return view;
	}
	private FlowPane createColumnsChooser(TreeTableView<Object> nav){
		FlowPane chooser=new FlowPane();
		chooser.getChildren().add(createColumnChooser("Name",new Callback<TreeTableColumn.CellDataFeatures<Object, String>,ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object,String> p){
				return new ReadOnlyObjectWrapper<>(p.getValue().toString());
			}
		},true,nav));
		chooser.getChildren().add(createColumnChooser("Message",new Callback<TreeTableColumn.CellDataFeatures<Object, String>,ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object,String> p){
				if(p.getValue() instanceof CommitTreeItem)
					return new ReadOnlyObjectWrapper<>(((RevCommit)p.getValue().getValue()).getShortMessage());
				else
					return new ReadOnlyObjectWrapper<>("");
			}
		},false,nav));
		chooser.getChildren().add(createColumnChooser("Author",new Callback<TreeTableColumn.CellDataFeatures<Object, String>,ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object,String> p){
				if(p.getValue() instanceof CommitTreeItem)
					return new ReadOnlyObjectWrapper<>(((RevCommit)p.getValue().getValue()).getAuthorIdent().toExternalString());
				else
					return new ReadOnlyObjectWrapper<>("");
			}
		},false,nav));
		chooser.getChildren().add(createColumnChooser("Committer",new Callback<TreeTableColumn.CellDataFeatures<Object, String>,ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object,String> p){
				if(p.getValue() instanceof CommitTreeItem)
					return new ReadOnlyObjectWrapper<>(((RevCommit)p.getValue().getValue()).getCommitterIdent().toExternalString());
				else
					return new ReadOnlyObjectWrapper<>("");
			}
		},false,nav));
		chooser.getChildren().add(createColumnChooser("Time",new Callback<TreeTableColumn.CellDataFeatures<Object, String>,ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object,String> p){
				if(p.getValue() instanceof CommitTreeItem)
					return new ReadOnlyObjectWrapper<>(timeToString(((RevCommit)p.getValue().getValue()).getCommitTime()));
				else
					return new ReadOnlyObjectWrapper<>("");
			}
		},false,nav));
		chooser.getChildren().add(createColumnChooser("Refernece",new Callback<TreeTableColumn.CellDataFeatures<Object, String>,ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object,String> p){
				if(p.getValue() instanceof BranchTreeItem){
					ObjectId id=((Ref)p.getValue().getValue()).getLeaf().getObjectId();
					return new ReadOnlyObjectWrapper<>(id==null?"":id.getName());
				}else if(p.getValue() instanceof TagTreeItem){
					ObjectId id=((Ref)p.getValue().getValue()).getTarget().getLeaf().getObjectId();
					return new ReadOnlyObjectWrapper<>(id==null?"":id.getName());
				}else
					return new ReadOnlyObjectWrapper<>("");
			}
		},false,nav));
		chooser.getChildren().add(createColumnChooser("URI",new Callback<TreeTableColumn.CellDataFeatures<Object, String>,ObservableValue<String>>() {
			@Override
			public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<Object,String> p){
				if(p.getValue() instanceof RemoteTreeItem){
					String uris=((RemoteConfig)p.getValue().getValue()).getURIs().stream().map((url)->url.toString()).collect(Collectors.joining(" "));
					return new ReadOnlyObjectWrapper<>(uris);
				}else
					return new ReadOnlyObjectWrapper<>("");
			}
		},false,nav));

		return chooser;
	}
	private static String timeToString(int time){
		return DateFormat.getDateTimeInstance().format(new Date(time*1000l));
	}
	private CheckBox createColumnChooser(String name,Callback callback,boolean visible,TreeTableView<Object> nav){
		TreeTableColumn<Object,String> column=new TreeTableColumn<>(name);
		column.setCellValueFactory(callback);
		CheckBox chooser=new CheckBox(name);
		chooser.setSelected(visible);
		if(visible)
			nav.getColumns().add(column);
		chooser.selectedProperty().addListener((v)->{
			if(chooser.isSelected())
				nav.getColumns().add(column);
			else
				nav.getColumns().remove(column);
		});
		return chooser;
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
				navigationRoot.getChildren().add(new GitTreeItem(Git.open(dir)));
		}catch(Exception ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			Util.informUser(ex);
		}
	}
	private void gitInit(){
		try{
			File dir=dirChooser.showDialog(null);
			if(dir!=null)
				navigationRoot.getChildren().add(new GitTreeItem(Git.init().setDirectory(dir).call()));
		}catch(Exception ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			Util.informUser(ex);
		}
	}
	private void gitClone(){
		File dir=dirChooser.showDialog(null);
		if(dir!=null){
			TextInputDialog urlDialog=new TextInputDialog();
			urlDialog.setTitle("Choose a repository to clone");
			urlDialog.setHeaderText("Enter the URL:");
			Optional<String> url=urlDialog.showAndWait();
			if(url.isPresent()){
				ProgressDialog progressDialog=new ProgressDialog("GC");
				new Thread(()->{
					try{
						Git repository=Git.cloneRepository().setDirectory(dir).setURI(url.get()).setProgressMonitor(progressDialog).call();
						Platform.runLater(()->{
							navigationRoot.getChildren().add(new GitTreeItem(repository));
						});
					}catch(Exception ex){
						Logger.getLogger(GitTreeItem.class.getName()).log(Level.SEVERE,null,ex);
						Platform.runLater(()->{
							progressDialog.hide();
							Util.informUser(ex);
						});
					}
				}).start();

			}
		}
	}
}
