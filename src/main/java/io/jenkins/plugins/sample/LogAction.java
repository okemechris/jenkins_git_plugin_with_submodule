/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.jenkins.plugins.sample;

import hudson.model.Action;

/**
 *
 * @author djbabs
 */
public class LogAction implements Action {

    private String log;
    
    public LogAction(String log) {
        this.log = log;
    }
    
    @Override
    public String getIconFileName() {
        return "document.png"; 
    }

    @Override
    public String getDisplayName() {
        return "Git log for submodules"; 
    }

    @Override
    public String getUrlName() {
        return "Gitlogforsubmodules"; 
    }
    
    public String getName() {
        return log;
    }
}
