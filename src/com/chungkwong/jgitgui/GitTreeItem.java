/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chungkwong.jgitgui;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javafx.scene.control.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.transport.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class GitTreeItem extends TreeItem<Object> implements NavigationTreeItem{
	private final File directory;
	public GitTreeItem(Git git,File directory) throws GitAPIException{
		super(git);
		this.directory=directory;
		//getChildren().add(new WorkingTreeItem(directory));
		//getChildren().add(new StageTreeItem(git));
		getChildren().add(new LogTreeItem(git));
		getChildren().add(new TagListTreeItem(git));
		getChildren().add(new LocalTreeItem(git));
		for(RemoteConfig remote:git.remoteList().call())
			getChildren().add(new RemoteTreeItem(remote));

	}
	@Override
	public String toString(){
		return directory.getName();
	}
	@Override
	public MenuItem[] getContextMenuItems(){
		MenuItem remote=new MenuItem("New remote");
		remote.setOnAction((e)->gitRemoteNew());
		return new MenuItem[]{remote};
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
}