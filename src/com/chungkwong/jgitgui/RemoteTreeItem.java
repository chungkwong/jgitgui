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
import javafx.application.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.eclipse.jgit.api.*;
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
		MenuItem push=new MenuItem(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("PUSH"));
		push.setOnAction((e)->gitPush());
		MenuItem pull=new MenuItem(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("PULL"));
		pull.setOnAction((e)->gitPull());
		MenuItem fetch=new MenuItem(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("FETCH"));
		fetch.setOnAction((e)->gitFetch());
		MenuItem remove=new MenuItem(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("REMOVE REMOTE"));
		remove.setOnAction((e)->gitRemoteRemove());
		MenuItem resetURL=new MenuItem(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("RESET URL"));
		resetURL.setOnAction((e)->gitRemoteResetURL());
		return new MenuItem[]{push,pull,fetch,remove,resetURL};
	}
	private void gitRemoteResetURL(){
		TextInputDialog branchDialog=new TextInputDialog();
		branchDialog.setTitle(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("CHOOSE A NEW URL FOR THE REMOTE CONFIGURE"));
		branchDialog.setHeaderText(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("ENTER THE NEW URL OF THE REMOTE CONFIGURE:"));
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
			}catch(Exception ex){
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				Util.informUser(ex);
			}
	}
	private void gitRemoteRemove(){
		try{
			RemoteRemoveCommand command=((Git)getParent().getValue()).remoteRemove();
			command.setName(((RemoteConfig)getValue()).getName());
			command.call();
			getParent().getChildren().remove(this);
		}catch(Exception ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				Util.informUser(ex);
		}
	}
	private void gitFetch(){
		ProgressDialog progressDialog=new ProgressDialog(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("FETCH"));
		FetchCommand command=((Git)getParent().getValue()).fetch().setRemote(((RemoteConfig)getValue()).getName()).setProgressMonitor(progressDialog);
		new Thread(()->{
			try{
				FetchResult result=command.call();
				ArrayList<CommitTreeItem> commits=new ArrayList<>();
				Platform.runLater(()->{
					for(Ref ref:result.getAdvertisedRefs())
						try{
							commits.add(new CommitTreeItem(((Git)getParent().getValue()).log().addRange(ref.getObjectId(),ref.getObjectId()).call().iterator().next()));
						}catch(Exception ex){
							Logger.getLogger(RemoteTreeItem.class.getName()).log(Level.SEVERE,null,ex);
							Util.informUser(ex);
						}
					getParent().getChildren().filtered(item->item instanceof LocalTreeItem).
						forEach((item)->item.getChildren().addAll(commits));
				});
			}catch(Exception ex){
				Logger.getLogger(GitTreeItem.class.getName()).log(Level.SEVERE,null,ex);
				Platform.runLater(()->{
					progressDialog.hide();
					Util.informUser(ex);
				});
			}
		}).start();
	}
	private void gitPull(){
		ProgressDialog progressDialog=new ProgressDialog(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("PULLING"));
		PullCommand command=((Git)getParent().getValue()).pull().setRemote(((RemoteConfig)getValue()).getName()).setProgressMonitor(progressDialog);
		new Thread(()->{
			try{
				PullResult result=command.call();
				HashSet<CommitTreeItem> commits=new HashSet<>();
				Platform.runLater(()->{
					if(result.getFetchResult()!=null){
						for(Ref ref:result.getFetchResult().getAdvertisedRefs())
							try{
								commits.add(new CommitTreeItem(((Git)getParent().getValue()).log().addRange(ref.getObjectId(),ref.getObjectId()).call().iterator().next()));
							}catch(Exception ex){
								Logger.getLogger(RemoteTreeItem.class.getName()).log(Level.SEVERE,null,ex);
								Util.informUser(ex);
							}
					}
					if(result.getMergeResult()!=null&&result.getMergeResult().getMergeStatus().equals(MergeResult.MergeStatus.MERGED)){
						try{
							ObjectId head=result.getMergeResult().getNewHead();
							commits.add(new CommitTreeItem(((Git)getParent().getValue()).log().addRange(head,head).call().iterator().next()));
						}catch(Exception ex){
							Logger.getLogger(RemoteTreeItem.class.getName()).log(Level.SEVERE,null,ex);
							Util.informUser(ex);
						}
					}else{
						new Alert(Alert.AlertType.INFORMATION,result.toString(),ButtonType.CLOSE).show();
					}
					getParent().getChildren().filtered(item->item instanceof LocalTreeItem).
						forEach((item)->item.getChildren().addAll(commits));
				});
			}catch(Exception ex){
				Logger.getLogger(GitTreeItem.class.getName()).log(Level.SEVERE,null,ex);
				Platform.runLater(()->{
					progressDialog.hide();
					Util.informUser(ex);
				});
			}
		}).start();
	}
	private void gitPush(){
		ProgressDialog progressDialog=new ProgressDialog(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("PUSHING"));
		PushCommand command=((Git)getParent().getValue()).push().setRemote(((RemoteConfig)getValue()).getName()).setProgressMonitor(progressDialog);
		TextField user=new TextField();
		PasswordField pass=new PasswordField();
		GridPane auth=new GridPane();
		auth.addRow(0,new Label(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("USER")),user);
		auth.addRow(1,new Label(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("PASSWORD")),pass);
		Dialog dialog=new Dialog();
		dialog.getDialogPane().setContent(auth);
		dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE,ButtonType.APPLY);
		dialog.showAndWait();
		if(true){
			command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(user.getText(),pass.getText()));
		}
		new Thread(()->{
			try{
				command.call();
			}catch(Exception ex){
				Logger.getLogger(GitTreeItem.class.getName()).log(Level.SEVERE,null,ex);
				Platform.runLater(()->{
					progressDialog.hide();
					Util.informUser(ex);
				});
			}
		}).start();
	}
}