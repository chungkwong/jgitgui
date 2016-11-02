/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chungkwong.jgitgui;
import java.util.*;
import java.util.logging.*;
import javafx.scene.control.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.errors.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class BranchTreeItem extends TreeItem<Object> implements NavigationTreeItem{
	public BranchTreeItem(Ref ref){
		super(ref);
	}
	@Override
	public String toString(){
		return ((Ref)getValue()).getName();
	}
	@Override
	public MenuItem[] getContextMenuItems(){
		MenuItem checkout=new MenuItem("Checkout");
		checkout.setOnAction((e)->gitCheckout());
		MenuItem revert=new MenuItem("Revert");
		revert.setOnAction((e)->gitRevert());
		MenuItem mergeBranch=new MenuItem("Merge");
		mergeBranch.setOnAction((e)->gitMerge());
		MenuItem rmBranch=new MenuItem("Remove branch");
		rmBranch.setOnAction((e)->gitBranchRemove());
		MenuItem renameBranch=new MenuItem("Rename branch");
		renameBranch.setOnAction((e)->gitBranchRename());
		return new MenuItem[]{checkout,revert,mergeBranch,rmBranch,renameBranch};
	}
	private void gitMerge(){
		try{
			MergeResult result=((Git)getParent().getParent().getValue()).merge().include((Ref)getValue()).call();
			if(result.getMergeStatus().equals(MergeResult.MergeStatus.MERGED)){
				RevCommit commit=((Git)getParent().getParent().getValue()).log().addRange(result.getNewHead(),result.getNewHead()).call().iterator().next();
				getParent().getParent().getChildren().filtered(item->item instanceof LocalTreeItem).
					forEach((item)->item.getChildren().add(new CommitTreeItem(commit)));
			}else{
				new Alert(Alert.AlertType.INFORMATION,result.getMergeStatus().toString(),ButtonType.CLOSE).show();
			}
		}catch(GitAPIException|MissingObjectException|IncorrectObjectTypeException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
	private void gitCheckout(){
		try{
			((Git)getParent().getParent().getValue()).checkout().setName(((Ref)getValue()).getName()).call();
		}catch(GitAPIException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
	private void gitBranchRename(){
		TextInputDialog branchDialog=new TextInputDialog();
		branchDialog.setTitle("Choose a new name for the branch");
		branchDialog.setHeaderText("Enter the new name of the branch:");
		Optional<String> name=branchDialog.showAndWait();
		if(name.isPresent())
			try{
				setValue(((Git)getParent().getParent().getValue()).branchRename().setOldName(((Ref)getValue()).getName()).setNewName(name.get()).call());
			}catch(GitAPIException ex){
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
			}
	}
	private void gitBranchRemove(){
		try{
			((Git)getParent().getParent().getValue()).branchDelete().setBranchNames(((Ref)getValue()).getName()).call();
			getParent().getChildren().remove(this);
		}catch(GitAPIException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
	private void gitRevert(){
		try{
			RevCommit rev=((Git)getParent().getParent().getValue()).revert().include((Ref)getValue()).call();
			getParent().getParent().getChildren().filtered(item->item instanceof LocalTreeItem).
					forEach((item)->item.getChildren().add(new CommitTreeItem(rev)));
		}catch(GitAPIException ex){
			Logger.getLogger(BranchTreeItem.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
}
