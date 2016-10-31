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
import java.util.*;
import java.util.logging.*;
import javafx.scene.control.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.revwalk.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class LogTreeItem extends TreeItem<Object> implements NavigationTreeItem{
	public LogTreeItem(Git git) throws GitAPIException{
		super("Commit");
		for(RevCommit rev:git.log().call())
			getChildren().add(new CommitTreeItem(rev));
	}
	@Override
	public String toString(){
		return "Commit";
	}
	@Override
	public MenuItem[] getContextMenuItems(){
		MenuItem commit=new MenuItem("New branch");
		commit.setOnAction((e)->gitCommit());
		return new MenuItem[]{commit};
	}
	private void gitCommit(){
		TextInputDialog branchDialog=new TextInputDialog();
		branchDialog.setTitle("Add a commit message");
		branchDialog.setHeaderText("Enter the commit message:");
		Optional<String> msg=branchDialog.showAndWait();
		if(msg.isPresent())
			try{
				getChildren().add(new CommitTreeItem(((Git)getParent().getValue()).commit().setMessage(msg.get()).call()));
			}catch(GitAPIException ex){
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
			}
	}
}