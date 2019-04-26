package com.netapsys.jira.plugins.cloneandmove.web.action.issue;

import com.atlassian.jira.bc.issue.CloneIssueCommand;
import com.atlassian.jira.bc.issue.CloneIssueCommand.CloneIssueResult;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.component.pico.ComponentManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.IssueNotFoundException;
import com.atlassian.jira.exception.IssuePermissionException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.TemporaryAttachmentsMonitorLocator;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.link.RemoteIssueLinkManager;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.action.issue.CloneIssueDetails;
import com.atlassian.jira.web.action.issue.IssueCreationHelperBean;
import com.atlassian.jira.web.bean.TaskDescriptorBean;
import com.atlassian.jira.web.bean.TaskDescriptorBean.Factory;
import com.netapsys.jira.plugins.cloneandmove.helper.BuildVersionHelper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CloneAndMoveIssueDetails
    extends CloneIssueDetails {

  private static final long serialVersionUID = 2609713011534627159L;
  private static final Pattern NOCLONE_PATTERN = Pattern.compile(".*<!--\\s*noclone\\s*-->.*");


  private final TaskManager taskManager;


  public CloneAndMoveIssueDetails(ApplicationProperties applicationProperties,
      PermissionManager permissionManager, IssueLinkManager issueLinkManager,
      RemoteIssueLinkManager remoteIssueLinkManager, IssueLinkTypeManager issueLinkTypeManager,
      SubTaskManager subTaskManager, AttachmentManager attachmentManager, FieldManager fieldManager,
      IssueCreationHelperBean issueCreationHelperBean, IssueFactory issueFactory,
      IssueService issueService, BuildVersionHelper buildVersionHelper,
      TemporaryAttachmentsMonitorLocator temporaryAttachmentsMonitorLocator,
      ProjectFactory projectFactory, TaskManager taskManager) {
    super(applicationProperties, permissionManager, issueLinkManager, issueLinkTypeManager,
        subTaskManager, attachmentManager, fieldManager, issueCreationHelperBean, issueFactory,
        issueService, temporaryAttachmentsMonitorLocator, taskManager,
        (TaskDescriptorBean.Factory) ComponentManager.getInstance()
            .getComponentInstanceOfType(TaskDescriptorBean.Factory.class));
    this.taskManager = taskManager;
  }

  public String getRedirect(String defaultUrl) {
    String moveURL = defaultUrl;
    IssueManager issueManager = getIssueManager();
    if ((this.taskManager.getTask(getCurrentTask().getTaskDescriptor().getTaskId()).isFinished())
        && (!this.taskManager.getTask(getCurrentTask().getTaskDescriptor().getTaskId())
        .isCancelled())) {
      MutableIssue issue = issueManager.getIssueByCurrentKey(
          ((CloneIssueCommand.CloneIssueResult) this.taskManager
              .getTask(getCurrentTask().getTaskDescriptor().getTaskId()).getResult())
              .getIssueKey());
      if ((issue != null) && (!issue.isSubTask())) {
        moveURL = "MoveIssue!default.jspa?id=" + issue.getId();

      } else {
        moveURL = "MoveSubTaskChooseOperation!default.jspa?id=" + issue.getId();
      }
      return super.getRedirect(moveURL);
    }
    try {
      Thread.sleep(500L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return getRedirect(getCurrentTask().getTaskDescriptor().getProgressURL());
  }


  public String doDefault()
      throws Exception {
    setCloneSubTasks(true);
    setCloneLinks(false);
    setCloneAttachments(false);
    try {
      setOriginalIssue(getIssueObject(getIssue()));
      String summary = getOriginalIssue().getSummary();
      if ((summary != null) && (!"".equals(summary))) {
        getFieldValuesHolder().put("summary", summary);
      }
    } catch (IssueNotFoundException e) {
      return "error";
    } catch (IssuePermissionException e) {
      return "error";
    }
    return "input";
  }


  protected String doExecute() {
    ApplicationProperties applicationProperties = getApplicationProperties();
    ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser();
    if ((applicationProperties.getDefaultBackedString("jira.cloneandmove.setreportertocurrentuser")
        != null) &&
        (applicationProperties.getDefaultBackedString("jira.cloneandmove.setreportertocurrentuser")
            .toLowerCase().compareTo("true") == 0)) {
      getIssueObject().setReporter(user);
    }
    return super.doExecute();
  }


  public List<CustomField> getCustomFields(Issue issue) {
    List<CustomField> modifiedCustomField = new ArrayList(super.getCustomFields(issue));
    Iterator<CustomField> customFieldsIterator = modifiedCustomField.iterator();
    while (customFieldsIterator.hasNext()) {
      CustomField customField = (CustomField) customFieldsIterator.next();
      String description = customField.getDescription();
      try {
        Matcher matcher = NOCLONE_PATTERN.matcher(description);
        if (matcher.matches()) {
          customFieldsIterator.remove();
        }
      } catch (Exception localException) {
      }
    }

    return modifiedCustomField;
  }


  public static boolean isPublicMode() {
    ApplicationProperties ap = ComponentAccessor.getApplicationProperties();
    boolean publicMode =
        (ap.getString("jira.mode") == null) || (ap.getString("jira.mode").equals("public"));
    return (publicMode) && (!isExternalUserManagementEnabled());
  }

  private static boolean isExternalUserManagementEnabled() {
    ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
    return applicationProperties.getOption("jira.option.user.externalmanagement");
  }
}