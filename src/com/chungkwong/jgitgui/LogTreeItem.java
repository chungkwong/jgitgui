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
import javafx.event.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.errors.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.treewalk.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class LogTreeItem extends TreeItem<Object> implements NavigationTreeItem{
	public LogTreeItem(Git git) throws GitAPIException{
		super("Commit");
		for(RevCommit rev:git.log().call())
			getChildren().add(new CommitTreeItem(rev));
	}
	@Override
	public String toString(){
		return "Commit";
	}
	@Override
	public MenuItem[] getContextMenuItems(){
		MenuItem commit=new MenuItem("New branch");
		commit.setOnAction((e)->gitCommit());
		return new MenuItem[]{commit};
	}
	private void gitCommit(){
		TextInputDialog branchDialog=new TextInputDialog();
		branchDialog.setTitle("Add a commit message");
		branchDialog.setHeaderText("Enter the commit message:");
		Optional<String> msg=branchDialog.showAndWait();
		if(msg.isPresent())
			try{
				getChildren().add(new CommitTreeItem(((Git)getParent().getValue()).commit().setMessage(msg.get()).call()));
			}catch(GitAPIException ex){
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE,null,ex);
				new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
			}
	}
	private RevCommit toRevCommit(ObjectId id) throws MissingObjectException, IncorrectObjectTypeException, GitAPIException{
		return ((Git)getParent().getValue()).log().addRange(id,id).call().iterator().next();
	}
	@Override
	public Node getContentPage(){
		GridPane page=new GridPane();
		TextField oldSrc=new TextField();
		TextField newSrc=new TextField();
		Button ok=new Button("Diff");
		TextArea diff=new TextArea();
		diff.setEditable(false);
		Git git=((Git)getParent().getValue());
		GridPane.setVgrow(diff,Priority.ALWAYS);
		GridPane.setHgrow(diff,Priority.ALWAYS);
		GridPane.setHgrow(oldSrc,Priority.ALWAYS);
		GridPane.setHgrow(newSrc,Priority.ALWAYS);
		ok.setOnAction((ActionEvent e)->{
			try(ObjectReader reader=git.getRepository().newObjectReader()){
				CanonicalTreeParser oldTreeIter=new CanonicalTreeParser();
				oldTreeIter.reset(reader,git.getRepository().resolve(oldSrc.getText()));
				CanonicalTreeParser newTreeIter=new CanonicalTreeParser();
				newTreeIter.reset(reader,git.getRepository().resolve(newSrc.getText()));
				List<DiffEntry> entries=((Git)getParent().getValue()).diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).call();
				diff.setText(entries.stream().map((o)->o.toString()).collect(Collectors.joining("\n\n")));
			}catch(GitAPIException|IOException ex){
				Logger.getLogger(LogTreeItem.class.getName()).log(Level.SEVERE,null,ex);
				new Alert(Alert.AlertType.ERROR,ex.getLocalizedMessage(),ButtonType.CLOSE).show();
			}
		});
		page.addColumn(0,oldSrc,newSrc,ok,diff);

		page.setMaxSize(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);
		return page;
	}
}