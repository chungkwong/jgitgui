/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chungkwong.jgitgui;
import java.io.*;
import javafx.scene.control.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class WorkingTreeItem extends TreeItem<Object> implements NavigationTreeItem{
	public WorkingTreeItem(File directory){
		super(directory);
	}
	@Override
	public String toString(){
		return "Working directory";
	}
	@Override
	public MenuItem[] getContextMenuItems(){
		return new MenuItem[0];
	}
}
