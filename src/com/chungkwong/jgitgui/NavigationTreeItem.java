/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chungkwong.jgitgui;
import javafx.scene.*;
import javafx.scene.control.*;
/**
 *
 * @author Chan Chung Kwong <1m02math@126.com>
 */
public interface NavigationTreeItem{
	MenuItem[] getContextMenuItems();
	default Node getContentPage(){
		return new Label();
	}
}
