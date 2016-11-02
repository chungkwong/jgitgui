/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chungkwong.jgitgui;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javafx.application.*;
import javafx.collections.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.errors.*;
import org.eclipse.jgit.transport.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class GitTreeItem extends TreeItem<Object> implements NavigationTreeItem{
	public GitTreeItem(Git git){
		super(git);
		//getChildren().add(new WorkingTreeItem(directory));
		//getChildren().add(new StageTreeItem(git));
		try{
			getChildren().add(new LogTreeItem(git));
			getChildren().add(new NoteListTreeItem(git));
			getChildren().add(new TagListTreeItem(git));
			getChildren().add(new LocalTreeItem(git));
			for(RemoteConfig remote:git.remoteList().call())
				getChildren().add(new RemoteTreeItem(remote));
		}catch(GitAPIException ex){
			Logger.getLogger(GitTreeItem.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
	@Override
	public String toString(){
		return ((Git)getValue()).getRepository().getDirectory().getParentFile().getName();
	}
	@Override
	public MenuItem[] getContextMenuItems(){
		MenuItem remote=new MenuItem("New remote");
		remote.setOnAction((e)->gitRemoteNew());
		MenuItem gc=new MenuItem("Collect gargage");
		gc.setOnAction((e)->gitGC());
		return new MenuItem[]{remote,gc};
	}
	private void gitRemoteNew(){
		TextInputDialog dialog=new TextInputDialog();
		dialog.setTitle("Choose a name for the new remote configure");
		dialog.setHeaderText("Enter the name of the new remote configure:");
		Optional<String> name=dialog.showAndWait();
		dialog.setTitle("Choose a URI for the new remote configure");
		dialog.setHeaderText("Enter the URI of the new remote configure:");
		Optional<String> uri=dialog.showAndWait();
		if(name.isPresent())
			try{
				RemoteAddCommand command=((Git)getValue()).remoteAdd();
				command.setName(name.get());
				command.setUri(new URIish(uri.get()));
				getChildren().add(new RemoteTreeItem(command.call()));
			}catch(GitAPIException|URISyntaxException ex){
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
			}
	}
	private void gitGC(){
		ProgressDialog progressDialog=new ProgressDialog("GC");
		GarbageCollectCommand command=((Git)getValue()).gc().setProgressMonitor(progressDialog);
		new Thread(()->{
			try{
				Properties stat=command.call();
				System.out.println(stat);
			}catch(GitAPIException ex){
				Logger.getLogger(GitTreeItem.class.getName()).log(Level.SEVERE,null,ex);
				Platform.runLater(()->{
					progressDialog.hide();
					new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
				});
			}
		}).start();
	}
	@Override
	public Node getContentPage(){
		GridPane node=new GridPane();
		try{
			Status status=((Git)getValue()).status().call();
			Node untracked=createList("Untracked File",status.getUntracked());
			Node missing=createList("Missing",status.getMissing());
			Node modified=createList("Modified",status.getModified());
			Node added=createList("Added",status.getAdded());
			Node removed=createList("Removed",status.getRemoved());
			Node changed=createList("Changed",status.getChanged());
			Button commit=new Button("Commit");
			commit.setOnAction((e)->gitCommit());
			Button clean=new Button("Clean");
			clean.setOnAction((e)->gitClean());
			node.addColumn(0,untracked,missing,modified);
			node.addColumn(1,added,removed,changed,commit,clean);
		}catch(GitAPIException|NoWorkTreeException ex){
			Logger.getLogger(GitTreeItem.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
		return node;
	}
	private void gitCommit(){
		try{
			((Git)getValue()).commit().call();
		}catch(GitAPIException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
	private void gitClean(){
		try{
			((Git)getValue()).clean().call();
		}catch(GitAPIException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
	private Node createList(String title,Set<String> data){
		ListView<String> list=new ListView<>(FXCollections.observableList(data.stream().collect(Collectors.toList())));
		return new TitledPane(title,list);
	}
}