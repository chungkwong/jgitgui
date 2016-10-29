/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chungkwong.jgitgui;
import java.io.*;
import javafx.scene.control.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class GitTreeItem extends TreeItem<Object>{
	private final File directory;
	public GitTreeItem(Git git,File directory) throws GitAPIException{
		super(git);
		this.directory=directory;
		getChildren().add(new WorkingTreeItem(directory));
		getChildren().add(new StageTreeItem(git));
		for(Ref ref:git.branchList().call())
			getChildren().add(new BranchTreeItem(ref));
	}
	@Override
	public String toString(){
		return directory.getName();
	}
}
