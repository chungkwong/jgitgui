/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chungkwong.jgitgui;
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
public class RemoteTreeItem extends TreeItem<Object> implements NavigationTreeItem{
	public RemoteTreeItem(RemoteConfig ref){
		super(ref);
	}
	@Override
	public String toString(){
		return ((RemoteConfig)getValue()).getName();
	}
	@Override
	public MenuItem[] getContextMenuItems(){
		MenuItem remove=new MenuItem("Remove remote");
		MenuItem resetURL=new MenuItem("Reset URL");
		remove.setOnAction((e)->gitRemoteRemove());
		resetURL.setOnAction((e)->gitRemoteResetURL());
		return new MenuItem[]{remove,resetURL};
	}
	private void gitRemoteResetURL(){
		TextInputDialog branchDialog=new TextInputDialog();
		branchDialog.setTitle("Choose a new URL for the remote configure");
		branchDialog.setHeaderText("Enter the new URL of the remote configure:");
		Optional<String> name=branchDialog.showAndWait();
		if(name.isPresent())
			try{
				RemoteSetUrlCommand command=((Git)getParent().getValue()).remoteSetUrl();
				command.setName(((RemoteConfig)getValue()).getName());
				command.setUri(new URIish(name.get()));
				command.setPush(true);
				command.call();
				command=((Git)getParent().getValue()).remoteSetUrl();
				command.setName(((RemoteConfig)getValue()).getName());
				command.setUri(new URIish(name.get()));
				command.setPush(false);
				command.call();
			}catch(GitAPIException|URISyntaxException ex){
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
			}
	}
	private void gitRemoteRemove(){
		try{
			RemoteRemoveCommand command=((Git)getParent().getValue()).remoteRemove();
			command.setName(((RemoteConfig)getValue()).getName());
			command.call();
			getParent().getChildren().remove(this);
		}catch(GitAPIException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
}