/*
 * Copyright (C) 2016 Chan Chung Kwong <1m02math@126.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.chungkwong.jgitgui;
import java.util.logging.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.blame.*;
import org.eclipse.jgit.diff.*;
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
		return java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("STAGING AREA");
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
		Button remove=new Button(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("REMOVE"));
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
		Button blame=new Button(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("BLAME"));
		blame.setOnAction((e)->{
			Stage dialog=new Stage();
			dialog.setTitle(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("BLAME"));
			StringBuilder buf=new StringBuilder();
			list.getSelectionModel().getSelectedItems().stream().forEach((path)->{
				try{
					BlameResult command=git.blame().setFilePath(path).call();
					RawText contents=command.getResultContents();
					for(int i=0;i<contents.size();i++){
						buf.append(command.getSourcePath(i)).append(':');
						buf.append(command.getSourceLine(i)).append(':');
						buf.append(command.getSourceCommit(i)).append(':');
						buf.append(command.getSourceAuthor(i)).append(':');
						buf.append(contents.getString(i)).append('\n');
					}
				}catch(Exception ex){
					Logger.getLogger(StageTreeItem.class.getName()).log(Level.SEVERE,null,ex);
					Util.informUser(ex);
				}
			});
			dialog.setScene(new Scene(new TextArea(buf.toString())));
			dialog.setMaximized(true);
			dialog.show();
		});
		page.addColumn(0,list,remove,blame);
		return page;
	}
}
