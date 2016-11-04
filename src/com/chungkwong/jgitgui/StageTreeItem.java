/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chungkwong.jgitgui;
import java.util.logging.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.dircache.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class StageTreeItem extends TreeItem<Object> implements NavigationTreeItem{
	public StageTreeItem(Git git){
		super(git);
	}
	@Override
	public String toString(){
		return "Staging area";
	}
	@Override
	public MenuItem[] getContextMenuItems(){
		return new MenuItem[0];
	}
	@Override
	public Node getContentPage(){
		Git git=(Git)getValue();
		GridPane page=new GridPane();
		ListView<String> list=new ListView<String>();
		GridPane.setHgrow(list,Priority.ALWAYS);
		GridPane.setVgrow(list,Priority.ALWAYS);
		list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		try{
			DirCache cache=((Git)getValue()).getRepository().readDirCache();
			for(int i=0;i<cache.getEntryCount();i++)
				list.getItems().add(cache.getEntry(i).getPathString());
		}catch(Exception ex){
			Logger.getLogger(StageTreeItem.class.getName()).log(Level.SEVERE,null,ex);
			Util.informUser(ex);
		}
		Button remove=new Button("Remove");
		remove.setOnAction((e)->{
			RmCommand command=git.rm().setCached(true);
			list.getSelectionModel().getSelectedItems().stream().forEach((path)->{
				command.addFilepattern(path);
			});
			list.getItems().removeAll(list.getSelectionModel().getSelectedItems());
			try{
				command.call();
			}catch(Exception ex){
				Logger.getLogger(StageTreeItem.class.getName()).log(Level.SEVERE,null,ex);
				Util.informUser(ex);
			}
		});
		page.addColumn(0,list,remove);
		return page;
	}
}
