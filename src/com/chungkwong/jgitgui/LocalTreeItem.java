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
import org.eclipse.jgit.lib.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class LocalTreeItem extends TreeItem<Object> implements NavigationTreeItem{
	public LocalTreeItem(Git git) throws GitAPIException{
		super("Local branch");
		for(Ref ref:git.branchList().call())
			getChildren().add(new BranchTreeItem(ref));
	}
	@Override
	public String toString(){
		return "Local branch";
	}
	@Override
	public MenuItem[] getContextMenuItems(){
		MenuItem branch=new MenuItem("New branch");
		branch.setOnAction((e)->gitBranchNew());
		return new MenuItem[]{branch};
	}
	private void gitBranchNew(){
		TextInputDialog branchDialog=new TextInputDialog();
		branchDialog.setTitle("Choose a name for the new branch");
		branchDialog.setHeaderText("Enter the name of the new branch:");
		Optional<String> name=branchDialog.showAndWait();
		if(name.isPresent())
			try{
				getChildren().add(new BranchTreeItem(((Git)getValue()).branchCreate().setName(name.get()).call()));
			}catch(GitAPIException ex){
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
			}
	}
}