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
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;
import javafx.beans.value.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.treewalk.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class CommitTreeItem extends TreeItem<Object> implements NavigationTreeItem{
	public CommitTreeItem(RevCommit rev){
		super(rev);
	}
	@Override
	public String toString(){
		return ((RevCommit)getValue()).getName();
	}
	@Override
	public MenuItem[] getContextMenuItems(){
		MenuItem checkout=new MenuItem("Checkout");
		checkout.setOnAction((e)->gitCheckout());
		MenuItem revert=new MenuItem("Revert");
		revert.setOnAction((e)->gitRevert());
		MenuItem tag=new MenuItem("Tag");
		tag.setOnAction((e)->gitTag());
		return new MenuItem[]{checkout,revert,tag};
	}
	private void gitCheckout(){
		try{
			((Git)getParent().getParent().getValue()).checkout().setName(((RevCommit)getValue()).getName()).call();
		}catch(GitAPIException ex){
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
	private void gitRevert(){
		try{
			RevCommit rev=((Git)getParent().getParent().getValue()).revert().include((RevCommit)getValue()).call();
			getParent().getChildren().add(new CommitTreeItem(rev));
		}catch(GitAPIException ex){
			Logger.getLogger(BranchTreeItem.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
	}
	private void gitTag(){
		TextInputDialog dialog=new TextInputDialog();
		dialog.setTitle("Choose name for the tag");
		dialog.setHeaderText("Enter the name of the tag:");
		Optional<String> name=dialog.showAndWait();
		if(name.isPresent())
			try{
				Ref tag=((Git)getParent().getParent().getValue()).tag().setName(name.get()).setObjectId((RevCommit)getValue()).call();
				getParent().getParent().getChildren().filtered(item->item instanceof TagListTreeItem).
					forEach((item)->item.getChildren().add(new TagTreeItem(tag)));
			}catch(GitAPIException ex){
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
			}
	}
	@Override
	public Node getContentPage(){
		RevCommit rev=(RevCommit)getValue();
		Repository repository=((Git)getParent().getParent().getValue()).getRepository();
		SplitPane page=new SplitPane();
		page.setOrientation(Orientation.VERTICAL);
		TextArea msg=new TextArea(rev.getFullMessage());
		msg.setEditable(false);
		page.getItems().add(msg);
		SplitPane fileViewer=new SplitPane();
		fileViewer.setOrientation(Orientation.HORIZONTAL);
		TreeView tree=new TreeView(new TreeItem());
		tree.setShowRoot(false);
		TextArea content=new TextArea();
		content.setEditable(false);
		try(TreeWalk walk=new TreeWalk(repository)){
			walk.addTree(rev.getTree());
			walk.setRecursive(true);
			LinkedList<TreeItem> items=new LinkedList<>();
			items.add(tree.getRoot());
			while(walk.next()){
				TreeItem item=new FileTreeItem(walk.getObjectId(0),walk.getPathString());
				/*while(walk.getDepth()<items.size()-1)
					items.removeLast();
				if(walk.getDepth()>items.size()-1)
					items.addLast(item);*/
				items.getLast().getChildren().add(item);
			}
		}catch(IOException ex){
			Logger.getLogger(CommitTreeItem.class.getName()).log(Level.SEVERE,null,ex);
			new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
		}
		tree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
			@Override
			public void changed(ObservableValue ov,Object t,Object t1){
				if(t1!=null){
					try{
						ObjectLoader obj=repository.open(((FileTreeItem)t1).getId());
						if(obj.getType()!=Constants.OBJ_TREE){
							StringBuilder buf=new StringBuilder();
							BufferedReader in=new BufferedReader(new InputStreamReader(obj.openStream(),rev.getEncoding()));
							content.setText(in.lines().collect(Collectors.joining("\n")));
						}
					}catch(IOException ex){
						Logger.getLogger(CommitTreeItem.class.getName()).log(Level.SEVERE,null,ex);
						new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
					}
				}
			}
		});
		fileViewer.getItems().add(tree);
		fileViewer.getItems().add(content);
		page.getItems().add(fileViewer);
		page.setDividerPositions(0.1,0.9);
		return page;
	}
}
class FileTreeItem extends TreeItem<String>{
	private final ObjectId id;
	public FileTreeItem(ObjectId id,String name){
		super(name);
		this.id=id;
	}
	public ObjectId getId(){
		return id;
	}
}