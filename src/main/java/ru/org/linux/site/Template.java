/*
 * Copyright 1998-2012 Linux.org.ru
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ru.org.linux.site;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import ru.org.linux.auth.AccessViolationException;
import ru.org.linux.auth.AuthUtil;
import ru.org.linux.spring.Configuration;
import ru.org.linux.storage.FileStorage;
import ru.org.linux.storage.Storage;
import ru.org.linux.storage.StorageException;
import ru.org.linux.user.Profile;
import ru.org.linux.user.ProfileProperties;
import ru.org.linux.user.User;
import ru.org.linux.util.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletRequest;
import java.io.IOException;
import java.io.OutputStream;

public final class Template {
  private final Profile userProfile;
  private final Configuration configuration;
  private User currentUser = null;

  private final Storage storage;

  public Template(WebApplicationContext ctx) {
    configuration = (Configuration)ctx.getBean("configuration");
    storage = new FileStorage(configuration.getPathPrefix() + "linux-storage/");
    userProfile = AuthUtil.getCurrentProfile();

    if(AuthUtil.isSessionAuthorized()) {
      currentUser = AuthUtil.getCurrentUser();
    }
  }


  public Template(ServletRequest request) {
    this(WebApplicationContextUtils.getWebApplicationContext(request.getServletContext()));
  }

  public String getProfileName() {
    return getNick();
  }

  public void writeProfile(String name) throws IOException, AccessViolationException, StorageException {
    if (name.charAt(0) == '_') {
      throw new AccessViolationException("нельзя менять специальный профиль");
    }

    if (!StringUtil.checkLoginName(name)) {
      throw new AccessViolationException("некорректное имя пользователя");
    }

    if ("anonymous".equals(name)) {
      throw new AccessViolationException("нельзя менять профиль по умолчанию");
    }

    OutputStream df = null;
    try {
      df = storage.getWriteStream("profile", name);
      userProfile.write(df);
    } finally {
      if (df!=null) {
        df.close();
      }
    }
  }

  public String getStyle() {
    User user = getCurrentUser();
    if(user == null) {
      return "tango"; // TODO move to properties?
    } else {
      return user.getStyle();
    }
  }

  public String getFormatMode() {
    return userProfile.getProperties().getFormatMode();
  }

  public ProfileProperties getProf() {
    return userProfile.getProperties();
  }

  public boolean isUsingDefaultProfile() {
    return userProfile.isDefault();
  }

  public String getMainUrl() {
    return configuration.getMainUrl();
  }

  public String getMainUrlNoSlash() {
    return configuration.getMainUrlWithoutSlash();
  }

  public String getSecureMainUrl() {
    return configuration.getSecureUrl();
  }

  public Configuration getConfig() {
    return configuration;
  }

  public boolean isSessionAuthorized() {
    return AuthUtil.isSessionAuthorized();
  }

  public boolean isModeratorSession() {
    return AuthUtil.isModeratorSession();
  }

  public boolean isCorrectorSession() {
    return AuthUtil.isCorrectorSession();
  }

  /**
   * Get current authorized users nick
   * @return nick or null if not authorized
   */
  public String getNick() {
    if (!isSessionAuthorized()) {
      return null;
    } else {
      return currentUser.getNick();
    }
  }

  @Nonnull
  public static Template getTemplate(ServletRequest request) {
    return new Template(request);
  }

  public void updateCurrentUser() {
    initCurrentUser(true);
  }

  private void initCurrentUser(boolean forceUpdate) {
    if (!isSessionAuthorized()) {
      return;
    }
    if (currentUser != null && !forceUpdate) {
      return;
    }
    currentUser = AuthUtil.getCurrentUser();
  }

  @Nullable
  public User getCurrentUser()  {
    return AuthUtil.getCurrentUser();
  }
}
