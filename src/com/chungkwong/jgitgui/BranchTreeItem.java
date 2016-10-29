/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chungkwong.jgitgui;
import javafx.scene.control.*;
import org.eclipse.jgit.lib.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class BranchTreeItem extends TreeItem<Object>{
	public BranchTreeItem(Ref ref){
		super(ref);
	}
	@Override
	public String toString(){
		return ((Ref)getValue()).getName();
	}
}
