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
		super(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("COMMIT"));
		try{
			for(RevCommit rev:git.log().call()){
				getChildren().add(new CommitTreeItem(rev));
			}
		}catch(NoHeadException ex){
		}
	}
	@Override
	public String toString(){
		return java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("COMMIT");
	}
	@Override
	public MenuItem[] getContextMenuItems(){
		return new MenuItem[0];
	}
	private RevCommit toRevCommit(ObjectId id) throws MissingObjectException, IncorrectObjectTypeException, GitAPIException{
		return ((Git)getParent().getValue()).log().addRange(id,id).call().iterator().next();
	}
	@Override
	public Node getContentPage(){
		GridPane page=new GridPane();
		TextField oldSrc=new TextField();
		TextField newSrc=new TextField();
		CheckBox detailed=new CheckBox(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("DETAILED"));
		Button ok=new Button(java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString("DIFF"));
		TextArea diff=new TextArea();
		diff.setEditable(false);
		Git git=((Git)getParent().getValue());
		GridPane.setVgrow(diff,Priority.ALWAYS);
		GridPane.setHgrow(diff,Priority.ALWAYS);
		GridPane.setHgrow(oldSrc,Priority.ALWAYS);
		GridPane.setHgrow(newSrc,Priority.ALWAYS);
		ok.setOnAction((ActionEvent e)->{
			try(ObjectReader reader=git.getRepository().newObjectReader()){
				List<DiffEntry> entries;
				if(oldSrc.getText().isEmpty()&&newSrc.getText().isEmpty()){
					entries=((Git)getParent().getValue()).diff().setCached(true).call();
				}else{
					CanonicalTreeParser oldTreeIter=new CanonicalTreeParser();
					oldTreeIter.reset(reader,git.getRepository().resolve(oldSrc.getText()+"^{tree}"));
					CanonicalTreeParser newTreeIter=new CanonicalTreeParser();
					newTreeIter.reset(reader,git.getRepository().resolve(newSrc.getText()+"^{tree}"));
					entries=((Git)getParent().getValue()).diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).call();
				}
				if(detailed.isSelected()){
					PipedInputStream in=new PipedInputStream();
					PipedOutputStream out=new PipedOutputStream(in);
					DiffFormatter formatter=new DiffFormatter(out);
					formatter.setRepository(git.getRepository());
					formatter.format(entries);
					out.close();
					diff.setText(new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n")));
				}else{
					diff.setText(entries.stream().map((o)->toString(o)).collect(Collectors.joining("\n")));
				}
			}catch(Exception ex){
				Logger.getLogger(LogTreeItem.class.getName()).log(Level.SEVERE,null,ex);
				Util.informUser(ex);
			}
		});
		page.addColumn(0,oldSrc,newSrc,detailed,ok,diff);

		page.setMaxSize(Double.POSITIVE_INFINITY,Double.POSITIVE_INFINITY);
		return page;
	}
	private static String toString(DiffEntry entry){
		switch(entry.getChangeType()){
			case ADD:
				return entry.getNewPath()+java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString(" ADDED");
			case COPY:
				return entry.getOldPath()+java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString(" COPIED TO ")+entry.getNewPath();
			case DELETE:
				return entry.getOldPath()+java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString(" REMOVED");
			case MODIFY:
				return entry.getOldPath()+java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString(" MODIFIED");
			case RENAME:
				return entry.getOldPath()+java.util.ResourceBundle.getBundle("com/chungkwong/jgitgui/text").getString(" RENAMED TO ")+entry.getNewPath();
			default:
				return "";
		}
	}
}