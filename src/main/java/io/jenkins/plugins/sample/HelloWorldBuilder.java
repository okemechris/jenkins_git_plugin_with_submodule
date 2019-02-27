package io.jenkins.plugins.sample;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher.ProcStarter;
import hudson.Proc;
import hudson.util.FormValidation;
import hudson.model.AbstractProject;
import hudson.model.Computer;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import java.io.ByteArrayOutputStream;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import static org.kohsuke.stapler.Facet.LOGGER;

public class HelloWorldBuilder extends Builder implements SimpleBuildStep {
    
    private  Computer computer; 
    private  Launcher launcher;

    @DataBoundConstructor
    public HelloWorldBuilder() {
       
    }
    
    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
       
        this.launcher = launcher;
        final EnvVars env = run.getEnvironment(listener);
        String currentCommit = env.get("GIT_COMMIT");
        String PreviousCommit = env.get("GIT_PREVIOUS_COMMIT");
        
        String cmd = "git log --graph --oneline -U0 --source --pretty=medium --submodule '" + PreviousCommit + "'...'" + currentCommit + "'";
        listener.getLogger().println("result :"+readFromProcess(cmd,workspace));
        //launcher.launch(cmd, new String[0], listener.getLogger(), workspace);

        run.addAction(new LogAction(readFromProcess(cmd,workspace)));
     
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckName(@QueryParameter String value, @QueryParameter boolean useFrench)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error(Messages.HelloWorldBuilder_DescriptorImpl_errors_missingName());
            }
            if (value.length() < 4) {
                return FormValidation.warning(Messages.HelloWorldBuilder_DescriptorImpl_warnings_tooShort());
            }
            if (!useFrench && value.matches(".*[éáàç].*")) {
                return FormValidation.warning(Messages.HelloWorldBuilder_DescriptorImpl_warnings_reallyFrench());
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.HelloWorldBuilder_DescriptorImpl_DisplayName();
        }

    }

    @Nonnull
    private Computer getComputer() throws AbortException {
        if (computer != null) {
            return computer;
        }

        String node = null;
        Jenkins j = Jenkins.getActiveInstance();

        for (Computer c : j.getComputers()) {
            if (c.getChannel() == launcher.getChannel()) {
                node = c.getName();
                break;
            }
        }

        if (node == null) {
            throw new AbortException("Could not find computer for the job");
        }

        computer = j.getComputer(node);
        if (computer == null) {
            throw new AbortException("No such computer " + node);
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "Computer: {0}", computer.getName());
            try {
                LOGGER.log(Level.FINE, "Env: {0}", computer.getEnvironment());
            } catch (IOException | InterruptedException e) {// ignored
            }
        }
        return computer;
    }
    
  @Nullable
private String readFromProcess(String args, FilePath workspace ) throws InterruptedException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        ProcStarter ps = launcher.launch();
        Proc p = launcher.launch(args, new String[0], baos, workspace);
        int exitCode = p.join();
        if (exitCode == 0) {
            String retunValue = baos.toString(getComputer().getDefaultCharset().name()).replaceAll("\t", " &nbsp;&nbsp;&nbsp;&nbsp;").trim();
            retunValue = retunValue.replaceAll("\n", "<br><br>").trim();
            retunValue = retunValue.replaceAll("\r", " &nbsp;&nbsp;&nbsp;&nbsp;;").trim();
     
            return retunValue;
                    
        } else {
            
            return null;
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    return null;
}

}
