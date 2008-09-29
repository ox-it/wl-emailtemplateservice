/**
 * $Id$
 * $URL$
 * ModifyEmailProducer.java - evaluation - Feb 29, 2008 6:06:42 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.emailtemplateservice.tool.producers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.emailtemplateservice.model.EmailTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.tool.locators.EmailTemplateLocator;
import org.sakaiproject.emailtemplateservice.tool.params.EmailTemplateViewParams;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.user.api.UserDirectoryService;

import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * Page for Modifying Email templates
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class ModifyEmailProducer implements ViewComponentProducer, ViewParamsReporter, ActionResultInterceptor {

   private static Log log = LogFactory.getLog(ModifyEmailProducer.class);
   public static final String VIEW_ID = "modify_email";
   public String getViewID() {
      return VIEW_ID;
   }

   private EmailTemplateService emailTemplateService;
   public void setEmailTemplateService(EmailTemplateService ets) {
      emailTemplateService = ets;
   }

   private DeveloperHelperService developerHelperService;
   public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
      this.developerHelperService = developerHelperService;
   }

   private UserDirectoryService userDirectoryService;
   public void setUserDirectoryService(UserDirectoryService uds) {
	   this.userDirectoryService = uds;
   }

   private String emailTemplateLocator = "EmailTemplateLocator.";

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
    */
   public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {

      // handle the input params for the view
      EmailTemplateViewParams emailViewParams = (EmailTemplateViewParams) viewparams;

      
      String actionBean = "EmailTemplateLocator.";
      EmailTemplate template = null;
      // form the proper OTP path
      boolean newEmailTemplate = true;
      String emailTemplateId = EmailTemplateLocator.NEW_1; // default is new one of the supplied type
      if (emailViewParams.id == null) {
         log.debug("this is a new tamplate");
         template = new EmailTemplate();
      } else {

         emailTemplateId = emailViewParams.id.toString();
         template = emailTemplateService.getEmailTemplateById(new Long(emailTemplateId));
         newEmailTemplate = false;
      }
      String emailTemplateOTP = emailTemplateLocator + emailTemplateId + ".";

      // local variables used in the render logic
      String currentUserRef = developerHelperService.getCurrentUserReference();
      boolean userAdmin = developerHelperService.isUserAdmin(currentUserRef);

      /* not needed?

      if (emailViewParams.evaluationId == null) {
         /*
       * top links here
       */
      /*     UIInternalLink.make(tofill, "summary-link", 
               UIMessage.make("summary.page.title"), 
               new SimpleViewParameters(SummaryProducer.VIEW_ID));

         if (userAdmin) {
            UIInternalLink.make(tofill, "administrate-link", 
                  UIMessage.make("administrate.page.title"),
                  new SimpleViewParameters(AdministrateProducer.VIEW_ID));
         }

         UIInternalLink.make(tofill, "control-emailtemplates-link",
               UIMessage.make("controlemailtemplates.page.title"),
               new SimpleViewParameters(ControlEmailTemplatesProducer.VIEW_ID));
      }
       */

      String headerName = template.getKey();
      if (template.getLocale() != null && !template.getLocale().trim().equals("")) 
         headerName = headerName + " (" + template.getLocale() + ")";

      UIMessage.make(tofill, "modify-template-header", "modifyemail.modify.template.header", 
            new Object[] {headerName});

      UIVerbatim.make(tofill, "email_templates_fieldhints", UIMessage.make("email.templates.field.names"));

      UIForm form = UIForm.make(tofill, "emailTemplateForm");

      String actionBinding = null;
      actionBinding = actionBean + "saveAll";

      if (template.getId() != null) {
         // bind in the evaluationId
         //form.parameters.add(new UIELBinding(actionBean + "id", template.getId().toString()));
         //actionBinding = actionBean + "saveAndAssignEmailTemplate";
         //form.parameters.add(new UIELBinding(actionBean + "locale", template.getLocale()));
      }

      // add in the close window control
      UIMessage.make(tofill, "closeWindow", "general.close.window.button");
      if (! newEmailTemplate) {
         // add in the reset to default if not a new email template
         UICommand resetCommand = UICommand.make(form, "resetEmailTemplate", UIMessage.make("modifyemail.reset.to.default.link"), 
               actionBean + "resetToDefaultEmailTemplate");
         //resetCommand.addParameter( new UIELBinding(actionBean + "emailTemplateType", emailViewParams.emailType) );
      }
      /*
      } else {
         // not part of an evaluation so use the WBL
         actionBinding = emailTemplateLocator + "saveAll";
         // add in a cancel button
         UIMessage.make(form, "cancel-button", "general.cancel.button");
      }
       */


      UIInput.make(form, "emailSubject", emailTemplateOTP + "subject",template.getSubject());
      UIInput.make(form, "emailKey", emailTemplateOTP + "key",template.getKey());
      UIInput.make(form, "emailLocale", emailTemplateOTP + "locale",template.getLocale());
      UIInput.make(form, "emailMessage", emailTemplateOTP + "message",template.getMessage());
      log.info(actionBinding);
      form.parameters.add(new UIELBinding(emailTemplateOTP + "owner", userDirectoryService.getCurrentUser().getId()));
      UICommand.make(form, "saveEmailTemplate", UIMessage.make("modifyemail.save.changes.link"), actionBinding);

   }


   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.flow.ActionResultInterceptor#interceptActionResult(uk.org.ponder.rsf.flow.ARIResult, uk.org.ponder.rsf.viewstate.ViewParameters, java.lang.Object)
    */
   public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
      // handles the navigation cases and passing along data from view to view
      EmailTemplateViewParams evp = (EmailTemplateViewParams) incoming;
      EmailTemplateViewParams outgoing = (EmailTemplateViewParams) evp.copyBase(); // inherit all the incoming data
      if ("success".equals(actionReturn) 
            || "successAssign".equals(actionReturn) 
            || "successReset".equals(actionReturn) ) {
         //outgoing.viewID = PreviewEmailProducer.VIEW_ID;
         result.resultingView = outgoing;
      } else if ("failure".equals(actionReturn)) {
         // failure just comes back here
         result.resultingView = outgoing;
      } else {
         // default
         result.resultingView = new SimpleViewParameters(MainViewProducer.VIEW_ID);
      }
   }

   /* (non-Javadoc)
    * @see uk.org.ponder.rsf.viewstate.ViewParamsReporter#getViewParameters()
    */
   public ViewParameters getViewParameters() {
      return new EmailTemplateViewParams();
   }

}
