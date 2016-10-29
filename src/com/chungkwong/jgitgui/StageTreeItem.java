/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chungkwong.jgitgui;
import javafx.scene.control.*;
import org.eclipse.jgit.api.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class StageTreeItem extends TreeItem<Object>{
	public StageTreeItem(Git git){
		super(git);
	}
	@Override
	public String toString(){
		return "Staging area";
	}

}
