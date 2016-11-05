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
import javafx.scene.control.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.notes.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class NoteListTreeItem extends TreeItem<Object> implements NavigationTreeItem{
	public NoteListTreeItem(Git git) throws GitAPIException{
		super(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("NOTE"));
		for(Note note:git.notesList().call())
			getChildren().add(new NoteTreeItem(note));
	}
	@Override
	public String toString(){
		return java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("NOTE");
	}
	@Override
	public MenuItem[] getContextMenuItems(){
		return new MenuItem[0];
	}
}