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
import org.eclipse.jgit.transport.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class RemoteSpecTreeItem extends TreeItem implements NavigationTreeItem{
	private final boolean fetch;
	public RemoteSpecTreeItem(RefSpec ref,boolean fetch){
		super(ref);
		this.fetch=fetch;
	}
	public boolean isFetch(){
		return fetch;
	}
	@Override
	public String toString(){
		return ((RefSpec)getValue()).getSource()+"->"+getType()+((RefSpec)getValue()).getDestination();
	}
	private String getType(){
		return fetch?"(Fetch)":"(Push)";
	}
	@Override
	public MenuItem[] getContextMenuItems(){
		return new MenuItem[0];
	}
}
