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
import org.eclipse.jgit.revwalk.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class CommitTreeItem extends TreeItem<Object> implements NavigationTreeItem{
	public CommitTreeItem(RevCommit rev){
		super(rev);
	}
	@Override
	public String toString(){
		return ((RevCommit)getValue()).getName();
	}
	@Override
	public MenuItem[] getContextMenuItems(){
		MenuItem checkout=new MenuItem("Checkout");
		checkout.setOnAction((e)->gitCheckout());
		MenuItem revert=new MenuItem("Revert");
		revert.setOnAction((e)->gitRevert());
		MenuItem tag=new MenuItem("Tag");
		tag.setOnAction((e)->gitTag());
		return new MenuItem[]{checkout,revert,tag};
	}
	private void gitCheckout(){
		try{
			((Git)getParent().getParent().getValue()).checkout().setName(((RevCommit)getValue()).getName()).call();
		}catch(GitAPIException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
	private void gitRevert(){
		try{
			RevCommit rev=((Git)getParent().getParent().getValue()).revert().include((RevCommit)getValue()).call();
			getParent().getChildren().add(new CommitTreeItem(rev));
		}catch(GitAPIException ex){
			Logger.getLogger(BranchTreeItem.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
	private void gitTag(){
		TextInputDialog dialog=new TextInputDialog();
		dialog.setTitle("Choose name for the tag");
		dialog.setHeaderText("Enter the name of the tag:");
		Optional<String> name=dialog.showAndWait();
		if(name.isPresent())
			try{
				Ref tag=((Git)getParent().getParent().getValue()).tag().setName(name.get()).setObjectId((RevCommit)getValue()).call();
				getParent().getParent().getChildren().filtered(item->item instanceof TagListTreeItem).
					forEach((item)->item.getChildren().add(new TagTreeItem(tag)));
			}catch(GitAPIException ex){
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
			}
	}
}
