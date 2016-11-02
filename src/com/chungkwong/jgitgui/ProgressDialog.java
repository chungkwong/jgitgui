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
import javafx.application.*;
import javafx.scene.control.*;
import org.eclipse.jgit.lib.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public class ProgressDialog extends Dialog<Object> implements ProgressMonitor{
	private final ProgressBar bar=new ProgressBar();
	private int totalTask=1,finishedTask=0;
	private int totalWork=0,finishedWork=0;
	public ProgressDialog(String title){
		setTitle(title);
		getDialogPane().setContent(bar);
		getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

		show();
	}
	@Override
	public synchronized void start(int totalTasks){
		this.totalTask=totalTasks;
	}
	@Override
	public synchronized void beginTask(String title,int totalWork){
		this.totalWork=totalWork;
		this.finishedWork=0;
		Platform.runLater(()->setHeaderText(title));
	}
	@Override
	public synchronized void update(int completed){
		finishedWork+=completed;
		Platform.runLater(()->{
			if(totalWork>0)
				bar.setProgress((finishedTask+((double)finishedWork)/totalWork)/totalTask);
		});
	}
	@Override
	public synchronized void endTask(){
		++finishedTask;
		Platform.runLater(()->{
			if(finishedTask>=totalTask)
				hide();
			bar.setProgress((double)finishedTask/totalTask);
		});
	}
	@Override
	public boolean isCancelled(){
		return false;
	}
}
