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
import java.io.*;
import java.util.logging.*;
import javafx.scene.*;
import javafx.scene.control.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.errors.*;
import org.eclipse.jgit.notes.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.revwalk.filter.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class NoteTreeItem extends TreeItem<Object> implements NavigationTreeItem{
	public NoteTreeItem(Note ref){
		super(ref);
	}
	@Override
	public String toString(){
		return ((Note)getValue()).getName();
	}
	@Override
	public MenuItem[] getContextMenuItems(){
		MenuItem remove=new MenuItem("Remove note");
		remove.setOnAction((e)->gitNoteRemove());
		return new MenuItem[]{remove};
	}
	private void gitNoteRemove(){
		try{
			RevCommit rev=((Git)getParent().getParent().getValue()).log().setRevFilter(new RevFilter() {
				@Override
				public boolean include(RevWalk walker,RevCommit cmit) throws StopWalkException,MissingObjectException,IncorrectObjectTypeException,IOException{
					return cmit.getName().equals(NoteTreeItem.this.toString());
				}
				@Override
				public RevFilter clone(){
					return this;
				}
			}).call().iterator().next();
			((Git)getParent().getParent().getValue()).notesRemove().setObjectId(rev).call();
					getParent().getChildren().remove(this);
		}catch(Exception ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			Util.informUser(ex);
		}
	}
	@Override
	public Node getContentPage(){
		TextArea area=new TextArea();
		area.setEditable(false);
		try{
			byte[] bytes=((Git)getParent().getParent().getValue()).getRepository().open(((Note)getValue()).getData()).getBytes();
			area.setText(new String(bytes,"UTF-8"));
		}catch(Exception ex){
			Logger.getLogger(NoteTreeItem.class.getName()).log(Level.SEVERE,null,ex);
			Util.informUser(ex);
		}
		return area;
	}
}