/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.tagHandler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.springframework.web.servlet.support.RequestContextUtils;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;

/**
 * @author Nucleus Software Exports Limited
 *This class is to handle user tag which renders user info based on supplied user id.
 */
public class UserTagHandler extends SimpleTagSupport {

    UserService  userService;

    private Long userId;

    private String userIdUri;

    @Override
    public void doTag() throws JspException {

        JspWriter out = getJspContext().getOut();
        PageContext pc = (PageContext) getJspContext();
        HttpServletRequest request = (HttpServletRequest) pc.getRequest();
        StringBuilder stringBuilder = new StringBuilder();
        userService = (UserService) RequestContextUtils.findWebApplicationContext(request).getBean("userService");

        try {
            if(null!=userIdUri && userIdUri.indexOf(':')!=-1){
                userId = Long.parseLong(userIdUri.split(":")[1]);
            }
            if (userId != null) {
                String username = userService.getUserNameByUserId(userId);
                // stringBuilder.append("<a class=\"userQtip\" rel=\""+ui.getId()+"\" href=\"javascript:void(0)\">");
                stringBuilder.append(username);
                // stringBuilder.append("</a>");
            }
            out.print(stringBuilder);

        } catch (NumberFormatException | IOException ex) {
            throw new SystemException(ex);
        }
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserIdUri() {
        return userIdUri;
    }

    public void setUserIdUri(String userIdUri) {
        this.userIdUri = userIdUri;
    }
}
