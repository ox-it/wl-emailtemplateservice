/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.emailtemplateservice.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.emailtemplateservice.dao.impl.EmailTemplateServiceDao;
import org.sakaiproject.emailtemplateservice.model.EmailTemplate;
import org.sakaiproject.emailtemplateservice.model.RenderedTemplate;
import org.sakaiproject.emailtemplateservice.service.EmailTemplateService;
import org.sakaiproject.emailtemplateservice.util.TextTemplateLogicUtils;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.genericdao.api.search.Restriction;
import org.sakaiproject.genericdao.api.search.Search;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.user.api.User;

public class EmailTemplateServiceImpl implements EmailTemplateService {

   private static Log log = LogFactory.getLog(EmailTemplateServiceImpl.class);

   private EmailTemplateServiceDao dao;
   public void setDao(EmailTemplateServiceDao d) {
      dao = d;
   }

   private DeveloperHelperService developerHelperService;
   public void setDeveloperHelperService(DeveloperHelperService developerHelperService) {
      this.developerHelperService = developerHelperService;
   }

   private PreferencesService preferencesService;
   public void setPreferencesService(PreferencesService ps) {
      preferencesService = ps;
   }

   private ServerConfigurationService serverConfigurationService;
   public void setServerConfigurationService(ServerConfigurationService scs) {
      serverConfigurationService = scs;
   }


   public EmailTemplate getEmailTemplateById(Long id) {
      if (id == null) {
         throw new IllegalArgumentException("id cannot be null or empty");
      }
      EmailTemplate et = dao.findById(EmailTemplate.class, id);
      return et;
   }

   public EmailTemplate getEmailTemplate(String key, Locale locale) {
      if (key == null || "".equals(key)) {
         throw new IllegalArgumentException("key cannot be null or empty");
      }
      EmailTemplate et = null;
      // TODO make this more efficient
      if (locale != null) {
         Search search = new Search("key", key);
         search.addRestriction( new Restriction("locale", locale.toString()) );
         et = dao.findOneBySearch(EmailTemplate.class, search);
         if (et == null) {
            search.addRestriction( new Restriction("locale", locale.getLanguage()) );
            et = dao.findOneBySearch(EmailTemplate.class, search);
         }
      }
      if (et == null) {
         et = dao.findOneBySearch(EmailTemplate.class, new Search("key", key));
      }
      if (et == null) {
         log.warn("no template found for: " + key + " in locale " + locale );
      }
      return et;
   }

   public List<EmailTemplate> getEmailTemplates(int max, int start) {
      return dao.findAll(EmailTemplate.class, start, max);
   }

   public RenderedTemplate getRenderedTemplate(String key, Locale locale, Map<String, String> replacementValues) {
      EmailTemplate temp = getEmailTemplate(key, locale);
      //if no template was found we need to return null to avoid an NPE
      if (temp == null)
    	  return null;
      
      RenderedTemplate ret = new RenderedTemplate(temp);

      //get the default current user fields
      log.debug("getting default values");

      Map<String, String> userVals = getCurrentUserFields();
      replacementValues.putAll(userVals);
      log.debug("got replacement values");

      ret.setRenderedSubject(this.processText(ret.getSubject(), replacementValues));
      ret.setRenderedMessage(this.processText(ret.getMessage(), replacementValues));

      return ret;
   }

   public RenderedTemplate getRenderedTemplateForUser(String key, String userReference, Map<String, String> replacementValues) {
      log.debug("getRenderedTemplateForUser(" + key + ", " +userReference);
	  String userId = developerHelperService.getUserIdFromRef(userReference);
      Locale loc = getUserLocale(userId);
      return getRenderedTemplate(key,loc,replacementValues);
   }

   public void saveTemplate(EmailTemplate template) {
      //update the modified date
      template.setLastModified(new Date());

      dao.save(template);
      log.info("saved template: " + template.getId());
   }

   protected Locale getUserLocale(String userId) {
      Locale loc = null;
      Preferences prefs = preferencesService.getPreferences(userId);
      ResourceProperties locProps = prefs.getProperties(InternationalizedMessages.APPLICATION_ID);
      String localeString = locProps.getProperty(InternationalizedMessages.LOCALE_KEY);

      if (localeString != null)
      {			String[] locValues = localeString.split("_");
      if (locValues.length > 1)
         loc = new Locale(locValues[0], locValues[1]); // language, country
      else if (locValues.length == 1) 
         loc = new Locale(locValues[0]); // just language
      }
      //the user has no preference set - get the system default
      if (loc == null ) {
         String lang = System.getProperty("user.language");
         String region = System.getProperty("user.region");

         if (region != null) {
            log.debug("getting system locale for: " + lang + "_" + region);
            loc = new Locale(lang,region);
         } else { 
            log.debug("getting system locale for: " + lang );
            loc = new Locale(lang);
         }
      }

      return loc;
   }


   protected String processText(String text, Map<String, String> values) {
      return TextTemplateLogicUtils.processTextTemplate(text, values);
   }

   protected Map<String, String> getCurrentUserFields() {
      Map<String, String> rv = new HashMap<String, String>();
      String userRef = developerHelperService.getCurrentUserReference();
      if (userRef != null) {
         User user = (User) developerHelperService.fetchEntity(userRef);
         try {
            String email = user.getEmail();
            if (email == null)
               email = "";
            String first = user.getFirstName();
            if (first == null)
               first = "";
            String last = user.getLastName();
            if (last == null)
               last ="";
   
            rv.put(CURRENT_USER_EMAIL, email);
            rv.put(CURRENT_USER_FIRST_NAME, first);
            rv.put(CURRENT_USER_LAST_NAME, last);
            rv.put(CURRENT_USER_DISPLAY_NAME, user.getDisplayName());
            rv.put(CURRENT_USER_DISPLAY_ID, user.getDisplayId());
            rv.put(LOCAL_SAKAI_NAME, serverConfigurationService.getString("ui.service", "Sakai"));
            rv.put(LOCAL_SAKAI_SUPPORT_MAIL,serverConfigurationService.getPortalUrl());
            rv.put(LOCAL_SAKAI_URL,serverConfigurationService.getPortalUrl());

         } catch (Exception e) {
            log.warn("Failed to get current user replacements: " + userRef, e);
         }
      }
      return rv;
   }

}
