/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chungkwong.jgitgui;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javafx.application.*;
import javafx.scene.control.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.errors.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.transport.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class RemoteTreeItem extends TreeItem<Object> implements NavigationTreeItem{
	public RemoteTreeItem(RemoteConfig ref){
		super(ref);
		for(RefSpec refSpec:ref.getFetchRefSpecs()){
			getChildren().add(new RemoteSpecTreeItem(refSpec,true));
		}
		for(RefSpec refSpec:ref.getPushRefSpecs()){
			getChildren().add(new RemoteSpecTreeItem(refSpec,false));
		}
	}
	@Override
	public String toString(){
		return ((RemoteConfig)getValue()).getName();
	}
	@Override
	public MenuItem[] getContextMenuItems(){
		MenuItem push=new MenuItem("Push");
		push.setOnAction((e)->gitPush());
		MenuItem pull=new MenuItem("Pull");
		pull.setOnAction((e)->gitPull());
		MenuItem fetch=new MenuItem("Fetch");
		fetch.setOnAction((e)->gitFetch());
		MenuItem remove=new MenuItem("Remove remote");
		remove.setOnAction((e)->gitRemoteRemove());
		MenuItem resetURL=new MenuItem("Reset URL");
		resetURL.setOnAction((e)->gitRemoteResetURL());
		return new MenuItem[]{push,pull,fetch,remove,resetURL};
	}
	private void gitRemoteResetURL(){
		TextInputDialog branchDialog=new TextInputDialog();
		branchDialog.setTitle("Choose a new URL for the remote configure");
		branchDialog.setHeaderText("Enter the new URL of the remote configure:");
		Optional<String> name=branchDialog.showAndWait();
		if(name.isPresent())
			try{
				RemoteSetUrlCommand command=((Git)getParent().getValue()).remoteSetUrl();
				command.setName(((RemoteConfig)getValue()).getName());
				command.setUri(new URIish(name.get()));
				command.setPush(true);
				command.call();
				command=((Git)getParent().getValue()).remoteSetUrl();
				command.setName(((RemoteConfig)getValue()).getName());
				command.setUri(new URIish(name.get()));
				command.setPush(false);
				command.call();
			}catch(GitAPIException|URISyntaxException ex){
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
			}
	}
	private void gitRemoteRemove(){
		try{
			RemoteRemoveCommand command=((Git)getParent().getValue()).remoteRemove();
			command.setName(((RemoteConfig)getValue()).getName());
			command.call();
			getParent().getChildren().remove(this);
		}catch(GitAPIException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
	private void gitFetch(){
		ProgressDialog progressDialog=new ProgressDialog("Fetch");
		FetchCommand command=((Git)getParent().getValue()).fetch().setRemote(((RemoteConfig)getValue()).getName()).setProgressMonitor(progressDialog);
		new Thread(()->{
			try{
				FetchResult result=command.call();
				ArrayList<CommitTreeItem> commits=new ArrayList<>();
				for(Ref ref:result.getAdvertisedRefs())
					commits.add(new CommitTreeItem(((Git)getParent().getValue()).log().addRange(ref.getObjectId(),ref.getObjectId()).call().iterator().next()));
				getParent().getChildren().filtered(item->item instanceof LocalTreeItem).
					forEach((item)->item.getChildren().addAll(commits));
			}catch(GitAPIException|MissingObjectException|IncorrectObjectTypeException ex){
				Logger.getLogger(GitTreeItem.class.getName()).log(Level.SEVERE,null,ex);
				Platform.runLater(()->{
					progressDialog.hide();
					new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
				});
			}
		}).start();
	}
	private void gitPull(){
		ProgressDialog progressDialog=new ProgressDialog("Pulling");
		PullCommand command=((Git)getParent().getValue()).pull().setRemote(((RemoteConfig)getValue()).getName()).setProgressMonitor(progressDialog);
		new Thread(()->{
			try{
				PullResult result=command.call();
				HashSet<CommitTreeItem> commits=new HashSet<>();
				if(result.getFetchResult()!=null){
					for(Ref ref:result.getFetchResult().getAdvertisedRefs())
						commits.add(new CommitTreeItem(((Git)getParent().getValue()).log().addRange(ref.getObjectId(),ref.getObjectId()).call().iterator().next()));
				}
				if(result.getMergeResult()!=null&&result.getMergeResult().getMergeStatus().equals(MergeResult.MergeStatus.MERGED)){
					ObjectId head=result.getMergeResult().getNewHead();
					commits.add(new CommitTreeItem(((Git)getParent().getValue()).log().addRange(head,head).call().iterator().next()));
				}else{
					new Alert(Alert.AlertType.INFORMATION,result.toString(),ButtonType.CLOSE).show();
				}
				getParent().getChildren().filtered(item->item instanceof LocalTreeItem).
					forEach((item)->item.getChildren().addAll(commits));
			}catch(GitAPIException|MissingObjectException|IncorrectObjectTypeException ex){
				Logger.getLogger(GitTreeItem.class.getName()).log(Level.SEVERE,null,ex);
				Platform.runLater(()->{
					progressDialog.hide();
					new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
				});
			}
		}).start();
	}
	private void gitPush(){
		ProgressDialog progressDialog=new ProgressDialog("Pushing");
		PushCommand command=((Git)getParent().getValue()).push().setRemote(((RemoteConfig)getValue()).getName()).setProgressMonitor(progressDialog);
		new Thread(()->{
			try{
				command.call();
			}catch(GitAPIException ex){
				Logger.getLogger(GitTreeItem.class.getName()).log(Level.SEVERE,null,ex);
				Platform.runLater(()->{
					progressDialog.hide();
					new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
				});
			}
		}).start();
	}
}