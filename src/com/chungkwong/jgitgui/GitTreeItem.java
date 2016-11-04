/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chungkwong.jgitgui;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javafx.application.*;
import javafx.collections.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.revwalk.*;
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
			getChildren().add(new StageTreeItem(git));
			getChildren().add(new LogTreeItem(git));
			getChildren().add(new NoteListTreeItem(git));
			getChildren().add(new TagListTreeItem(git));
			getChildren().add(new LocalTreeItem(git));
			for(RemoteConfig remote:git.remoteList().call())
				getChildren().add(new RemoteTreeItem(remote));
		}catch(Exception ex){
			Logger.getLogger(GitTreeItem.class.getName()).log(Level.SEVERE,null,ex);
			Util.informUser(ex);
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
			}catch(Exception ex){
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				Util.informUser(ex);
			}
	}
	private void gitGC(){
		ProgressDialog progressDialog=new ProgressDialog("GC");
		GarbageCollectCommand command=((Git)getValue()).gc().setProgressMonitor(progressDialog);
		new Thread(()->{
			try{
				Properties stat=command.call();
				System.out.println(stat);
			}catch(Exception ex){
				Logger.getLogger(GitTreeItem.class.getName()).log(Level.SEVERE,null,ex);
				Platform.runLater(()->{
					progressDialog.hide();
					Util.informUser(ex);
				});
			}
		}).start();
	}
	@Override
	public Node getContentPage(){
		GridPane node=new GridPane();
		try{
			Status status=((Git)getValue()).status().call();
			Set<String> untrackedSet=new HashSet<>(status.getUntrackedFolders());
			untrackedSet.addAll(status.getUntracked());
			untrackedSet.removeAll(status.getIgnoredNotInIndex());
			TitledPane untracked=createList("Untracked File",untrackedSet);
			TitledPane missing=createList("Missing",status.getMissing());
			TitledPane modified=createList("Modified",status.getModified());
			TitledPane added=createList("Added",status.getAdded());
			TitledPane removed=createList("Removed",status.getRemoved());
			TitledPane changed=createList("Changed",status.getChanged());
			Button add=new Button("Add");
			add.setOnAction((e)->gitAdd(untracked,modified,added,changed));
			Button commit=new Button("Commit");
			commit.setOnAction((e)->gitCommit(added,removed,changed));
			Button clean=new Button("Clean");
			clean.setOnAction((e)->gitClean(untracked));
			node.addColumn(0,untracked,missing,modified,add);
			node.addColumn(1,added,removed,changed,commit,clean);
		}catch(Exception ex){
			Logger.getLogger(GitTreeItem.class.getName()).log(Level.SEVERE,null,ex);
			Util.informUser(ex);
		}
		return node;
	}
	private void gitCommit(TitledPane addedView,TitledPane removedView,TitledPane changedView){
		TextInputDialog dialog=new TextInputDialog();
		dialog.setTitle("Choose a message for the commit");
		dialog.setHeaderText("Enter the message:");
		Optional<String> msg=dialog.showAndWait();
		if(msg.isPresent())
			try{
				RevCommit commit=((Git)getValue()).commit().setMessage(msg.get()).call();
				((ListView<String>)addedView.getContent()).getItems().clear();
				((ListView<String>)removedView.getContent()).getItems().clear();
				((ListView<String>)changedView.getContent()).getItems().clear();
				getChildren().stream().filter((item)->item instanceof LogTreeItem).forEach(
						(item)->((LogTreeItem)item).getChildren().add(new CommitTreeItem(commit)));
			}catch(Exception ex){
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				Util.informUser(ex);
			}
	}
	private void gitClean(TitledPane untrackedView){
		try{
			((Git)getValue()).clean().setIgnore(true).call();
			((ListView<String>)untrackedView.getContent()).getItems().clear();
		}catch(Exception ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			Util.informUser(ex);
		}
	}
	private TitledPane createList(String title,Set<String> data){
		ListView<String> list=new ListView<>(FXCollections.observableList(data.stream().collect(Collectors.toList())));
		list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		TitledPane titledPane=new TitledPane(title,list);
		GridPane.setHgrow(titledPane,Priority.ALWAYS);
		GridPane.setVgrow(titledPane,Priority.ALWAYS);
		return titledPane;
	}
	private void gitAdd(TitledPane untrackedView,TitledPane modifiedView,TitledPane addedView,TitledPane changedView){
		Git git=(Git)getValue();
		ListView<String> untracked=((ListView<String>)untrackedView.getContent());
		ListView<String> modified=((ListView<String>)modifiedView.getContent());
		ListView<String> added=((ListView<String>)addedView.getContent());
		ListView<String> changed=((ListView<String>)changedView.getContent());
		try{
			for(String item:untracked.getSelectionModel().getSelectedItems()){
				git.add().addFilepattern(item).call();
				untracked.getItems().remove(item);
				added.getItems().add(item);
			}
			for(String item:modified.getSelectionModel().getSelectedItems()){
				git.add().addFilepattern(item).call();
				modified.getItems().remove(item);
				changed.getItems().add(item);
			}
		}catch(Exception ex){
			Logger.getLogger(GitTreeItem.class.getName()).log(Level.SEVERE,null,ex);
			Util.informUser(ex);
		}
	}
}